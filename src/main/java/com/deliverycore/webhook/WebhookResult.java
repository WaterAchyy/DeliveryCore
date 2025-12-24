package com.deliverycore.webhook;

/**
 * Represents the result of a webhook request.
 *
 * @param success    whether the request was successful
 * @param statusCode the HTTP status code (0 if request failed before sending)
 * @param message    a descriptive message about the result
 */
public record WebhookResult(
    boolean success,
    int statusCode,
    String message
) {
    /**
     * Creates a successful result.
     *
     * @param statusCode the HTTP status code
     * @return a successful WebhookResult
     */
    public static WebhookResult success(int statusCode) {
        return new WebhookResult(true, statusCode, "Webhook sent successfully");
    }
    
    /**
     * Creates a failure result.
     *
     * @param statusCode the HTTP status code
     * @param message    the error message
     * @return a failed WebhookResult
     */
    public static WebhookResult failure(int statusCode, String message) {
        return new WebhookResult(false, statusCode, message);
    }
    
    /**
     * Creates a failure result for connection errors.
     *
     * @param message the error message
     * @return a failed WebhookResult with status code 0
     */
    public static WebhookResult connectionError(String message) {
        return new WebhookResult(false, 0, message);
    }
}
