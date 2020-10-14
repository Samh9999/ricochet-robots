package org.hirschhorn.ricochet.solver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

import org.hirschhorn.ricochet.board.Color;
import org.hirschhorn.ricochet.board.Target;
import org.hirschhorn.ricochet.game.BoardState;
import org.hirschhorn.ricochet.game.Move;

public class MoveNode {

  private List<MoveNode> children;
  private MoveNode parent;
  private int depth;
  private BoardState boardState;
  private Move move;
  private Potential potential;
  
  public Potential getPotential() {
	return potential;
}

public MoveNode(MoveNode parent, BoardState boardState, Move move) {
    this.parent = parent;
    if (parent == null) {
      depth = 0;
    } else {
      depth = parent.getDepth() + 1;
    }
    this.move = move;
    this.boardState = boardState;
    children = new ArrayList<>();
    potential = new Potential(); 
  }
  
  public void addChildren(List<MoveNode> nextMoves) {
    children.addAll(nextMoves);
  }

  public BoardState getBoardState() {
    return boardState;
  }

  public int getDepth() {
    return depth;
  }

  public List<MoveNode> getChildren() {
    return children;
  }
  
  public MoveNode getParent(){
    return parent;
  }
  
  public Move getMove(){
    return move;
  }

  public Target getChosenTarget() {
    return boardState.getChosenTarget();
  }
  
  public String toString() {
    return asMovesString();
  }
  
  public String asMovesString() {
    StringBuilder sb = new StringBuilder();
    int moveNum = 0;
    for (MoveNode moveNode : getAncestorsFromRootDownToSelf()) {      
      Move move = moveNode.move;
      if (move == null) {
        sb.append(moveNode.boardState.asRobotPositionsString());
      } else {
        moveNum++;
        if (moveNum == 1) {
          sb.append(".  ");
        } else {        
          sb.append(" --> ");
        }
        sb.append("MoveNode ");
        sb.append(moveNum);
        sb.append(": ");
        sb.append(move.getRobot());
        sb.append(" ");
        sb.append(move.getDirection());
        sb.append(" to (");
        sb.append(moveNode.boardState.getRobotPosition(move.getRobot()).asSimpleString());
        sb.append(")");
      }
    }
    return sb.toString();
  }

  public List<MoveNode> getAncestorsFromParentUp() {
    List<MoveNode> ancestors = new ArrayList<>();
    MoveNode ancestor = parent;
    while (ancestor != null) {
      ancestors.add(ancestor);
      ancestor = ancestor.parent;
    }
    return ancestors;
  }

  public List<MoveNode> getAncestorsFromSelfUp() {
    List<MoveNode> ancestors = getAncestorsFromParentUp();
    ancestors.add(0, this);
    return ancestors;
  }
  
  public List<MoveNode> getAncestorsFromRootDownToParent() {
    List<MoveNode> ancestors = getAncestorsFromParentUp();
    Collections.reverse(ancestors);
    return ancestors;
  }
  
  public List<MoveNode> getAncestorsFromRootDownToSelf() {
    List<MoveNode> ancestors = getAncestorsFromSelfUp();
    Collections.reverse(ancestors);
    return ancestors;
  }

  public static Comparator<MoveNode> getPotentialComparator() {
	return new Comparator<MoveNode>(){
		@Override
		public int compare(MoveNode move1, MoveNode move2) {
			return move1.getPotential().compareTo(move2.getPotential());
		}
	};
  }

  public boolean isRoot() {
    return depth == 0;
  }
  
  public int numberOfColorsInPath() {
    EnumSet<Color> colors = EnumSet.noneOf(Color.class);
    for (MoveNode moveNode : getAncestorsFromRootDownToSelf()) {
      if (!moveNode.isRoot()) {
        colors.add(moveNode.getMove().getRobot());
      }
    }
    return colors.size();
  }
}
