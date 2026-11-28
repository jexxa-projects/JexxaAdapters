package io.jexxa.adapters.jdbc.facade;

import java.util.Objects;

public record SQLDataType(String string)
{
    public static final SQLDataType INTEGER = new SQLDataType("INTEGER ");
    public static final SQLDataType NUMERIC = new SQLDataType("NUMERIC ");
    public static final SQLDataType FLOAT = new SQLDataType("FLOAT ");
    public static final SQLDataType DOUBLE = new SQLDataType("DOUBLE PRECISION ");
    public static final SQLDataType TIMESTAMP = new SQLDataType("TIMESTAMP ");
    public static final SQLDataType TEXT = new SQLDataType("TEXT ");
    public static final SQLDataType VARCHAR = new SQLDataType("VARCHAR ");
    public static final SQLDataType JSONB = new SQLDataType("JSONB ");

    @Override
    public String toString()
    {
        return Objects.requireNonNull(string);
    }
}
