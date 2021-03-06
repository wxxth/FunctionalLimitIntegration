package functional;


import mdp.MDP;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

/**
 * Created by Xiaoxi Wang on 7/9/14.
 */
public class PiecewisePolynomialFunction {
    AdvancedPolynomialFunction[] polyFuncs;
    private double[] bounds;    // the overall domain is defined on [0, +inf)
    private int pieces;

    public PiecewisePolynomialFunction(AdvancedPolynomialFunction[] polyFuncs, double[] bounds) {
        this.polyFuncs = Arrays.copyOf(polyFuncs, polyFuncs.length);
        pieces = polyFuncs.length;
        this.bounds = bounds.clone();
//        this.bounds = new double[bounds.length];
//        for (int i = 0; i < pieces; ++i) {
//            this.bounds[i] = (double) Math.round(bounds[i] * MDP.PRECISION_FACTOR) / MDP.PRECISION_FACTOR;
//        }
//        this.bounds[pieces] = bounds[pieces];
    }

    public int degree() {
        int deg = 0;
        for (AdvancedPolynomialFunction apf : polyFuncs) {
            if (apf.degree() > deg) deg = apf.degree();
        }
        return deg;
    }

    public double value(double x) {
        for (int i = 0; i < pieces; ++i) {
            if (x >= bounds[i] && x < bounds[i + 1]) {
                return polyFuncs[i].value(x);
            }
        }
        return Double.NaN;
    }

    public void simplify() {
        AdvancedPolynomialFunction[] tmpPolyFuncs = new AdvancedPolynomialFunction[polyFuncs.length];
        double[] tmpBounds = new double[bounds.length];
        double[] lastCoefs = null, currentCoefs;
        int n = 0;
        for (int i = 0; i < polyFuncs.length; ++i) {
            currentCoefs = polyFuncs[i].getCoefficients();
//            System.out.println("last: " + Arrays.toString(lastCoefs) + " current: " + Arrays.toString(currentCoefs));
//            System.out.println("is eq: " + Arrays.equals(currentCoefs, lastCoefs));
            if (!Arrays.equals(currentCoefs, lastCoefs)){
                tmpPolyFuncs[n] = polyFuncs[i];
                tmpBounds[n] = bounds[i];
                ++n;
                lastCoefs = currentCoefs;
            }
        }
        tmpBounds[n] = bounds[bounds.length - 1];
        polyFuncs = new AdvancedPolynomialFunction[n];
        bounds = new double[n + 1];
        for (int i = 0; i < n; ++i) {
            polyFuncs[i] = tmpPolyFuncs[i];
            bounds[i] = tmpBounds[i];
        }
        bounds[n] = tmpBounds[n];
        pieces = n;
    }

    public void roundTrivial() {
        long lastMax = Math.round(polyFuncs[0].value(0));
//        int deg;
        for (int i = 0; i < pieces; ++i) {
            if (polyFuncs[i].degree() == 0) {
                long v = Math.round(polyFuncs[i].value(bounds[i]));
                if (v > lastMax) v = lastMax;
                polyFuncs[i] = AdvancedPolynomialFunction.N(v);
                lastMax = v;
            } else {
                double dl = polyFuncs[i].value(bounds[i]);
                long vl = Math.round(dl);
                long vr = Math.round(polyFuncs[i].value(bounds[i + 1]));
                if (vl <= vr) {
                    if (vl > lastMax) vl = lastMax;
                    polyFuncs[i] = AdvancedPolynomialFunction.N(vl);
                    lastMax = vl;
                }
//                else {
//                    if (dl > lastMax) {
//                        polyFuncs[i] = polyFuncs[i].subtract(dl - lastMax);
//                    }
//                }
            }
        }
    }

    public void linearApproximation(double interval) {
//        int ppp = pieces;
//        if (ppp>5)
//            System.out.println(this);
        if (pieces < 2) {
            return;
        } else if (pieces == 2) {
            polyFuncs[0] = simpleLinearize(polyFuncs[0], bounds[0], bounds[1]);
            return;
        }
//        System.out.println("--------");
//        System.out.println(this);
        List<AdvancedPolynomialFunction> polyList = new ArrayList<AdvancedPolynomialFunction>();
        List<Double> boundList = new ArrayList<Double>();
        double t = bounds[1] - bounds[0];
        double lastBound = bounds[0];
        AdvancedPolynomialFunction tmpPoly = polyFuncs[0];
        for (int i = 1; i < pieces - 1; ++i) {
//            System.out.println("t:" + t);
            if (t < interval) {
                tmpPoly = merge2PolyFuncIntoLinear(tmpPoly, polyFuncs[i], lastBound, bounds[i + 1]);
                t = bounds[i + 1] - lastBound;
            } else {
                tmpPoly = simpleLinearize(tmpPoly, lastBound, bounds[i + 1]);
                polyList.add(tmpPoly);
                boundList.add(lastBound);
                tmpPoly = polyFuncs[i];
                t = bounds[i + 1] - bounds[i];
                lastBound = bounds[i];
            }
        }
//        polyList.add(tmpPoly);
//        boundList.add(lastBound);
//        for (Double d : boundList) {
//            System.out.print(d + ",");
//        }
//        System.out.println(" [" + lastBound + "]");
        if (bounds[pieces - 1] - bounds[pieces - 2] < interval) {
            int id = polyList.size() - 1;
//            tmpPoly = merge2PolyFuncIntoLinear(polyList.get(id), polyFuncs[pieces - 2], boundList.get(id), bounds[pieces - 1]);
            tmpPoly = merge2PolyFuncIntoLinear(tmpPoly, polyFuncs[pieces - 2], lastBound, bounds[pieces - 1]);
            polyList.remove(id);
            polyList.add(tmpPoly);
        } else {
            tmpPoly = simpleLinearize(tmpPoly, lastBound, bounds[pieces - 1]);
            polyList.add(tmpPoly);
            boundList.add(lastBound);
            polyList.add(simpleLinearize(polyFuncs[pieces - 2], bounds[pieces - 2], bounds[pieces - 1]));
            boundList.add(bounds[pieces - 2]);
        }
        boundList.add(bounds[pieces - 1]);
        polyList.add(polyFuncs[pieces - 1]);
        boundList.add(bounds[pieces]);
        pieces = polyList.size();
        polyFuncs = new AdvancedPolynomialFunction[pieces];
        bounds = new double[pieces + 1];
        for (int i = 0; i < pieces; ++i) {
            polyFuncs[i] = polyList.get(i);
            bounds[i] = boundList.get(i);
        }
        bounds[pieces] = boundList.get(pieces);
//        if (ppp > 5 && pieces != ppp) {
//            System.out.println(this);
//            System.exit(999);
//        }
//        return new PiecewisePolynomialFunction(polyFuncs, bounds);
    }

    private AdvancedPolynomialFunction simpleLinearize(AdvancedPolynomialFunction apf, double leftBound, double rightBound) {
        double v1 = apf.value(leftBound);
        double v2 = apf.value(rightBound);
        if (v1 <= v2) {
            return AdvancedPolynomialFunction.N(v1);
        }
        return AdvancedPolynomialFunction.Linear(leftBound, v1, rightBound, v2);
    }

    private AdvancedPolynomialFunction merge2PolyFuncIntoLinear(AdvancedPolynomialFunction apf1, AdvancedPolynomialFunction apf2, double leftBound, double rightBound) {
        double v1 = apf1.value(leftBound);
        double v2 = apf2.value(rightBound);
        if (v1 <= v2) {
            return AdvancedPolynomialFunction.N(v1);
        }
        return AdvancedPolynomialFunction.Linear(leftBound, v1, rightBound, v2);
    }

    public PiecewisePolynomialFunction add(PiecewisePolynomialFunction ppf) {
        double[] newBounds;
        int newPieceNum;
        int n, i, j;
        {
            double[] tmpBounds = new double[pieces + ppf.pieces];
            tmpBounds[0] = bounds[0];
            n = 1;
            i = 1;
            j = 1;
            while (i <= pieces && j <= ppf.pieces) {
                if (bounds[i] < ppf.bounds[j]) tmpBounds[n++] = bounds[i++];
                else if (ppf.bounds[j] < bounds[i]) tmpBounds[n++] = ppf.bounds[j++];
                else {
                    tmpBounds[n++] = bounds[i++];
                    ++j;
                }
            }
            newBounds = new double[n];
            System.arraycopy(tmpBounds, 0, newBounds, 0, n);
//            System.out.println("bounds: " + Arrays.toString(bounds));
//            System.out.println("ppf.bounds: " + Arrays.toString(ppf.bounds));
//            System.out.println("tmp bounds: " + Arrays.toString(tmpBounds));
//            System.out.println("new bounds: " + Arrays.toString(newBounds));
            newPieceNum = n - 1; // bounds [0, +inf)
        }
        AdvancedPolynomialFunction[] pfs = new AdvancedPolynomialFunction[newPieceNum];
        for (n = 0, i = 0, j = 0; n < newPieceNum; ++n) {
            pfs[n] = polyFuncs[i].add(ppf.polyFuncs[j]);
            if (newBounds[n+1] >= bounds[i+1]) ++i;
            if (newBounds[n+1] >= ppf.bounds[j+1]) ++j;
        }
        PiecewisePolynomialFunction result = new PiecewisePolynomialFunction(pfs, newBounds);
        result.simplify();
        return result;
    }

    public PiecewisePolynomialFunction add(final double c) {
        AdvancedPolynomialFunction[] pfs = new AdvancedPolynomialFunction[pieces];
        for (int i = 0; i < pieces; ++i) {
            pfs[i] = polyFuncs[i].add(c);
        }
        PiecewisePolynomialFunction result = new PiecewisePolynomialFunction(pfs, bounds.clone());
        result.simplify();
        return result;
    }

    public PiecewisePolynomialFunction shift(final double t) {
        // V(t) -> V(t + t')
        // keep the first and last bound
        List<AdvancedPolynomialFunction> pfsList = new ArrayList<AdvancedPolynomialFunction>();
        List<Double> boundList = new ArrayList<Double>();
        boundList.add(bounds[0]);
        for (int i = 0; i < pieces - 1; ++i) {
            if (bounds[i + 1] - t > bounds[0] && bounds[i + 1] - t <= bounds[pieces]) {
                pfsList.add(polyFuncs[i].shift(t));
                boundList.add(bounds[i + 1] - t);
            }
        }
        if (boundList.get(boundList.size() - 1) < bounds[pieces]) {
            pfsList.add(polyFuncs[pieces - 1].shift(t));
            boundList.add(bounds[pieces]);
        }
        AdvancedPolynomialFunction[] pfs = new AdvancedPolynomialFunction[pfsList.size()];
        double[] newBounds = new double[boundList.size()];
        for (int i = 0; i < pfsList.size(); ++i) {
            pfs[i] = pfsList.get(i);
            newBounds[i] = boundList.get(i);
        }
        newBounds[newBounds.length - 1] = boundList.get(newBounds.length - 1);
        //        result.simplify();
        return new PiecewisePolynomialFunction(pfs, newBounds);
    }

    public PiecewisePolynomialFunction replace(AdvancedPolynomialFunction apf, double leftBound, double rightBound) {
//        System.out.println(apf);
//        System.out.println(leftBound + " " + rightBound);
        int i, start = 0, end = 0;
        for (i = 1; i < pieces + 1; ++i) {
            if (leftBound < bounds[i]) {
                start = i - 1;
                break;
            }
        }
        for (; i < pieces + 1; ++i) {
            if (rightBound <= bounds[i]) {
                end = i - 1;
                break;
            }
        }
        int newPieces = start + pieces - end + (leftBound == bounds[start] ? 0 : 1) + (rightBound == bounds[end + 1] ? 0 : 1);
        AdvancedPolynomialFunction[] newPolyFuncs = new AdvancedPolynomialFunction[newPieces];
        double[] newBounds = new double[newPieces + 1];
        for (i = 0; i < start; ++i) {
            newPolyFuncs[i] = polyFuncs[i];
            newBounds[i] = bounds[i];
        }
//        System.out.println(this);
//        System.out.println(pieces + " " + newPieces + " " + " start: " +start + " end: " + end + " left: " + leftBound + " right: " + rightBound + " " + i);
        if (bounds[start] < leftBound) {
            newPolyFuncs[i] = polyFuncs[start];
            newBounds[i] = bounds[start];
            ++i;
        }
        newPolyFuncs[i] = apf;
        newBounds[i] = leftBound;
        ++i;
        if (bounds[end + 1] > rightBound) {
            newPolyFuncs[i] = polyFuncs[end];
            newBounds[i] = rightBound;
            ++i;
        }
        for (; i < newPieces; ++i) {
            newPolyFuncs[i] = polyFuncs[pieces - newPieces + i];
            newBounds[i] = bounds[pieces - newPieces + i];
        }
        newBounds[newPieces] = bounds[pieces];
        return new PiecewisePolynomialFunction(newPolyFuncs, newBounds);
    }

    public AdvancedPolynomialFunction[] getPolynomialFunctions() {
        return this.polyFuncs;
    }

    public AdvancedPolynomialFunction getPolynomialFunction(int piece) {
        return this.polyFuncs[piece];
    }

    public double[] getBounds() {
        return this.bounds.clone();
    }

    public int getPieceNum() {
        return this.polyFuncs.length;
    }

    @Override
    public int hashCode() {
        final int prime = 17;
        int result = 1;
        result = prime * result + Arrays.hashCode(bounds);
        result = prime * result + Arrays.hashCode(polyFuncs);
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof PiecewisePolynomialFunction)) return false;
        PiecewisePolynomialFunction other = (PiecewisePolynomialFunction) obj;
        return Arrays.equals(this.getPolynomialFunctions(), other.getPolynomialFunctions()) && Arrays.equals(this.getBounds(), other.getBounds());
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < pieces; ++i) {
            s.append("[");
            s.append(bounds[i]);
            s.append(",");
            s.append(bounds[i + 1]);
            s.append(")\t");
            s.append(polyFuncs[i]);
            s.append("\n");
        }
//        s.deleteCharAt(s.length() - 1);
        return s.toString().trim();
    }
}
