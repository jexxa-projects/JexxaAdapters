package io.jexxa.adapters.jdbc.facade;

import io.jexxa.adapters.core.facade.utils.properties.PropertiesPrefix;

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


    private JDBCProperties()
    {
        //private constructor
    }

}

