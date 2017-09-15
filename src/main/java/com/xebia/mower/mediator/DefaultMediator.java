package com.xebia.mower.mediator;

import com.xebia.mower.model.Grid;
import com.xebia.mower.model.Instruction;
import com.xebia.mower.model.Mower;
import com.xebia.mower.model.Position;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DefaultMediator implements IMediator {

    private Grid grid;
    List<Mower> mowerList;

    public static DefaultMediator create(Grid grid) {
        return new DefaultMediator(grid, new CopyOnWriteArrayList<>());
    }

    private DefaultMediator(Grid grid, List<Mower> mowerList) {
        this.grid = grid;
        this.mowerList = mowerList;
    }

    @Override
    public DefaultMediator registerMower(Mower mower) {
        if (!isPositionValid(mower.getCurrentPosition()))
            throw new IllegalArgumentException("Mower has invalid position.");
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
        Position potentialNewPosition = mower.shouldMove();
        if (isPositionValid(potentialNewPosition) && !isPositionAlreadyFilled(potentialNewPosition)) {
            return mower.move();
        }
        return mower.getCurrentPosition();
    }

    boolean isPositionValid(Position position) {
        return grid.isPositionValid(position);
    }

    boolean isPositionAlreadyFilled(Position position) {
        return mowerList.stream().anyMatch(mower -> mower.getCurrentPosition().isSame(position)); //
    }
}
