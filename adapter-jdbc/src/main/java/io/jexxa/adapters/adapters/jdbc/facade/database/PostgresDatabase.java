package io.jexxa.adapters.adapters.jdbc.facade.database;


import io.jexxa.adapters.adapters.jdbc.facade.JDBCConnection;
import io.jexxa.adapters.adapters.jdbc.facade.SQLDataType;

public class PostgresDatabase extends GenericSQLDatabase
{
    PostgresDatabase(String connectionURL) {
        super(connectionURL);
    }

    @Override
    public SQLDataType matchingPrimaryKey(SQLDataType requestedDataType)
    {
        return requestedDataType;
    }

    @Override
    public SQLDataType matchingValue(SQLDataType requestedDataType)
    {
        return requestedDataType;
    }


    @Override
    public  void alterColumnType(JDBCConnection jdbcConnection, Class<?> tableName, String columnName, SQLDataType sqlDataType)
    {
        var keyRow = jdbcConnection.tableCommand()
                .alterTable(tableName)
                .alterColumn(columnName, sqlDataType, " USING " + columnName + "::" + sqlDataType)
                .create();

        keyRow.asIgnore();
    }

}
