package com.xebia.mower.model;

public enum Orientation {

    N, E, W, S;

    private Orientation leftOrientation;
    private Orientation rightOrientation;

    static {
        N.leftOrientation = W;
        S.leftOrientation = E;
        E.leftOrientation = N;
        W.leftOrientation = S;

        N.rightOrientation = E;
        S.rightOrientation = W;
        E.rightOrientation = S;
        W.rightOrientation = N;
    }

    public Orientation getLeftOrientation() {
        return leftOrientation;
    }

    public Orientation getRightOrientation() {
        return rightOrientation;
    }
}
