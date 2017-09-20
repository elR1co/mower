package com.xebia.mower.mediator;

import com.xebia.mower.model.Grid;
import com.xebia.mower.model.Instruction;
import com.xebia.mower.model.Mower;
import com.xebia.mower.model.Position;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static lombok.AccessLevel.PRIVATE;

@Slf4j
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PRIVATE) // For Mockito
public class DefaultMediator implements IMediator {

    public static final int DEFAULT_WAIT_TIMEOUT = 5000;
    public static final int MAX_WAITING_TIMES = 2;

    Grid grid;
    List<Mower> mowerList;

    public static DefaultMediator create(Grid grid) {
        return new DefaultMediator(grid, new CopyOnWriteArrayList<>());
    }

    @Override
    public DefaultMediator register(Mower mower) {
        if (!isPositionValid(mower.getCurrentPosition()))
            throw new IllegalArgumentException(format("Mower %s has invalid position.", mower.getId()));
        log.info("Mower {} added.", mower.getId());
        mowerList.add(mower);
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

    synchronized Position handleMove(Mower mower) {
        Position currentPosition = mower.getCurrentPosition();
        Position potentialNewPosition = mower.shouldMove();

        if (!isPositionValid(potentialNewPosition)) {
            log.warn("New Position {} Invalid for {}", potentialNewPosition, mower);
            return currentPosition;
        }

        int times = 0;
        while (isPositionLocked(potentialNewPosition)) {
            log.warn("Collision for {}", mower);
            waitNewPositionToUnlock(DEFAULT_WAIT_TIMEOUT);
            if (++times == MAX_WAITING_TIMES) {
                log.warn("Waiting {} times. We go out.", times);
                notifyUnlock();
                return currentPosition;
            }
        }

        Position newPosition = mower.move();
        notifyUnlock();
        return newPosition;
    }

    boolean isPositionValid(Position position) {
        return grid.isPositionValid(position);
    }

    boolean isPositionLocked(Position position) {
        return  nonNull(position) &&
                mowerList.stream().anyMatch(mower -> mower.getCurrentPosition().isSame(position));
    }

    @SneakyThrows(InterruptedException.class)
    void waitNewPositionToUnlock(int timeout) {
        wait(timeout);
    }

    void notifyUnlock() {
        notifyAll();
    }
}
