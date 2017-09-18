package com.xebia.mower.model;

import lombok.Getter;

public enum Orientation {

    N, E, W, S;

    @Getter private Orientation leftOrientation;
    @Getter private Orientation rightOrientation;

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
}
