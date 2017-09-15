package com.xebia.mower.move;

import com.xebia.mower.model.Position;

public interface IMowerStrategy {

    Position shouldMove(Position currentPosition);
    Position shouldTurnRight(Position currentPosition);
    Position shouldTurnLeft(Position currentPosition);
}
