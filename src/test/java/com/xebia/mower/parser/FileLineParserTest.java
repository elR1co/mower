package com.xebia.mower.parser;

import com.xebia.mower.model.Grid;
import com.xebia.mower.model.Instruction;
import com.xebia.mower.model.Position;
import org.junit.Test;

import java.util.List;

import static com.xebia.mower.model.Orientation.E;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class FileLineParserTest {

    @Test public void should_parse_grid_right_corner_position() throws Exception {
        // Given
        String line = "7 7";

        // When
        Grid result = FileLineParser.parseGridXMaxYMax(0, 0, line);

        // Then
        assertThat(result).isEqualTo(new Grid(0, 0, 7, 7));
    }

    @Test public void should_throw_exception_when_grid_line_is_invalid() throws Exception {
        // Given
        String line = "7";

        // When // Then
        assertThatThrownBy(() -> FileLineParser.parseGridXMaxYMax(0, 0, line)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test public void should_parse_mower_initial_position() throws Exception {
        // Given
        String line = "4 9 E";

        // When
        Position result = FileLineParser.parseMowerInitialPosition(line);

        // Then
        assertThat(result).isEqualTo(new Position(4, 9, E));
    }

    @Test public void should_throw_exception_when_mower_initial_position_is_invalid() throws Exception {
        // Given
        String line = "7 5";

        // When // Then
        assertThatThrownBy(() -> FileLineParser.parseMowerInitialPosition(line)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test public void should_parse_instructions() throws Exception {
        // Given
        String instructions = "GAGAGAGAD";

        // When
        List<Instruction> result = FileLineParser.parseInstructions(instructions);

        // Then
        assertThat(result).containsExactly(
                Instruction.G,
                Instruction.A,
                Instruction.G,
                Instruction.A,
                Instruction.G,
                Instruction.A,
                Instruction.G,
                Instruction.A,
                Instruction.D);
    }

}