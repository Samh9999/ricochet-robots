package org.hirschhorn.ricochet.game;

import java.util.ArrayList;
import java.util.List;

import org.hirschhorn.ricochet.board.Color;
import org.hirschhorn.ricochet.board.Position;

public class RobotPositions {

  //private Map<Color, Position> positions;
   private List<Position> positions;

  public static class Builder {
    //private Map<Color, Position> positions;
     private List<Position> positions;

    public Builder() {
      //positions = new HashMap<>();
       positions = new ArrayList<>();
       for (int i = 0; i < Color.values().length; i++) {
       positions.add(null);
       }
    }

    public Builder(RobotPositions robotPositions) {
      this();
      for (Color color : Color.values()) {
        setRobotPosition(color, robotPositions.getRobotPosition(color));
      }
    }

    public Builder setRobotPosition(Color robot, Position position) {
      //positions.put(robot, position);
       positions.set(indexOfColor(robot), position);
      return this;
    }

    public RobotPositions build() {
      return new RobotPositions(positions);
    }

    public Position getRobotPosition(Color robot) {
      //return positions.get(robot);
       return positions.get(indexOfColor(robot));
    }
  }

  //private RobotPositions(Map<Color, Position> positions) {
     private RobotPositions(List<Position> positions) {

     while (positions.size() < Color.values().length) {
     positions.add(null);
     }

    for (Color color : Color.values()) {
      //Position position = positions.get(color);
       Position position = positions.get(indexOfColor(color));
      if (position == null) {
        // throw new IllegalArgumentException("Cannot create RobotPositions
        // because not all positions are specified: " + positions);
        //positions.put(color, Position.of(-1, -1));
         positions.set(indexOfColor(color), Position.of(-1, -1));
      }
    }

    //this.positions = new HashMap<>(positions);
     this.positions = new ArrayList<>(positions);
  }

  // Makes a copy of RobotPositions
  public RobotPositions(RobotPositions robotPositions) {
    this(robotPositions.positions);
  }

  public int size() {
    return positions.size();
  }

  public static int indexOfColor(Color color) {
    switch (color) {
      case Red:
        return 0;
      case Yellow:
        return 1;
      case Green:
        return 2;
      case Blue:
        return 3;
      default:
        throw new AssertionError("Unknown color: " + color);
    }
  }

  public static Color colorOfIndex(int index) {
    switch (index) {
      case 0:
        return Color.Red;
      case 1:
        return Color.Yellow;
      case 2:
        return Color.Green;
      case 3:
        return Color.Blue;
      default:
        throw new AssertionError("Unknown index for color: " + index);
    }
  }

  public Position getRobotPosition(Color robot) {
    //return positions.get(robot);
     return positions.get(indexOfColor(robot));
  }

  public String toString() {
    return String.format(
        "%s(%s) %s(%s) %s(%s) %s(%s)",
        Color.Red,
        getRobotPosition(Color.Red).asSimpleString(),
        Color.Green,
        getRobotPosition(Color.Green).asSimpleString(),
        Color.Blue,
        getRobotPosition(Color.Blue).asSimpleString(),
        Color.Yellow,
        getRobotPosition(Color.Yellow).asSimpleString());
  }

  @Override
  public int hashCode() {
    return positions.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    return positions.equals(((RobotPositions)obj).positions);
  }

  public static int compressRobotPositions(RobotPositions expandedPositions) {
    int compressed = 0;
    
    Position position = expandedPositions.getRobotPosition(colorOfIndex(0));
    compressed = (compressed * 16) + position.getX();
    compressed = (compressed * 16) + position.getY();

    position = expandedPositions.getRobotPosition(colorOfIndex(1));
    compressed = (compressed * 16) + position.getX();
    compressed = (compressed * 16) + position.getY();
    
    position = expandedPositions.getRobotPosition(colorOfIndex(2));
    compressed = (compressed * 16) + position.getX();
    compressed = (compressed * 16) + position.getY();

    position = expandedPositions.getRobotPosition(colorOfIndex(3));
    compressed = (compressed * 16) + position.getX();
    if (position.getY() <= 7) {
      compressed = (compressed * 8) + position.getY();      
    } else {
      compressed = (compressed * 8) + (position.getY() - 8);
      compressed = compressed - Integer.MAX_VALUE - 1;
    }
    
    return compressed;
  }

  public static RobotPositions expandRobotPositions(int compressedPositions) {
    RobotPositions.Builder expandedPositions = new RobotPositions.Builder();

    int y = 0;
    if (compressedPositions < 0) {
      compressedPositions = compressedPositions + 1 + Integer.MAX_VALUE;
      y = (compressedPositions % 8) + 8;
      compressedPositions /= 8;
    } else {
      y = compressedPositions % 8;
      compressedPositions /= 8;
    }
    int x = compressedPositions % 16;
    compressedPositions /= 16;
    expandedPositions.setRobotPosition(colorOfIndex(3), Position.of(x, y));
    
    y = compressedPositions % 16;
    compressedPositions /= 16;
    x = compressedPositions % 16;
    compressedPositions /= 16;
    expandedPositions.setRobotPosition(colorOfIndex(2), Position.of(x, y));

    y = compressedPositions % 16;
    compressedPositions /= 16;
    x = compressedPositions % 16;
    compressedPositions /= 16;
    expandedPositions.setRobotPosition(colorOfIndex(1), Position.of(x, y));

    y = compressedPositions % 16;
    compressedPositions /= 16;
    x = compressedPositions % 16;
    compressedPositions /= 16;
    expandedPositions.setRobotPosition(colorOfIndex(0), Position.of(x, y));

    return expandedPositions.build();
  }

}
