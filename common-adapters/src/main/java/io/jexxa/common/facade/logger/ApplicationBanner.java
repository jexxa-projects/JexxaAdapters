package io.jexxa.common.facade.logger;

import io.jexxa.adapterapi.JexxaContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;

import static io.jexxa.common.facade.logger.SLF4jLogger.getLogger;

@SuppressWarnings("unused")
public final class ApplicationBanner
{
    private static final ApplicationBanner APPLICATION_BANNER = new ApplicationBanner();
    private final List<Consumer<Properties>> configBanner = new ArrayList<>();
    private final List<Consumer<Properties>> accessBanner = new ArrayList<>();



    public static void addConfigBanner(Consumer<Properties> consumer)
    {
        APPLICATION_BANNER.configBanner.add(consumer);
    }

    public static void addAccessBanner(Consumer<Properties> consumer)
    {
        APPLICATION_BANNER.accessBanner.add(consumer);
    }

    public static void clear()
    {
        APPLICATION_BANNER.accessBanner.clear();
        APPLICATION_BANNER.configBanner.clear();
    }

    public static void show(Properties properties)
    {
        getLogger(ApplicationBanner.class).info("Config Information: ");
        APPLICATION_BANNER.configBanner.forEach(element -> element.accept(properties));

        getLogger(ApplicationBanner.class).info("");
        getLogger(ApplicationBanner.class).info("Access Information: ");
        APPLICATION_BANNER.accessBanner.forEach(element -> element.accept(properties));
    }

    private ApplicationBanner()
    {
        JexxaContext.registerCleanupHandler(ApplicationBanner::clear);
        JexxaContext.registerInitHandler(ApplicationBanner::clear);
    }
}
