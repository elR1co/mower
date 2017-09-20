package com.xebia.mower.model;

import com.xebia.mower.move.DefaultMowerStrategy;
import com.xebia.mower.move.IMowerStrategy;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import static lombok.AccessLevel.PRIVATE;

@Slf4j
@ToString(of = {"id", "currentPosition"})
@NoArgsConstructor(access = PRIVATE) // For Mockito
public class Mower {

    @Getter String id;
    @Getter Position currentPosition;
    IMowerStrategy mowerStrategy;

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
        log.info("{}", this);
    }

    public Position shouldMove() {
        return mowerStrategy.shouldMove(currentPosition);
    }

    public Position move() {
        currentPosition = shouldMove();
        log.info("{}", this);
        return currentPosition;
    }

    public Position turnRight() {
        currentPosition = mowerStrategy.shouldTurnRight(currentPosition);
        log.info("{}", this);
        return currentPosition;
    }

    public Position turnLeft() {
        currentPosition = mowerStrategy.shouldTurnLeft(currentPosition);
        log.info("{}", this);
        return currentPosition;
    }
}
