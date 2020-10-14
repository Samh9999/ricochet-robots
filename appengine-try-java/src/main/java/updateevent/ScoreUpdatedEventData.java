package org.hirschhorn.ricochet.updateevent;

import java.util.List;
import java.util.Map;

import org.hirschhorn.ricochet.game.PlayerAndGuess;
import org.hirschhorn.ricochet.game.UpdateEventData;

public class ScoreUpdatedEventData implements UpdateEventData{
  
  private List<PlayerAndScore> playersAndScores;
  
  public ScoreUpdatedEventData(List<PlayerAndScore> playersAndScores) {
    super();
    this.playersAndScores = playersAndScores;
  }

  public List<PlayerAndScore> getPlayersAndScores() {
    return playersAndScores;
  }

  public void setPlayersAndGuesses(List<PlayerAndGuess> playersAndGuesses) {
    this.playersAndScores = playersAndScores;
  }
}
