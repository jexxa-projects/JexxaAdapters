package io.jexxa.common.drivenadapter.outbox;

import static io.jexxa.adapterapi.PropertiesPrefix.prefix;

public final class TransactionalOutboxProperties {
    public static final String OUTBOX_TABLE = "outbox.table";

    public static String outboxTable() { return prefix() + OUTBOX_TABLE; }

    private TransactionalOutboxProperties()
    {
        //private constructor
    }

}
