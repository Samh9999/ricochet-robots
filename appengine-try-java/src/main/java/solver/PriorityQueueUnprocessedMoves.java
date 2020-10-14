package org.hirschhorn.ricochet.solver;

import java.util.PriorityQueue;

public class PriorityQueueUnprocessedMoves implements UnprocessedMoves {

	private PriorityQueue<MoveNode> queue;


	public PriorityQueueUnprocessedMoves() {
		queue = new PriorityQueue<>(MoveNode.getPotentialComparator());
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
