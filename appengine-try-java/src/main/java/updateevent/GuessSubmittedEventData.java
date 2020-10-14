package org.hirschhorn.ricochet.updateevent;

import java.util.List;

import org.hirschhorn.ricochet.game.PlayerAndGuess;
import org.hirschhorn.ricochet.game.UpdateEventData;

public class GuessSubmittedEventData implements UpdateEventData {
  private List<PlayerAndGuess> playersAndGuesses;
  
  public GuessSubmittedEventData(List<PlayerAndGuess> playersAndGuesses) {
    super();
    this.setPlayersAndGuesses(playersAndGuesses);
  }

  public List<PlayerAndGuess> getPlayersAndGuesses() {
    return playersAndGuesses;
  }

  public void setPlayersAndGuesses(List<PlayerAndGuess> playersAndGuesses) {
    this.playersAndGuesses = playersAndGuesses;
  }
}
