package io.jexxa.commons.facade.jdbc.builder;

import java.util.ArrayList;
import java.util.List;


@SuppressWarnings("unused")
public class  JDBCBuilder <T extends Enum<T>>
{
    private final StringBuilder sqlQueryBuilder = new StringBuilder();
    private final List<Object> arguments = new ArrayList<>();

    public String getStatement()
    {
        return getStatementBuilder().toString();
    }

    public final StringBuilder getStatementBuilder()
    {
        return sqlQueryBuilder;
    }

    protected final void addArgument(Object argument)
    {
        arguments.add(argument);
    }

    protected final List<Object> getArguments()
    {
        return arguments;
    }


    public static class JDBCCondition<V extends Enum<V>, T extends JDBCBuilder<V> >
    {
        private final T queryBuilder;

        public JDBCCondition( T queryBuilder)
        {
            this.queryBuilder = queryBuilder;
        }

        public T isEqual(Object value)
        {
            return is(SQLSyntax.SQLOperation.EQUAL, value);
        }

        public T isEqual(JDBCObject value)
        {
            return is(SQLSyntax.SQLOperation.EQUAL, value.getJdbcValue(), value.getBindParameter());
        }

        public T isNull()
        {
            queryBuilder.getStatementBuilder()
                    .append(SQLSyntax.SQLOperation.IS_NULL);

            return queryBuilder;
        }

        public T isNotNull()
        {
            queryBuilder.getStatementBuilder()
                    .append(SQLSyntax.SQLOperation.IS_NOT_NULL);

            return queryBuilder;
        }

        public T isLessThan(Object value)
        {
            return is(SQLSyntax.SQLOperation.LESS_THAN, value);
        }

        public T isLessOrEqual(Object value)
        {
            return is(SQLSyntax.SQLOperation.LESS_THAN_OR_EQUAL, value);
        }

        public T isGreaterThan(Object value)
        {
            return is(SQLSyntax.SQLOperation.GREATER_THAN, value);
        }

        public T isGreaterOrEqual(Object value)
        {
            return is(SQLSyntax.SQLOperation.GREATER_THAN_OR_EQUAL, value);
        }

        public T like(String pattern)
        {
            return is(SQLSyntax.SQLOperation.LIKE, pattern);
        }

        public T notLike(String pattern)
        {
            return is(SQLSyntax.SQLOperation.NOT_LIKE, pattern);
        }

        public T isNotEqual(Object value)
        {
            return is(SQLSyntax.SQLOperation.NOT_EQUAL, value);
        }


        public T is(SQLSyntax.SQLOperation operation, Object attribute)
        {
            return is(operation, attribute, SQLSyntax.ARGUMENT_PLACEHOLDER);
        }

        private T is(SQLSyntax.SQLOperation operation, Object attribute, String argumentPlaceHolder)
        {
            queryBuilder.getStatementBuilder()
                    .append(operation.toString())
                    .append(argumentPlaceHolder);

            queryBuilder.addArgument(attribute);

            return queryBuilder;
        }
    }
}
