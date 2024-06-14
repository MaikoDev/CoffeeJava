package org.maikodev.order;

public class Position {
    public Position(int column, int row) {
        this.x = column;
        this.y = row;
    }

    public int getRow() { return y; }
    public int getColumn() { return x; }

    public int y;
    public int x;
}
