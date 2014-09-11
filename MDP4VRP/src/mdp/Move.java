package mdp;

import functional.PiecewiseStochasticPolynomialFunction;
import vrp.Node;

/**
 * Created by Xiaoxi Wang on 9/3/14.
 */
public class Move implements Action{
    private Node from, to;
    private PiecewiseStochasticPolynomialFunction costFunc;

    public Move(Node from, Node to) {
        this.from = from;
        this.to = to;
    }
    @Override
    public State perform(State state) {
        if (state.getLocation().equals(from) && state.getLocation().getOutgoingEdges().contains(to)) {
            return new State(to, state.getTaskSet());
        } else return null;
    }
}