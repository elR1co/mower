package com.xebia.mower.model;

import static java.util.Objects.nonNull;

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
                position.getX() <= getxMax() &&
                position.getX() >= getxMin() &&
                position.getY() >= getyMin() &&
                position.getY() <= getyMax();
    }

    public int getxMin() {
        return xMin;
    }

    public int getyMin() {
        return yMin;
    }

    public int getxMax() {
        return xMax;
    }

    public int getyMax() {
        return yMax;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Grid grid = (Grid) o;

        if (xMin != grid.xMin) return false;
        if (yMin != grid.yMin) return false;
        if (xMax != grid.xMax) return false;
        return yMax == grid.yMax;
    }

    @Override
    public int hashCode() {
        int result = xMin;
        result = 31 * result + yMin;
        result = 31 * result + xMax;
        result = 31 * result + yMax;
        return result;
    }
}
