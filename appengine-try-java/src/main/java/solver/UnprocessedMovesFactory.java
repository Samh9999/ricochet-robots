package org.hirschhorn.ricochet.solver;

public class UnprocessedMovesFactory {

  public static UnprocessedMoves newUnprocessedMoves(UnprocessedMovesType unprocessedMovesType) {
    switch (unprocessedMovesType) {
      case BREADTH_FIRST_SEARCH:
        return new BreadthFirstUnprocessedMoves();    
      case DEPTH_FIRST_SEARCH:
        return new DepthFirstUnprocessedMoves();    
      case PRIORITY_QUEUE_SEARCH:
        return new PriorityQueueUnprocessedMoves();
      default:
        throw new AssertionError("Unknown unprocessedMovesType: " + unprocessedMovesType);
    }
  }

}
