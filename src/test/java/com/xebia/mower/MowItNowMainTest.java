package com.xebia.mower;

import com.xebia.mower.mediator.DefaultMediator;
import com.xebia.mower.mediator.IMediator;
import com.xebia.mower.model.Grid;
import com.xebia.mower.model.Instruction;
import com.xebia.mower.model.Mower;
import com.xebia.mower.model.Position;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

import static com.xebia.mower.model.Orientation.E;
import static com.xebia.mower.model.Orientation.N;
import static com.xebia.mower.parser.FileLineParser.parseInstructions;
import static org.assertj.core.api.Assertions.assertThat;

public class MowItNowMainTest {

    @Test public void should_run_sequencially() throws Exception {
        // Given
        Grid grid = new Grid(0, 0, 5, 5);
        Mower mower1 = new Mower("1", 1, 2, N);
        Mower mower2 = new Mower("2", 3, 3, E);
        IMediator manager = DefaultMediator.create(grid).registerMower(mower1).registerMower(mower2);
        List<Instruction> instructions1 = parseInstructions("GAGAGAGAA");
        List<Instruction> instructions2 = parseInstructions("AADAADADDA");

        // When
        instructions1.forEach(instruction -> manager.sendInstruction(instruction, mower1));
        instructions2.forEach(instruction -> manager.sendInstruction(instruction, mower2));

        // Then
        assertThat(mower1.getCurrentPosition()).isEqualTo(new Position(1, 3, N));
        assertThat(mower2.getCurrentPosition()).isEqualTo(new Position(5, 1, E));
    }

    @Test public void should_run_concurrently_with_runnables() throws Exception {
        // Given
        Grid grid = new Grid(0, 0, 5, 5);
        Mower mower1 = new Mower("1", 1, 2, N);
        Mower mower2 = new Mower("2", 3, 3, E);
        IMediator manager = DefaultMediator.create(grid).registerMower(mower1).registerMower(mower2);
        List<Instruction> instructions1 = parseInstructions("GAGAGAGAA");
        List<Instruction> instructions2 = parseInstructions("AADAADADDA");

        // When
        CountDownLatch latch = new CountDownLatch(2);

        new Thread(() -> {
            instructions1.forEach(instruction -> manager.sendInstruction(instruction, mower1));
            latch.countDown();
        }).start();

        new Thread(() -> {
            instructions2.forEach(instruction -> manager.sendInstruction(instruction, mower2));
            latch.countDown();
        }).start();

        latch.await();

        // Then
        assertThat(mower1.getCurrentPosition()).isEqualTo(new Position(1, 3, N));
        assertThat(mower2.getCurrentPosition()).isEqualTo(new Position(5, 1, E));
    }

    @Test public void should_run_concurrently_with_callables() throws Exception {
        // Given
        Grid grid = new Grid(0, 0, 5, 5);
        Mower mower1 = new Mower("1", 1, 2, N);
        Mower mower2 = new Mower("2", 3, 3, E);
        IMediator manager = DefaultMediator.create(grid).registerMower(mower1).registerMower(mower2);
        List<Instruction> instructions1 = parseInstructions("GAGAGAGAA");
        List<Instruction> instructions2 = parseInstructions("AADAADADDA");
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        // When
        Callable<Mower> mower1Instructions = () -> {
            instructions1.forEach(instruction -> manager.sendInstruction(instruction, mower1));
            return mower1;
        };

        Callable<Mower> mower2Instructions = () -> {
            instructions2.forEach(instruction -> manager.sendInstruction(instruction, mower2));
            return mower2;
        };

        List<Future<Mower>> results = executorService.invokeAll(Arrays.asList(mower1Instructions, mower2Instructions));

        // Then
        Mower firstResult = results.get(0).get();
        Mower secondResult = results.get(1).get();

        if (firstResult.getId().equals("1")) {
            assertThat(firstResult.getCurrentPosition()).isEqualTo(new Position(1, 3, N));
            assertThat(secondResult.getCurrentPosition()).isEqualTo(new Position(5, 1, E));
        } else if (firstResult.getId().equals("2")) {
            assertThat(firstResult.getCurrentPosition()).isEqualTo(new Position(5, 1, E));
            assertThat(secondResult.getCurrentPosition()).isEqualTo(new Position(1, 3, N));
        }
    }
}
