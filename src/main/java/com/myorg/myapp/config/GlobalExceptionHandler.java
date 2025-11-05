package com.myorg.myapp.config;

import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
    ProblemDetail pd =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
    pd.setType(URI.create("https://example.com/problems/validation-error"));
    pd.setProperty(
        "errors",
        ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
            .toList());
    return pd;
  }

  @ExceptionHandler(ResponseStatusException.class)
  ProblemDetail handleResponseStatus(ResponseStatusException ex) {
    ProblemDetail pd =
        ProblemDetail.forStatusAndDetail(
            ex.getStatusCode(), ex.getReason() == null ? "" : ex.getReason());
    pd.setType(URI.create("https://example.com/problems/http-error"));
    return pd;
  }

  @ExceptionHandler(ErrorResponseException.class)
  ProblemDetail handleErrorResponse(ErrorResponseException ex) {
    return ex.getBody();
  }

  @ExceptionHandler(Exception.class)
  ProblemDetail handleOther(Exception ex) {
    ProblemDetail pd =
        ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error");
    pd.setType(URI.create("https://example.com/problems/unexpected-error"));
    return pd;
  }
}
