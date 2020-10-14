package org.hirschhorn.ricochet.game;

public class PlayerAndGuess {
  
  private String playerId;
  private int guess;
  
  public PlayerAndGuess(String playerId, int guess) {
    this.playerId = playerId;
    this.guess = guess;
  }

  public String getPlayerId() {
    return playerId;
  }

  public int getGuess() {
    return guess;
  }
}
