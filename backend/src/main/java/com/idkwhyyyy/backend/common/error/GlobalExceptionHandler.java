package com.idkwhyyyy.backend.common.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ApiException.class)
  ProblemDetail handleApiException(ApiException exception, HttpServletRequest request) {
    ProblemDetail detail =
        ProblemDetail.forStatusAndDetail(exception.getStatus(), exception.getMessage());
    detail.setTitle(exception.getCode());
    enrich(detail, request);
    return detail;
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ProblemDetail handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
    ProblemDetail detail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, "Payload validation failed for one or more fields.");
    detail.setTitle("validation_error");
    Map<String, String> fieldErrors =
        exception.getBindingResult().getFieldErrors().stream()
            .collect(
                Collectors.toMap(
                    FieldError::getField,
                    error -> error.getDefaultMessage() == null ? "Invalid value" : error.getDefaultMessage(),
                    (left, right) -> right));
    detail.setProperty("fieldErrors", fieldErrors);
    enrich(detail, request);
    return detail;
  }

  @ExceptionHandler(ConstraintViolationException.class)
  ProblemDetail handleConstraintViolation(
      ConstraintViolationException exception, HttpServletRequest request) {
    ProblemDetail detail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, "Constraint validation failed for request parameters.");
    detail.setTitle("constraint_violation");
    detail.setProperty(
        "violations",
        exception.getConstraintViolations().stream()
            .map(v -> v.getPropertyPath() + ": " + v.getMessage())
            .toList());
    enrich(detail, request);
    return detail;
  }

  @ExceptionHandler({HttpMessageNotReadableException.class, DataIntegrityViolationException.class})
  ProblemDetail handleBadRequest(Exception exception, HttpServletRequest request) {
    ProblemDetail detail =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Request could not be processed.");
    detail.setTitle("bad_request");
    enrich(detail, request);
    return detail;
  }

  @ExceptionHandler(Exception.class)
  ProblemDetail handleUnhandled(Exception exception, HttpServletRequest request) {
    ProblemDetail detail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error.");
    detail.setTitle("internal_error");
    enrich(detail, request);
    return detail;
  }

  private void enrich(ProblemDetail detail, HttpServletRequest request) {
    detail.setType(URI.create("about:blank"));
    detail.setInstance(URI.create(request.getRequestURI()));
    detail.setProperty("traceId", resolveTraceId(request));
  }

  private String resolveTraceId(HttpServletRequest request) {
    String fromHeader = request.getHeader("X-Trace-Id");
    if (fromHeader != null && !fromHeader.isBlank()) {
      return fromHeader;
    }
    return UUID.randomUUID().toString();
  }
}

