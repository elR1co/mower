package com.xebia.mower.mediator;

import com.xebia.mower.model.Grid;
import com.xebia.mower.model.Mower;
import com.xebia.mower.model.Position;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import static com.xebia.mower.mediator.DefaultMediator.DEFAULT_WAIT_TIMEOUT;
import static com.xebia.mower.model.Instruction.*;
import static com.xebia.mower.model.Orientation.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DefaultMediatorTest {

    @Spy DefaultMediator mediator;
    @Mock Mower mowerMock;
    @Mock Lock positionLockMock;
    @Mock Condition positionUnlocked;

    @Before
    public void onSetUp() throws Exception {
        mediator.grid = new Grid(0, 0, 5, 5);
        mediator.mowerList = new ArrayList<>();
        mediator.positionLock = positionLockMock;
        mediator.positionUnlocked = positionUnlocked;
        doNothing().when(positionLockMock).lock();
        doNothing().when(positionLockMock).unlock();
        doReturn(true).when(positionUnlocked).await(DEFAULT_WAIT_TIMEOUT, MILLISECONDS);
        doNothing().when(positionUnlocked).signalAll();
    }

    @Test public void should_create_mediator() throws Exception {
        // When
        DefaultMediator mediator = new DefaultMediator(new Grid(0, 0, 5, 5));

        // Then
        assertThat(mediator).isNotNull();
    }

    @Test public void should_register_mower() throws Exception {
        // Given
        Position currentPosition = new Position(1, 1, E);
        when(mowerMock.getId()).thenReturn("1");
        when(mowerMock.getCurrentPosition()).thenReturn(currentPosition);
        doReturn(true).when(mediator).isPositionValid(currentPosition);
        doReturn(false).when(mediator).isPositionLocked(currentPosition);

        // When
        DefaultMediator result = mediator.register(mowerMock);

        // Then
        InOrder inOrder = Mockito.inOrder(mowerMock, mediator, positionLockMock, positionUnlocked);

        inOrder.verify(mowerMock).getCurrentPosition();
        inOrder.verify(mediator).isPositionValid(currentPosition);
        inOrder.verify(positionLockMock).lock();
        inOrder.verify(mediator).isPositionLocked(currentPosition);
        inOrder.verify(mowerMock).getId();
        inOrder.verify(positionUnlocked).signalAll();
        inOrder.verify(positionLockMock).unlock();
        inOrder.verify(positionUnlocked, never()).await(DEFAULT_WAIT_TIMEOUT, MILLISECONDS);

        assertThat(result).isEqualTo(mediator);
        assertThat(result.mowerList).containsExactly(mowerMock);
    }

    @Test public void should_register_mower_when_position_is_locked_only_once() throws Exception {
        // Given
        Position currentPosition = new Position(1, 1, E);
        when(mowerMock.getId()).thenReturn("1");
        when(mowerMock.getCurrentPosition()).thenReturn(currentPosition);
        doReturn(true).when(mediator).isPositionValid(currentPosition);
        doReturn(true, false).when(mediator).isPositionLocked(currentPosition);

        // When
        DefaultMediator result = mediator.register(mowerMock);

        // Then
        InOrder inOrder = Mockito.inOrder(mowerMock, mediator, positionLockMock, positionUnlocked);

        inOrder.verify(mowerMock).getCurrentPosition();
        inOrder.verify(mediator).isPositionValid(currentPosition);
        inOrder.verify(positionLockMock).lock();
        inOrder.verify(mediator).isPositionLocked(currentPosition);
        inOrder.verify(positionUnlocked).await(DEFAULT_WAIT_TIMEOUT, MILLISECONDS);
        inOrder.verify(mediator).isPositionLocked(currentPosition);
        inOrder.verify(mowerMock).getId();
        inOrder.verify(positionUnlocked).signalAll();
        inOrder.verify(positionLockMock).unlock();

        assertThat(result).isEqualTo(mediator);
        assertThat(result.mowerList).containsExactly(mowerMock);
    }

    @Test public void should_not_register_mower_when_position_is_locked_twice() throws Exception {
        // Given
        Position currentPosition = new Position(1, 1, E);
        when(mowerMock.getCurrentPosition()).thenReturn(currentPosition);
        doReturn(true).when(mediator).isPositionValid(currentPosition);
        doReturn(true, true).when(mediator).isPositionLocked(currentPosition);

        // When
        DefaultMediator result = mediator.register(mowerMock);

        // Then
        InOrder inOrder = Mockito.inOrder(mowerMock, mediator, positionLockMock, positionUnlocked);

        inOrder.verify(mowerMock).getCurrentPosition();
        inOrder.verify(mediator).isPositionValid(currentPosition);
        inOrder.verify(positionLockMock).lock();
        inOrder.verify(mediator).isPositionLocked(currentPosition);
        inOrder.verify(positionUnlocked).await(DEFAULT_WAIT_TIMEOUT, MILLISECONDS);
        inOrder.verify(mediator).isPositionLocked(currentPosition);
        inOrder.verify(positionUnlocked).await(DEFAULT_WAIT_TIMEOUT, MILLISECONDS);
        inOrder.verify(positionUnlocked).signalAll();
        inOrder.verify(positionLockMock).unlock();

        assertThat(result).isEqualTo(mediator);
        assertThat(result.mowerList).isEmpty();
    }

    @Test public void should_throw_exception_when_mower_position_is_invalid() throws Exception {
        // Given
        Position currentPosition = new Position(-1, 1, E);
        when(mowerMock.getCurrentPosition()).thenReturn(currentPosition);
        when(mowerMock.getId()).thenReturn("1");
        doReturn(false).when(mediator).isPositionValid(currentPosition);

        // When // Then
        assertThatThrownBy(() -> mediator.register(mowerMock)).isInstanceOf(IllegalArgumentException.class).hasMessage("Mower 1 has invalid position.");
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

    @Test public void should_move_when_all_conditions_resolved() throws Exception {
        // Given
        Position nextPosition = new Position(1, 3, N);

        when(mowerMock.shouldMove()).thenReturn(nextPosition);
        when(mowerMock.move()).thenReturn(nextPosition);
        doReturn(true).when(mediator).isPositionValid(nextPosition);
        doReturn(false).when(mediator).isPositionLocked(nextPosition);

        // When
        Position result = mediator.handleMove(mowerMock);

        // Then
        InOrder inOrder = Mockito.inOrder(mediator, mowerMock, positionLockMock, positionUnlocked);

        inOrder.verify(mowerMock).getCurrentPosition();
        inOrder.verify(mowerMock).shouldMove();
        inOrder.verify(mediator).isPositionValid(nextPosition);
        inOrder.verify(positionLockMock).lock();
        inOrder.verify(mediator).isPositionLocked(nextPosition);
        inOrder.verify(mowerMock).move();
        inOrder.verify(positionUnlocked).signalAll();
        inOrder.verify(positionLockMock).unlock();
        inOrder.verify(positionUnlocked, never()).await(DEFAULT_WAIT_TIMEOUT, MILLISECONDS);

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
        InOrder inOrder = Mockito.inOrder(mediator, mowerMock, positionLockMock, positionUnlocked);

        inOrder.verify(mowerMock).getCurrentPosition();
        inOrder.verify(mowerMock).shouldMove();
        inOrder.verify(mowerMock, never()).move();
        inOrder.verify(mediator).isPositionValid(nextPosition);
        inOrder.verify(mediator, never()).isPositionLocked(nextPosition);
        inOrder.verify(positionLockMock, never()).lock();
        inOrder.verify(positionLockMock, never()).unlock();
        inOrder.verify(positionUnlocked, never()).await(DEFAULT_WAIT_TIMEOUT, MILLISECONDS);
        inOrder.verify(positionUnlocked, never()).signalAll();

        assertThat(result).isEqualTo(currentPosition);
    }

    @Test public void should_not_move_when_new_position_is_locked() throws Exception {
        // Given
        Position currentPosition = new Position(1, 0, W);
        Position nextPosition = new Position(0, 0, W);

        when(mowerMock.shouldMove()).thenReturn(nextPosition);
        when(mowerMock.getCurrentPosition()).thenReturn(currentPosition);
        doReturn(true).when(mediator).isPositionValid(nextPosition);
        doReturn(true, true).when(mediator).isPositionLocked(nextPosition);

        // When
        Position result = mediator.handleMove(mowerMock);

        InOrder inOrder = Mockito.inOrder(mediator, mowerMock, positionLockMock, positionUnlocked);

        // Then
        inOrder.verify(mowerMock).getCurrentPosition();
        inOrder.verify(mowerMock).shouldMove();
        inOrder.verify(mediator).isPositionValid(nextPosition);
        inOrder.verify(positionLockMock).lock();
        inOrder.verify(mediator).isPositionLocked(nextPosition);
        inOrder.verify(positionUnlocked).await(DEFAULT_WAIT_TIMEOUT, MILLISECONDS);
        inOrder.verify(mediator).isPositionLocked(nextPosition);
        inOrder.verify(positionUnlocked).await(DEFAULT_WAIT_TIMEOUT, MILLISECONDS);
        inOrder.verify(positionUnlocked).signalAll();
        inOrder.verify(positionLockMock).unlock();
        inOrder.verify(mowerMock, never()).move();

        assertThat(result).isEqualTo(currentPosition);
    }

    @Test public void should_move_when_position_is_locked_only_once() throws Exception {
        // Given
        Position currentPosition = new Position(1, 0, W);
        Position nextPosition = new Position(0, 0, W);

        when(mowerMock.shouldMove()).thenReturn(nextPosition);
        when(mowerMock.move()).thenReturn(nextPosition);
        when(mowerMock.getCurrentPosition()).thenReturn(currentPosition);
        doReturn(true).when(mediator).isPositionValid(nextPosition);
        doReturn(true, false).when(mediator).isPositionLocked(nextPosition);

        // When
        Position result = mediator.handleMove(mowerMock);

        InOrder inOrder = Mockito.inOrder(mowerMock, positionLockMock, positionUnlocked, mediator);

        // Then
        inOrder.verify(mowerMock).getCurrentPosition();
        inOrder.verify(mowerMock).shouldMove();
        inOrder.verify(mediator).isPositionValid(nextPosition);
        inOrder.verify(positionLockMock).lock();
        inOrder.verify(mediator).isPositionLocked(nextPosition);
        inOrder.verify(positionUnlocked).await(DEFAULT_WAIT_TIMEOUT, MILLISECONDS);
        inOrder.verify(mediator).isPositionLocked(nextPosition);
        inOrder.verify(mowerMock).move();
        inOrder.verify(positionUnlocked).signalAll();
        inOrder.verify(positionLockMock).unlock();

        assertThat(result).isEqualTo(nextPosition);
    }

    @Test public void position_should_be_valid() throws Exception {
        // Given
        Position currentPosition = new Position(1, 0, S);

        // When
        boolean result = mediator.isPositionValid(currentPosition);

        // Then
        assertThat(result).isTrue();
    }

    @Test public void position_should_be_invalid() throws Exception {
        Position currentPosition = new Position(-1, 0, S);

        // When
        boolean result = mediator.isPositionValid(currentPosition);

        // Then
        assertThat(result).isFalse();
    }

    @Test public void position_should_not_be_locked() throws Exception {
        // Given
        Position currentPosition = new Position(2, 1, N);
        Position positionToCompare = new Position(1, 1, N);
        mediator.mowerList.add(mowerMock);
        when(mowerMock.getCurrentPosition()).thenReturn(currentPosition);

        // When
        boolean result = mediator.isPositionLocked(positionToCompare);

        // Then
        verify(mowerMock).getCurrentPosition();
        assertThat(result).isFalse();
    }

    @Test public void position_should_be_locked() throws Exception {
        // Given
        Position currentPosition = new Position(1, 1, S);
        Position positionToCompare = new Position(1, 1, S);
        mediator.mowerList.add(mowerMock);
        when(mowerMock.getCurrentPosition()).thenReturn(currentPosition);

        // When
        boolean result = mediator.isPositionLocked(positionToCompare);

        // Then
        verify(mowerMock).getCurrentPosition();
        assertThat(result).isTrue();
    }

}
