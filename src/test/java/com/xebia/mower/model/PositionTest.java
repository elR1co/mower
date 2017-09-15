package com.xebia.mower.model;

import org.junit.Test;

import static com.xebia.mower.model.Orientation.N;
import static com.xebia.mower.model.Orientation.S;
import static org.assertj.core.api.Assertions.assertThat;

public class PositionTest {

    @Test public void should_be_the_same() throws Exception {
        // Given
        Position position1 = new Position(1, 1, S);
        Position position2 = new Position(1, 1, N);

        // When
        boolean result = position1.isSame(position2);

        // Then
        assertThat(result).isTrue();
    }

    @Test public void should_not_be_the_same() throws Exception {
        // Given
        Position position1 = new Position(1, 1, S);
        Position position2 = new Position(1, 2, S);

        // When
        boolean result = position1.isSame(position2);

        // Then
        assertThat(result).isFalse();
    }

    @Test public void should_be_equal() throws Exception {
        // Given
        Position position1 = new Position(1, 1, S);
        Position position2 = new Position(1, 1, S);

        // When
        boolean result = position1.equals(position2);

        // Then
        assertThat(result).isTrue();
    }

    @Test public void should_not_be_equal() throws Exception {
        // Given
        Position position1 = new Position(1, 1, N);
        Position position2 = new Position(1, 1, S);

        // When
        boolean result = position1.equals(position2);

        // Then
        assertThat(result).isFalse();
    }

}