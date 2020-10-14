package org.hirschhorn.ricochet.solver;

public interface UnprocessedMoves {

	boolean isEmpty();

	MoveNode removeFirst();

	void add(MoveNode nextMove);

	void clear();

}