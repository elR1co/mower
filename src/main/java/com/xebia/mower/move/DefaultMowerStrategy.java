package com.xebia.mower.move;

import com.xebia.mower.model.Orientation;
import com.xebia.mower.model.Position;

import static java.util.Objects.requireNonNull;

public class DefaultMowerStrategy implements IMowerStrategy {

    @Override
    public Position shouldMove(Position currentPosition) {
        requireNonNull(currentPosition, "currentPosition should not be null.");
        Orientation currentOrientation = currentPosition.getOrientation();
        int currentXPosition = currentPosition.getX();
        int currentYPosition = currentPosition.getY();
        switch (currentOrientation) {
            case N: return new Position(currentXPosition, currentYPosition + 1, currentOrientation);
            case S: return new Position(currentXPosition, currentYPosition - 1, currentOrientation);
            case E: return new Position(currentXPosition + 1, currentYPosition, currentOrientation);
            case W: return new Position(currentXPosition - 1, currentYPosition, currentOrientation);
            default: throw new IllegalStateException("Mower orientation unknown.");
        }
    }

    @Override
    public Position shouldTurnRight(Position currentPosition) {
        return new Position(currentPosition.getX(), currentPosition.getY(), currentPosition.getOrientation().getRightOrientation());
    }

    @Override
    public Position shouldTurnLeft(Position currentPosition) {
        return new Position(currentPosition.getX(), currentPosition.getY(), currentPosition.getOrientation().getLeftOrientation());
    }
}
