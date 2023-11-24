package io.jexxa.commons.facade.factory;



import io.jexxa.commons.facade.TestConstants;
import io.jexxa.commons.facade.testapplication.SimpleApplicationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.ArrayList;
import java.util.List;

import static io.jexxa.commons.facade.utils.function.ThrowingConsumer.exceptionCollector;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.CONCURRENT)
@Tag(TestConstants.UNIT_TEST)
class ClassFactoryTest
{
    @Test
    void createApplicationServices()
    {
        //Arrange
        var collectedException = new ArrayList<Throwable>();

        var factoryResults = new ArrayList<>();

        var validApplicationServices = List.of(SimpleApplicationService.class);
        //Act
        validApplicationServices.forEach( exceptionCollector(element -> factoryResults.add( ClassFactory.newInstanceOf(element)), collectedException));

        //Assert
        assertTrue(collectedException.isEmpty());
        assertFalse(factoryResults.isEmpty());
        factoryResults.forEach(Assertions::assertNotNull);
    }
}
