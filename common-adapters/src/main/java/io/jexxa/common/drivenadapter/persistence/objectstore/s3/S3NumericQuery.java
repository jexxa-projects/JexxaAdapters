package io.jexxa.common.drivenadapter.persistence.objectstore.s3;


import io.jexxa.common.drivenadapter.persistence.objectstore.INumericQuery;
import io.jexxa.common.drivenadapter.persistence.objectstore.metadata.NumericTag;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

class S3NumericQuery<T, K, S> implements INumericQuery<T, S>
{
    private final NumericTag<T, S> numericTag;
    private final S3ObjectStore<T, K, ?> objectStore;

    private List<T> getAggregates()
    {
        return objectStore.getAggregates();
    }

    S3NumericQuery(S3ObjectStore<T, K, ?> objectStore, NumericTag<T, S> numericTag, Class<S> queryType)
    {
        this.objectStore = objectStore;
        this.numericTag = numericTag;
        Objects.requireNonNull( queryType );//Type required for java type inference
    }

    @Override
    public List<T> isGreaterOrEqualThan(S startValue)
    {
        return getAggregates()
                .stream()
                .filter( element -> numericTag.getFromAggregate(element) != null)
                .filter(element -> compareToValue(element, startValue) >= 0)
                .sorted(this::compareToAggregate)
                .toList();
    }

    @Override
    public List<T> isGreaterThan(S value)
    {
        return getAggregates()
                .stream()
                .filter( element -> numericTag.getFromAggregate(element) != null)
                .filter(element -> compareToValue(element, value) > 0)
                .sorted(this::compareToAggregate)
                .toList();
    }

    @Override
    public List<T> getRangeClosed(S startValue, S endValue)
    {
        return getAggregates()
                .stream()
                .filter( element -> numericTag.getFromAggregate(element) != null)
                .filter(element -> compareToValue(element, startValue) >= 0)
                .filter(element -> compareToValue(element, endValue) <= 0)
                .toList();
    }

    @Override
    public List<T> getRange(S startValue, S endValue)
    {
        return getAggregates()
                .stream()
                .filter( element -> numericTag.getFromAggregate(element) != null)
                .filter(element -> compareToValue(element, startValue) >= 0)
                .filter(element -> compareToValue(element, endValue) < 0)
                .toList();
    }

    @Override
    public List<T> isLessOrEqualThan(S endValue)
    {
        return getAggregates()
                .stream()
                .filter( element -> numericTag.getFromAggregate(element) != null)
                .filter(element -> compareToValue(element, endValue) <= 0)
                .sorted(this::compareToAggregate)
                .toList();
    }

    @Override
    public List<T> isLessThan(S endValue)
    {
        return getAggregates()
                .stream()
                .filter( element -> numericTag.getFromAggregate(element) != null)
                .filter(element -> compareToValue(element, endValue) < 0)
                .sorted(this::compareToAggregate)
                .toList();
    }

    @Override
    public List<T> getAscending(int amount)
    {
        return getAggregates()
                .stream()
                .sorted(this::compareToAggregate)
                .limit(amount)
                .toList();
    }

    @Override
    public List<T> getAscending()
    {
        return getAggregates()
                .stream()
                .sorted(this::compareToAggregate)
                .toList();
    }

    @Override
    public List<T> getDescending(int amount)
    {
        return getAggregates()
                .stream()
                .sorted((element1, element2) -> compareToAggregate(element2, element1)) //switch the order of attributes for descending
                .limit(amount)
                .toList();
    }

    @Override
    public List<T> getDescending()
    {
        return getAggregates()
                .stream()
                .sorted(
                                (v1, v2) -> {
                                    boolean v1Valid = isValidForComparison(v1);
                                    boolean v2Valid = isValidForComparison(v2);

                                    if (v1Valid && v2Valid) {
                                        // beide gültig → normale Sortierung
                                        return compareToAggregate(v2, v1); // absteigend
                                    } else if (v1Valid) {
                                        // nur v1 gültig → v1 kommt zuerst
                                        return -1;
                                    } else if (v2Valid) {
                                        // nur v2 gültig → v2 kommt zuerst
                                        return 1;
                                    } else {
                                        // beide ungültig → Reihenfolge egal, nulls ans Ende
                                        return 0;
                                    }
                                })
                .toList();
    }

    private boolean isValidForComparison(T value) {
        return (value != null) && (numericTag.getFromAggregate(value) != null);
    }

    @Override
    public List<T> isEqualTo(S value)
    {
        return getAggregates()
                .stream()
                .filter(element-> compareToValue(element, value) == 0)
                .toList();
    }

    @Override
    public List<T> isNotEqualTo(S value)
    {
        return getAggregates()
                .stream()
                .filter(element-> compareToValue(element, value) != 0)
                .sorted(this::compareToAggregate)
                .toList();
    }

    @Override
    public List<T> isNull()
    {
        return getAggregates()
                .stream()
                .filter( element -> numericTag.getFromAggregate(element) == null)
                .sorted(this::compareToAggregate)
                .toList();
    }

    @Override
    public List<T> isNotNull()
    {
        return getAggregates()
                .stream()
                .filter( element -> numericTag.getFromAggregate(element) != null)
                .sorted(this::compareToAggregate)
                .toList();
    }

    private int compareToValue(T aggregate, S value)
    {
        Objects.requireNonNull(aggregate);
        Objects.requireNonNull(value);

        var aggregateValue = numericTag.getFromAggregate(aggregate);

        if(aggregateValue == null)
        {
            return 1;
        }

        return typeSpecificCompareTo(aggregateValue, numericTag.getFromValue(value));
    }

    protected int typeSpecificCompareTo(Number value1, Number value2)
    {
        //Handle both != null
        var aggregateValue1BD = new BigDecimal( value1.toString() );
        var aggregateValue2BD = new BigDecimal( value2.toString() );
        return aggregateValue1BD.compareTo(aggregateValue2BD);
    }

    /**
     * Compares the value of the two aggregates which each other
     *
     * @param aggregate1 first aggregate
     * @param aggregate2 second aggregate
     * @return 0: If the value of aggregate1 is equal to value aggregate2 <br>
     *     -1: If value of aggregate1 &lt; value of aggregate2 <br>
     *     1: If value of aggregate1 &gt; value of aggregate2 <br>
     *     1: If aggregate1 is null <br>
     *     -1: If aggregate2 is null <br>
     */
    @SuppressWarnings("DuplicatedCode")
    private int compareToAggregate(T aggregate1, T aggregate2)
    {
        Objects.requireNonNull(aggregate1);
        Objects.requireNonNull(aggregate2);

        var aggregateValue1 = numericTag.getFromAggregate(aggregate1);
        var aggregateValue2 = numericTag.getFromAggregate(aggregate2);

        if ( aggregateValue1 == null && aggregateValue2 == null)
        {
            return 0;
        } else if (aggregateValue1 == null)
        {
            return 1;
        }else if (aggregateValue2 == null)
        {
            return -1;
        }

        return typeSpecificCompareTo( aggregateValue1, aggregateValue2);
    }
}
