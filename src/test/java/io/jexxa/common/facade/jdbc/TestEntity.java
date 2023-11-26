package io.jexxa.common.facade.jdbc;

import io.jexxa.common.facade.testapplication.TestValueObject;

import java.util.Objects;

public final class TestEntity
{
    private final TestValueObject testValueObject;

    private int internalValue;

    public static TestEntity create(TestValueObject key)
    {
        return new TestEntity(key);
    }

    public void setInternalValue(int value)
    {
        internalValue = value;
    }

    public int getInternalValue()
    {
        return internalValue;
    }


    public TestValueObject getKey()
    {
        return testValueObject;
    }

    private TestEntity(TestValueObject testValueObject)
    {
        this.testValueObject = testValueObject;
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
        TestEntity that = (TestEntity) o;
        return Objects.equals(getKey(), that.getKey());     // Only compare keys
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(testValueObject);
    }
}
