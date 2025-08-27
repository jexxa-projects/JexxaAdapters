package io.jexxa.common.facade.jdbc.database;

import java.util.Locale;

public final class DatabaseManager
{
    public static IDatabase getDatabase(String connectionURL)
    {
        if (connectionURL == null )
        {
            throw new IllegalArgumentException("Connection URL is null");
        }

        if ( connectionURL.toLowerCase(Locale.ENGLISH).contains("postgres") )
        {
            return new PostgresDatabase(connectionURL);
        }

        return new GenericSQLDatabase(connectionURL);
    }

    private DatabaseManager()
    {
        // private constructor
    }

}
