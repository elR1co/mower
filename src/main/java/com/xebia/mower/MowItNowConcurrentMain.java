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

/**
 * The concurrent version takes into consideration that several mowers can move concurrently, but two instructions
 * for the same mower cannot be treated concurrently, they must be treated sequencially in order to keep the same end position.
 * That's why we only create callables for each list of instructions, not for each instruction separately, otherwise the end
 * position is unpredictable, which the specification obviously does not want.
 *
 * In this case, it is useless to make the Mower class methods synchronized.
 * However, collisions can happen when two mowers try to access the same position at the same time.
 *
 * In this case, it is necessary to synchronize the mower {@link Mower#move()} process only ({@link Mower#turnRight()} or {@link Mower#turnLeft()} is useless)
 * via the mediator, in order to control who can access which position and when.
 */
public class MowItNowConcurrentMain {

    public static void main(String[] args) throws Exception {
        if (args.length < 1) throw new IllegalArgumentException("Cannot find file path in given program arguments.");

        try (Scanner scanner = new Scanner(Paths.get(ClassLoader.getSystemResource(args[0]).toURI()))) {
            Grid grid = parseGridXMaxYMax(0, 0, scanner.nextLine());
            IMediator mediator = new DefaultMediator(grid);
            List<Callable<Position>> mowerEndPositions = new ArrayList<>();
            AtomicInteger mowerCpt = new AtomicInteger(1);

            while (true) {
                Position mowerInitialPosition = parseMowerInitialPosition(scanner.nextLine());
                List<Instruction> instructions = parseInstructions(scanner.nextLine());
                Mower mower = new Mower(String.valueOf(mowerCpt.getAndIncrement()), mowerInitialPosition);

                Callable<Position> mowerEndPosition = () -> {
                    Thread.sleep((int) (Math.random() * 1000)); // try to randomize the instant when the mower is registered
                    mediator.register(mower);
                    instructions.forEach(instruction -> {
                        mediator.sendInstruction(instruction, mower);
                        try {
                            Thread.sleep((int) (Math.random() * 1000)); // try to randomize the time between spent between 2 instructions
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    });
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
        }
    }
}
