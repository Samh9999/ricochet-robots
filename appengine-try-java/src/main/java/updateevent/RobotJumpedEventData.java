package org.hirschhorn.ricochet.updateevent;

import org.hirschhorn.ricochet.board.Color;
import org.hirschhorn.ricochet.board.Position;
import org.hirschhorn.ricochet.game.UpdateEventData;

public class RobotJumpedEventData implements UpdateEventData {

  private Color robot;
  private Position oldPosition;
  private Position newPosition;
  
  public RobotJumpedEventData(Color robot, Position oldPosition, Position newPosition) {
    super();
    this.robot = robot;
    this.oldPosition = oldPosition;
    this.newPosition = newPosition;
  }
  
  public Color getRobot() {
    return robot;
  }
  public Position getOldPosition() {
    return oldPosition;
  }
  public Position getNewPosition() {
    return newPosition;
  }
  
  
}
