package io.jexxa.common.facade.jdbc.builder;

import io.jexxa.common.facade.jdbc.JDBCCommand;
import io.jexxa.common.facade.jdbc.JDBCConnection;

import java.util.function.Supplier;

@SuppressWarnings("unused")
public class JDBCTableBuilder<T extends Enum<T>> extends JDBCBuilder<T>
{
    private final Supplier<JDBCConnection> jdbcConnection;

    public JDBCTableBuilder(Supplier<JDBCConnection> jdbcConnection )
    {
        this.jdbcConnection = jdbcConnection;
    }

    public JDBCCommand dropTableIfExists(String table)
    {
        getStatementBuilder()
                .append(SQLSyntax.DROP_TABLE)
                .append(SQLSyntax.IF_EXISTS)
                .append(table);

        return create();
    }

    public JDBCCommand dropTableIfExists(T element)
    {
        return dropTableIfExists(element.name());
    }

    public JDBCCommand dropTableIfExists(Class<?> clazz)
    {
        return dropTableIfExists(clazz.getSimpleName());
    }

    public JDBCColumnBuilder<T> alterTable(Class<?> clazz)
    {
        getStatementBuilder()
                .append(SQLSyntax.ALTER_TABLE)
                .append(clazz.getSimpleName())
                .append(SQLSyntax.BLANK);
        return new JDBCColumnBuilder<>(this);
    }


    public JDBCColumnBuilder<T> createTableIfNotExists(String tableName)
    {
        getStatementBuilder()
                .append(SQLSyntax.CREATE_TABLE)
                .append(SQLSyntax.IF_NOT_EXISTS)
                .append(tableName);

        return new JDBCColumnBuilder<>(this);
    }

    public JDBCColumnBuilder<T> createTableIfNotExists(T element)
    {
        return createTableIfNotExists(element.name());
    }

    public JDBCColumnBuilder<T> createTableIfNotExists(Class<?> clazz)
    {
        return createTableIfNotExists(clazz.getSimpleName());
    }

    public JDBCColumnBuilder<T> createTable(String tableName)
    {
        getStatementBuilder()
                .append(SQLSyntax.CREATE_TABLE)
                .append(tableName);

        return new JDBCColumnBuilder<>(this);
    }
    public JDBCColumnBuilder<T> createTable(T element)
    {
        return createTable(element.name());
    }

    public JDBCColumnBuilder<T> createTable(Class<?> clazz)
    {
        return createTable(clazz.getSimpleName());
    }

    public JDBCCommand dropTable(String tableName)
    {
        getStatementBuilder()
                .append(SQLSyntax.DROP_TABLE)
                .append(tableName);

        return create();
    }

    public JDBCCommand dropTable(T element)
    {
        return dropTable(element.name());
    }

    public JDBCCommand dropTable(Class<?> clazz)
    {
        return dropTable(clazz.getSimpleName());
    }

    public JDBCCommand create()
    {
        return new JDBCCommand(jdbcConnection, getStatementBuilder().toString(), getArguments() );
    }

    public static class JDBCColumnBuilder<T extends Enum<T>>
    {
        private final JDBCTableBuilder<T> commandBuilder;
        private boolean firstColumn = true;
        private boolean openBraces = false;

        JDBCColumnBuilder( JDBCTableBuilder<T> commandBuilder )
        {
            this.commandBuilder = commandBuilder;
        }

        public JDBCTableBuilder<T> alterColumn(T element, SQLDataType newDataType )
        {
            return alterColumn(element, newDataType, "");
        }

        public JDBCTableBuilder<T> alterColumn(String element, SQLDataType newDataType )
        {
            return alterColumn(element, newDataType, "");
        }

        public JDBCTableBuilder<T> alterColumn(T element, SQLDataType newDataType, String usingStatement )
        {
            return alterColumn(element.name(), newDataType, usingStatement);
        }

        public JDBCTableBuilder<T> alterColumn(String element, SQLDataType newDataType, String usingStatement )
        {
            addCommaSeparatorIfRequired();

            commandBuilder
                    .getStatementBuilder()
                    .append(SQLSyntax.ALTER_COLUMN)
                    .append(element)
                    .append(SQLSyntax.BLANK)
                    .append(SQLSyntax.TYPE)
                    .append(newDataType.toString())
                    .append(usingStatement);

            return commandBuilder;
        }

        public <S extends Enum<S>> JDBCColumnBuilder<T> addColumn(S element, SQLDataType dataType)
        {
            addCommaSeparatorIfRequired();
            openBracesIfRequired();

            commandBuilder
                    .getStatementBuilder()
                    .append(element.name())
                    .append(SQLSyntax.BLANK)
                    .append(dataType.toString());

            return this;
        }

        /**
         * @deprecated Use {@link #addColumn(S element, SQLDataType dataType)} instead
         */
        @Deprecated(since = "1.2.0",forRemoval = true)
        public <S extends Enum<S>> JDBCColumnBuilder<T> addColumn(S element, SQLDataType dataType, Class<S> ignore)
        {
            addCommaSeparatorIfRequired();
            openBracesIfRequired();

            commandBuilder
                    .getStatementBuilder()
                    .append(element.name())
                    .append(SQLSyntax.BLANK)
                    .append(dataType.toString());

            return this;
        }


        public JDBCColumnBuilder<T> addConstraint( SQLConstraint sqlConstraint)
        {
            commandBuilder
                    .getStatementBuilder()
                    .append(sqlConstraint.toString())
                    .append(SQLSyntax.BLANK);

            return this;
        }

        public JDBCCommand create()
        {
            closeBracesIfRequired();
            return commandBuilder.create();
        }

        private void addCommaSeparatorIfRequired()
        {
            if (firstColumn)
            {
                firstColumn = false;
            } else {
                commandBuilder.getStatementBuilder().append(SQLSyntax.COMMA);
            }
        }

        private void openBracesIfRequired()
        {
            if (!openBraces)
            {
                commandBuilder.getStatementBuilder().append("( ");
                openBraces = true;
            }
        }

        private void closeBracesIfRequired()
        {
            if (openBraces)
            {
                commandBuilder.getStatementBuilder().append(" )");
                openBraces = false;
            }
        }
    }

    public enum SQLConstraint
    {
        PRIMARY_KEY("PRIMARY KEY");

        private final String string;

        // constructor to set the string
        SQLConstraint(String name){string = name;}

        // the toString just returns the given name
        @Override
        public final String toString() {
            return string;
        }
    }

}
