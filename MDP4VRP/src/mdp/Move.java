package mdp;

import functional.PiecewisePolynomialFunction;
import functional.PiecewiseStochasticPolynomialFunction;
import vrp.Edge;
import vrp.Node;

/**
 * Created by Xiaoxi Wang on 9/3/14.
 */
public class Move implements Action{
    private Edge edge;

    public Move(Edge edge) {
        this.edge = edge;
    }

    public String toString() {
        return "move(" + edge.getStartNode().getID() + "," + edge.getEndNode().getID() + ")";
    }
    @Override
    public State perform(State state) {
        if (state.getLocation().equals(edge.getStartNode()))
            return new State(edge.getEndNode(), state.getTaskSet());
        else return state;
    }

    @Override
    public PiecewisePolynomialFunction preValueFunc(PiecewisePolynomialFunction currentValueFunc) {
//        System.out.println("^^^:\n"+ currentValueFunc.toString());
        return MDP.integrationOnXiOfComposition_test(currentValueFunc, edge.getTimeCostFunction());
    }
}
