package io.github.t3r1jj.fcms.backend.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
public class UnprocessableException extends RuntimeException {
    public UnprocessableException(String message) {
        super(message);
    }

    public UnprocessableException(Throwable cause) {
        super(cause);
    }
}
