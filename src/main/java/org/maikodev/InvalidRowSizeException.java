package org.maikodev;

public class InvalidRowSizeException extends CupLoadException {
    public InvalidRowSizeException(int row) {
        super(String.format("Line %d of the Cup file does not match size of other lines!", row));
    }
}
