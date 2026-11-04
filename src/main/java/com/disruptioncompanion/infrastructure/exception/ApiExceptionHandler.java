package com.disruptioncompanion.infrastructure.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
  @ExceptionHandler({
    MethodArgumentNotValidException.class,
    HttpMessageNotReadableException.class,
    IllegalArgumentException.class
  })
  public ResponseEntity<ProblemDetail> invalidRequest(Exception exception) {
    return ResponseEntity.badRequest()
        .body(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid request"));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ProblemDetail> internalError(Exception exception) {
    if (exception instanceof org.springframework.web.ErrorResponse error) {
      return ResponseEntity.status(error.getStatusCode())
          .body(ProblemDetail.forStatus(error.getStatusCode()));
    }
    return ResponseEntity.internalServerError()
        .body(
            ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "Assessment could not be completed"));
  }
}
