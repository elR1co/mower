package com.xebia.mower.parser;

import com.xebia.mower.model.Grid;
import com.xebia.mower.model.Instruction;
import com.xebia.mower.model.Orientation;
import com.xebia.mower.model.Position;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class FileLineParser {

    public static Grid parseGridXMaxYMax(int xMin, int yMin, String line) {
        String[] pos = line.split(" ");
        if (pos.length < 2) throw new IllegalArgumentException("Grid configuration should have a xMax and yMax.");
        return new Grid(xMin, yMin, Integer.valueOf(pos[0]), Integer.valueOf(pos[1]));
    }

    public static Position parseMowerInitialPosition(String line) {
        String[] pos = line.split(" ");
        if (pos.length < 3) throw new IllegalArgumentException("Mower Initial Position should have a x, y and orientation.");
        return new Position(Integer.valueOf(pos[0]), Integer.valueOf(pos[1]), Orientation.valueOf(pos[2]));
    }

    public static List<Instruction> parseInstructions(String line) {
        return Collections.unmodifiableList(Arrays.stream(line.split("")).map(Instruction::valueOf).collect(toList()));
    }
}
