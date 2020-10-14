package org.hirschhorn.ricochet.updateevent;

import org.hirschhorn.ricochet.game.UpdateEventData;

public class PlayerToMoveChangedData implements UpdateEventData {
  
  String player = null;
  
  public PlayerToMoveChangedData(String player){
    this.player = player;
  }
  
}
