package com.xebia.mower.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import static java.util.Objects.nonNull;

@Data
@AllArgsConstructor
public class Position {

    private final int x;
    private final int y;
    @NonNull private final Orientation orientation;

    public boolean isSame(Position position) {
        return nonNull(position) && getX() == position.getX() && getY() == position.getY();
    }
}
