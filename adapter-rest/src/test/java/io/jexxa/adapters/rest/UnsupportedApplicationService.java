package io.jexxa.adapters.rest;


import io.jexxa.adapters.testapplication.TestValueObject;

/*
* This service is not available via RESTfulRPC because method setSimpleValueObject is available twice 
*/
@SuppressWarnings("unused")
class UnsupportedApplicationService
{
    private TestValueObject first;

    public void setSimpleValueObject(TestValueObject simpleValueObject)
    {
        this.first = simpleValueObject;
    }
    public void setSimpleValueObject(TestValueObject first, TestValueObject second)
    {
        this.first = new TestValueObject(first.getValue() * second.getValue());
    }
}
