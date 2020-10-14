package org.hirschhorn.ricochet.game;

import org.hirschhorn.ricochet.board.Color;
import org.hirschhorn.ricochet.board.Position;
import org.hirschhorn.ricochet.board.Target;

public class BoardState {

  private Target chosenTarget;
  private RobotPositions robotPositions;
  
  public BoardState(Target chosenTarget, RobotPositions robotPositions) {
    this.chosenTarget = chosenTarget;
    this.robotPositions = robotPositions;
  }
  
  // Makes a copy of BoardState
  public BoardState(BoardState boardState) {
    this.chosenTarget = boardState.chosenTarget;
    this.robotPositions = new RobotPositions(boardState.robotPositions);
  }
  
	public RobotPositions getRobotPositions() {
		  return robotPositions;
		}

	public Position getRobotPosition(Color robot) {
    return robotPositions.getRobotPosition(robot);
  }
  
  public Target getChosenTarget() {
    return chosenTarget;
  }

  public String asRobotPositionsString() {
    return robotPositions.toString();
  }

  public void setChosenTarget(Target target) {
    this.chosenTarget = target;
  }

}
