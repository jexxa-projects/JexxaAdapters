package io.jexxa.common.facade.jdbc.builder;

@SuppressWarnings("ClassCanBeRecord")
public class JDBCObject {
    private final Object jdbcValue;
    private final SQLDataType sqlDataType;

    public JDBCObject(Object jdbcValue, SQLDataType sqlDataType) {
        this.jdbcValue = jdbcValue;
        this.sqlDataType = sqlDataType;
    }

    public Object getJdbcValue() {
        return jdbcValue;
    }

    public SQLDataType getSqlDataType() {
        return sqlDataType;
    }

    public String getBindParameter()
    {
        if (sqlDataType == SQLDataType.JSONB) {
            return "(?::" + getSqlDataType() + ")";
        }

        return "( ? )";
    }
}
