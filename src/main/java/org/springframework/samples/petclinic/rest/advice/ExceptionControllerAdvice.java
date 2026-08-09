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
    private static final String ERROR_DUPLICATE_TELEPHONE = "An owner with the given telephone already exists";
    private static final String ERROR_DUPLICATE_EMAIL = "An owner with the given email already exists";
    private static final String ERROR_DUPLICATE_HOUSEHOLD = "An owner with the given last name and address already exists";
    private static final String ERROR_DUPLICATE_IDENTITY = "An owner with the given identity already exists";
    private static final String ERROR_CITY_AT_CAPACITY = "The owner's city already contains the maximum number of owners";
    private static final String ERROR_DAILY_LIMIT_REACHED = "The maximum number of owners for today has already been reached";

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
            List<String> missingFields = bindingResult.getFieldErrors().stream()
                .filter(fieldError -> {
                    Object rejected = fieldError.getRejectedValue();
                    return rejected == null || (rejected instanceof String value && value.isBlank());
                })
                .map(fieldError -> fieldError.getField())
                .distinct()
                .toList();
            detail.setProperty("errors", missingFields);
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
     * Handles {@link MissingOwnerFieldsException} raised when an owner create/update request omits
     * or leaves blank one or more mandatory fields that Bean Validation does not otherwise reject.
     * Returns a 400 Bad Request whose {@code errors} array lists the name of each missing field.
     *
     * @param e The {@link MissingOwnerFieldsException} to be handled
     * @param request {@link HttpServletRequest} object referring to the current request.
     * @return A {@link ResponseEntity} containing the error information and a 400 Bad Request status.
     */
    @ExceptionHandler(MissingOwnerFieldsException.class)
    @ResponseBody
    public ResponseEntity<ProblemDetail> handleMissingOwnerFieldsException(MissingOwnerFieldsException e, HttpServletRequest request) {
        logger.debug("Missing owner fields at {} {}: {}",
            request.getMethod(),
            request.getRequestURI(),
            e.getMissingFields());
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ProblemDetail detail = this.detailBuild(e, status, request.getRequestURL(), ERROR_INVALID_REQUEST);
        detail.setProperty("errors", e.getMissingFields());
        return ResponseEntity.status(status).body(detail);
    }

    /**
     * Handles {@link InvalidOwnerFieldsException} raised when an owner create/update request supplies
     * a mandatory field whose value violates a business rule that Bean Validation does not otherwise
     * reject (for example a telephone that is not exactly ten digits after non-digit characters are
     * stripped). Returns a 400 Bad Request whose {@code errors} array lists the name of each field.
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
            e.getInvalidFields());
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ProblemDetail detail = this.detailBuild(e, status, request.getRequestURL(), ERROR_INVALID_REQUEST);
        detail.setProperty("errors", e.getInvalidFields());
        return ResponseEntity.status(status).body(detail);
    }

    /**
     * Handles {@link DuplicateOwnerTelephoneException} raised when an owner create request supplies a
     * telephone whose normalized form is already used by another owner. Returns a 409 Conflict.
     *
     * @param e The {@link DuplicateOwnerTelephoneException} to be handled
     * @param request {@link HttpServletRequest} object referring to the current request.
     * @return A {@link ResponseEntity} containing the error information and a 409 Conflict status.
     */
    @ExceptionHandler(DuplicateOwnerTelephoneException.class)
    @ResponseBody
    public ResponseEntity<ProblemDetail> handleDuplicateOwnerTelephoneException(DuplicateOwnerTelephoneException e, HttpServletRequest request) {
        logger.debug("Duplicate owner telephone at {} {}: {}",
            request.getMethod(),
            request.getRequestURI(),
            e.getTelephone());
        HttpStatus status = HttpStatus.CONFLICT;
        ProblemDetail detail = this.detailBuild(e, status, request.getRequestURL(), ERROR_DUPLICATE_TELEPHONE);
        detail.setProperty("errors", List.of("telephone"));
        return ResponseEntity.status(status).body(detail);
    }

    /**
     * Handles {@link DuplicateOwnerEmailException} raised when an owner create request supplies an
     * email whose lower-cased form is already used by another owner. Returns a 409 Conflict.
     *
     * @param e The {@link DuplicateOwnerEmailException} to be handled
     * @param request {@link HttpServletRequest} object referring to the current request.
     * @return A {@link ResponseEntity} containing the error information and a 409 Conflict status.
     */
    @ExceptionHandler(DuplicateOwnerEmailException.class)
    @ResponseBody
    public ResponseEntity<ProblemDetail> handleDuplicateOwnerEmailException(DuplicateOwnerEmailException e, HttpServletRequest request) {
        logger.debug("Duplicate owner email at {} {}: {}",
            request.getMethod(),
            request.getRequestURI(),
            e.getEmail());
        HttpStatus status = HttpStatus.CONFLICT;
        ProblemDetail detail = this.detailBuild(e, status, request.getRequestURL(), ERROR_DUPLICATE_EMAIL);
        detail.setProperty("errors", List.of("email"));
        return ResponseEntity.status(status).body(detail);
    }

    /**
     * Handles {@link DuplicateOwnerHouseholdException} raised when an owner create request supplies a
     * last name and postcode that already belong to another owner (the last name compared
     * case-insensitively with collapsed whitespace) without acknowledging the shared household.
     * Returns a 409 Conflict.
     *
     * @param e The {@link DuplicateOwnerHouseholdException} to be handled
     * @param request {@link HttpServletRequest} object referring to the current request.
     * @return A {@link ResponseEntity} containing the error information and a 409 Conflict status.
     */
    @ExceptionHandler(DuplicateOwnerHouseholdException.class)
    @ResponseBody
    public ResponseEntity<ProblemDetail> handleDuplicateOwnerHouseholdException(DuplicateOwnerHouseholdException e, HttpServletRequest request) {
        logger.debug("Duplicate owner household at {} {}: {}, {}",
            request.getMethod(),
            request.getRequestURI(),
            e.getLastName(),
            e.getPostcode());
        HttpStatus status = HttpStatus.CONFLICT;
        ProblemDetail detail = this.detailBuild(e, status, request.getRequestURL(), ERROR_DUPLICATE_HOUSEHOLD);
        detail.setProperty("errors", List.of("lastName", "postcode"));
        return ResponseEntity.status(status).body(detail);
    }

    /**
     * Handles {@link DuplicateOwnerIdentityException} raised when an owner create request produces an
     * {@code identityKey} - the derived {@code normalizedTelephone|email|householdId} triple - that
     * exactly matches an existing owner's. This single key subsumes the former separate telephone,
     * email and household duplicate checks. Returns a 409 Conflict.
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
        ProblemDetail detail = this.detailBuild(e, status, request.getRequestURL(), ERROR_DUPLICATE_IDENTITY);
        detail.setProperty("errors", List.of("identityKey"));
        return ResponseEntity.status(status).body(detail);
    }

    /**
     * Handles {@link OwnerCityCapacityExceededException} raised when an owner create request names a
     * city that already contains the maximum number of owners (compared case-insensitively with
     * collapsed whitespace). Returns a 409 Conflict.
     *
     * @param e The {@link OwnerCityCapacityExceededException} to be handled
     * @param request {@link HttpServletRequest} object referring to the current request.
     * @return A {@link ResponseEntity} containing the error information and a 409 Conflict status.
     */
    @ExceptionHandler(OwnerCityCapacityExceededException.class)
    @ResponseBody
    public ResponseEntity<ProblemDetail> handleOwnerCityCapacityExceededException(OwnerCityCapacityExceededException e, HttpServletRequest request) {
        logger.debug("Owner city at capacity at {} {}: {}",
            request.getMethod(),
            request.getRequestURI(),
            e.getCity());
        HttpStatus status = HttpStatus.CONFLICT;
        ProblemDetail detail = this.detailBuild(e, status, request.getRequestURL(), ERROR_CITY_AT_CAPACITY);
        detail.setProperty("errors", List.of("city"));
        return ResponseEntity.status(status).body(detail);
    }

    /**
     * Handles {@link OwnerDailyLimitExceededException} raised when an owner create request would
     * exceed the maximum number of owners that may be registered today (grouped by
     * {@code registrationDate}). Returns a 429 Too Many Requests.
     *
     * @param e The {@link OwnerDailyLimitExceededException} to be handled
     * @param request {@link HttpServletRequest} object referring to the current request.
     * @return A {@link ResponseEntity} containing the error information and a 429 Too Many Requests status.
     */
    @ExceptionHandler(OwnerDailyLimitExceededException.class)
    @ResponseBody
    public ResponseEntity<ProblemDetail> handleOwnerDailyLimitExceededException(OwnerDailyLimitExceededException e, HttpServletRequest request) {
        logger.debug("Owner daily limit reached at {} {}: {}",
            request.getMethod(),
            request.getRequestURI(),
            e.getDate());
        HttpStatus status = HttpStatus.TOO_MANY_REQUESTS;
        ProblemDetail detail = this.detailBuild(e, status, request.getRequestURL(), ERROR_DAILY_LIMIT_REACHED);
        detail.setProperty("errors", List.of("registrationDate"));
        return ResponseEntity.status(status).body(detail);
    }

}
