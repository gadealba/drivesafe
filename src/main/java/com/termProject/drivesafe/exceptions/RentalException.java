package com.termProject.drivesafe.exceptions;

import org.springframework.http.HttpStatus;

/** One exception for the rental feature. The status says which HTTP error the client gets. */
public class RentalException extends RuntimeException {

    private final HttpStatus status;

    private RentalException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus status() {
        return status;
    }

    public static RentalException badRequest(String message) {
        return new RentalException(HttpStatus.BAD_REQUEST, message);
    }

    public static RentalException forbidden(String message) {
        return new RentalException(HttpStatus.FORBIDDEN, message);
    }

    public static RentalException notFound(String message) {
        return new RentalException(HttpStatus.NOT_FOUND, message);
    }

    public static RentalException conflict(String message) {
        return new RentalException(HttpStatus.CONFLICT, message);
    }
}
