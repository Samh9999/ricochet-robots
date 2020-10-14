package org.hirschhorn.ricochet.board;

public class BoardItem {
  
  private Position position;
  private boolean northWall;
  private boolean southWall;
  private boolean eastWall;
  private boolean westWall;

  public BoardItem(Position position) {
    this.position = position;
  }

  public boolean hasNorthWall() {
    return northWall;
  }

  public boolean hasEastWall() {
    return eastWall;
  }

  public boolean hasSouthWall() {
    return southWall;
  }

  public boolean hasWestWall() {
    return westWall;
  }

  public Position getPosition() {
    return position;
  }

  public BoardItem setEastWall(boolean b) {
    eastWall = b;
    return this;
  }
  
  public BoardItem setNorthWall(boolean b) {
    northWall = b;
    return this;
  }
  
  public BoardItem setWestWall(boolean b) {
    westWall = b;
    return this;
  }
  
  public BoardItem setSouthWall(boolean b) {
    southWall = b;
    return this;
  }

  @Override
  public String toString() {
    return "BoardItem [position=" + position.asSimpleString() + ", northWall=" + northWall + ", southWall=" + southWall
        + ", eastWall=" + eastWall + ", westWall=" + westWall + "]";
  }  
}
