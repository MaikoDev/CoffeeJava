package org.maikodev.order;

public class RowMajor {
    public static int getIndex(int row, int column, int maxColumns) {
        return (row * maxColumns) + column;
    }
}
