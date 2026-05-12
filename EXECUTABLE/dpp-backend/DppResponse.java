package com.dpp.api;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * DIN EN 18222 – Tabelle 16 / Table 16
 * Generic Status Codes for DPP API responses.
 */
public class DppResponse {

    // -------------------------------------------------------------------------
    // 1.  StatusCode enum  (Tabelle 16)
    // -------------------------------------------------------------------------
    public enum StatusCode {
        Success,
        SuccessCreated,
        SuccessAccepted,
        SuccessNoContent,
        ClientErrorBadRequest,
        ClientNotAuthorized,
        ClientForbidden,
        ClientMethodNotAllowed,
        ClientErrorResourceNotFound,
        ClientResourceConflict,
        ServerInternalError,
        ServerErrorBadGateway
    }

    // -------------------------------------------------------------------------
    // 2.  MessageTypeEnum  (Tabelle 15)
    // -------------------------------------------------------------------------
    public enum MessageTypeEnum {
        Info,
        Warning,
        Error,
        Exception
    }

    // -------------------------------------------------------------------------
    // 3.  Message  (Tabelle 14)
    // -------------------------------------------------------------------------
    public static class Message {
        public MessageTypeEnum messageType;
        public String text;
        public String code;           // optional – technology-dependent
        public String correlationId;  // optional
        public String timestamp;      // optional – ISO-8601

        public Message() {}

        public Message(MessageTypeEnum type, String text) {
            this.messageType = type;
            this.text        = text;
            this.timestamp   = Instant.now().toString();
        }

        public Message(MessageTypeEnum type, String text, String code) {
            this(type, text);
            this.code = code;
        }
    }

    // -------------------------------------------------------------------------
    // 4.  Result  (Tabelle 13) – returned on *failed* executions
    // -------------------------------------------------------------------------
    public static class Result {
        public List<Message> message = new ArrayList<>();

        public Result() {}

        public Result addMessage(Message m) {
            this.message.add(m);
            return this;
        }

        /** Convenience: single error message. */
        public static Result error(String text) {
            return new Result().addMessage(new Message(MessageTypeEnum.Error, text));
        }

        /** Convenience: single error message with HTTP-style code. */
        public static Result error(String text, String code) {
            return new Result().addMessage(new Message(MessageTypeEnum.Error, text, code));
        }

        /** Convenience: single exception message. */
        public static Result exception(String text) {
            return new Result().addMessage(new Message(MessageTypeEnum.Exception, text));
        }

        /** Convenience: single info message. */
        public static Result info(String text) {
            return new Result().addMessage(new Message(MessageTypeEnum.Info, text));
        }
    }

    // -------------------------------------------------------------------------
    // 5.  Generic response wrapper
    //     – statusCode is always present
    //     – result (with messages) is only present on error status codes
    //     – payload is present on success
    // -------------------------------------------------------------------------
    public static class ApiResponse {
        public StatusCode statusCode;
        public Result     result;   // null on success
        public T          payload;  // null on error / no-content

        private ApiResponse() {}

        // --- success factories -----------------------------------------------

        public static ApiResponse success( payload) {
            ApiResponse<T> r = new ApiResponse<>();
            r.statusCode = StatusCode.Success;
            r.payload    = payload;
            return r;
        }

        public static <T> ApiResponse<T> successCreated(T payload) {
            ApiResponse<T> r = new ApiResponse<>();
            r.statusCode = StatusCode.SuccessCreated;
            r.payload    = payload;
            return r;
        }

        public static <T> ApiResponse<T> successNoContent() {
            ApiResponse<T> r = new ApiResponse<>();
            r.statusCode = StatusCode.SuccessNoContent;
            return r;
        }

        // --- error factories -------------------------------------------------

        public static <T> ApiResponse<T> badRequest(String msg) {
            ApiResponse<T> r = new ApiResponse<>();
            r.statusCode = StatusCode.ClientErrorBadRequest;
            r.result     = Result.error(msg, "400");
            return r;
        }

        public static <T> ApiResponse<T> notFound(String msg) {
            ApiResponse<T> r = new ApiResponse<>();
            r.statusCode = StatusCode.ClientErrorResourceNotFound;
            r.result     = Result.error(msg, "404");
            return r;
        }

        public static <T> ApiResponse<T> internalError(String msg) {
            ApiResponse<T> r = new ApiResponse<>();
            r.statusCode = StatusCode.ServerInternalError;
            r.result     = Result.exception(msg);
            return r;
        }
    }
}
