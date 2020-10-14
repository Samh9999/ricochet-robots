package org.hirschhorn.ricochet.servlets;

//TODO: Add click handler to move robots

//TODO: Convert Image to SVG

//TODO: separate solver from multiplayer


import java.io.IOException;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hirschhorn.ricochet.board.Color;
import org.hirschhorn.ricochet.board.Direction;
import org.hirschhorn.ricochet.board.Position;
import org.hirschhorn.ricochet.board.Shape;
import org.hirschhorn.ricochet.board.Target;
import org.hirschhorn.ricochet.game.Board;
import org.hirschhorn.ricochet.game.BoardState;
import org.hirschhorn.ricochet.game.Game;
import org.hirschhorn.ricochet.game.GameFactory;
import org.hirschhorn.ricochet.game.Move;
import org.hirschhorn.ricochet.game.MoveCalculator;
import org.hirschhorn.ricochet.game.Phase;
import org.hirschhorn.ricochet.game.PlayerAndGuess;
import org.hirschhorn.ricochet.game.RobotPositions;
import org.hirschhorn.ricochet.game.UpdateEvent;
import org.hirschhorn.ricochet.game.UpdateEventData;
import org.hirschhorn.ricochet.game.UpdateEventType;
import org.hirschhorn.ricochet.solver.MoveNode;
import org.hirschhorn.ricochet.solver.Solver;
import org.hirschhorn.ricochet.solver.SolverFactory;
import org.hirschhorn.ricochet.solver.UnprocessedMovesType;
import org.hirschhorn.ricochet.updateevent.GameRestartedEventData;
import org.hirschhorn.ricochet.updateevent.GuessSubmittedEventData;
import org.hirschhorn.ricochet.updateevent.PlayerListChangedEventData;
import org.hirschhorn.ricochet.updateevent.PlayerAndScore;
import org.hirschhorn.ricochet.updateevent.PlayerToMoveChangedData;
import org.hirschhorn.ricochet.updateevent.RobotGlidedEventData;
import org.hirschhorn.ricochet.updateevent.RobotJumpedEventData;
import org.hirschhorn.ricochet.updateevent.ScoreUpdatedEventData;
import org.hirschhorn.ricochet.updateevent.TargetSetEventData;
import org.hirschhorn.ricochet.updateevent.TimerChangedData;

import com.google.gson.Gson;

public class RicochetRobotsServlet extends HttpServlet {

  private static final long serialVersionUID = 153254652788906133L;
  private static final long LATEST_CHANGES_TIMEOUT_MILLIS = 10000;
  
  private static Logger logger = Logger.getLogger(RicochetRobotsServlet.class.getName());

  public void init() throws ServletException {
    logger.info("Entering Servlet.init()");
    //TODO: MIGHT HAVE BROKEN SOMETHING. UNDERSTAND
//    if (getUpdateEvents() == null) {   
//      logger.info("In Servlet.init(). Initializing.");
//      intializeUpdateEvents();
//    }
    intializeUpdateEvents();
    getServletContext().setAttribute("GAMES", new ArrayList<Game>());
  }

  @SuppressWarnings("unchecked")
  private int initializeGame(int targetIndex) {
    Game game = (new GameFactory()).createGame(targetIndex);
    ((ArrayList<Game>) getServletContext().getAttribute("GAMES")).add(game);
    int gameId = ((ArrayList<Game>) getServletContext().getAttribute("GAMES")).size() - 1;
    ((List<List<UpdateEvent>>) getServletContext().getAttribute("UPDATE_EVENTS")).add(new ArrayList<UpdateEvent>());
    game.setGameId(gameId);
    System.out.println("Game Id: " + game.getId());
    for (Color robot : Color.values()) {
      Position newPosition = getBoardState(gameId).getRobotPosition(robot);
      addUpdateEvent(UpdateEventType.ROBOT_JUMPED,  new RobotJumpedEventData(robot, null, newPosition), gameId);
    }
    
    Target oldTarget = null;
    Target newTarget = Target.getTarget(Color.Red, Shape.Star);
    //Target newTarget = Target.getTargets().get(targetIndex);
    changeTarget(gameId, oldTarget, newTarget);    
    
    game.setPhase(Phase.GUESSING);
    
    return gameId;
  }

  @SuppressWarnings("unchecked")
  private Game getGame(int gameId) {
    return ((ArrayList<Game>) getServletContext().getAttribute("GAMES")).get(gameId);
  }
  
  private Solver getSolver() {
    return (Solver) getServletContext().getAttribute("SOLVER");
  }

  private void setSolver(Solver solver) {
    getServletContext().setAttribute("SOLVER", solver);
  }

  private synchronized void intializeUpdateEvents() {
    getServletContext().setAttribute("UPDATE_EVENTS", new ArrayList<ArrayList<UpdateEvent>>());    
    getServletContext().setAttribute("LAST_GAME_RESTART_EVENT", 0);
  }
  
  @SuppressWarnings("unchecked")
  private synchronized void addUpdateEvent(UpdateEventType updateEventType, UpdateEventData updateEventData, int gameId) {
    int newVersion = getUpdateVersion(gameId) + 1;
    logger.info("Add UpdateEvent: " + newVersion);
    UpdateEvent updateEvent = new UpdateEvent(updateEventType, updateEventData, newVersion);
    ((List<List<UpdateEvent>>) getServletContext().getAttribute("UPDATE_EVENTS")).get(gameId).add(updateEvent);
    if (updateEvent.getEventType().equals(UpdateEventType.GAME_RESTARTED)) {
      getServletContext().setAttribute("LAST_GAME_RESTART_EVENT", updateEvent.getCurrentVersion());  
    }
  }
  
  @SuppressWarnings("unchecked")
  private synchronized List<UpdateEvent> getUpdateEvents(int gameId) {
    return ((List<List<UpdateEvent>>) getServletContext().getAttribute("UPDATE_EVENTS")).get(gameId);
  }

  private synchronized int getUpdateVersion(int gameId) {
    return getUpdateEvents(gameId).size() - 1;
  }
  
  private synchronized Integer getLastGameRestartVersion() {
    return (Integer)getServletContext().getAttribute("LAST_GAME_RESTART_EVENT");
  }

  private boolean hasChangedSince(int updateVersion, int gameId) {
    return getUpdateVersion(gameId) != updateVersion;
  }

  private synchronized List<UpdateEvent> getUpdateEventsSince(int oldVersion, int gameId) {
    int currentVersion = getUpdateVersion(gameId);
    if (oldVersion == currentVersion) {
      // Client and server are at same version so nothing changed -- return empty list.
      return new ArrayList<UpdateEvent>();
    } 

    int lastGameRestartVersion = getLastGameRestartVersion();

    // Client is at version AFTER server. This can only happen if server was restarted, but client
    // was not. This should be rare, but if it happens, pretend client is at version right before
    // the last restart.
    if (oldVersion >= currentVersion) {
      oldVersion = lastGameRestartVersion - 1;
    }

    // Client is at version before last server restart. Only return events since last restart, not before.
    if (oldVersion < lastGameRestartVersion) {
      return new ArrayList<>(getUpdateEvents(gameId).subList(lastGameRestartVersion, currentVersion + 1));
    }
    
    // Client is before server. Return all events since client version.
    return new ArrayList<>(getUpdateEvents(gameId).subList(oldVersion + 1, currentVersion + 1));
  }

  private BoardState getBoardState(int gameId) {
    return getGame(gameId).getBoardState();
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    // See http://codebox.org.uk/pages/java-servlet-url-parts
    logger.info("Received request: " + request.getRequestURI() + "?" + request.getQueryString());
    
    String pathInfo = request.getPathInfo();
    switch (pathInfo) {
      case "/gameids":
        doGetGameIds(request, response);
        break;
      case "/getcurrrentversion":
        doGetCurrentVersion(request, response);
        break;
      case "/game/start":
        doGetStartGame(request, response);
        break;
      case "/getcurrentgamestate":
        doGetCurrentGameState(request, response);
        break;
      case "/getlatestchanges":
        doGetLatestChanges(request, response);
        break;
      case "/board/walls/get":
        doGetWalls(request, response);
        break;
      case "/board/targets/get":
        doGetTargets(request, response);
        break;
      case "/game/get":
        doGetGame(request, response);
        break;
      case "/boardstate/get":
        doGetBoardState(request, response);
        break;
      case "/game/restart":
        doRestartGame(request, response);
        break;
      case "/game/solve":
        doGetSolveGame(request, response);
        break;
      case "/game/target/set":
        doGetSetTarget(request, response);
        break;
      case "/game/target/chooseNew":
        doGetChooseNewTarget(request, response);
        break;
      case "/robot/move":
        doMoveRobot(request, response);
        break;
      case "/robot/iswinner":
        doIsWinner(request, response);
        break;
      case "/submit/guess":
        doSubmitGuess(request, response);
        break;
      case "/submit/newplayer":
        doAddNewPlayer(request, response);
        break;
      case "/submit/removeplayer":
        doRemovePlayer(request, response);
        break;
      default:
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.getWriter().print("Unrecognized request: " + request.getRequestURI());
    }
  }

  private void doRemovePlayer(HttpServletRequest request, HttpServletResponse response) throws IOException {
    System.out.println("in do remove player");
    PrintWriter out = response.getWriter();
    response.setContentType("text/plain");
    response.setStatus(HttpServletResponse.SC_OK);
    String playerId = request.getParameter("playerId");
    int gameId = Integer.parseInt(request.getParameter("gameId"));
    Game game = getGame(gameId);
    
    if(!game.isDuplicateId(playerId)){
      out.println("{\"isValid\" : \"false\"}");
      System.out.println("Removing non-existant player");
      return;
    }
    
    System.out.println("player to move: " + game.getPlayerToMove() + ". player id: " + playerId);
    if (game.getPlayerToMove() != null && game.getPlayerToMove().equals(playerId)){
      System.out.println("player to move was removed");
      BoardState oldBoardState = game.getBoardStateBeforeMovement();
      BoardState currentBoardState = game.getBoardState();
      addUpdateEvent(UpdateEventType.ROBOT_JUMPED, new RobotJumpedEventData(Color.Red, currentBoardState.getRobotPosition(Color.Red), oldBoardState.getRobotPosition(Color.Red)), gameId);
      addUpdateEvent(UpdateEventType.ROBOT_JUMPED, new RobotJumpedEventData(Color.Blue, currentBoardState.getRobotPosition(Color.Blue), oldBoardState.getRobotPosition(Color.Blue)), gameId);
      addUpdateEvent(UpdateEventType.ROBOT_JUMPED, new RobotJumpedEventData(Color.Green, currentBoardState.getRobotPosition(Color.Green), oldBoardState.getRobotPosition(Color.Green)), gameId);
      addUpdateEvent(UpdateEventType.ROBOT_JUMPED, new RobotJumpedEventData(Color.Yellow, currentBoardState.getRobotPosition(Color.Yellow), oldBoardState.getRobotPosition(Color.Yellow)), gameId);
      game.updateBoardState(oldBoardState);
      if (game.allPlayersAttemptedToSolve()) {
        initNextRound(gameId);
      } else {
        game.setAndRemoveNextPlayerToMove();
        addUpdateEvent(UpdateEventType.PLAYER_TO_MOVE_CHANGED, new PlayerToMoveChangedData(game.getPlayerToMove()), gameId);
      }
    }
    
    game.removePlayer(playerId);
    List<PlayerAndScore> playersAndScores = game.getPlayersAndScoresOrderedByHighestScores();
    out.println("{\"isValid\" : \"true\" }");
    System.out.println("removing player");
    addUpdateEvent(
        UpdateEventType.PLAYER_LIST_CHANGED,
        new PlayerListChangedEventData(playersAndScores), gameId);
    
    //Check if this is the last player. If it is, destroy the game
    if(game.getPlayers().isEmpty()){
      ((ArrayList<Game>) getServletContext().getAttribute("GAMES")).remove(game);
    }
  
  }

  private void doGetGameIds(HttpServletRequest request, HttpServletResponse response) throws IOException {
    PrintWriter out = response.getWriter();
    response.setContentType("text/plain");
    response.setStatus(HttpServletResponse.SC_OK);
    Gson gson = new Gson();
    List<Game> games = ((ArrayList<Game>) getServletContext().getAttribute("GAMES"));
    System.out.println("num games: " + games.size());
    List<Integer> gameIds = new ArrayList<>();
    for(Game game : games){
      gameIds.add(game.getId());
      System.out.println("GameId: " + game.getId());
    }
    out.println(gson.toJson(gameIds));
  }

  private void doGetCurrentGameState(HttpServletRequest request, HttpServletResponse response) throws IOException {
    PrintWriter out = response.getWriter();
    int gameId = Integer.parseInt(request.getParameter("gameId"));
    response.setContentType("text/plain");
    response.setStatus(HttpServletResponse.SC_OK);
    Gson gson = new Gson();
    int version = getUpdateVersion(gameId);
    out.println(gson.toJson(getGame(gameId)));
  }
  
  private void doGetCurrentVersion(HttpServletRequest request, HttpServletResponse response) throws IOException {
    int gameId = Integer.parseInt(request.getParameter("gameId"));
    PrintWriter out = response.getWriter();
    response.setContentType("text/plain");
    response.setStatus(HttpServletResponse.SC_OK);
    Gson gson = new Gson();
    int version = getUpdateVersion(gameId);
    out.println("{ \"version\" : " + version + "}");
  }
  
  private void doGetStartGame(HttpServletRequest request, HttpServletResponse response) throws IOException{
    int gameId = initializeGame(0);
    PrintWriter out = response.getWriter();
    response.setContentType("text/plain");
    response.setStatus(HttpServletResponse.SC_OK);
    out.println("{\"gameId\" : " + gameId + "}");
  }

  private void doAddNewPlayer(HttpServletRequest request, HttpServletResponse response) throws IOException {
    PrintWriter out = response.getWriter();
    response.setContentType("text/plain");
    response.setStatus(HttpServletResponse.SC_OK);
    String playerId = request.getParameter("playerId");
    int gameId = Integer.parseInt(request.getParameter("gameId"));
    Game game = getGame(gameId);
    if(game.isDuplicateId(playerId)){
      out.println("{\"isValid\" : \"false\"}");
      System.out.println("DUPE");
      return;
    }
    game.addToPlayerIds(playerId);
    game.addPlayerAndScoreToMap(playerId, 0);
    List<PlayerAndScore> playersAndScores = game.getPlayersAndScoresOrderedByHighestScores();
    out.println("{\"isValid\" : \"true\" }");
    System.out.println("not dupe");
    addUpdateEvent(
        UpdateEventType.PLAYER_LIST_CHANGED,
        new PlayerListChangedEventData(playersAndScores), gameId);
  }

  private void doSubmitGuess(HttpServletRequest request, HttpServletResponse response) {
    String guesserId = request.getParameter("guesserId");
    String guess = request.getParameter("guess");
    int gameId = Integer.parseInt(request.getParameter("gameId"));
    Game game = getGame(gameId);
    if (game.getPhase().equals(Phase.GUESSING)) {
      if (game.isFirstGuess()) {
        game.startCountdownToChangePhase();
      }
      game.addGuessToMap(guesserId, Integer.parseInt(guess));
      List<PlayerAndGuess> playersAndGuesses = game.getPlayersAndGuessesOrderedByLowestGuess();
      addUpdateEvent(
          UpdateEventType.GUESS_SUBMITTED,
          new GuessSubmittedEventData(playersAndGuesses), gameId);
    }
  }

  private void doGetLatestChanges(HttpServletRequest request, HttpServletResponse response) throws IOException {
    int updateVersion = Integer.valueOf(request.getParameter("version"));
    int gameId = Integer.parseInt(request.getParameter("gameId"));
    Game game = getGame(gameId);
    long startTime = System.currentTimeMillis();
    while (!hasChangedSince(updateVersion, gameId)
            && (System.currentTimeMillis() - startTime) < LATEST_CHANGES_TIMEOUT_MILLIS) {
      try {
        Thread.sleep(250);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      long timeElapsed = game.updateGuessingPhaseCountdown();
      if (timeElapsed >= 0 && timeElapsed < game.getCountdownStartTime()) {
        addUpdateEvent(UpdateEventType.TIMER_CHANGED, new TimerChangedData(game.getCountdownStartTime() - timeElapsed), gameId);
      } else if (timeElapsed >= game.getCountdownStartTime() && game.isHasNotUpdatedPlayerToMove()){
        addUpdateEvent(UpdateEventType.PLAYER_TO_MOVE_CHANGED, new PlayerToMoveChangedData(game.getPlayerToMove()), gameId);
        game.setHasNotUpdatedPlayerToMove(false);
      }
    }
    PrintWriter out = response.getWriter();
    Gson gson = new Gson();
    System.out.print(gson.toJson(getUpdateEventsSince(updateVersion, gameId)));
    out.println(gson.toJson(getUpdateEventsSince(updateVersion, gameId)));
  }

  /**
   * This does NOT remove target from unused targets
   */
  private void doGetSetTarget(HttpServletRequest request, HttpServletResponse response) throws IOException {
    int gameId = Integer.parseInt(request.getParameter("gameId"));
    Target oldTarget = getBoardState(gameId).getChosenTarget();

    Color color = Color.valueOf(request.getParameter("color"));
    Shape shape = Shape.valueOf(request.getParameter("shape"));
    Target newTarget = Target.getTarget(color, shape);
    changeTarget(gameId, oldTarget, newTarget);
  }
  
  private void changeTarget(int gameId, Target oldTarget, Target newTarget) {
    getBoardState(gameId).setChosenTarget(newTarget);
    addUpdateEvent(UpdateEventType.TARGET_SET, new TargetSetEventData(oldTarget, newTarget, getPosition(gameId, newTarget)), gameId);    
  }

  private void doGetChooseNewTarget(HttpServletRequest request, HttpServletResponse response) throws IOException {
    int gameId = Integer.parseInt(request.getParameter("gameId"));
    Target oldTarget = getBoardState(gameId).getChosenTarget();
    if (oldTarget != null) {
      getGame(gameId).removeTarget(getGame(gameId).getBoardState().getChosenTarget());
    }

    // Choose random target to return
    List<Target> unusedTargets = getGame(gameId).getUnusedTargets();
    int n = (int) Math.floor((Math.random() * unusedTargets.size()));
    Target newTarget = unusedTargets.get(n);

    changeTarget(gameId, oldTarget, newTarget);
  }

  private void chooseNewTarget(int gameId) throws IOException {
    Target oldTarget = getBoardState(gameId).getChosenTarget();
    if (oldTarget != null) {
      getGame(gameId).removeTarget(getGame(gameId).getBoardState().getChosenTarget());
    }

    // Choose random target to return
    List<Target> unusedTargets = getGame(gameId).getUnusedTargets();
    int n = (int) Math.floor((Math.random() * unusedTargets.size()));
    Target newTarget = unusedTargets.get(n);

    changeTarget(gameId, oldTarget, newTarget);
  }
  
  private Position getPosition(int gameId, Target target) {
    return getGame(gameId).getBoard().getTargetPosition(target);
  }

  private void updateBoardState(int gameId, Color robot, Position newPosition) {
    RobotPositions.Builder robotPositionsBuilder = new RobotPositions.Builder(getBoardState(gameId).getRobotPositions());
    robotPositionsBuilder.setRobotPosition(robot, newPosition);
    BoardState newBoardState = new BoardState(getBoardState(gameId).getChosenTarget(), robotPositionsBuilder.build());
    getGame(gameId).updateBoardState(newBoardState);
  }

  private void doIsWinner(HttpServletRequest request, HttpServletResponse response) throws IOException {
    PrintWriter out = response.getWriter();
    int gameId = Integer.parseInt(request.getParameter("gameId"));
    Color robot = Color.valueOf(request.getParameter("robot"));
    Gson gson = new Gson();
    boolean isWinner = isWinner(robot, getGame(gameId).getBoard(), getBoardState(gameId));
    out.println(gson.toJson(isWinner));
  }

  private void doMoveRobot(HttpServletRequest request, HttpServletResponse response) throws IOException {
    int gameId = Integer.parseInt(request.getParameter("gameId"));
    Color robot = Color.valueOf(request.getParameter("robot"));
    Direction direction = Direction.valueOf(request.getParameter("direction"));
    Position oldPosition = getBoardState(gameId).getRobotPosition(robot);
    Position newPosition = MoveCalculator.calculateRobotPositionAfterMoving(getBoardState(gameId), getGame(gameId).getBoard(),
            robot, direction);
    String moverId = request.getParameter("moverId");
    Game game = getGame(gameId);
    if (!game.getPhase().equals(Phase.SOLVING)) {
      return;
    }
    if (!game.getPlayerToMove().equals(moverId)){
      return;
    }
    updateBoardState(gameId, robot, newPosition);
    if(!oldPosition.equals(newPosition)){
      game.incrementMoves();
    }
    addUpdateEvent(UpdateEventType.ROBOT_GLIDED, new RobotGlidedEventData(robot, oldPosition, newPosition, game.getMoves(), direction), gameId);
    if (isWinner(robot, game.getBoard(), game.getBoardState())) {
      game.addPlayerAndScoreToMap(moverId, game.getPlayersToScores().get(moverId)+1);
      addUpdateEvent(UpdateEventType.SCORE_UPDATED, new ScoreUpdatedEventData(game.getPlayersAndScoresOrderedByHighestScores()), gameId);
      initNextRound(gameId);
    } else {
      if (game.playerSurpassedGuess()) {
        BoardState oldBoardState = game.getBoardStateBeforeMovement();
        BoardState currentBoardState = game.getBoardState();
        addUpdateEvent(UpdateEventType.ROBOT_JUMPED, new RobotJumpedEventData(Color.Red, currentBoardState.getRobotPosition(Color.Red), oldBoardState.getRobotPosition(Color.Red)), gameId);
        addUpdateEvent(UpdateEventType.ROBOT_JUMPED, new RobotJumpedEventData(Color.Blue, currentBoardState.getRobotPosition(Color.Blue), oldBoardState.getRobotPosition(Color.Blue)), gameId);
        addUpdateEvent(UpdateEventType.ROBOT_JUMPED, new RobotJumpedEventData(Color.Green, currentBoardState.getRobotPosition(Color.Green), oldBoardState.getRobotPosition(Color.Green)), gameId);
        addUpdateEvent(UpdateEventType.ROBOT_JUMPED, new RobotJumpedEventData(Color.Yellow, currentBoardState.getRobotPosition(Color.Yellow), oldBoardState.getRobotPosition(Color.Yellow)), gameId);
        game.updateBoardState(oldBoardState);
        if (game.allPlayersAttemptedToSolve()) {
          initNextRound(gameId);
        } else {
          game.setAndRemoveNextPlayerToMove();
          addUpdateEvent(UpdateEventType.PLAYER_TO_MOVE_CHANGED, new PlayerToMoveChangedData(game.getPlayerToMove()), gameId);
          return;
        }
      }
    }
  }
  
  private void initNextRound(int gameId) throws IOException {
    addUpdateEvent(UpdateEventType.GUESS_SUBMITTED, new GuessSubmittedEventData(new ArrayList<PlayerAndGuess>()), gameId);
    addUpdateEvent(UpdateEventType.PLAYER_TO_MOVE_CHANGED, new PlayerToMoveChangedData(" "), gameId);
    Game game = getGame(gameId);
    chooseNewTarget(gameId);
    game.updateBoardStateBeforeMovement();
    game.clearAllRoundData();
    game.incrementRound();
    game.setHasNotUpdatedPlayerToMove(true);
    game.setPhase(Phase.GUESSING);
  }

  private boolean isWinner(Color robot, Board board, BoardState boardState) {
    Target chosenTarget = boardState.getChosenTarget();
    Color targetColor = chosenTarget.getColor();
    return boardState.getRobotPosition(targetColor).equals(board.getTargetPosition(chosenTarget));
  }
  
  private synchronized void doRestartGame(HttpServletRequest request, HttpServletResponse response) throws IOException {
    int gameId = Integer.parseInt(request.getParameter("gameId"));
    Solver solver = getSolver();
    if(solver != null){
      solver.tryToCancel();
    }
    int newGameId =  initializeGame(0);
    addUpdateEvent(UpdateEventType.GAME_RESTARTED, new GameRestartedEventData(newGameId), gameId);
  }  

  private void doGetSolveGame(HttpServletRequest request, HttpServletResponse response) throws IOException {
    int gameId = Integer.parseInt(request.getParameter("gameId"));
    response.setContentType("text/html");
    UnprocessedMovesType movesType = UnprocessedMovesType.BREADTH_FIRST_SEARCH;
    String movesTypeParam = request.getParameter("unprocessedMovesType");
    if (movesTypeParam != null) {
      movesType = UnprocessedMovesType.valueOf(movesTypeParam);
    }
    
    String numberOfRoundsString = request.getParameter("numberOfRounds");
    int numberOfRounds = (numberOfRoundsString == null) ? 1 : Integer.parseInt(numberOfRoundsString);

    Game game = getGame(gameId);
    if (getSolver() != null) {
      getSolver().tryToCancel();
    }
    
    while (numberOfRounds > 0 && !game.getUnusedTargets().isEmpty()) {
      Solver newSolver = (new SolverFactory()).createSolver(game, movesType);
      setSolver(newSolver);
      List<MoveNode> winningMoves = newSolver.solve();
      if (winningMoves.isEmpty()) {
        // Solver was cancelled or couldn't find a solution. Should send this info to client.
      }
      else {
        MoveNode winningMove = winningMoves.get(0);      
        for (MoveNode moveNode : winningMove.getAncestorsFromRootDownToSelf()) {
          Move move = moveNode.getMove();
          if (move != null) {
            Color robot = move.getRobot();
            Direction direction = move.getDirection();
            Position oldPosition = getBoardState(gameId).getRobotPosition(robot);
            Position newPosition = MoveCalculator.calculateRobotPositionAfterMoving(getBoardState(gameId), getGame(gameId).getBoard(),
                  robot, direction);
          
            updateBoardState(gameId, robot, newPosition);
            addUpdateEvent(UpdateEventType.ROBOT_GLIDED, new RobotGlidedEventData(robot, oldPosition, newPosition, getGame(gameId).getMoves(), direction), gameId);      
          }
        }
        doGetChooseNewTarget(request, response);
        numberOfRounds--;
      }
    }
  }

  public int getTargetIndexParam(HttpServletRequest request) {
    int targetIndex = 0;
    String targetIndexParam = request.getParameter("targetIndex");
    if (targetIndexParam != null) {
      targetIndex = Integer.parseInt(targetIndexParam);
    }
    return targetIndex;
  }

  private void doGetWalls(HttpServletRequest request, HttpServletResponse response) throws IOException {
    int gameId = Integer.parseInt(request.getParameter("gameId"));
    PrintWriter out = response.getWriter();
    response.setContentType("text/plain");
    response.setStatus(HttpServletResponse.SC_OK);
    Gson gson = new Gson();
    out.println(gson.toJson(getGame(gameId).getBoard().getBoardItems()));
  }

  private static class TargetsAndPositions {
    private List<Target> targets = new ArrayList<>();
    private List<Position> positions = new ArrayList<>();
    private List<Integer> targetIndex = new ArrayList<>();
  }

  private void doGetTargets(HttpServletRequest request, HttpServletResponse response) throws IOException {
    int gameId = Integer.parseInt(request.getParameter("gameId"));
    PrintWriter out = response.getWriter();
    response.setContentType("text/plain");
    response.setStatus(HttpServletResponse.SC_OK);
    Map<Target, Position> targetsToPositions = getGame(gameId).getBoard().getTargetsToPositions();
    TargetsAndPositions targetsAndPositions = new TargetsAndPositions();
    targetsAndPositions.targets = new ArrayList<>(targetsToPositions.keySet());
    targetsAndPositions.positions = new ArrayList<>(targetsToPositions.values());
    Gson gson = new Gson();
    String jsonString = gson.toJson(targetsAndPositions);
    out.println(jsonString);
  }

  private void doGetGame(HttpServletRequest request, HttpServletResponse response) throws IOException {
    int gameId = Integer.parseInt(request.getParameter("gameId"));
    PrintWriter out = response.getWriter();
    response.setContentType("text/plain");
    response.setStatus(HttpServletResponse.SC_OK);
    Gson gson = new Gson();
    String jsonString = gson.toJson(getGame(gameId));
    out.println(jsonString);
  }

  private void doGetBoardState(HttpServletRequest request, HttpServletResponse response) throws IOException {
    int gameId = Integer.parseInt(request.getParameter("gameId"));
    PrintWriter out = response.getWriter();
    response.setContentType("text/plain");
    response.setStatus(HttpServletResponse.SC_OK);
    Gson gson = new Gson();
    out.println(gson.toJson(getBoardState(gameId)));
  }
}