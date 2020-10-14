package org.hirschhorn.ricochet.updateevent;

import java.util.ArrayList;
import java.util.List;

import org.hirschhorn.ricochet.game.UpdateEventData;

public class PlayerListChangedEventData implements UpdateEventData{
  private List<PlayerAndScore> playersAndScores;
  
  public PlayerListChangedEventData(List<PlayerAndScore> playersAndScores) {
    super();
    this.playersAndScores = playersAndScores;

  }

  public List<PlayerAndScore> getPlayersAndScores() {
    return playersAndScores;
  }
}
