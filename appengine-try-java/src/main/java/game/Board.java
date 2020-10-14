package org.hirschhorn.ricochet.game;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.hirschhorn.ricochet.board.BoardItem;
import org.hirschhorn.ricochet.board.Direction;
import org.hirschhorn.ricochet.board.Position;
import org.hirschhorn.ricochet.board.Target;

public class Board {
  
  private LinkedHashMap<Target, Position> targetsToPositions;
  private List<BoardItem> boardItems;
  
  public Board(LinkedHashMap<Target, Position> targetsToPositions, List<BoardItem> boardItems) { 
    this.targetsToPositions = targetsToPositions;
    this.boardItems = boardItems;
  }
  
  public boolean hasWall(Position position, Direction direction) {
    BoardItem boardItem = getBoardItem(position);
    switch(direction) {
      case North:
        return boardItem.hasNorthWall();
      case East:
        return boardItem.hasEastWall();
      case South:
        return boardItem.hasSouthWall(); 
      case West:
        return boardItem.hasWestWall();
      default:
        throw new AssertionError("unknown direction: " + direction);    
    }
  }

  public BoardItem getBoardItem(Position position) {
    for (BoardItem boardItem : boardItems) {
      if (boardItem.getPosition().equals(position)) {
        return boardItem;
      }
    }
    throw new AssertionError ("Invalid position: " + position);
  }

  public Position getTargetPosition(Target target) {
    return targetsToPositions.get(target);
  }
  
  public Map<Target, Position> getTargetsToPositions(){
    return Collections.unmodifiableMap(targetsToPositions);
  }

  public List<BoardItem> getBoardItems() {
    return Collections.unmodifiableList(boardItems);
  }

}
