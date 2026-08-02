package br.com.serasa.serasa.transport;

public enum TransactionStatus {

    /** Truck received a transport demand and is on its way to/from the farm. */
    STARTED,

    /** Truck is on the scale, weight readings are being accumulated until they stabilize. */
    WEIGHING,

    /** Weight stabilized and persisted; the transaction is closed. */
    COMPLETED,

    /** Manually aborted before weighing completed. */
    CANCELLED
}
