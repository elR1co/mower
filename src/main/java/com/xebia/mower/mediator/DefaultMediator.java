package com.xebia.mower.mediator;

import com.xebia.mower.model.Grid;
import com.xebia.mower.model.Instruction;
import com.xebia.mower.model.Mower;
import com.xebia.mower.model.Position;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static lombok.AccessLevel.PRIVATE;

/**
 * Mediator which sends instructions to mowers, and has to handle possible collisions when a mower tries to access
 * a position already occupied by another mower.
 *
 * For our list of registered mowers, {@link java.util.concurrent.CopyOnWriteArrayList} is useless in our case
 * because we always need to get the latest iterator version, and CopyOnWriteArrayList only provides a copy which
 * might not be the latest one. So, we have to synchronize the list read and write methods to be sure to always have
 * the latest underlying array version. For that, we use the same lock in {@link DefaultMediator#register(Mower)}
 * and {@link DefaultMediator#isPositionLocked(Position) (which is only called by {@link DefaultMediator#handleMove(Mower)})
 * That's why we use a simple {@link ArrayList} with {@link ReentrantLock} and {@link Condition}
 *
 */
@Slf4j
@NoArgsConstructor(access = PRIVATE) // For Mockito
public class DefaultMediator implements IMediator {

    public static final int DEFAULT_WAIT_TIMEOUT = 5000;
    public static final int MAX_WAITING_TIMES = 2;

    Grid grid;
    List<Mower> mowerList;
    Lock positionLock;
    Condition positionUnlocked;

    public DefaultMediator(Grid grid) {
        this.grid = grid;
        this.mowerList = new ArrayList<>();
        this.positionLock = new ReentrantLock();
        this.positionUnlocked = positionLock.newCondition();
    }

    @Override
    public DefaultMediator register(Mower mower) {
        Position potentialPosition = mower.getCurrentPosition();

        if (!isPositionValid(potentialPosition))
            throw new IllegalArgumentException(format("Mower %s has invalid position.", mower.getId()));

        positionLock.lock();
        try {
            int times = 0;
            while (isPositionLocked(potentialPosition)) {
                log.warn("Collision when register for {}", mower);
                positionUnlocked.await(DEFAULT_WAIT_TIMEOUT, MILLISECONDS);
                if (++times == MAX_WAITING_TIMES) {
                    log.warn("Waiting {} times. We do not register.", times);
                    positionUnlocked.signalAll();
                    return this;
                }
            }
            mowerList.add(mower);
            log.info("Mower {} added.", mower.getId());
            positionUnlocked.signalAll();
        } catch (InterruptedException e) {
            log.error("Thread interrupted.", e);
        } finally {
            positionLock.unlock();
        }
        return this;
    }

    @Override
    public Position sendInstruction(Instruction instruction, Mower mower) {
        Position newPosition;
        switch (instruction) {
            case D: newPosition = mower.turnRight(); break;
            case G: newPosition = mower.turnLeft(); break;
            case A: newPosition = handleMove(mower); break;
            default: throw new IllegalStateException("Unknown instruction : " + instruction);
        }
        return newPosition;
    }

    Position handleMove(Mower mower) {
        Position currentPosition = mower.getCurrentPosition();
        Position potentialNewPosition = mower.shouldMove();

        if (!isPositionValid(potentialNewPosition)) {
            log.warn("New Position {} Invalid for {}", potentialNewPosition, mower);
            return currentPosition;
        }

        positionLock.lock();
        try {
            int times = 0;
            while (isPositionLocked(potentialNewPosition)) {
                log.warn("Collision for {}", mower);
                positionUnlocked.await(DEFAULT_WAIT_TIMEOUT, MILLISECONDS);
                if (++times == MAX_WAITING_TIMES) {
                    log.warn("Waiting {} times. We skip the instruction.", times);
                    positionUnlocked.signalAll();
                    return currentPosition;
                }
            }

            Position newPosition = mower.move();
            positionUnlocked.signalAll();
            return newPosition;
        } catch (InterruptedException e) {
            log.error("Thread interrupted.", e);
            return currentPosition;
        } finally {
            positionLock.unlock();
        }
    }

    boolean isPositionValid(Position position) {
        return grid.isPositionValid(position);
    }

    boolean isPositionLocked(Position position) {
        return  nonNull(position) &&
                mowerList.stream().anyMatch(mower -> mower.getCurrentPosition().isSame(position));
    }
}
