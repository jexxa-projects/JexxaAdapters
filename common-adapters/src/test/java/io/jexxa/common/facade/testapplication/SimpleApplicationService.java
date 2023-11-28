package io.jexxa.common.facade.testapplication;


import io.jexxa.common.facade.logger.SLF4jLogger;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unused", "SameReturnValue"})
public class SimpleApplicationService
{
    private int firstValue;
    private List<String> messages = new ArrayList<>();
    private List<TestValueObject> testValueObjects = new ArrayList<>();
    private List<TestRecord> recordList = new ArrayList<>();

    private TestEnum testEnum = TestEnum.ENUM_VALUE1;

    public static class SimpleApplicationException extends Exception
    {
        @Serial
        private static final long serialVersionUID = 1L;

        public SimpleApplicationException(String information)
        {
            super(information);
        }
        public SimpleApplicationException(String information, Throwable cause)
        {
            super(information, cause);
        }
    }

    public SimpleApplicationService()
    {
        firstValue = 42;
    }

    public int getSimpleValue()
    {
      return firstValue;
    }

    public int setGetSimpleValue(int newValue )
    {
        int oldValue = firstValue;
        this.firstValue = newValue;
        return oldValue;
    }

    public void throwExceptionTest() throws SimpleApplicationException
    {
        throw new SimpleApplicationException("TestException");
    }

    public void setEnumValue(TestEnum testEnum)
    {
        this.testEnum = testEnum;
    }

    public TestEnum getEnumValue()
    {
        return testEnum;
    }

    @SuppressWarnings("DataFlowIssue") // Because this method should caus a NullPointerException for testing purpose
    public int throwNullPointerException()   // Test runtime exception
    {
        TestValueObject testValueObject = null;
        return testValueObject.getValue();
    }


    public void setSimpleValue(int simpleValue)
    {
        this.firstValue = simpleValue;
    }

    public void setSimpleValueObject(TestValueObject simpleTestValueObject)
    {
        setSimpleValue(simpleTestValueObject.getValue());
    }

    public void setSimpleValueObjectTwice(TestValueObject first, TestValueObject second)
    {
        setSimpleValue(first.getValue());
        setSimpleValue(second.getValue());
    }

    public void setSimpleValueTwice(int first, int second)
    {
        setSimpleValue(first);
        setSimpleValue(second);
    }


    public void addMessage(String message)
    {
        messages.add(message);
    }

    public void setMessages(List<String> messages)
    {
        this.messages = messages;
    }

    public void setValueObjectsAndMessages(List<TestValueObject> testValueObjects, List<String> messages)
    {
        this.messages = messages;
        this.testValueObjects = testValueObjects;
    }

    public List<String> getMessages()
    {
        return messages;
    }

    public List<TestValueObject> getValueObjects()
    {
        return testValueObjects;
    }

    public TestValueObject getSimpleValueObject()
    {
        return  new TestValueObject(firstValue);
    }

    public SpecialCasesValueObject getSpecialCasesValueObject()
    {
        return  SpecialCasesValueObject.SPECIAL_CASES_VALUE_OBJECT;
    }

    public TestRecord testRecord()
    {
        return  new TestRecord("");
    }

    public List<TestRecord> testRecordList()
    {
        return  recordList;
    }

    public void recordList(List<TestRecord> recordList)
    {
        recordList.forEach(element -> SLF4jLogger.getLogger(SimpleApplicationService.class).info(element.testRecord()));
        this.recordList = recordList;
    }

    /** The following static methods should NOT be offered by any DrivingAdapter according to our conventions  */
    public static SpecialCasesValueObject testStaticGetMethod()
    {
        throw new IllegalArgumentException("Method testStaticGetMethod should not be available or called" );
    }

    public static void testStaticSetMethod(TestValueObject testValueObject)
    {
        throw new IllegalArgumentException("Method testStaticSetMethod should not be available or called" );
    }


}
