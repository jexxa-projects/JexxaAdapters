package io.jexxa.common.drivenadapter.outbox;

public final class TransactionalOutboxProperties {
    private static String prefix = "";
    public static final String OUTBOX_TABLE = "outbox.table";

    public static String outboxTable() { return prefix() + OUTBOX_TABLE; }

    public static String prefix() {return prefix;}
    public static void prefix(String prefix) { TransactionalOutboxProperties.prefix = prefix;}

    private TransactionalOutboxProperties()
    {
        //private constructor
    }

}
