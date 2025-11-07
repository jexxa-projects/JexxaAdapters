package io.jexxa.common.facade.jdbc;

import io.jexxa.common.facade.utils.properties.PropertiesPrefix;

public class JDBCProperties
{
    public static String jdbcFileUsername()  { return PropertiesPrefix.globalPrefix() + "jdbc.file.username" ;}
    public static String jdbcFilePassword() { return  PropertiesPrefix.globalPrefix() + "jdbc.file.password" ;}

    public static String jdbcUrl() { return  PropertiesPrefix.globalPrefix() + "jdbc.url";}
    public static String jdbcUsername() { return  PropertiesPrefix.globalPrefix() + "jdbc.username";}
    public static String jdbcPassword() { return  PropertiesPrefix.globalPrefix() + "jdbc.password";}
    public static String jdbcDriver() { return PropertiesPrefix.globalPrefix() +  "jdbc.driver";}
    public static String jdbcAutocreateDatabase() { return  PropertiesPrefix.globalPrefix() + "jdbc.autocreate.database";}
    public static String jdbcAutocreateTable() { return PropertiesPrefix.globalPrefix() +  "jdbc.autocreate.table";}

    /** Defines the jdbc transaction level. This must be one of the following values "read-uncommitted", "read-committed", "repeatable-read", "serializable"*/
    public static String jdbcTransactionIsolationLevel() { return  PropertiesPrefix.globalPrefix() + "jdbc.transaction.isolation.level";}

    public static String repositoryStrategy() { return  PropertiesPrefix.globalPrefix() + "repository.strategy";}

    public static String objectstoreStrategy() { return PropertiesPrefix.globalPrefix() +  "objectstore.strategy";}


    private JDBCProperties()
    {
        //private constructor
    }

}

