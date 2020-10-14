package org.hirschhorn.ricochet.updateevent;

public class PlayerAndScore {
  private String playerId;
  private int score;
  
  public PlayerAndScore(String playerId, int score) {
    this.playerId = playerId;
    this.score = score;
  }

  public String getPlayerId() {
    return playerId;
  }

  public int getScore() {
    return score;
  }
}
