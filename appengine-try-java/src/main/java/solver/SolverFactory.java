package org.hirschhorn.ricochet.solver;

import org.hirschhorn.ricochet.game.Game;

public class SolverFactory {

  public Solver createSolver(Game game, UnprocessedMovesType unprocessedMovesType) {
    MoveNode rootMove = new MoveNode(null, game.getBoardState(), null);
    Solver solver = new Solver(game.getBoard(), rootMove, unprocessedMovesType);
    return solver;
  }

}
