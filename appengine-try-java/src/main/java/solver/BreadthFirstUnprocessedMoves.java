package org.hirschhorn.ricochet.solver;

import java.util.LinkedList;
import java.util.Queue;

public class BreadthFirstUnprocessedMoves implements UnprocessedMoves {

	private Queue<MoveNode> queue;

	public BreadthFirstUnprocessedMoves() {
		queue = new LinkedList<>();
	}

	@Override
	public boolean isEmpty() {
		return queue.isEmpty();
	}

	@Override
	public MoveNode removeFirst() {
		return queue.poll();
	}

	@Override
	public void add(MoveNode nextMove) {
		queue.add(nextMove);
	}

	@Override
	public void clear() {
		queue.clear();

	}

}
