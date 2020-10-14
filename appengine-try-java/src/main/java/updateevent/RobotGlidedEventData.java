package org.hirschhorn.ricochet.updateevent;

import org.hirschhorn.ricochet.board.Color;
import org.hirschhorn.ricochet.board.Direction;
import org.hirschhorn.ricochet.board.Position;
import org.hirschhorn.ricochet.game.UpdateEventData;

public class RobotGlidedEventData implements UpdateEventData {

  private Color robot;
  private Position oldPosition;
  private Position newPosition;
  private Direction direction;
  private int numberOfMoves;
  
  public RobotGlidedEventData(Color robot, Position oldPosition, Position newPosition, int numberOfMoves, Direction direction) {
    super();
    this.robot = robot;
    this.oldPosition = oldPosition;
    this.newPosition = newPosition;
    this.numberOfMoves = numberOfMoves;
    this.direction = direction;
  }
  
  public int getNumberOfMoves(){
    return numberOfMoves;
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
  public Direction getDirection() {
    return direction;
  }
  
}
