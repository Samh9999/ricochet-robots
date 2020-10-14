package org.hirschhorn.ricochet.game;

import org.hirschhorn.ricochet.board.Color;
import org.hirschhorn.ricochet.board.Direction;
import org.hirschhorn.ricochet.board.Position;

public class MoveCalculator {
  
  public static Position calculateRobotPositionAfterMoving(BoardState boardState, Board board, Color robot, Direction direction) {
    RobotPositions robotPositions = boardState.getRobotPositions();
    Position robotPosition = robotPositions.getRobotPosition(robot);
      boolean hitObject = false;
      while (!hitObject) {
        if (board.hasWall(robotPosition, direction)) {
          hitObject = true;
        } else {
          Position potentialPosition = getAdjacentPosition(robotPosition, direction);
          if (hasRobot(potentialPosition, robotPositions)) {
            hitObject = true;
          } else {
            robotPosition = potentialPosition;
          }
        }
      }   
    return robotPosition;
  }
  
  private static Position getAdjacentPosition(Position robotPosition, Direction direction) {
    int newX = robotPosition.getX();
    int newY = robotPosition.getY();
    switch (direction) {
      case North:
        newY--;
        break;
      case South:
        newY++;
        break;
      case East:
        newX++;
        break;
      case West:
        newX--;
        break;
      default:
        throw new AssertionError("Unknown direction: " + direction);
    }
    Position adjacentPosition = Position.of(newX, newY);
    return adjacentPosition;
  }

  private static boolean hasRobot(Position potentialPosition, RobotPositions positions) {
    for (Color robot : Color.values()) {
      if (potentialPosition.equals(positions.getRobotPosition(robot))) {
        return true;
      }
    }
    return false;
  }
  
}
