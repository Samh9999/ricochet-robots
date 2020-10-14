package org.hirschhorn.ricochet.solver;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class MoveStats {

  private static final long PRINT_MOVE_STATS_FREQUENCY = 10000;

  private static Logger logger = Logger.getLogger(MoveStats.class.getName());

  private long startMillis;
  
  private int maxDepth;
  private int mostRecentDepth;
  
  private int maxWinners;
  private List<MoveNode> winners;
  
  /** All theoretically possible moves (based on depth) */
  private long maxPossibleMoves;
  
  /** Current remaining possible moves (based on depth and removing moves for paths that won't be followed).
   *  Does not include moves that will never be created, nor nodes in unprocessed, alive, or dead states.*/
  private long remainingPossibleMoves;
  
  /** Moves that were built and added to tree, but have not yet been "processed" -- i.e. children added). */
  private long unprocessedMoves;
  
  /** Moves that have been processed and have either at least one unprocessed child, or (recursively) one alive child */
  private long aliveMoves;
  
  /** Moves that have been processed and have no alive children. Note that some moves are eliminated before they become dead moves */
  private long deadMoves;
  
  public MoveStats(int maxDepth, int maxWinners) {    
    this.maxDepth = maxDepth;
    this.maxWinners = maxWinners;
    
    winners = new ArrayList<>();

    maxPossibleMoves = 0;
    for (int depth = 1; depth <= maxDepth; depth++) {
      maxPossibleMoves += Math.pow(16, depth);
    }
    remainingPossibleMoves = maxPossibleMoves;
  }
  
  public List<MoveNode> getWinners() {
    return winners;
  }
  
  public MoveStats playStarted() {
    startMillis = System.currentTimeMillis();
    return this;
  }
  
  public MoveStats moveProcessed(MoveNode parentMove, List<MoveNode> childMovesCreated) {
    // Root move is not really a MoveNode -- it is just a placeholder for the initial boardState. We don't incremente unprocessed
    // moves when we create it, so don't decrement when processed.
    if (!parentMove.isRoot()) {
      unprocessedMoves--;
    }
    
    if (!childMovesCreated.isEmpty()) {
      aliveMoves++;
      unprocessedMoves += childMovesCreated.size();
      remainingPossibleMoves -= childMovesCreated.size();
    } else {
      deadMoves++;
      
      //TODO: Now that this MoveNode is dead, check if each of its ancestor Moves can be moved from alive to dead as well. This
      //      probably requires keeping a Map of MoveNode to current state, which we are planning on adding anyway.
    }
    
    if (parentMove.getDepth() < maxDepth) {
      int childMovesNotCreated = 16 - childMovesCreated.size();
      if (childMovesNotCreated > 0) {
        long futureMovesNoLongerPossibleAtEachDepth = childMovesNotCreated;
        long totalFutureMovesNoLongerPossible = futureMovesNoLongerPossibleAtEachDepth;
        for (int depth = parentMove.getDepth() + 2; depth <= maxDepth; depth++) {
          // TODO: Is this correct?
          futureMovesNoLongerPossibleAtEachDepth *= 16;
          totalFutureMovesNoLongerPossible += futureMovesNoLongerPossibleAtEachDepth;
        }
        remainingPossibleMoves -= totalFutureMovesNoLongerPossible;
      }
    }
    
    if (parentMove.getDepth() != mostRecentDepth) {
      logger.fine("" + parentMove);
      logger.info("Depth: " + parentMove.getDepth());
      mostRecentDepth = parentMove.getDepth();
    }
    
    if ((getProcessedMovesCount() % PRINT_MOVE_STATS_FREQUENCY) == 0) {
      printStats(parentMove.getDepth());
    }
    
    return this;
  }
  
  public void printStats(int depth) {
    long elapsedMillis = System.currentTimeMillis() - startMillis;
    //logger.info("Depth: " + parentMove.getDepth() + " MovesProcessed: " + processedMoves + " ElapsedSeconds: "
    //     + (elapsedMillis / 1000) + " MoveNode: " + parentMove);
    logger.info(String.format("%.2f seconds. Depth: %d. %s", (elapsedMillis / 1000.0), depth, toString()));
  }
  
  
  public String toString() {
    return String.format("%.2f %% complete, %d remaining,  %d unprocessed, %d alive, %d dead, %d maxPossible", 
            ((maxPossibleMoves - remainingPossibleMoves) / (float)maxPossibleMoves) * 100,
            remainingPossibleMoves,
            unprocessedMoves,
            aliveMoves, 
            deadMoves,
            maxPossibleMoves);
  }

  public void winnerFound(MoveNode nextMove) {
    winners.add(nextMove);
    logger.severe("FOUND A WINNER AT DEPTH " + nextMove.getDepth() + ". pocessesedMoves: " + getProcessedMovesCount() + ". colors: " + nextMove.numberOfColorsInPath() + ".  " + nextMove.toString());
  }

  private long getProcessedMovesCount() {
    return aliveMoves + deadMoves;
  }

  public boolean maxWinnersReached() {
    return winners.size() >= maxWinners;
  }

  public void printWinners() {
    logger.info("-----");
    logger.info(winners.size() + " WINNERS");
    logger.info("-----");
    printMoves(winners);    
  }
  

  private void printMove(MoveNode moveNode) {
    logger.info(moveNode.asMovesString());    
  }
  
  private void printMoves(List<MoveNode> moveNodes) {
    for (MoveNode moveNode : moveNodes) {
      printMove(moveNode);
    }
  }  

}
