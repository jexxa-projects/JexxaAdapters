package io.jexxa.common.facade.jdbc.database;


import io.jexxa.common.facade.jdbc.JDBCConnection;
import io.jexxa.common.facade.jdbc.builder.SQLDataType;

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
