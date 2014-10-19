package mdp;

import functional.PiecewisePolynomialFunction;

/**
 * Created by Xiaoxi Wang on 10/19/14.
 */
public class DoNothing implements Action{

    private int id;

    public DoNothing(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @Override
    public State perform(State state) {
        return null;
    }

    @Override
    public PiecewisePolynomialFunction preValueFunc(PiecewisePolynomialFunction currentValueFunc) {
        return null;
    }
}