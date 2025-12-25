package com.seffafbagis.api.enums;

/**
 * Payment session status enum.
 * Tracks the lifecycle of a payment session (shopping cart checkout).
 * 
 * @author System
 * @version 1.0
 */
public enum PaymentSessionStatus {
    /**
     * Session created, waiting for payment initiation.
     */
    PENDING,

    /**
     * Payment is being processed.
     */
    PROCESSING,

    /**
     * Payment completed successfully, all donations are confirmed.
     */
    COMPLETED,

    /**
     * Payment failed or was cancelled.
     */
    FAILED,

    /**
     * Session expired without completion.
     */
    EXPIRED
}
