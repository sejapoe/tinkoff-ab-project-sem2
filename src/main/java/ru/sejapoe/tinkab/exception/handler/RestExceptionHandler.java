package ru.sejapoe.tinkab.exception.handler;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import ru.sejapoe.tinkab.dto.SuccessResponse;
import ru.sejapoe.tinkab.exception.BaseException;

@RestControllerAdvice
public class RestExceptionHandler {
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public SuccessResponse methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException ex) {

        FieldError fieldError = ex.getBindingResult().getFieldError();

        if (fieldError == null) {
            return new SuccessResponse(
                    false,
                    ex.getBody().getTitle() + ": " + ex.getBody().getDetail()
            );
        }

        return new SuccessResponse(
                false,
                ex.getBody().getTitle() + ": " + fieldError.getField() + " - " + fieldError.getDefaultMessage()
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HandlerMethodValidationException.class)
    public SuccessResponse handlerMethodValidationException(HandlerMethodValidationException ex) {
        Object[] detailMessageArguments = ex.getDetailMessageArguments();
        if (detailMessageArguments == null || detailMessageArguments.length == 0) {
            return new SuccessResponse(
                    false,
                    ex.getMessage()
            );
        }

        return new SuccessResponse(
                false,
                ex.getStatusCode() + ": " + detailMessageArguments[0]
        );
    }

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<SuccessResponse> baseExceptionHandler(BaseException ex) {
        return ResponseEntity
                .status(ex.getHttpStatus())
                .body(new SuccessResponse(false, ex.getMessage()));
    }
}
