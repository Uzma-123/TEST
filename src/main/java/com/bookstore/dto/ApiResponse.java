package com.bookstore.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Generic response envelope returned by every REST endpoint.
 *
 * <pre>
 * {
 *   "success": true,
 *   "message": "Books retrieved successfully",
 *   "data": { ... }
 * }
 * </pre>
 *
 * <p>Null {@code data} fields are omitted from the serialised JSON.</p>
 *
 * @param <T> type of the payload carried in {@code data}
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final String message;
    private final T data;

    // ------------------------------------------------------------------
    // Private constructor — callers use the static factory methods below
    // ------------------------------------------------------------------

    private ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // ------------------------------------------------------------------
    // Static factory methods
    // ------------------------------------------------------------------

    /** Success with a data payload and a default message. */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Success", data);
    }

    /** Success with a custom message and a data payload. */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    /** Success with only a message and no payload (e.g. for deletes). */
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null);
    }

    /** Error with a plain message and no payload. */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }

    /** Error with a message and a supplementary payload (e.g. field errors). */
    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>(false, message, data);
    }

    // ------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}
