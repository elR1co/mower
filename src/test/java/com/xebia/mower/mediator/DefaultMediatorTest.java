package com.xebia.mower.mediator;

import com.xebia.mower.model.Grid;
import com.xebia.mower.model.Mower;
import com.xebia.mower.model.Position;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;

import static com.xebia.mower.model.Instruction.*;
import static com.xebia.mower.model.Orientation.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DefaultMediatorTest {

    @InjectMocks @Spy DefaultMediator mediator;

    @Mock Mower mowerMock;
    @Mock Grid gridMock;
    @Mock Position currentPositionMock;

    @Test public void should_create_mediator() throws Exception {
        // When
        DefaultMediator mediator = DefaultMediator.create(gridMock);

        // Then
        assertThat(mediator).isNotNull();
    }

    @Test public void should_register_mowers() throws Exception {
        // Given
        Position currentPosition = new Position(1, 1, E);
        mediator.mowerList = new ArrayList<>();
        when(mowerMock.getCurrentPosition()).thenReturn(currentPosition);
        doReturn(true).when(mediator).isPositionValid(currentPosition);

        // When
        DefaultMediator result = mediator.registerMower(mowerMock);

        // Then
        verify(mowerMock).getCurrentPosition();
        verify(mediator).isPositionValid(currentPosition);
        assertThat(result).isEqualTo(mediator);
        assertThat(result.mowerList).containsExactly(mowerMock);
    }

    @Test public void should_throw_exception_when_mower_position_is_invalid() throws Exception {
        // Given
        Position currentPosition = new Position(-1, 1, E);
        mediator.mowerList = new ArrayList<>();
        when(mowerMock.getCurrentPosition()).thenReturn(currentPosition);
        doReturn(false).when(mediator).isPositionValid(currentPosition);

        // When // Then
        assertThatThrownBy(() -> mediator.registerMower(mowerMock)).isInstanceOf(IllegalArgumentException.class).hasMessage("Mower has invalid position.");
    }

    @Test public void should_send_instruction_to_turn_right() throws Exception {
        // Given
        Position expectedPosition = new Position(0, 0, E);
        when(mowerMock.turnRight()).thenReturn(expectedPosition);

        // When
        Position result = mediator.sendInstruction(D, mowerMock);

        // Then
        verify(mowerMock).turnRight();
        assertThat(result).isEqualTo(expectedPosition);
    }

    @Test public void should_send_instruction_to_turn_left() throws Exception {
        // Given
        Position expectedPosition = new Position(0, 0, S);
        when(mowerMock.turnLeft()).thenReturn(expectedPosition);

        // When
        Position result = mediator.sendInstruction(G, mowerMock);

        // Then
        verify(mowerMock).turnLeft();
        assertThat(result).isEqualTo(expectedPosition);
    }

    @Test public void should_send_instruction_to_move() throws Exception {
        // Given
        Position expectedPosition = new Position(1, 4, S);
        doReturn(expectedPosition).when(mediator).handleMove(mowerMock);

        // When
        Position result = mediator.sendInstruction(A, mowerMock);

        // Then
        verify(mediator).handleMove(mowerMock);
        assertThat(result).isEqualTo(expectedPosition);
    }

    @Test public void should_move_when_all_conditions_filled() throws Exception {
        // Given
        Position nextPosition = new Position(1, 3, N);

        when(mowerMock.shouldMove()).thenReturn(nextPosition);
        when(mowerMock.move()).thenReturn(nextPosition);
        doReturn(true).when(mediator).isPositionValid(nextPosition);
        doReturn(false).when(mediator).isPositionAlreadyFilled(nextPosition);

        // When
        Position result = mediator.handleMove(mowerMock);

        // Then
        verify(mowerMock).shouldMove();
        verify(mowerMock, never()).getCurrentPosition();
        verify(mowerMock).move();
        verify(mediator).isPositionValid(nextPosition);
        verify(mediator).isPositionAlreadyFilled(nextPosition);

        assertThat(result).isEqualTo(nextPosition);
    }

    @Test public void should_not_move_when_new_position_is_invalid() throws Exception {
        // Given
        Position currentPosition = new Position(0, 0, W);
        Position nextPosition = new Position(-1, 0, W);

        when(mowerMock.shouldMove()).thenReturn(nextPosition);
        when(mowerMock.getCurrentPosition()).thenReturn(currentPosition);
        doReturn(false).when(mediator).isPositionValid(nextPosition);

        // When
        Position result = mediator.handleMove(mowerMock);

        // Then
        verify(mowerMock).shouldMove();
        verify(mowerMock).getCurrentPosition();
        verify(mowerMock, never()).move();
        verify(mediator).isPositionValid(nextPosition);
        verify(mediator, never()).isPositionAlreadyFilled(nextPosition);

        assertThat(result).isEqualTo(currentPosition);
    }

    @Test public void should_not_move_when_new_position_is_already_filled() throws Exception {
        // Given
        Position currentPosition = new Position(1, 0, W);
        Position nextPosition = new Position(0, 0, W);

        when(mowerMock.shouldMove()).thenReturn(nextPosition);
        when(mowerMock.getCurrentPosition()).thenReturn(currentPosition);
        doReturn(true).when(mediator).isPositionValid(nextPosition);
        doReturn(true).when(mediator).isPositionAlreadyFilled(nextPosition);

        // When
        Position result = mediator.handleMove(mowerMock);

        // Then
        verify(mowerMock).shouldMove();
        verify(mowerMock).getCurrentPosition();
        verify(mowerMock, never()).move();
        verify(mediator).isPositionValid(nextPosition);
        verify(mediator).isPositionAlreadyFilled(nextPosition);

        assertThat(result).isEqualTo(currentPosition);
    }

    @Test public void position_should_be_valid() throws Exception {
        // Given
        Position currentPosition = new Position(1, 0, S);
        when(gridMock.isPositionValid(currentPosition)).thenReturn(true);

        // When
        boolean result = mediator.isPositionValid(currentPosition);

        // Then
        verify(gridMock).isPositionValid(currentPosition);
        assertThat(result).isTrue();
    }

    @Test public void position_should_be_invalid() throws Exception {
        Position currentPosition = new Position(-1, 0, S);
        when(gridMock.isPositionValid(currentPosition)).thenReturn(false);

        // When
        boolean result = mediator.isPositionValid(currentPosition);

        // Then
        verify(gridMock).isPositionValid(currentPosition);
        assertThat(result).isFalse();
    }

    @Test public void position_should_not_be_filled() throws Exception {
        // Given
        Position positionToCompare = new Position(1, 1, N);
        mediator.mowerList = new ArrayList<>();
        mediator.mowerList.add(mowerMock);

        // When
        when(mowerMock.getCurrentPosition()).thenReturn(currentPositionMock);
        when(currentPositionMock.isSame(positionToCompare)).thenReturn(false);
        boolean result = mediator.isPositionAlreadyFilled(positionToCompare);

        // Then
        assertThat(result).isFalse();
    }

    @Test public void position_should_already_be_filled() throws Exception {
        // Given
        Position positionToCompare = new Position(1, 1, S);
        mediator.mowerList = new ArrayList<>();
        mediator.mowerList.add(mowerMock);

        // When
        when(mowerMock.getCurrentPosition()).thenReturn(currentPositionMock);
        when(currentPositionMock.isSame(positionToCompare)).thenReturn(true);
        boolean result = mediator.isPositionAlreadyFilled(positionToCompare);

        // Then
        assertThat(result).isTrue();
    }

}
