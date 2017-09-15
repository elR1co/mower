package com.xebia.mower.model;

import org.junit.Test;

import static com.xebia.mower.model.Orientation.*;
import static org.assertj.core.api.Assertions.assertThat;

public class GridTest {

    @Test public void position_is_valid() throws Exception {
        // Given
        Grid grid = new Grid(0, 0, 5, 5);

        // When
        boolean result = grid.isPositionValid(new Position(1, 2, S));

        // Then
        assertThat(result).isTrue();
    }

    @Test public void position_is_invalid() throws Exception {
        // Given
        Grid grid = new Grid(0, 0, 5, 5);

        // When
        boolean result = grid.isPositionValid(new Position(-1, 2, S));

        // Then
        assertThat(result).isFalse();
    }

}