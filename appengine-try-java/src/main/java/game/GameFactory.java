package org.hirschhorn.ricochet.game;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.hirschhorn.ricochet.board.BoardItem;
import org.hirschhorn.ricochet.board.BoardSection;
import org.hirschhorn.ricochet.board.Color;
import org.hirschhorn.ricochet.board.Position;
import org.hirschhorn.ricochet.board.Target;

public class GameFactory {

  public static final int MAX_Y = 16;
  public static final int MAX_X = 16;
  
  public Game createGame(int targetIndex) {
    return createGame(targetIndex, null);
  }

  public Game createGame(int targetIndex, RobotPositions robotPositions) {
    Board board = new Board(createTargetsToPositions(), createBoardItems());
    
    if (robotPositions == null) {
      robotPositions = createRobotsToPositions();
    }
    
    BoardState boardState = new BoardState(chooseTarget(targetIndex), robotPositions);
    Game game = new Game(board, boardState);
    return game;
  }
  
  private LinkedHashMap<Target, Position> createTargetsToPositions() {
    LinkedHashMap<Target, Position> targetsToPosition = new LinkedHashMap<>();
    BoardSection upperLeft = BoardSection.createBoardSectionA1();
    BoardSection upperRight = BoardSection.createBoardSectionB1().shiftRight();
    BoardSection bottomRight = BoardSection.createBoardSectionC1().shiftRight().shiftDown();
    BoardSection bottomLeft = BoardSection.createBoardSectionD1().shiftDown();
    
    targetsToPosition.putAll(upperLeft.getTargetsToPosition());
    targetsToPosition.putAll(upperRight.getTargetsToPosition());
    targetsToPosition.putAll(bottomRight.getTargetsToPosition());
    targetsToPosition.putAll(bottomLeft.getTargetsToPosition());

    return targetsToPosition;
  }

  private List<BoardItem> createBoardItems() {
    List<BoardItem> boardItems = new ArrayList<>();
    BoardSection upperLeft = BoardSection.createBoardSectionA1();
    BoardSection upperRight = BoardSection.createBoardSectionB1().shiftRight();
    BoardSection bottomRight = BoardSection.createBoardSectionC1().shiftRight().shiftDown();
    BoardSection bottomLeft = BoardSection.createBoardSectionD1().shiftDown();

    boardItems.addAll(upperLeft.getBoardItems());
    boardItems.addAll(upperRight.getBoardItems());
    boardItems.addAll(bottomRight.getBoardItems());
    boardItems.addAll(bottomLeft.getBoardItems());

    return boardItems;
  }
  
  private Target chooseTarget(int index) {
    return Target.getTargets().get(index);
  }
  
  private RobotPositions createRobotsToPositions() {
    RobotPositions.Builder builder = new RobotPositions.Builder();
    builder.setRobotPosition(Color.Blue, Position.of(0, 0));
    builder.setRobotPosition(Color.Red, Position.of(0, (MAX_Y - 1)));
    builder.setRobotPosition(Color.Green, Position.of((MAX_X - 1), 0));
    builder.setRobotPosition(Color.Yellow, Position.of((MAX_X - 1), (MAX_Y - 1)));
    return builder.build();
  }

}
