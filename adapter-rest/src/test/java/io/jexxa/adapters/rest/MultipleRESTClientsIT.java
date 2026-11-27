package io.jexxa.adapters.rest;

import kong.unirest.Unirest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static io.jexxa.adapters.core.facade.utils.function.ThrowingConsumer.exceptionCollector;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


//@Tag(TestConstants.INTEGRATION_TEST)
class MultipleRESTClientsIT
{
    private static final String METHOD_GET_SIMPLE_VALUE = "increment";
    private static final int MAX_COUNTER = 1000;
    private static final int MAX_THREADS = 5;

    private IncrementApplicationService applicationService;
    private RESTfulRPCAdapter objectUnderTest;

    @BeforeEach
    void setUp()
    {
        var properties = new Properties();
        var defaultHost = "localhost";
        var defaultPort = 7500;

        properties.put(JexxaWebProperties.JEXXA_REST_HOST, defaultHost);
        properties.put(JexxaWebProperties.JEXXA_REST_PORT, Integer.toString(defaultPort));

        objectUnderTest = RESTfulRPCAdapter.createAdapter(properties);

        applicationService = new IncrementApplicationService();
        objectUnderTest = RESTfulRPCAdapter.createAdapter(properties);

        objectUnderTest.register(applicationService);
        objectUnderTest.start();
    }

    @Test
    void synchronizeMultipleClients()
    {
        //Arrange
        applicationService.setMaxCounter(MAX_COUNTER);
        List<Integer> expectedResult = IntStream.rangeClosed(1, MAX_COUNTER)
                .boxed()
                .toList();

        var clientPool = Stream.generate(() -> new Thread(this::incrementService))
                .limit(MAX_THREADS)
                .toList();

        var exceptionList = new ArrayList<Throwable>();

        //Act
        clientPool.forEach(Thread::start);

        clientPool.forEach(exceptionCollector(Thread::join, exceptionList));


        //Assert
        assertEquals(expectedResult, applicationService.getUsedCounter());
        assertTrue(exceptionList.isEmpty());
    }

    void incrementService()
    {
        while ( applicationService.getCounter() < MAX_COUNTER )
        {
            //Act
            var restPath = "http://localhost:7500/IncrementApplicationService/";
            var response = Unirest.post(restPath + METHOD_GET_SIMPLE_VALUE)
                    .header(RESTConstants.CONTENT_TYPE, RESTConstants.APPLICATION_TYPE)
                    .asJson();
            if (!response.isSuccess())
            {
                throw new IllegalArgumentException("HTTP Response Error: " + response.getStatus() + " " + response.getStatusText() );
            }
        }
    }

    @AfterEach
    void tearDown()
    {
        objectUnderTest.stop();
        Unirest.shutDown();
    }

}
