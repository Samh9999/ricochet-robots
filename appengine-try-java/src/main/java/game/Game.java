package org.hirschhorn.ricochet.game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hirschhorn.ricochet.board.Target;
import org.hirschhorn.ricochet.updateevent.PlayerAndScore;

public class Game implements Serializable {
  
  private static final long serialVersionUID = -3822275760307111181L;
  
  private Board board;
  private BoardState boardState;
  private List<Target> unusedTargets;
  private int round;
  private Phase phase;
  private LinkedHashMap<String, Integer> playersToGuesses;
  private List<String> playerIds;
  private Map<String, Integer> playersToScores;
  
  private String playerToMove;
  private boolean hasNotUpdatedPlayerToMove;

  private int moves;
  private int moveLimit;
  private long guessingPhaseCountdownStartedTime;
  private BoardState boardStateBeforeMovement;
  
  private int gameId;
  
  //TODO: CHANGE ONLY FOR TESTING. SHOULD BE 30000 FOR ACTUAL GAMEPLAY
  private static final long COUNTDOWN_START_TIME = 5000;

  public BoardState getBoardStateBeforeMovement() {
    return boardStateBeforeMovement;
  }

  public String getPlayerToMove() {
    return playerToMove;
  }

  public int getMoves() {
    return moves;
  }
  
  public int getMoveLimit(){
    return moveLimit;
  }
  
  public boolean isHasNotUpdatedPlayerToMove() {
    return hasNotUpdatedPlayerToMove;
  }

  public void setHasNotUpdatedPlayerToMove(boolean hasNotUpdatedPlayerToMove) {
    this.hasNotUpdatedPlayerToMove = hasNotUpdatedPlayerToMove;
  }
  
  public Game(Board board, BoardState boardState) {
    this.board = board;
    this.boardState = boardState;
    playerIds = new ArrayList<>();
    playersToGuesses = new LinkedHashMap<>();
    playersToScores = new HashMap<>();
    unusedTargets = Target.buildAllTargets();
    playerToMove = null;
    guessingPhaseCountdownStartedTime = 0;
    boardStateBeforeMovement = new BoardState(boardState);
    round = 0;
    hasNotUpdatedPlayerToMove = true;
  }

  public Board getBoard() {
    return board;
  }

  public BoardState getBoardState() {
    return boardState;
  }

  public void updateBoardState(BoardState newBoardState) {
    boardState = newBoardState;
  }
  
  public List<Target> getUnusedTargets() {
    return unusedTargets;
  }
  
  public void removeTarget(Target target){
    unusedTargets.remove(target);
  }

  public void addGuessToMap(String playerId, int guess) {
    if (!playersToGuesses.containsKey(playerId) || playersToGuesses.get(playerId) > guess) {
      playersToGuesses.put(playerId, guess);
    }
  }
  
  public void addToPlayerIds(String playerId){
    playerIds.add(playerId);
  }
  
  public Phase getPhase() {
    return phase;
  }

  public void setPhase(Phase phase) {
    this.phase = phase;
  }

  private void transitionToSolvingPhase() {
    if (!phase.equals(Phase.GUESSING)) {
      throw new AssertionError("Cannot transition to solving from " + phase);
    }
    setPhase(Phase.SOLVING);
    setAndRemoveNextPlayerToMove();
  }
  
  public void setAndRemoveNextPlayerToMove() {
    int lowestGuess = Integer.MAX_VALUE;
    String candidate = null;
    for(Entry<String, Integer> entry : playersToGuesses.entrySet()) {
      if (entry.getValue() < lowestGuess) {
        lowestGuess = entry.getValue();
        candidate = entry.getKey();
      }
    }
    moveLimit = playersToGuesses.get(candidate);
    playersToGuesses.remove(candidate);
    playerToMove = candidate;
    boardState = boardStateBeforeMovement;
    moves = 0;
  }

  public synchronized void startCountdownToChangePhase() {
    //because its already started and only one person can start it
    if(guessingPhaseCountdownStartedTime != 0){
      return;
    }
    guessingPhaseCountdownStartedTime = System.currentTimeMillis();
  }

  public boolean isFirstGuess() {
    return playersToGuesses.isEmpty();
  }

  public boolean playerSurpassedGuess() {
    if (moveLimit > moves){
      return false;
    }
  return true;
  }

  public void updateBoardStateBeforeMovement() {
    boardStateBeforeMovement = new BoardState(boardState);
  }

  public synchronized long updateGuessingPhaseCountdown() {
    if(guessingPhaseCountdownStartedTime == 0) {
      return -1;
    }
    long millisecondsElapsed = System.currentTimeMillis() - guessingPhaseCountdownStartedTime;
    if(millisecondsElapsed > COUNTDOWN_START_TIME) {
      transitionToSolvingPhase();
      guessingPhaseCountdownStartedTime = 0;
    }
    return millisecondsElapsed;
  }
  
  public long getCountdownStartTime(){
    return COUNTDOWN_START_TIME;
  }

  public void incrementMoves() {
    moves++;
  }

  public void clearAllRoundData() {
    playersToGuesses.clear();
    playerToMove = null;
  }

  public void incrementRound() {
    round++;
  }

  public boolean allPlayersAttemptedToSolve() {
    return playersToGuesses.isEmpty();
  }

  /**
   * Return PlayerAndGuess list sorted by lowest guess.
   * @return
   */
  public List<PlayerAndGuess> getPlayersAndGuessesOrderedByLowestGuess() {
    List<PlayerAndGuess> playersAndGuesses = new ArrayList<>();
    Map<String, Integer> tempCopy = new LinkedHashMap<>(playersToGuesses);
    while (!tempCopy.isEmpty()){
      int lowestGuess = Integer.MAX_VALUE;
      String candidate = null;
      for(Entry<String, Integer> entry : tempCopy.entrySet()) {
        if (entry.getValue() < lowestGuess) {
          lowestGuess = entry.getValue();
          candidate = entry.getKey();
        }
      }
      tempCopy.remove(candidate);
      playersAndGuesses.add(new PlayerAndGuess(candidate, lowestGuess));
    }
    return playersAndGuesses;
  }
  
  public List<PlayerAndScore> getPlayersAndScoresOrderedByHighestScores(){
    List<PlayerAndScore> playersAndScores = new ArrayList<>();
    Map<String, Integer> tempCopy = new LinkedHashMap<>(playersToScores);
    while (!tempCopy.isEmpty()){
      int highestScore = Integer.MIN_VALUE;
      String candidate = null;
      for(Entry<String, Integer> entry : tempCopy.entrySet()) {
        if (entry.getValue() > highestScore) {
          highestScore = entry.getValue();
          candidate = entry.getKey();
        }
      }
      tempCopy.remove(candidate);
      playersAndScores.add(new PlayerAndScore(candidate, highestScore));
    }
    return playersAndScores;
  }

  public List<String> getPlayers() {
    return playerIds;
  }

  public void addPlayerAndScoreToMap(String playerId, Integer score) {
    playersToScores.put(playerId, score);
  }
  
  public void removePlayer(String playerId){
    playerIds.remove(playerId);
    playersToGuesses.remove(playerId);
    playersToScores.remove(playerId);
  }
  
  public Map<String, Integer> getPlayersToScores(){
    return playersToScores;
  }

  public int getId() {
    return gameId;
  }

  public void setGameId(int gameId) {
    this.gameId = gameId;
  }

  public boolean isDuplicateId(String playerId) {
    return playerIds.contains(playerId);
  };

  
}
