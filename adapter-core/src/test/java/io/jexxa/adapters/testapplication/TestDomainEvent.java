package io.jexxa.adapters.testapplication;

public record TestDomainEvent(TestValueObject testValueObject) {

    public static TestDomainEvent create(TestValueObject testValueObject) {
        return new TestDomainEvent(testValueObject);
    }
}
