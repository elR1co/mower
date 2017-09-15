package com.xebia.mower.move;

import com.xebia.mower.model.Position;
import org.junit.Test;

import static com.xebia.mower.model.Orientation.*;
import static org.assertj.core.api.Assertions.assertThat;

public class DefaultMowerStrategyTest {

    DefaultMowerStrategy strategy = new DefaultMowerStrategy();

    @Test public void should_move_south() throws Exception {
        // Given
        Position currentPosition = new Position(1, 2, S);

        // When
        Position nextPosition = strategy.shouldMove(currentPosition);

        // Then
        assertThat(nextPosition).isEqualTo(new Position(1, 1, S));
    }

    @Test public void should_move_north() throws Exception {
        // Given
        Position currentPosition = new Position(1, 2, N);

        // When
        Position nextPosition = strategy.shouldMove(currentPosition);

        // Then
        assertThat(nextPosition).isEqualTo(new Position(1, 3, N));
    }

    @Test public void should_move_west() throws Exception {
        // Given
        Position currentPosition = new Position(1, 2, W);

        // When
        Position nextPosition = strategy.shouldMove(currentPosition);

        // Then
        assertThat(nextPosition).isEqualTo(new Position(0, 2, W));
    }

    @Test public void should_move_east() throws Exception {
        // Given
        Position currentPosition = new Position(1, 2, E);

        // When
        Position nextPosition = strategy.shouldMove(currentPosition);

        // Then
        assertThat(nextPosition).isEqualTo(new Position(2, 2, E));
    }

    @Test public void should_turn_right() throws Exception {
        // Given
        Position currentPosition = new Position(1, 2, E);

        // When
        Position result = strategy.shouldTurnRight(currentPosition);

        // Then
        assertThat(result).isEqualTo(new Position(1, 2, S));
    }

    @Test public void should_turn_left() throws Exception {
        // Given
        Position currentPosition = new Position(1, 2, E);

        // When
        Position result = strategy.shouldTurnLeft(currentPosition);

        // Then
        assertThat(result).isEqualTo(new Position(1, 2, N));
    }

}