package com.seffafbagis.api.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when payment processing fails (HTTP 400 or 500 depending on cause).
 * 
 * This exception is raised when payment operations encounter errors.
 * It includes information about whether the operation can be retried.
 * 
 * @author Furkan
 * @version 1.0
 */
public class PaymentException extends BusinessException {

    /**
     * Payment provider error code.
     */
    private final String paymentErrorCode;

    /**
     * Whether the payment operation can be retried.
     */
    private final boolean retryable;

    /**
     * Constructor accepting all required fields.
     * 
     * @param message User-friendly error message
     * @param errorCode Machine-readable error code
     * @param retryable Whether the operation can be retried
     */
    public PaymentException(String message, String errorCode, boolean retryable) {
        super(
            message,
            retryable ? HttpStatus.BAD_REQUEST : HttpStatus.INTERNAL_SERVER_ERROR,
            errorCode
        );
        this.paymentErrorCode = errorCode;
        this.retryable = retryable;
    }

    /**
     * Constructor with custom HTTP status.
     * 
     * @param message User-friendly error message
     * @param errorCode Machine-readable error code
     * @param retryable Whether the operation can be retried
     * @param status HTTP status to return
     */
    public PaymentException(String message, String errorCode, boolean retryable, HttpStatus status) {
        super(message, status, errorCode);
        this.paymentErrorCode = errorCode;
        this.retryable = retryable;
    }

    /**
     * Gets the payment provider error code.
     * 
     * @return Payment error code
     */
    public String getPaymentErrorCode() {
        return paymentErrorCode;
    }

    /**
     * Checks if the payment operation can be retried.
     * 
     * @return true if operation can be retried, false otherwise
     */
    public boolean isRetryable() {
        return retryable;
    }
}
