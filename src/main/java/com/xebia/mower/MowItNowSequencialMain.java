package com.xebia.mower;

import com.xebia.mower.mediator.DefaultMediator;
import com.xebia.mower.mediator.IMediator;
import com.xebia.mower.model.Grid;
import com.xebia.mower.model.Instruction;
import com.xebia.mower.model.Mower;
import com.xebia.mower.model.Position;

import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

import static com.xebia.mower.parser.FileLineParser.*;

public class MowItNowSequencialMain {

    public static void main(String[] args) throws Exception {
        if (args.length < 1) throw new IllegalArgumentException("Cannot find file path in given program arguments.");

        try (Scanner scanner = new Scanner(Paths.get(ClassLoader.getSystemResource(args[0]).toURI()))) {
            Grid grid = parseGridXMaxYMax(0, 0, scanner.nextLine());
            IMediator mediator = new DefaultMediator(grid);
            AtomicInteger cptMower = new AtomicInteger(1);

            while (true) {
                Position mowerInitialPosition = parseMowerInitialPosition(scanner.nextLine());
                List<Instruction> instructions = parseInstructions(scanner.nextLine());

                Mower mower = new Mower(String.valueOf(cptMower.getAndIncrement()), mowerInitialPosition);
                mediator.register(mower);

                instructions.forEach(instruction -> mediator.sendInstruction(instruction, mower));

                if (!scanner.hasNextLine()) {
                    break;
                }
            }
        }
    }
}
