package com.xebia.mower.mediator;

import com.xebia.mower.model.Instruction;
import com.xebia.mower.model.Mower;
import com.xebia.mower.model.Position;

public interface IMediator {

    IMediator registerMower(Mower mower);

    Position sendInstruction(Instruction instruction, Mower mower);
}
