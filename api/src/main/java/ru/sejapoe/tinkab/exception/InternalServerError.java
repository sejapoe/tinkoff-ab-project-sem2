package ru.sejapoe.tinkab.exception;

import org.springframework.http.HttpStatus;

public class InternalServerError extends BaseException {
    public InternalServerError(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }
}
