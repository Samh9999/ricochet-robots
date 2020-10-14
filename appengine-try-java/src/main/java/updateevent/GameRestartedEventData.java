package org.hirschhorn.ricochet.updateevent;

import org.hirschhorn.ricochet.game.UpdateEventData;

public class GameRestartedEventData implements UpdateEventData {

  private int newGameId;
  
  public GameRestartedEventData(int newGameId) {
    super();
    this.newGameId = newGameId;
  }
  
  public int getNewGameId(){
    return newGameId;
  }

}
