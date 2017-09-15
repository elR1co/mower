package com.xebia.mower.model;

import com.xebia.mower.move.IMowerStrategy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import static com.xebia.mower.model.Orientation.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MowerTest {

    @InjectMocks @Spy Mower mower;

    @Mock IMowerStrategy mowerStrategyMock;
    @Mock Position currentPositionMock;

    @Test public void move() throws Exception {
        // Given
        Position nextPosition = new Position(1, 0, S);
        doReturn(nextPosition).when(mower).shouldMove();

        // When
        Position result = mower.move();

        // Then
        verify(mower).shouldMove();
        assertThat(result).isEqualTo(nextPosition);
    }

    @Test public void should_move() throws Exception {
        // Given
        Position nextPosition = new Position(1, 0, S);
        when(mowerStrategyMock.shouldMove(currentPositionMock)).thenReturn(nextPosition);

        // When
        Position result = mower.shouldMove();

        // Then
        verify(mowerStrategyMock).shouldMove(currentPositionMock);
        assertThat(result).isEqualTo(nextPosition);
    }

    @Test public void should_turn_right() throws Exception {
        // Given
        Position expectedPosition = new Position(1, 1, W);
        when(mowerStrategyMock.shouldTurnRight(currentPositionMock)).thenReturn(expectedPosition);

        // When
        Position result = mower.turnRight();

        // Then
        assertThat(result).isEqualTo(expectedPosition);
    }

    @Test public void should_turn_left() throws Exception {
        // Given
        Position expectedPosition = new Position(1, 1, E);
        when(mowerStrategyMock.shouldTurnLeft(currentPositionMock)).thenReturn(expectedPosition);

        // When
        Position result = mower.turnLeft();

        // Then
        assertThat(result).isEqualTo(expectedPosition);
    }
}
