package io.jexxa.common.facade.jdbc.builder;

import io.jexxa.common.facade.jdbc.JDBCConnection;
import io.jexxa.common.facade.jdbc.JDBCQuery;

import java.util.function.Supplier;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class JDBCQueryBuilder<T extends Enum<T>> extends JDBCBuilder<T>
{
    private final Supplier<JDBCConnection> jdbcConnection;

    private boolean orderByAdded = false;


    public JDBCQueryBuilder(Supplier<JDBCConnection> jdbcConnection)
    {
        this.jdbcConnection = jdbcConnection;
    }

    public JDBCQueryBuilder<T> select(T element)
    {
        getStatementBuilder()
                .append(SQLSyntax.SELECT)
                .append(element.name())
                .append(SQLSyntax.BLANK);

        return this;
    }

    @SafeVarargs
    @SuppressWarnings("varargs")
    public final JDBCQueryBuilder<T> select(T element, T... elements)
    {
        select(element);

        Stream.of( elements )
                .forEach( entry -> getStatementBuilder()
                        .append(SQLSyntax.COMMA)
                        .append(entry.name())
                        .append(SQLSyntax.BLANK)
                );

        return this;
    }

    @SuppressWarnings("java:S1172") // clazz required to ensure type safety
    public <S extends Enum<S>> JDBCQueryBuilder<T> select(Class<S> clazz, S element)
    {
        getStatementBuilder()
                .append(SQLSyntax.SELECT)
                .append(element.name())
                .append(SQLSyntax.BLANK);

        return this;
    }

    @SafeVarargs
    @SuppressWarnings("varargs")
public final <S extends Enum<S>> JDBCQueryBuilder<T> select(Class<S> clazz, S element, S... elements)
    {
        select(clazz, element);

        Stream.of( elements )
                .forEach( entry -> getStatementBuilder()
                        .append(SQLSyntax.COMMA)
                        .append(entry.name())
                        .append(SQLSyntax.BLANK)
                );

        return this;
    }


    public JDBCQueryBuilder<T> selectAll()
    {
        getStatementBuilder()
                .append(SQLSyntax.SELECT)
                .append("* ");
        return this;
    }

    public JDBCQueryBuilder<T> selectCount(T element)
    {
        getStatementBuilder()
                .append(SQLSyntax.SELECT_COUNT)
                .append("( ")
                .append(element)
                .append(" )");
        return this;
    }

    public JDBCQueryBuilder<T> selectCount()
    {
        getStatementBuilder()
                .append(SQLSyntax.SELECT_COUNT)
                .append("( * )");
        return this;
    }

    public JDBCQueryBuilder<T> selectMax(T element)
    {
        getStatementBuilder()
                .append(SQLSyntax.SELECT_MAX)
                .append("( ")
                .append(element.name())
                .append(" ) ");
        return this;
    }
    public JDBCQueryBuilder<T> selectMin(T element)
    {
        getStatementBuilder()
                .append(SQLSyntax.SELECT_MIN)
                .append("( ")
                .append(element.name())
                .append(" ) ");
        return this;
    }

    public JDBCQueryBuilder<T> from(String name)
    {
        getStatementBuilder()
                .append(SQLSyntax.FROM)
                .append(name)
                .append(SQLSyntax.BLANK);

        return this;
    }

    public JDBCQueryBuilder<T> from(T element)
    {
        return from(element.name());
    }

    public JDBCQueryBuilder<T> from(Class<?> clazz)
    {
        return from(clazz.getSimpleName());
    }

    public JDBCCondition<T, JDBCQueryBuilder<T>> where(T element)
    {
        getStatementBuilder()
                .append(SQLSyntax.WHERE)
                .append(element.name())
                .append(SQLSyntax.BLANK);

        return new JDBCCondition<>(this);
    }

    public JDBCCondition<T, JDBCQueryBuilder<T>> and(T element)
    {
        getStatementBuilder()
                .append(SQLSyntax.AND)
                .append(element.name())
                .append(SQLSyntax.BLANK);

        return new JDBCCondition<>(this);
    }

    public JDBCCondition<T, JDBCQueryBuilder<T>> or(T element)
    {
        getStatementBuilder()
                .append(SQLSyntax.OR)
                .append(element.name())
                .append(SQLSyntax.BLANK);

        return new JDBCCondition<>(this);
    }

    public JDBCQueryBuilder<T> limit(int number)
    {
        getStatementBuilder()
                .append( SQLSyntax.LIMIT )
                .append( SQLSyntax.ARGUMENT_PLACEHOLDER )
                .append(SQLSyntax.BLANK);

        addArgument(number);

        return this;
    }

    public JDBCQuery create()
    {
        return new JDBCQuery(jdbcConnection, getStatementBuilder().toString(), getArguments());
    }

    public JDBCQueryBuilder<T> orderBy(T element, SQLOrder order)
    {
        if (!orderByAdded)
        {
            getStatementBuilder().append(SQLSyntax.ORDER_BY);
            orderByAdded = true;
        }

        getStatementBuilder().append(element.name())
                .append(SQLSyntax.BLANK)
                .append(order.getOrderName())
                .append(SQLSyntax.BLANK);

        return this;
    }

    public JDBCQueryBuilder<T> orderBy(T element)
    {
        if (!orderByAdded)
        {
            getStatementBuilder().append(SQLSyntax.ORDER_BY);
            orderByAdded = true;
        }

        getStatementBuilder().append(element.name())
                .append(SQLSyntax.BLANK);

        return this;
    }


}
