package io.jexxa.common.drivenadapter.persistence.objectstore;


import io.jexxa.common.facade.jdbc.TestEntity;
import io.jexxa.common.facade.testapplication.TestValueObject;

import java.util.Objects;

import static java.lang.Math.floor;
import static java.lang.Math.log;

public final class TestObject
{
    private final TestEntity testEntity;
    private final TestValueObject testValueObject;
    private TestValueObject optionalValueObject;
    private String optionalString;
    private final String internalString;

    public void setOptionalValue(TestValueObject optionalValueObject)
    {
        this.optionalValueObject = optionalValueObject;
    }

    public void setOptionalString(String optionalString)
    {
        this.optionalString = optionalString;
    }

    public String getOptionalString()
    {
        return optionalString;
    }

    public String getString()
    {
        return internalString;
    }

    public TestValueObject getOptionalValue()
    {
        return optionalValueObject;
    }

    private TestObject(TestValueObject testValueObject, String internalString)
    {
        this.testEntity = TestEntity.create(testValueObject);
        this.testValueObject = testValueObject;
        this.internalString = internalString;
        this.optionalString = null;
        this.optionalValueObject = null;
    }

    // Create a sequence of chars of alphabet 'A' .. 'Z', 'AA', ...
    public static String createCharSequence(int n) {
        var counter = n;
        char[] buf = new char[(int) floor(log(25 * (counter + 1)) / log(26))];
        for (int i = buf.length - 1; i >= 0; i--)
        {
            counter--;
            buf[i] = (char) ('A' + counter % 26);
            counter /= 26;
        }
        return new String(buf);
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

    public static TestObject create(TestValueObject key)
    {
        return new TestObject(key, createCharSequence(key.getValue()));
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
        TestObject that = (TestObject) o;
        return Objects.equals(getKey(), that.getKey());     // Only compare keys
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(testValueObject);
    }
}
