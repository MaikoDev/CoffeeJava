package org.maikodev.order;

public class SubdivRange implements Comparable<SubdivRange> {

    public SubdivRange(long startRange, long endRange) {
        this.startRange = startRange;
        this.endRange = endRange;

        differenceLength = endRange - startRange;
    }

    @Override
    public int compareTo(SubdivRange other) {
        int comparison = 0;

        comparison = (this.differenceLength > other.differenceLength) ? 1 : comparison;
        comparison = (this.differenceLength < other.differenceLength) ? -1 : comparison;

        return comparison;
    }

    public long startRange;
    public long endRange;

    public long differenceLength;
}
