package org.hirschhorn.ricochet.updateevent;

import org.hirschhorn.ricochet.board.Position;
import org.hirschhorn.ricochet.board.Target;
import org.hirschhorn.ricochet.game.UpdateEventData;

public class TargetSetEventData implements UpdateEventData {

  private Target oldTarget;
  private Target newTarget;
  private Position position;
  
  public TargetSetEventData(Target oldTarget, Target newTarget, Position position) {
    super();
    this.oldTarget = oldTarget;
    this.newTarget = newTarget;
    this.position = position;
  }

  public Target getOldTarget() {
    return oldTarget;
  }

  public Target getNewTarget() {
    return newTarget;
  }

  public Position getPosition() {
    return position;
  }

}
