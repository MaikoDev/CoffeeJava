package org.maikodev.rendering.layers;

import org.maikodev.CupData;
import org.maikodev.exceptions.CupFileNotFoundException;
import org.maikodev.exceptions.CupLoadException;
import org.maikodev.exceptions.InvalidRowSizeException;
import org.maikodev.rendering.TerminalDisplayLayer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CoffeeForeground {
    public CoffeeForeground() {
        List<String> cupLines = CupData.CUP_TOKENS;

        int rows = cupLines.size(), columns = cupLines.getLast().length();
        int totalPixels = rows * columns;

        DISPLAY_BUFFER = new TerminalDisplayLayer(loadCupDisplay(cupLines, totalPixels), rows, columns);
    }

    public TerminalDisplayLayer getDisplay() { return DISPLAY_BUFFER; }

    private char[] loadCupDisplay(List<String> cupLines, int totalPixels) {
        char[] displayBuffer = new char[totalPixels];

        int writePointer = 0;
        for (String line : cupLines) {
            char[] pixelRow = line.toCharArray();

            for (char pixel : pixelRow) {
                displayBuffer[writePointer] = pixel;
                writePointer++;
            }
        }

        return displayBuffer;
    }

    private List<String> loadCupFile() throws CupLoadException {
        List<String> loadBuffer = new ArrayList<>();

        File cupFile = new File("cup.txt");
        if (!cupFile.exists() && !cupFile.isFile()) throw new CupFileNotFoundException();

        try(Scanner fileReader = new Scanner(cupFile)) {
            for (int lineCount = 1; fileReader.hasNextLine(); lineCount++) {
                if (loadBuffer.isEmpty()) {
                    loadBuffer.add(fileReader.nextLine());
                    continue;
                }

                String currentLine = fileReader.nextLine();
                if (currentLine.length() != loadBuffer.getLast().length()) throw new InvalidRowSizeException(lineCount);

                loadBuffer.add(currentLine);
            }

        } catch (FileNotFoundException ex) {
            throw new CupLoadException("Error while attempting to read Cup file!");
        }

        return loadBuffer;
    }

    private static int getDisplayIndex(int row, int column, int maxColumns) {
        return (row * maxColumns) + column;
    }

    private final TerminalDisplayLayer DISPLAY_BUFFER;
}
