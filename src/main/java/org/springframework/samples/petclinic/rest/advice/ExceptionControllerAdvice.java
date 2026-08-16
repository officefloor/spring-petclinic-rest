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
import org.springframework.samples.petclinic.rest.controller.CityAtCapacityException;
import org.springframework.samples.petclinic.rest.controller.DailyOwnerLimitExceededException;
import org.springframework.samples.petclinic.rest.controller.DuplicateEmailException;
import org.springframework.samples.petclinic.rest.controller.DuplicateHouseholdException;
import org.springframework.samples.petclinic.rest.controller.DuplicateIdentityException;
import org.springframework.samples.petclinic.rest.controller.DuplicateTelephoneException;
import org.springframework.samples.petclinic.rest.controller.InvalidEmailException;
import org.springframework.samples.petclinic.rest.controller.InvalidPostcodeException;
import org.springframework.samples.petclinic.rest.controller.RequiredFieldsMissingException;
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
    private static final String ERROR_DUPLICATE_TELEPHONE = "An owner with the given telephone already exists";
    private static final String ERROR_DUPLICATE_EMAIL = "An owner with the given email already exists";
    private static final String ERROR_DUPLICATE_HOUSEHOLD = "An owner with the given last name and address already exists";
    private static final String ERROR_DUPLICATE_IDENTITY = "An owner with the given identity already exists";
    private static final String ERROR_INVALID_EMAIL = "The request contains an invalid email address";
    private static final String ERROR_INVALID_POSTCODE = "The request contains a postcode that is not valid for the owner's city";
    private static final String ERROR_CITY_AT_CAPACITY = "The owner's city already contains the maximum number of owners";
    private static final String ERROR_DAILY_LIMIT = "The maximum number of owners for today has already been reached";

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
        List<String> invalidFields = bindingResult.getFieldErrors().stream()
            .map(FieldError::getField)
            .distinct()
            .toList();
        detail.setProperty("errors", invalidFields);
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
            return ResponseEntity.status(status).body(detail);
        }
        return ResponseEntity.status(status).body(detail);
    }

    /**
     * Handles {@link RequiredFieldsMissingException} raised when a request omits or blanks
     * out mandatory fields. Returns a 400 Bad Request whose body carries an {@code errors}
     * array listing the name of each offending field.
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
        detail.setProperty("errors", e.getFields());
        logger.debug("Missing required fields at {} {}: {}",
            request.getMethod(),
            request.getRequestURI(),
            e.getFields());
        return ResponseEntity.status(status).body(detail);
    }

    /**
     * Handles {@link DuplicateTelephoneException} raised when a create request supplies a
     * normalized telephone already used by another owner. Returns a 409 Conflict whose body
     * carries an {@code errors} array naming the {@code telephone} field.
     *
     * @param e The {@link DuplicateTelephoneException} to be handled
     * @param request {@link HttpServletRequest} object referring to the current request.
     * @return A {@link ResponseEntity} containing the error information and a 409 Conflict status.
     */
    /**
     * Handles {@link InvalidEmailException} raised when a create or update request supplies an
     * {@code email} that is present but not a syntactically valid address. Returns a 400 Bad
     * Request whose body carries an {@code errors} array naming the {@code email} field.
     *
     * @param e The {@link InvalidEmailException} to be handled
     * @param request {@link HttpServletRequest} object referring to the current request.
     * @return A {@link ResponseEntity} containing the error information and a 400 Bad Request status.
     */
    @ExceptionHandler(InvalidEmailException.class)
    @ResponseBody
    public ResponseEntity<ProblemDetail> handleInvalidEmailException(InvalidEmailException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ProblemDetail detail = this.detailBuild(e, status, request.getRequestURL(), ERROR_INVALID_EMAIL);
        detail.setProperty("errors", List.of("email"));
        logger.debug("Invalid email at {} {}: {}",
            request.getMethod(),
            request.getRequestURI(),
            e.getEmail());
        return ResponseEntity.status(status).body(detail);
    }

    /**
     * Handles {@link InvalidPostcodeException} raised when a create request supplies a
     * {@code postcode} that is present but out of range for the owner's city's region. Returns a
     * 400 Bad Request whose body carries an {@code errors} array naming the {@code postcode} field.
     *
     * @param e The {@link InvalidPostcodeException} to be handled
     * @param request {@link HttpServletRequest} object referring to the current request.
     * @return A {@link ResponseEntity} containing the error information and a 400 Bad Request status.
     */
    @ExceptionHandler(InvalidPostcodeException.class)
    @ResponseBody
    public ResponseEntity<ProblemDetail> handleInvalidPostcodeException(InvalidPostcodeException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ProblemDetail detail = this.detailBuild(e, status, request.getRequestURL(), ERROR_INVALID_POSTCODE);
        detail.setProperty("errors", List.of("postcode"));
        logger.debug("Invalid postcode at {} {}: {}",
            request.getMethod(),
            request.getRequestURI(),
            e.getPostcode());
        return ResponseEntity.status(status).body(detail);
    }

    /**
     * Handles {@link DuplicateEmailException} raised when a create request supplies an
     * {@code email} whose lower-cased form is already used by another owner. Returns a 409
     * Conflict whose body carries an {@code errors} array naming the {@code email} field.
     *
     * @param e The {@link DuplicateEmailException} to be handled
     * @param request {@link HttpServletRequest} object referring to the current request.
     * @return A {@link ResponseEntity} containing the error information and a 409 Conflict status.
     */
    @ExceptionHandler(DuplicateEmailException.class)
    @ResponseBody
    public ResponseEntity<ProblemDetail> handleDuplicateEmailException(DuplicateEmailException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        ProblemDetail detail = this.detailBuild(e, status, request.getRequestURL(), ERROR_DUPLICATE_EMAIL);
        detail.setProperty("errors", List.of("email"));
        logger.debug("Duplicate email at {} {}: {}",
            request.getMethod(),
            request.getRequestURI(),
            e.getEmail());
        return ResponseEntity.status(status).body(detail);
    }

    /**
     * Handles {@link DuplicateIdentityException} raised when a create request supplies an owner
     * whose whole derived {@code identityKey} already equals that of an existing owner. This one
     * key consolidates the former telephone, email and household duplicate checks. Returns a 409
     * Conflict whose body carries an {@code errors} array naming the {@code identityKey} field.
     *
     * @param e The {@link DuplicateIdentityException} to be handled
     * @param request {@link HttpServletRequest} object referring to the current request.
     * @return A {@link ResponseEntity} containing the error information and a 409 Conflict status.
     */
    @ExceptionHandler(DuplicateIdentityException.class)
    @ResponseBody
    public ResponseEntity<ProblemDetail> handleDuplicateIdentityException(DuplicateIdentityException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        ProblemDetail detail = this.detailBuild(e, status, request.getRequestURL(), ERROR_DUPLICATE_IDENTITY);
        detail.setProperty("errors", List.of("identityKey"));
        logger.debug("Duplicate identity at {} {}: {}",
            request.getMethod(),
            request.getRequestURI(),
            e.getIdentityKey());
        return ResponseEntity.status(status).body(detail);
    }

    @ExceptionHandler(DuplicateTelephoneException.class)
    @ResponseBody
    public ResponseEntity<ProblemDetail> handleDuplicateTelephoneException(DuplicateTelephoneException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        ProblemDetail detail = this.detailBuild(e, status, request.getRequestURL(), ERROR_DUPLICATE_TELEPHONE);
        detail.setProperty("errors", List.of("telephone"));
        logger.debug("Duplicate telephone at {} {}: {}",
            request.getMethod(),
            request.getRequestURI(),
            e.getTelephone());
        return ResponseEntity.status(status).body(detail);
    }

    /**
     * Handles {@link DuplicateHouseholdException} raised when a create request supplies a
     * last name and address already used by another owner (compared case-insensitively with
     * collapsed whitespace) without opting in via {@code sharesHousehold}. Returns a 409
     * Conflict whose body carries an {@code errors} array naming the {@code lastName} and
     * {@code address} fields.
     *
     * @param e The {@link DuplicateHouseholdException} to be handled
     * @param request {@link HttpServletRequest} object referring to the current request.
     * @return A {@link ResponseEntity} containing the error information and a 409 Conflict status.
     */
    @ExceptionHandler(DuplicateHouseholdException.class)
    @ResponseBody
    public ResponseEntity<ProblemDetail> handleDuplicateHouseholdException(DuplicateHouseholdException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        ProblemDetail detail = this.detailBuild(e, status, request.getRequestURL(), ERROR_DUPLICATE_HOUSEHOLD);
        detail.setProperty("errors", List.of("lastName", "address"));
        logger.debug("Duplicate household at {} {}: {} / {}",
            request.getMethod(),
            request.getRequestURI(),
            e.getLastName(),
            e.getAddress());
        return ResponseEntity.status(status).body(detail);
    }

    /**
     * Handles {@link CityAtCapacityException} raised when a create request supplies a
     * {@code city} that already contains the maximum number of owners (compared
     * case-insensitively). Returns a 409 Conflict whose body carries an {@code errors}
     * array naming the {@code city} field.
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
        logger.debug("City at capacity at {} {}: {}",
            request.getMethod(),
            request.getRequestURI(),
            e.getCity());
        return ResponseEntity.status(status).body(detail);
    }

    /**
     * Handles {@link DailyOwnerLimitExceededException} raised when a create request arrives
     * after the maximum number of owners for the current day (counted by
     * {@code registrationDate}) has already been reached. Returns a 429 Too Many Requests.
     *
     * @param e The {@link DailyOwnerLimitExceededException} to be handled
     * @param request {@link HttpServletRequest} object referring to the current request.
     * @return A {@link ResponseEntity} containing the error information and a 429 Too Many Requests status.
     */
    @ExceptionHandler(DailyOwnerLimitExceededException.class)
    @ResponseBody
    public ResponseEntity<ProblemDetail> handleDailyOwnerLimitExceededException(DailyOwnerLimitExceededException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.TOO_MANY_REQUESTS;
        ProblemDetail detail = this.detailBuild(e, status, request.getRequestURL(), ERROR_DAILY_LIMIT);
        detail.setProperty("errors", List.of("registrationDate"));
        logger.debug("Daily owner limit reached at {} {}: {}",
            request.getMethod(),
            request.getRequestURI(),
            e.getDate());
        return ResponseEntity.status(status).body(detail);
    }

}
