package io.jexxa.common.facade.testapplication;

import io.jexxa.common.facade.jdbc.TestEntity;

import java.util.Objects;

public final class TestAggregate
{
    private final TestEntity testEntity;
    private final TestValueObject testValueObject;

    private TestAggregate(TestValueObject testValueObject)
    {
        this.testEntity = TestEntity.create(testValueObject);
        this.testValueObject = testValueObject;
    }

    public void setInternalValue(int value)
    {
        testEntity.setInternalValue(value);
    }

    public int getInternalValue()
    {
        return testEntity.getInternalValue();
    }

    public TestValueObject getKey()
    {
        return testValueObject;
    }

    public static TestAggregate create(TestValueObject key)
    {
        return new TestAggregate(key);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        TestAggregate that = (TestAggregate) o;
        return Objects.equals(getKey(), that.getKey());     // Only compare keys
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(testValueObject);
    }
}
