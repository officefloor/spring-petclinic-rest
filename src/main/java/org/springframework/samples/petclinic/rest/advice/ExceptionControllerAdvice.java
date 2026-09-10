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
        BindingResult bindingResult = e.getBindingResult();
        ProblemDetail detail = this.detailBuild(e, status, request.getRequestURL(), ERROR_INVALID_REQUEST);
        if (bindingResult.hasErrors()) {
            logger.debug("Validation error at {} {}: {}",
                request.getMethod(),
                request.getRequestURI(),
                bindingResult.getFieldErrors());
            detail.setProperty("schemaValidationErrors", schemaValidationErrors(bindingResult));
            detail.setProperty("errors", rejectedFieldNames(bindingResult));
        }
        return ResponseEntity.status(status).body(detail);
    }

    /**
     * Handles {@link MissingOwnerFieldsException}, thrown when a request to create or update an owner omits or leaves
     * blank one or more required fields. Reports the offending field names in the {@code errors} array of a
     * {@code 400 Bad Request} response.
     *
     * @param e The {@link MissingOwnerFieldsException} to be handled
     * @param request {@link HttpServletRequest} object referring to the current request.
     * @return A {@link ResponseEntity} containing the error information and a 400 Bad Request status.
     */
    @ExceptionHandler(MissingOwnerFieldsException.class)
    @ResponseBody
    public ResponseEntity<ProblemDetail> handleMissingOwnerFieldsException(MissingOwnerFieldsException e, HttpServletRequest request) {
        logger.debug("Missing or blank required fields at {} {}: {}",
            request.getMethod(),
            request.getRequestURI(),
            e.getFields());
        return errorResponse(e, HttpStatus.BAD_REQUEST, request, ERROR_INVALID_REQUEST, e.getFields());
    }

    /**
     * Handles {@link InvalidTelephoneException}, thrown when an owner's telephone number cannot be converted into a
     * valid E.164 number (8 to 15 digits after the leading {@code '+'}). Returns a {@code 400 Bad Request} reporting the
     * {@code telephone} field.
     *
     * @param e The {@link InvalidTelephoneException} to be handled
     * @param request {@link HttpServletRequest} object referring to the current request.
     * @return A {@link ResponseEntity} containing the error information and a 400 Bad Request status.
     */
    @ExceptionHandler(InvalidTelephoneException.class)
    @ResponseBody
    public ResponseEntity<ProblemDetail> handleInvalidTelephoneException(InvalidTelephoneException e, HttpServletRequest request) {
        logger.debug("Invalid telephone at {} {}: {}",
            request.getMethod(),
            request.getRequestURI(),
            e.getMessage());
        return errorResponse(e, HttpStatus.BAD_REQUEST, request, ERROR_INVALID_REQUEST, List.of("telephone"));
    }

    /**
     * Handles {@link DuplicateTelephoneException}, thrown when a request to create an owner carries a normalized
     * telephone number that is already used by another owner. Returns a {@code 409 Conflict} reporting the
     * {@code telephone} field.
     *
     * @param e The {@link DuplicateTelephoneException} to be handled
     * @param request {@link HttpServletRequest} object referring to the current request.
     * @return A {@link ResponseEntity} containing the error information and a 409 Conflict status.
     */
    @ExceptionHandler(DuplicateTelephoneException.class)
    @ResponseBody
    public ResponseEntity<ProblemDetail> handleDuplicateTelephoneException(DuplicateTelephoneException e, HttpServletRequest request) {
        logger.debug("Duplicate telephone at {} {}: {}",
            request.getMethod(),
            request.getRequestURI(),
            e.getMessage());
        return errorResponse(e, HttpStatus.CONFLICT, request, e.getMessage(), List.of("telephone"));
    }

    /**
     * Builds the response body shared by the handlers that report a single business-rule violation: a
     * {@link ProblemDetail} for the given status carrying {@code detail} as its detail message and the offending field
     * names in its {@code errors} property.
     *
     * @param e the exception being handled, used to title the {@link ProblemDetail}
     * @param status the HTTP status to return
     * @param request {@link HttpServletRequest} object referring to the current request.
     * @param detail the human-readable detail message
     * @param errors the names of the fields the violation is reported against
     * @return a {@link ResponseEntity} carrying the problem detail and the given status
     */
    private ResponseEntity<ProblemDetail> errorResponse(Exception e, HttpStatus status, HttpServletRequest request,
                                                        String detail, List<String> errors) {
        ProblemDetail problemDetail = this.detailBuild(e, status, request.getRequestURL(), detail);
        problemDetail.setProperty("errors", errors);
        return ResponseEntity.status(status).body(problemDetail);
    }

    /**
     * Collects the distinct field names carried by the given {@link BindingResult}, in binding order, so they can be
     * reported in the {@code errors} array of a validation error response.
     *
     * @param bindingResult the binding result carrying the field errors
     * @return the names of the rejected fields, without duplicates
     */
    private List<String> rejectedFieldNames(BindingResult bindingResult) {
        return bindingResult.getFieldErrors().stream()
            .map(FieldError::getField)
            .distinct()
            .toList();
    }

    /**
     * Maps every field error in the given {@link BindingResult} to a {@link ValidationMessageDto} describing the
     * rejected field, its value and the reason it was rejected.
     *
     * @param bindingResult the binding result carrying the field errors
     * @return one {@link ValidationMessageDto} per field error, in binding order
     */
    private List<ValidationMessageDto> schemaValidationErrors(BindingResult bindingResult) {
        return bindingResult.getFieldErrors().stream()
            .map(this::toValidationMessage)
            .toList();
    }

    /**
     * Builds a {@link ValidationMessageDto} for a single {@link FieldError}, carrying the field name, rejected value
     * and default message both as a human-readable message and as individual properties.
     *
     * @param fieldError the rejected field
     * @return the validation message describing it
     */
    private ValidationMessageDto toValidationMessage(FieldError fieldError) {
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
    }

}
