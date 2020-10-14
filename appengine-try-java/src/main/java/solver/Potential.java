package org.hirschhorn.ricochet.solver;

import org.hirschhorn.ricochet.board.Color;

public class Potential {
    
	private int score;
	
	public Potential(){
		score = 100;
	}
	
	public int adjustIfMoveSameColorAsTarget(MoveNode moveNode){
		Color targetColor = moveNode.getBoardState().getChosenTarget().getColor();
		Color moveColor = moveNode.getMove().getRobot();
		if (moveColor.equals(targetColor)){
			score = score + 5;
		}
		return score;
	}
	
	public int getScore(){
		return score;
	}

	public int compareTo(Potential other) {
		return Integer.compare(this.getScore(), other.getScore());
	}
}
