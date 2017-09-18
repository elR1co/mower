package com.xebia.mower.model;

import lombok.Data;

import static java.util.Objects.nonNull;

@Data
public class Grid {

    private final int xMin;
    private final int yMin;
    private final int xMax;
    private final int yMax;

    public Grid(int xMin, int yMin, int xMax, int yMax) {
        if (xMax <= xMin) throw new IllegalArgumentException("xMax should be greater than xMin");
        if (yMax <= yMin) throw new IllegalArgumentException("yMax should be greater than yMin");

        this.xMin = xMin;
        this.yMin = yMin;
        this.xMax = xMax;
        this.yMax = yMax;
    }

    public boolean isPositionValid(Position position) {
        return  nonNull(position) &&
                position.getX() <= getXMax() &&
                position.getX() >= getXMin() &&
                position.getY() >= getYMin() &&
                position.getY() <= getYMax();
    }
}
