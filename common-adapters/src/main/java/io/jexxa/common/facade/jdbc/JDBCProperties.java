package io.jexxa.common.facade.jdbc;

import static io.jexxa.adapterapi.PropertiesPrefix.prefix;

public class JDBCProperties
{
    public static String jdbcFileUsername()  { return prefix() + "jdbc.file.username" ;}
    public static String jdbcFilePassword() { return  prefix() + "jdbc.file.password" ;}

    public static String jdbcUrl() { return  prefix() + "jdbc.url";}
    public static String jdbcUsername() { return  prefix() + "jdbc.username";}
    public static String jdbcPassword() { return  prefix() + "jdbc.password";}
    public static String jdbcDriver() { return prefix() +  "jdbc.driver";}
    public static String jdbcAutocreateDatabase() { return  prefix() + "jdbc.autocreate.database";}
    public static String jdbcAutocreateTable() { return prefix() +  "jdbc.autocreate.table";}

    /** Defines the jdbc transaction level. This must be one of the following values "read-uncommitted", "read-committed", "repeatable-read", "serializable"*/
    public static String jdbcTransactionIsolationLevel() { return  prefix() + "jdbc.transaction.isolation.level";}

    public static String repositoryStrategy() { return  prefix() + "repository.strategy";}

    public static String objectstoreStrategy() { return prefix() +  "objectstore.strategy";}


    private JDBCProperties()
    {
        //private constructor
    }

}

