package com.xebia.mower;

import com.xebia.mower.mediator.DefaultMediator;
import com.xebia.mower.mediator.IMediator;
import com.xebia.mower.model.Grid;
import com.xebia.mower.model.Instruction;
import com.xebia.mower.model.Mower;
import com.xebia.mower.model.Position;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static com.xebia.mower.parser.FileLineParser.*;

public class MowItNowConcurrentMain {

    public static void main(String[] args) throws Exception {
        if (args.length < 1) throw new IllegalArgumentException("Cannot find file path in given program arguments.");

        Scanner scanner = new Scanner(Paths.get(ClassLoader.getSystemResource(args[0]).toURI()));
        Grid grid = parseGridXMaxYMax(0, 0, scanner.nextLine());
        IMediator mediator = DefaultMediator.create(grid);
        List<Callable<Position>> mowerEndPositions = new ArrayList<>();
        AtomicInteger mowerCpt = new AtomicInteger(1);

        while (true) {
            Position mowerInitialPosition = parseMowerInitialPosition(scanner.nextLine());
            List<Instruction> instructions = parseInstructions(scanner.nextLine());

            Mower mower = new Mower(String.valueOf(mowerCpt.getAndIncrement()), mowerInitialPosition);
            mediator.registerMower(mower);

            Callable<Position> mowerEndPosition = () -> {
                instructions.forEach(instruction -> mediator.sendInstruction(instruction, mower));
                return mower.getCurrentPosition();
            };

            mowerEndPositions.add(mowerEndPosition);

            if (!scanner.hasNextLine()) {
                break;
            }
        }

        ExecutorService executorService = Executors.newFixedThreadPool(mowerCpt.get());
        executorService.invokeAll(mowerEndPositions);
        executorService.shutdown();
        scanner.close();
    }
}
