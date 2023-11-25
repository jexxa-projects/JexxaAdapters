package io.jexxa.common.facade.testapplication;

public record JexxaDomainEvent(JexxaValueObject jexxaValueObject) {

    public static JexxaDomainEvent create(JexxaValueObject jexxaValueObject) {
        return new JexxaDomainEvent(jexxaValueObject);
    }
}
