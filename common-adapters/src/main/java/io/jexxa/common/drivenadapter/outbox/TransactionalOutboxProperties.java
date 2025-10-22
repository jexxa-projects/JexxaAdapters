package io.jexxa.common.drivenadapter.outbox;

import io.jexxa.common.facade.utils.properties.PropertiesPrefix;

public final class TransactionalOutboxProperties {
    public static final String OUTBOX_TABLE = "outbox.table";

    public static String outboxTable() { return PropertiesPrefix.globalPrefix() + OUTBOX_TABLE; }

    private TransactionalOutboxProperties()
    {
        //private constructor
    }

}
