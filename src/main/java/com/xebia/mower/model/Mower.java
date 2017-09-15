package com.xebia.mower.model;

import com.xebia.mower.move.DefaultMowerStrategy;
import com.xebia.mower.move.IMowerStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Mower {

    private static final Logger LOGGER = LoggerFactory.getLogger(Mower.class);

    private String id;
    private Position currentPosition;
    private IMowerStrategy mowerStrategy;

    public Mower(String id, int x, int y, Orientation orientation) {
        this(id, new Position(x, y, orientation));
    }

    public Mower(String id, Position initialPosition) {
        this(id, initialPosition, new DefaultMowerStrategy());
    }

    Mower(String id, Position initialPosition, IMowerStrategy mowerStrategy) {
        this.id = id;
        this.currentPosition = initialPosition;
        this.mowerStrategy = mowerStrategy;
    }

    // For Mockito
    private Mower() {}

    public Position getCurrentPosition() {
        return currentPosition;
    }

    public String getId() {
        return id;
    }

    public Position shouldMove() {
        return mowerStrategy.shouldMove(currentPosition);
    }

    public Position move() {
        currentPosition = shouldMove();
        LOGGER.info("{}", this);
        return currentPosition;
    }

    public Position turnRight() {
        currentPosition = mowerStrategy.shouldTurnRight(currentPosition);
        LOGGER.info("{}", this);
        return currentPosition;
    }

    public Position turnLeft() {
        currentPosition = mowerStrategy.shouldTurnLeft(currentPosition);
        LOGGER.info("{}", this);
        return currentPosition;
    }

    @Override
    public String toString() {
        return "Mower{" +
                "id='" + id + '\'' +
                ", currentPosition=" + currentPosition +
                '}';
    }
}
