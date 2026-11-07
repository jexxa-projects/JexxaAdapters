package io.jexxa.adapters.testapplication;

public record TestRecordComparable(int number) implements Comparable<TestRecordComparable> {
    @Override
    public int compareTo(TestRecordComparable o) {
        return Integer.compare(number, o.number);
    }
}
