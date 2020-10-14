package org.hirschhorn.ricochet.solver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Logger;

import org.hirschhorn.ricochet.board.Color;
import org.hirschhorn.ricochet.board.Direction;
import org.hirschhorn.ricochet.board.Position;
import org.hirschhorn.ricochet.board.Target;
import org.hirschhorn.ricochet.game.Board;
import org.hirschhorn.ricochet.game.BoardState;
import org.hirschhorn.ricochet.game.Game;
import org.hirschhorn.ricochet.game.GameFactory;
import org.hirschhorn.ricochet.game.Move;
import org.hirschhorn.ricochet.game.MoveCalculator;
import org.hirschhorn.ricochet.game.RobotPositions;

public class Solver {

  private static final int NUMBER_OF_ITERATIONS = 15;
  private static final int START_ITERATION = 0;
  private static final boolean PAUSE_BEFORE_PLAY = false;
  private static final int MAX_DEPTH = 15;
  private static final int MAX_WINNERS = 1;

  private static Logger logger = Logger.getLogger(Solver.class.getName());
  private MoveNode rootMove;
  private Board board;
  private MoveStats moveStats;
  private Set<Integer> boardStateCache;
  private UnprocessedMovesType unprocessedMovesType;
  
  private boolean shouldCancel;

  public static void main(String[] args) throws IOException {

    SolverFactory solverFactory = new SolverFactory();
    UnprocessedMovesType movesType = UnprocessedMovesType.BREADTH_FIRST_SEARCH;
    
    //MoveNode previousWinner = null;
    for (int iteration = START_ITERATION; iteration <= NUMBER_OF_ITERATIONS; iteration++) {
      int targetIndex = iteration % 16;
      
      logger.info("");
      logger.info("");
      logger.severe("======================================");
      logger.severe("NEW GAME. Iteration " + iteration + "  TargetIndex " + targetIndex);  
      logger.info("======================================");
      RobotPositions robotPositions = null;
//      RobotPositions robotPositions = (previousWinner == null) ? null : previousWinner.getBoardState().getRobotPositions();
      Game game = (new GameFactory()).createGame(targetIndex, robotPositions);
      Solver solver = solverFactory.createSolver(game, movesType);
      
      BoardState boardState = solver.getRootMove().getBoardState();
      logger.severe("Target: " + boardState.getChosenTarget() + " at position "
              + solver.getBoard().getTargetPosition(boardState.getChosenTarget())
              + ". Robots: " + boardState.asRobotPositionsString());
      
      if (PAUSE_BEFORE_PLAY) {
        logger.severe("Press any key to continue...");
        (new Scanner(System.in)).nextLine();
        //logger.severe("Running...");
      }
      
      solver.solve();
      //List<MoveNode> winners = solver.solve();
      //previousWinner = winners.get(0);
    }
  }

  public Solver(Board board, MoveNode rootMove, UnprocessedMovesType unprocessedMovesType) {
    this.board = board;
    this.rootMove = rootMove;
    this.unprocessedMovesType = unprocessedMovesType;

    moveStats = new MoveStats(MAX_DEPTH, MAX_WINNERS);
    boardStateCache = new HashSet<>();
  }

  public MoveNode getRootMove() {
    return rootMove;
  }

  public Board getBoard() {
    return board;
  }

  public void tryToCancel() {
    shouldCancel = true;
  }
  
  public List<MoveNode> solve() throws IOException {
    shouldCancel = false;
    moveStats.playStarted();
    UnprocessedMoves unprocessedMoves = UnprocessedMovesFactory.newUnprocessedMoves(unprocessedMovesType);
    unprocessedMoves.add(rootMove);

    while (!(unprocessedMoves.isEmpty()) && !shouldCancel) {
      MoveNode moveNode = unprocessedMoves.removeFirst();
      List<MoveNode> nextMoves = createNextMoveNodes(moveNode);
      
      Iterator<MoveNode> moveIter = nextMoves.iterator();
      while (moveIter.hasNext()) {
        MoveNode nextMove = moveIter.next();
        if (isWinner(nextMove)) {
          moveStats.winnerFound(nextMove);
        } else if (shouldContinue(nextMove)) {
          int compressedMove = RobotPositions.compressRobotPositions(nextMove.getBoardState().getRobotPositions());
          boardStateCache.add(compressedMove);
          nextMove.getPotential().adjustIfMoveSameColorAsTarget(nextMove);
          unprocessedMoves.add(nextMove);
        } else {
          // Don't add to parent or unprocessedMoves if we aren't going to continue
          moveIter.remove();
        }
      }

      moveNode.addChildren(nextMoves);
      moveStats.moveProcessed(moveNode, nextMoves);
        
      if (moveStats.maxWinnersReached()) {
        unprocessedMoves.clear();
      }

    }
    
    if (shouldCancel) {
      logger.severe("Solving canceled.");
      return new ArrayList<>(); 
    }
    
    moveStats.printWinners();
    return moveStats.getWinners();
  }

  private boolean shouldContinue(MoveNode nextMove) {
    if (nextMove.getDepth() >= MAX_DEPTH) {
      return false;
    }
    if (noRobotsHaveMoved(nextMove)) {
       return false;
    }
    if (boardStateHasPreviouslyExisted(nextMove)) {
      return false;
    }
    return true;
  }

  
  private boolean boardStateHasPreviouslyExisted(MoveNode moveNode) {
    int compressedMove = RobotPositions.compressRobotPositions(moveNode.getBoardState().getRobotPositions());
    return boardStateCache.contains(compressedMove);
  }

  private boolean noRobotsHaveMoved(MoveNode moveNode) {
    return moveNode.getMove().getNumberOfSpaces() == 0;
  }

  private boolean isWinner(MoveNode nextMove) {
    BoardState boardState = nextMove.getBoardState();
    Target chosenTarget = boardState.getChosenTarget();
    Color targetColor = chosenTarget.getColor();
    return boardState.getRobotPosition(targetColor).equals(board.getTargetPosition(chosenTarget));
  }

  //@VisibleForTesting
  List<MoveNode> createNextMoveNodes(MoveNode parentMove) {
    List<MoveNode> nextMoves = new ArrayList<>();
    for (Color color : Color.values()) {
      for (Direction direction : Direction.values()) {
        MoveNode nextMove = createChildMoveNode(parentMove, color, direction);
        nextMoves.add(nextMove);
      }
    }
    return nextMoves;
  }

  MoveNode createChildMoveNode(MoveNode parentMove, Color robot, Direction direction) {
    RobotPositions robotPositions = parentMove.getBoardState().getRobotPositions();
    Position oldPosition = robotPositions.getRobotPosition(robot); 
    Position newPosition = MoveCalculator.calculateRobotPositionAfterMoving(parentMove.getBoardState(), board, robot, direction);
    int spacesMoved = Math.abs(newPosition.getX() - oldPosition .getX()) + Math.abs(newPosition.getY() - oldPosition.getY());
    
    Move move = new Move(robot, direction, spacesMoved);    
    RobotPositions newRobotPositions = (new RobotPositions.Builder(robotPositions)).setRobotPosition(robot, newPosition).build();
    BoardState childBoardState = new BoardState(parentMove.getChosenTarget(), newRobotPositions);
    return new MoveNode(parentMove, childBoardState, move);
  }
  
}
