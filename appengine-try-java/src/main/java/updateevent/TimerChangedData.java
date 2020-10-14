package org.hirschhorn.ricochet.updateevent;

import org.hirschhorn.ricochet.game.UpdateEventData;

public class TimerChangedData implements UpdateEventData {
  
    long timerValue;
    
    public TimerChangedData(long timeElapsed){
      this.timerValue = timeElapsed;
    }
}
