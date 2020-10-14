package org.hirschhorn.ricochet.solver;

import java.util.Stack;

public class DepthFirstUnprocessedMoves implements UnprocessedMoves {

	private Stack<MoveNode> stack;


	public DepthFirstUnprocessedMoves() {
		stack = new Stack<>();
	}

	@Override
	public boolean isEmpty() {
		return stack.isEmpty();
	}

	@Override
	public MoveNode removeFirst() {
		return stack.pop();
	}

	@Override
	public void add(MoveNode nextMove) {
		stack.add(nextMove);
	}

	@Override
	public void clear() {
		stack.clear();

	}

}
