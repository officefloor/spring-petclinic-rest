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
import java.util.Map;
import java.util.Objects;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.rest.dto.ValidationMessageDto;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Global Exception handler for REST controllers.
 * <p>
 * This class handles exceptions thrown by REST controllers and returns appropriate HTTP responses to the client.
 * Every rejection is reported as an RFC&nbsp;7807 {@code application/problem+json} body carrying at least the
 * {@code type}, {@code title}, {@code status} and {@code detail} members.
 * <p>
 * By extending {@link ResponseEntityExceptionHandler} the framework-level exceptions (such as an unreadable
 * request body, a missing parameter or a type mismatch) are mapped to their proper {@code 4xx} status and
 * rendered with the same problem+json shape instead of falling through to the generic {@code 500} handler.
 *
 * @author Vitaliy Fedoriv
 * @author Alexander Dudkin
 */
@ControllerAdvice
public class ExceptionControllerAdvice extends ResponseEntityExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionControllerAdvice.class);
    private static final String ERROR_UNEXPECTED = "An unexpected error occurred while processing your request";
    private static final String ERROR_DATA_INTEGRITY = "The requested resource could not be processed due to a data constraint violation";
    private static final String ERROR_INVALID_REQUEST = "The request contains invalid or missing parameters";
    private static final String ERROR_DUPLICATE_IDENTITY = "An owner with the given identity key already exists";
    private static final String ERROR_CITY_AT_CAPACITY = "The given city already contains the maximum number of owners";
    private static final String ERROR_DAILY_LIMIT_REACHED = "The maximum number of owners has already been created today";

    /**
     * Single home for this application's RFC 7807 problem+json skeleton. Applies the {@code type} (the
     * request URL), {@code title} (the exception's simple class name) and the {@code timestamp} /
     * {@code schemaValidationErrors} members to {@code detail}, and fills {@code detail}'s human-readable
     * message with {@code fallbackDetail} when none has been set yet. Every write is idempotent: a value
     * already supplied by a more specific handler (a bespoke message, a populated
     * {@code schemaValidationErrors} list) is preserved, so both the custom {@code @ExceptionHandler}
     * methods and the {@link ResponseEntityExceptionHandler} hook render through the same definition.
     *
     * @param detail the problem detail to normalise (freshly created, or one prepared by the framework)
     * @param e the thrown exception, whose simple class name becomes the {@code title}
     * @param url the request URL used as the problem {@code type}
     * @param fallbackDetail the {@code detail} message to apply when none is already present
     * @return the same {@code detail} instance, normalised
     */
    private ProblemDetail shapeProblem(ProblemDetail detail, Exception e, String url, String fallbackDetail) {
        detail.setType(URI.create(url));
        detail.setTitle(e.getClass().getSimpleName());
        if (detail.getDetail() == null) {
            detail.setDetail(fallbackDetail);
        }
        Map<String, Object> properties = detail.getProperties();
        if (properties == null || !properties.containsKey("timestamp")) {
            detail.setProperty("timestamp", Instant.now());
        }
        if (properties == null || !properties.containsKey("schemaValidationErrors")) {
            detail.setProperty("schemaValidationErrors", List.<ValidationMessageDto>of());
        }
        return detail;
    }

    /**
     * Convenience factory for the custom {@code @ExceptionHandler} methods: creates a {@link ProblemDetail}
     * for {@code status} and renders it through {@link #shapeProblem}.
     *
     * @param e Object referring to the thrown exception.
     * @param status HTTP response status.
     * @param url the request URL used as the problem {@code type}.
     * @param detail the human-readable {@code detail} message.
     */
    private ProblemDetail detailBuild(Exception e, HttpStatus status, String url, String detail) {
        return shapeProblem(ProblemDetail.forStatus(status), e, url, detail);
    }

    /**
     * Resolves the full request URL used as the problem {@code type}.
     *
     * @param request the current request
     * @return the request URL as a string
     */
    private String requestUrl(HttpServletRequest request) {
        return request.getRequestURL().toString();
    }

    /**
     * Resolves the full request URL used as the problem {@code type} for the
     * {@link ResponseEntityExceptionHandler} hook, deferring to {@link #requestUrl(HttpServletRequest)}
     * so both paths derive it the same way.
     *
     * @param request the current request
     * @return the request URL, or the request description when it cannot be resolved as a servlet request
     */
    private String requestUrl(WebRequest request) {
        if (request instanceof ServletWebRequest servletWebRequest) {
            return requestUrl(servletWebRequest.getRequest());
        }
        return request.getDescription(false);
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
        ProblemDetail detail = this.detailBuild(e, status, requestUrl(request), ERROR_UNEXPECTED);
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
        ProblemDetail detail = this.detailBuild(e, status, requestUrl(request), ERROR_DATA_INTEGRITY);
        return ResponseEntity.status(status).body(detail);
    }

    /**
     * Handles exception thrown by Bean Validation on controller method bodies, overriding the default
     * {@link ResponseEntityExceptionHandler} behaviour so the resulting 400 Bad Request carries this
     * application's problem+json shape (including the {@code schemaValidationErrors} and {@code errors}
     * members) rather than the framework default.
     *
     * @param ex The {@link MethodArgumentNotValidException} to be handled
     * @param headers the headers to be written to the response
     * @param status the selected response status (400 Bad Request)
     * @param request the current request
     * @return A {@link ResponseEntity} containing the problem+json error information and a 400 Bad Request status.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        BindingResult bindingResult = ex.getBindingResult();
        ProblemDetail detail = ProblemDetail.forStatus(status);
        detail.setDetail(ERROR_INVALID_REQUEST);
        if (bindingResult.hasErrors()) {
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
            logger.debug("Validation error at {}: {}",
                request.getDescription(false),
                bindingResult.getFieldErrors());
            detail.setProperty("schemaValidationErrors", schemaValidationErrors);
            List<String> invalidFields = bindingResult.getFieldErrors().stream()
                .map(FieldError::getField)
                .distinct()
                .toList();
            detail.setProperty("errors", invalidFields);
        }
        return this.handleExceptionInternal(ex, detail, headers, status, request);
    }

    /**
     * Central rendering hook for every exception handled by {@link ResponseEntityExceptionHandler}
     * (unreadable bodies, missing parameters, type mismatches, unsupported media types, and so on).
     * It normalises the body into this application's problem+json shape, guaranteeing the {@code type},
     * {@code title}, {@code status} and {@code detail} members are present while preserving any
     * problem detail already prepared by a more specific handler.
     *
     * @param ex the exception being handled
     * @param body the body prepared by the framework (a {@link ProblemDetail} for RFC 7807 exceptions)
     * @param headers the headers to be written to the response
     * @param statusCode the selected response status
     * @param request the current request
     * @return A {@link ResponseEntity} whose body is a fully populated {@link ProblemDetail}.
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body,
            HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        HttpStatus status = HttpStatus.valueOf(statusCode.value());
        ProblemDetail detail = (body instanceof ProblemDetail problemDetail)
            ? problemDetail
            : ProblemDetail.forStatus(status);
        String fallbackDetail = status.is5xxServerError() ? ERROR_UNEXPECTED : ERROR_INVALID_REQUEST;
        this.shapeProblem(detail, ex, requestUrl(request), fallbackDetail);
        if (status.is4xxClientError()) {
            logger.debug("Request rejected ({}) at {}: {}", status.value(), request.getDescription(false), ex.getMessage());
        }
        return super.handleExceptionInternal(ex, detail, headers, statusCode, request);
    }

    /**
     * Handles {@link InvalidOwnerFieldsException} thrown when an owner payload is missing or blank in
     * one or more required fields. Responds with a 400 Bad Request whose {@code errors} array lists the
     * name of each offending field.
     *
     * @param e The {@link InvalidOwnerFieldsException} to be handled
     * @param request {@link HttpServletRequest} object referring to the current request.
     * @return A {@link ResponseEntity} containing the error information and a 400 Bad Request status.
     */
    @ExceptionHandler(InvalidOwnerFieldsException.class)
    @ResponseBody
    public ResponseEntity<ProblemDetail> handleInvalidOwnerFieldsException(InvalidOwnerFieldsException e, HttpServletRequest request) {
        logger.debug("Invalid owner fields at {} {}: {}",
            request.getMethod(),
            request.getRequestURI(),
            e.getFields());
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ProblemDetail detail = this.detailBuild(e, status, requestUrl(request), ERROR_INVALID_REQUEST);
        detail.setProperty("errors", e.getFields());
        return ResponseEntity.status(status).body(detail);
    }

    /**
     * Handles {@link DuplicateOwnerIdentityException} thrown when an owner is created whose derived
     * {@code identityKey} — consolidating the normalized telephone, email and household id — exactly
     * matches an existing owner's. Responds with a 409 Conflict.
     *
     * @param e The {@link DuplicateOwnerIdentityException} to be handled
     * @param request {@link HttpServletRequest} object referring to the current request.
     * @return A {@link ResponseEntity} containing the error information and a 409 Conflict status.
     */
    @ExceptionHandler(DuplicateOwnerIdentityException.class)
    @ResponseBody
    public ResponseEntity<ProblemDetail> handleDuplicateOwnerIdentityException(DuplicateOwnerIdentityException e, HttpServletRequest request) {
        logger.debug("Duplicate owner identity at {} {}: {}",
            request.getMethod(),
            request.getRequestURI(),
            e.getIdentityKey());
        HttpStatus status = HttpStatus.CONFLICT;
        ProblemDetail detail = this.detailBuild(e, status, requestUrl(request), ERROR_DUPLICATE_IDENTITY);
        detail.setProperty("errors", List.of("identityKey"));
        return ResponseEntity.status(status).body(detail);
    }

    /**
     * Handles {@link OwnerCityAtCapacityException} thrown when an owner is created whose city
     * already contains the maximum number of owners (compared case-insensitively with collapsed
     * whitespace). Responds with a 409 Conflict.
     *
     * @param e The {@link OwnerCityAtCapacityException} to be handled
     * @param request {@link HttpServletRequest} object referring to the current request.
     * @return A {@link ResponseEntity} containing the error information and a 409 Conflict status.
     */
    @ExceptionHandler(OwnerCityAtCapacityException.class)
    @ResponseBody
    public ResponseEntity<ProblemDetail> handleOwnerCityAtCapacityException(OwnerCityAtCapacityException e, HttpServletRequest request) {
        logger.debug("Owner city at capacity at {} {}: {}",
            request.getMethod(),
            request.getRequestURI(),
            e.getCity());
        HttpStatus status = HttpStatus.CONFLICT;
        ProblemDetail detail = this.detailBuild(e, status, requestUrl(request), ERROR_CITY_AT_CAPACITY);
        detail.setProperty("errors", List.of("city"));
        return ResponseEntity.status(status).body(detail);
    }

    /**
     * Handles {@link DailyOwnerLimitReachedException} thrown when an owner is created on a day that
     * has already reached the maximum number of owners registered that day (counted by
     * {@code registrationDate}). Responds with a 429 Too Many Requests.
     *
     * @param e The {@link DailyOwnerLimitReachedException} to be handled
     * @param request {@link HttpServletRequest} object referring to the current request.
     * @return A {@link ResponseEntity} containing the error information and a 429 Too Many Requests status.
     */
    @ExceptionHandler(DailyOwnerLimitReachedException.class)
    @ResponseBody
    public ResponseEntity<ProblemDetail> handleDailyOwnerLimitReachedException(DailyOwnerLimitReachedException e, HttpServletRequest request) {
        logger.debug("Daily owner limit reached at {} {}: {}",
            request.getMethod(),
            request.getRequestURI(),
            e.getDate());
        HttpStatus status = HttpStatus.TOO_MANY_REQUESTS;
        ProblemDetail detail = this.detailBuild(e, status, requestUrl(request), ERROR_DAILY_LIMIT_REACHED);
        return ResponseEntity.status(status).body(detail);
    }

}
