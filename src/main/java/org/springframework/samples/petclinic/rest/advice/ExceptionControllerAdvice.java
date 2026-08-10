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
    private static final String ERROR_DUPLICATE_IDENTITY = "An owner with the given identity already exists";
    private static final String ERROR_DUPLICATE_TELEPHONE = "An owner with the given telephone already exists";
    private static final String ERROR_DUPLICATE_EMAIL = "An owner with the given email already exists";
    private static final String ERROR_DUPLICATE_HOUSEHOLD = "An owner with the given last name and address already exists";
    private static final String ERROR_CITY_AT_CAPACITY = "The owner's city has reached its owner capacity";
    private static final String ERROR_DAILY_OWNER_LIMIT = "The daily owner registration limit has been reached";

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
        BindingErrorsResponse errors = new BindingErrorsResponse();
        BindingResult bindingResult = e.getBindingResult();
        ProblemDetail detail = this.detailBuild(e, status, request.getRequestURL(), ERROR_INVALID_REQUEST);
        if (bindingResult.hasErrors()) {
            errors.addAllErrors(bindingResult);
            List<String> invalidFields = bindingResult.getFieldErrors().stream()
                .map(FieldError::getField)
                .distinct()
                .toList();
            detail.setProperty("errors", invalidFields);
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
            return ResponseEntity.status(status).body(detail);
        }
        return ResponseEntity.status(status).body(detail);
    }

    /**
     * Handles {@link RequiredFieldsMissingException} raised when a request is missing or blank in
     * one or more required fields. Returns a 400 Bad Request whose body's {@code errors} array
     * lists the name of each offending field.
     *
     * @param e The {@link RequiredFieldsMissingException} to be handled
     * @param request {@link HttpServletRequest} object referring to the current request.
     * @return A {@link ResponseEntity} containing the error information and a 400 Bad Request status.
     */
    @ExceptionHandler(RequiredFieldsMissingException.class)
    @ResponseBody
    public ResponseEntity<ProblemDetail> handleRequiredFieldsMissingException(RequiredFieldsMissingException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ProblemDetail detail = this.detailBuild(e, status, request.getRequestURL(), ERROR_INVALID_REQUEST);
        detail.setProperty("errors", e.getMissingFields());
        logger.debug("Missing required fields at {} {}: {}",
            request.getMethod(),
            request.getRequestURI(),
            e.getMissingFields());
        return ResponseEntity.status(status).body(detail);
    }

    /**
     * Handles {@link InvalidFieldsException} raised when a request carries one or more fields
     * whose values are present but invalid. Returns a 400 Bad Request whose body's {@code errors}
     * array lists the name of each offending field.
     *
     * @param e The {@link InvalidFieldsException} to be handled
     * @param request {@link HttpServletRequest} object referring to the current request.
     * @return A {@link ResponseEntity} containing the error information and a 400 Bad Request status.
     */
    @ExceptionHandler(InvalidFieldsException.class)
    @ResponseBody
    public ResponseEntity<ProblemDetail> handleInvalidFieldsException(InvalidFieldsException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ProblemDetail detail = this.detailBuild(e, status, request.getRequestURL(), ERROR_INVALID_REQUEST);
        detail.setProperty("errors", e.getInvalidFields());
        logger.debug("Invalid fields at {} {}: {}",
            request.getMethod(),
            request.getRequestURI(),
            e.getInvalidFields());
        return ResponseEntity.status(status).body(detail);
    }

    /**
     * Handles {@link DuplicateOwnerIdentityException} raised when an owner is created whose derived
     * identity key ({@code normalizedTelephone|email|householdId}) is already used by another owner.
     * Returns a 409 Conflict whose body's {@code errors} array names the offending
     * {@code identityKey} field.
     *
     * @param e The {@link DuplicateOwnerIdentityException} to be handled
     * @param request {@link HttpServletRequest} object referring to the current request.
     * @return A {@link ResponseEntity} containing the error information and a 409 Conflict status.
     */
    @ExceptionHandler(DuplicateOwnerIdentityException.class)
    @ResponseBody
    public ResponseEntity<ProblemDetail> handleDuplicateOwnerIdentityException(DuplicateOwnerIdentityException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        ProblemDetail detail = this.detailBuild(e, status, request.getRequestURL(), ERROR_DUPLICATE_IDENTITY);
        detail.setProperty("errors", List.of("identityKey"));
        logger.debug("Duplicate owner identity at {} {}: {}",
            request.getMethod(),
            request.getRequestURI(),
            e.getIdentityKey());
        return ResponseEntity.status(status).body(detail);
    }

    /**
     * Handles {@link DuplicateOwnerTelephoneException} raised when an owner is created with a
     * normalized telephone that is already used by another owner. Returns a 409 Conflict whose
     * body's {@code errors} array names the offending {@code telephone} field.
     *
     * @param e The {@link DuplicateOwnerTelephoneException} to be handled
     * @param request {@link HttpServletRequest} object referring to the current request.
     * @return A {@link ResponseEntity} containing the error information and a 409 Conflict status.
     */
    @ExceptionHandler(DuplicateOwnerTelephoneException.class)
    @ResponseBody
    public ResponseEntity<ProblemDetail> handleDuplicateOwnerTelephoneException(DuplicateOwnerTelephoneException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        ProblemDetail detail = this.detailBuild(e, status, request.getRequestURL(), ERROR_DUPLICATE_TELEPHONE);
        detail.setProperty("errors", List.of("telephone"));
        logger.debug("Duplicate owner telephone at {} {}: {}",
            request.getMethod(),
            request.getRequestURI(),
            e.getTelephone());
        return ResponseEntity.status(status).body(detail);
    }

    /**
     * Handles {@link DuplicateOwnerEmailException} raised when an owner is created with a
     * lower-cased email that is already used by another owner. Returns a 409 Conflict whose
     * body's {@code errors} array names the offending {@code email} field.
     *
     * @param e The {@link DuplicateOwnerEmailException} to be handled
     * @param request {@link HttpServletRequest} object referring to the current request.
     * @return A {@link ResponseEntity} containing the error information and a 409 Conflict status.
     */
    @ExceptionHandler(DuplicateOwnerEmailException.class)
    @ResponseBody
    public ResponseEntity<ProblemDetail> handleDuplicateOwnerEmailException(DuplicateOwnerEmailException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        ProblemDetail detail = this.detailBuild(e, status, request.getRequestURL(), ERROR_DUPLICATE_EMAIL);
        detail.setProperty("errors", List.of("email"));
        logger.debug("Duplicate owner email at {} {}: {}",
            request.getMethod(),
            request.getRequestURI(),
            e.getEmail());
        return ResponseEntity.status(status).body(detail);
    }

    /**
     * Handles {@link DuplicateOwnerHouseholdException} raised when an owner is created whose last
     * name and address already match another owner (a shared household) without the request opting
     * in via the {@code sharesHousehold} flag. Returns a 409 Conflict whose body's {@code errors}
     * array names the offending {@code lastName} and {@code address} fields.
     *
     * @param e The {@link DuplicateOwnerHouseholdException} to be handled
     * @param request {@link HttpServletRequest} object referring to the current request.
     * @return A {@link ResponseEntity} containing the error information and a 409 Conflict status.
     */
    @ExceptionHandler(DuplicateOwnerHouseholdException.class)
    @ResponseBody
    public ResponseEntity<ProblemDetail> handleDuplicateOwnerHouseholdException(DuplicateOwnerHouseholdException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        ProblemDetail detail = this.detailBuild(e, status, request.getRequestURL(), ERROR_DUPLICATE_HOUSEHOLD);
        detail.setProperty("errors", List.of("lastName", "address"));
        logger.debug("Duplicate owner household at {} {}: lastName={} address={}",
            request.getMethod(),
            request.getRequestURI(),
            e.getLastName(),
            e.getAddress());
        return ResponseEntity.status(status).body(detail);
    }

    /**
     * Handles {@link CityAtCapacityException} raised when an owner is created in a city that
     * already contains the maximum permitted number of owners. Returns a 409 Conflict whose
     * body's {@code errors} array names the offending {@code city} field.
     *
     * @param e The {@link CityAtCapacityException} to be handled
     * @param request {@link HttpServletRequest} object referring to the current request.
     * @return A {@link ResponseEntity} containing the error information and a 409 Conflict status.
     */
    @ExceptionHandler(CityAtCapacityException.class)
    @ResponseBody
    public ResponseEntity<ProblemDetail> handleCityAtCapacityException(CityAtCapacityException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        ProblemDetail detail = this.detailBuild(e, status, request.getRequestURL(), ERROR_CITY_AT_CAPACITY);
        detail.setProperty("errors", List.of("city"));
        logger.debug("City at capacity at {} {}: city={}",
            request.getMethod(),
            request.getRequestURI(),
            e.getCity());
        return ResponseEntity.status(status).body(detail);
    }

    /**
     * Handles {@link DailyOwnerLimitException} raised when an owner is created on a day that has
     * already reached the maximum permitted number of owner registrations. Returns a 429 Too Many
     * Requests, since the request exceeds the allowed rate of owner creations for the current day.
     *
     * @param e The {@link DailyOwnerLimitException} to be handled
     * @param request {@link HttpServletRequest} object referring to the current request.
     * @return A {@link ResponseEntity} containing the error information and a 429 Too Many Requests status.
     */
    @ExceptionHandler(DailyOwnerLimitException.class)
    @ResponseBody
    public ResponseEntity<ProblemDetail> handleDailyOwnerLimitException(DailyOwnerLimitException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.TOO_MANY_REQUESTS;
        ProblemDetail detail = this.detailBuild(e, status, request.getRequestURL(), ERROR_DAILY_OWNER_LIMIT);
        logger.debug("Daily owner limit reached at {} {}: date={}",
            request.getMethod(),
            request.getRequestURI(),
            e.getDate());
        return ResponseEntity.status(status).body(detail);
    }

}
