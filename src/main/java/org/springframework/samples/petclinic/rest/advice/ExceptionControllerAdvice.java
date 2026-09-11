/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.samples.petclinic.rest.advice;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.rest.controller.BindingErrorsResponse;
import org.springframework.samples.petclinic.rest.dto.ValidationMessageDto;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Global Exception handler for REST controllers.
 * <p>
 * This class handles exceptions thrown by REST controllers and returns appropriate HTTP responses to the client.
 *
 * @author Vitaliy Fedoriv
 * @author Alexander Dudkin
 */
@ControllerAdvice
public class ExceptionControllerAdvice {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionControllerAdvice.class);
    private static final String ERROR_UNEXPECTED = "An unexpected error occurred while processing your request";
    private static final String ERROR_DATA_INTEGRITY = "The requested resource could not be processed due to a data constraint violation";
    private static final String ERROR_INVALID_REQUEST = "The request contains invalid or missing parameters";
    private static final String ERROR_DAILY_LIMIT_EXCEEDED = "The maximum number of owners that may be created today has been reached";

    /**
     * Private method for constructing the {@link ProblemDetail} object passing the name and details of the exception
     * class.
     *
     * @param e     Object referring to the thrown exception.
     * @param status HTTP response status.
     * @param url URL request.
     */
    private ProblemDetail detailBuild(Exception e, HttpStatus status, StringBuffer url, String detail) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(status);
        problemDetail.setType(URI.create(url.toString()));
        problemDetail.setTitle(e.getClass().getSimpleName());
        problemDetail.setDetail(detail);
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("schemaValidationErrors", List.<ValidationMessageDto>of());
        return problemDetail;
    }

    /**
     * Re-throws {@link AccessDeniedException} so Spring Security's {@code ExceptionTranslationFilter}
     * translates it as it normally would - 403 for an authenticated user lacking the required role,
     * or the configured entry point for an anonymous one. Without this, {@link #handleGeneralException}
     * would catch it and report an authorization failure as a 500.
     *
     * @param e The {@link AccessDeniedException} to propagate
     * @throws AccessDeniedException always
     */
    @ExceptionHandler(AccessDeniedException.class)
    public void handleAccessDeniedException(AccessDeniedException e) throws AccessDeniedException {
        throw e;
    }

    /**
     * Handles all general exceptions by returning a 500 Internal Server Error status with error details.
     *
     * @param e The {@link Exception} to be handled
     * @param request {@link HttpServletRequest} object referring to the current request.
     * @return A {@link ResponseEntity} containing the error information and a 500 Internal Server Error status
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<ProblemDetail> handleGeneralException(Exception e, HttpServletRequest request) {
        logger.error("Unexpected error at {} {}", request.getMethod(), request.getRequestURI(), e);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ProblemDetail detail = this.detailBuild(e, status, request.getRequestURL(), ERROR_UNEXPECTED);
        return ResponseEntity.status(status).body(detail);
    }

    /**
     * Handles {@link DataIntegrityViolationException} which typically indicates database constraint violations. This
     * method returns a 404 Not Found status if an entity does not exist.
     *
     * @param e The {@link DataIntegrityViolationException} to be handled
     * @param request {@link HttpServletRequest} object referring to the current request.
     * @return A {@link ResponseEntity} containing the error information and a 404 Not Found status
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseBody
    public ResponseEntity<ProblemDetail> handleDataIntegrityViolationException(DataIntegrityViolationException e, HttpServletRequest request) {
        logger.warn("Data integrity violation at {} {}: {}",
            request.getMethod(),
            request.getRequestURI(),
            e.getMessage());
        logger.debug("Data integrity violation stacktrace", e);
        HttpStatus status = HttpStatus.NOT_FOUND;
        ProblemDetail detail = this.detailBuild(e, status, request.getRequestURL(), ERROR_DATA_INTEGRITY);
        return ResponseEntity.status(status).body(detail);
    }

    /**
     * Handles {@link OwnerConflictException}, the base type for every create-time rule that rejects
     * a new owner with a 409 Conflict (e.g. a duplicate identity key or a city at capacity),
     * returning a 409 Conflict status. The client-facing detail is taken from the
     * exception's {@link OwnerConflictException#getDetail()} so each rule keeps its own wording
     * while sharing this single handler.
     *
     * @param e The {@link OwnerConflictException} to be handled
     * @param request {@link HttpServletRequest} object referring to the current request.
     * @return A {@link ResponseEntity} containing the error information and a 409 Conflict status
     */
    @ExceptionHandler(OwnerConflictException.class)
    @ResponseBody
    public ResponseEntity<ProblemDetail> handleOwnerConflictException(OwnerConflictException e, HttpServletRequest request) {
        return handleOwnerRuleException(e, "Owner conflict", HttpStatus.CONFLICT, request);
    }

    /**
     * Handles {@link DailyOwnerLimitExceededException}, raised when an owner is created once 100 or
     * more owners have already been created today (compared by registration date), returning a
     * 429 Too Many Requests status.
     *
     * @param e The {@link DailyOwnerLimitExceededException} to be handled
     * @param request {@link HttpServletRequest} object referring to the current request.
     * @return A {@link ResponseEntity} containing the error information and a 429 Too Many Requests status
     */
    @ExceptionHandler(DailyOwnerLimitExceededException.class)
    @ResponseBody
    public ResponseEntity<ProblemDetail> handleDailyOwnerLimitExceededException(DailyOwnerLimitExceededException e, HttpServletRequest request) {
        logger.warn("Daily owner limit exceeded at {} {}: {}",
            request.getMethod(),
            request.getRequestURI(),
            e.getMessage());
        HttpStatus status = HttpStatus.TOO_MANY_REQUESTS;
        ProblemDetail detail = this.detailBuild(e, status, request.getRequestURL(), ERROR_DAILY_LIMIT_EXCEEDED);
        return ResponseEntity.status(status).body(detail);
    }

    /**
     * Handles {@link InvalidOwnerFieldException}, the base type for every field-level validation
     * rule that rejects a supplied owner field with a 400 Bad Request (e.g. a telephone that is not
     * valid E.164 or an address blank after normalization), returning a 400 Bad Request status. The
     * client-facing detail is taken from the exception's {@link InvalidOwnerFieldException#getDetail()}
     * so each rule keeps its own wording while sharing this single handler.
     *
     * @param e The {@link InvalidOwnerFieldException} to be handled
     * @param request {@link HttpServletRequest} object referring to the current request.
     * @return A {@link ResponseEntity} containing the error information and a 400 Bad Request status
     */
    @ExceptionHandler(InvalidOwnerFieldException.class)
    @ResponseBody
    public ResponseEntity<ProblemDetail> handleInvalidOwnerFieldException(InvalidOwnerFieldException e, HttpServletRequest request) {
        return handleOwnerRuleException(e, "Invalid owner field", HttpStatus.BAD_REQUEST, request);
    }

    /**
     * Shared handling for the {@link OwnerRuleException} hierarchy: logs the offending rule and
     * builds a {@link ProblemDetail} response at the status the rule maps to, using the exception's
     * own {@link OwnerRuleException#getDetail()} as the client-facing detail.
     *
     * @param e        the rule violation to report
     * @param logLabel the human-readable category prefix for the warning log line
     * @param status   the HTTP status this rule maps to
     * @param request  {@link HttpServletRequest} object referring to the current request.
     * @return A {@link ResponseEntity} containing the error information and the given status
     */
    private ResponseEntity<ProblemDetail> handleOwnerRuleException(OwnerRuleException e, String logLabel, HttpStatus status, HttpServletRequest request) {
        logger.warn("{} ({}) at {} {}: {}",
            logLabel,
            e.getClass().getSimpleName(),
            request.getMethod(),
            request.getRequestURI(),
            e.getMessage());
        ProblemDetail detail = this.detailBuild(e, status, request.getRequestURL(), e.getDetail());
        return ResponseEntity.status(status).body(detail);
    }

    /**
     * Handles exception thrown by Bean Validation on controller methods parameters
     *
     * @param e The {@link MethodArgumentNotValidException} to be handled
     * @param request {@link HttpServletRequest} object referring to the current request.
     * @return A {@link ResponseEntity} containing the error information and a 400 Bad Request status.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseEntity<ProblemDetail> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        BindingErrorsResponse errors = new BindingErrorsResponse();
        BindingResult bindingResult = e.getBindingResult();
        ProblemDetail detail = this.detailBuild(e, status, request.getRequestURL(), ERROR_INVALID_REQUEST);
        if (bindingResult.hasErrors()) {
            errors.addAllErrors(bindingResult);
            List<ValidationMessageDto> schemaValidationErrors = bindingResult.getFieldErrors().stream()
                .map(fieldError -> {
                    String rejectedValue = Objects.toString(fieldError.getRejectedValue(), "null");
                    String defaultMessage = Objects.toString(fieldError.getDefaultMessage(), "Validation failed");
                    String message = "Field '%s' %s (rejected value: %s)".formatted(
                        fieldError.getField(),
                        defaultMessage,
                        rejectedValue);
                    return new ValidationMessageDto(message)
                        .putAdditionalProperty("field", fieldError.getField())
                        .putAdditionalProperty("rejectedValue", rejectedValue)
                        .putAdditionalProperty("defaultMessage", defaultMessage);
                })
                .toList();
            logger.debug("Validation error at {} {}: {}",
                request.getMethod(),
                request.getRequestURI(),
                bindingResult.getFieldErrors());
            detail.setProperty("schemaValidationErrors", schemaValidationErrors);
            detail.setProperty("errors", rejectedFieldNames(bindingResult));
            return ResponseEntity.status(status).body(detail);
        }
        return ResponseEntity.status(status).body(detail);
    }

    /**
     * Collects the distinct names of the fields that failed validation, in the order they were
     * reported, so responses can list the offending fields (e.g. those that are missing or blank).
     *
     * @param bindingResult the {@link BindingResult} carrying the validation field errors
     * @return the distinct rejected field names
     */
    private List<String> rejectedFieldNames(BindingResult bindingResult) {
        return bindingResult.getFieldErrors().stream()
            .map(FieldError::getField)
            .distinct()
            .toList();
    }

}
