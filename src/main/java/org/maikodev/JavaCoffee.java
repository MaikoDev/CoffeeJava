package org.maikodev;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class JavaCoffee{
    public JavaCoffee() {
        List<String> cupLines = loadCupFile();

        int rows = cupLines.size(), columns = cupLines.getLast().length();
        int totalPixels = rows * columns;

        DISPLAY_BUFFER = new TerminalDisplayBuffer(loadCupDisplay(cupLines, totalPixels), rows, columns);
    }

    public TerminalDisplayBuffer getDisplay() { return DISPLAY_BUFFER; }

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

    private final TerminalDisplayBuffer DISPLAY_BUFFER;
}
