package io.jexxa.adapters.core.drivenadapter.persistence;

import io.jexxa.adapters.core.facade.utils.properties.PropertiesPrefix;

public class PersistenceProperties {
    public static String repositoryStrategy() { return  PropertiesPrefix.globalPrefix() + "repository.strategy";}

    public static String objectstoreStrategy() { return PropertiesPrefix.globalPrefix() +  "objectstore.strategy";}

    private PersistenceProperties() {
        /* This utility class should not be instantiated */
    }

}
