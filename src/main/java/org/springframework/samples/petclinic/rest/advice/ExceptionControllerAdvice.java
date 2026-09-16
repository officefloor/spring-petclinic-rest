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
import org.springframework.samples.petclinic.rest.validation.CityOwnerCapacityExceededException;
import org.springframework.samples.petclinic.rest.validation.DailyRegistrationLimitExceededException;
import org.springframework.samples.petclinic.rest.validation.DisposableEmailDomainException;
import org.springframework.samples.petclinic.rest.validation.DuplicateHouseholdException;
import org.springframework.samples.petclinic.rest.validation.FutureRegistrationDateException;
import org.springframework.samples.petclinic.rest.validation.InvalidEmailException;
import org.springframework.samples.petclinic.rest.validation.InvalidPostcodeException;
import org.springframework.samples.petclinic.rest.validation.InvalidTelephoneException;
import org.springframework.samples.petclinic.rest.validation.MissingOwnerFieldsException;
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
        BindingErrorsResponse errors = new BindingErrorsResponse();
        BindingResult bindingResult = e.getBindingResult();
        ProblemDetail detail = this.detailBuild(e, status, request.getRequestURL(), ERROR_INVALID_REQUEST);
        List<String> fieldNames = bindingResult.getFieldErrors().stream()
            .map(FieldError::getField)
            .distinct()
            .toList();
        detail.setProperty("errors", fieldNames);
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
     * Handles {@link MissingOwnerFieldsException} raised when an owner is created or updated with
     * required fields that are missing or blank. Returns a 400 Bad Request whose body carries an
     * {@code errors} array listing the name of each offending field.
     *
     * @param e The {@link MissingOwnerFieldsException} to be handled
     * @param request {@link HttpServletRequest} object referring to the current request.
     * @return A {@link ResponseEntity} containing the error information and a 400 Bad Request status.
     */
    @ExceptionHandler(MissingOwnerFieldsException.class)
    @ResponseBody
    public ResponseEntity<ProblemDetail> handleMissingOwnerFieldsException(MissingOwnerFieldsException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ProblemDetail detail = this.detailBuild(e, status, request.getRequestURL(), ERROR_INVALID_REQUEST);
        detail.setProperty("errors", e.getMissingFields());
        logger.debug("Missing or blank required fields at {} {}: {}",
            request.getMethod(),
            request.getRequestURI(),
            e.getMissingFields());
        return ResponseEntity.status(status).body(detail);
    }

    /**
     * Handles {@link InvalidTelephoneException} raised when an owner's telephone cannot be converted
     * to a valid E.164 number ({@code '+'} followed by 8 to 15 digits). Returns a 400 Bad Request
     * whose body carries an {@code errors} array naming the {@code telephone} field.
     *
     * @param e The {@link InvalidTelephoneException} to be handled
     * @param request {@link HttpServletRequest} object referring to the current request.
     * @return A {@link ResponseEntity} containing the error information and a 400 Bad Request status.
     */
    @ExceptionHandler(InvalidTelephoneException.class)
    @ResponseBody
    public ResponseEntity<ProblemDetail> handleInvalidTelephoneException(InvalidTelephoneException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ProblemDetail detail = this.detailBuild(e, status, request.getRequestURL(), ERROR_INVALID_REQUEST);
        detail.setProperty("errors", List.of("telephone"));
        logger.debug("Invalid telephone at {} {}: {}",
            request.getMethod(),
            request.getRequestURI(),
            e.getMessage());
        return ResponseEntity.status(status).body(detail);
    }

    /**
     * Handles {@link InvalidEmailException} raised when an owner's submitted email is present but
     * is not a syntactically valid address. Returns a 400 Bad Request whose body carries an
     * {@code errors} array naming the {@code email} field.
     *
     * @param e The {@link InvalidEmailException} to be handled
     * @param request {@link HttpServletRequest} object referring to the current request.
     * @return A {@link ResponseEntity} containing the error information and a 400 Bad Request status.
     */
    @ExceptionHandler(InvalidEmailException.class)
    @ResponseBody
    public ResponseEntity<ProblemDetail> handleInvalidEmailException(InvalidEmailException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ProblemDetail detail = this.detailBuild(e, status, request.getRequestURL(), ERROR_INVALID_REQUEST);
        detail.setProperty("errors", List.of("email"));
        logger.debug("Invalid email at {} {}: {}",
            request.getMethod(),
            request.getRequestURI(),
            e.getMessage());
        return ResponseEntity.status(status).body(detail);
    }

    /**
     * Handles {@link DisposableEmailDomainException} raised when an owner's submitted email is
     * syntactically valid but its domain is on the disposable-domain blocklist. Returns a 400 Bad
     * Request whose body carries an {@code errors} array naming the {@code email} field.
     *
     * @param e The {@link DisposableEmailDomainException} to be handled
     * @param request {@link HttpServletRequest} object referring to the current request.
     * @return A {@link ResponseEntity} containing the error information and a 400 Bad Request status.
     */
    @ExceptionHandler(DisposableEmailDomainException.class)
    @ResponseBody
    public ResponseEntity<ProblemDetail> handleDisposableEmailDomainException(DisposableEmailDomainException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ProblemDetail detail = this.detailBuild(e, status, request.getRequestURL(), ERROR_INVALID_REQUEST);
        detail.setProperty("errors", List.of("email"));
        logger.debug("Disposable email domain at {} {}: {}",
            request.getMethod(),
            request.getRequestURI(),
            e.getMessage());
        return ResponseEntity.status(status).body(detail);
    }

    /**
     * Handles {@link InvalidPostcodeException} raised when an owner's supplied postcode is out of
     * range for its city's region. Returns a 400 Bad Request whose body carries an {@code errors}
     * array naming the {@code postcode} field.
     *
     * @param e The {@link InvalidPostcodeException} to be handled
     * @param request {@link HttpServletRequest} object referring to the current request.
     * @return A {@link ResponseEntity} containing the error information and a 400 Bad Request status.
     */
    @ExceptionHandler(InvalidPostcodeException.class)
    @ResponseBody
    public ResponseEntity<ProblemDetail> handleInvalidPostcodeException(InvalidPostcodeException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ProblemDetail detail = this.detailBuild(e, status, request.getRequestURL(), ERROR_INVALID_REQUEST);
        detail.setProperty("errors", List.of("postcode"));
        logger.debug("Invalid postcode at {} {}: {}",
            request.getMethod(),
            request.getRequestURI(),
            e.getMessage());
        return ResponseEntity.status(status).body(detail);
    }

    /**
     * Handles {@link FutureRegistrationDateException} raised when an owner's supplied registration
     * date is later than the server's current date. Returns a 400 Bad Request whose body carries an
     * {@code errors} array naming the {@code registrationDate} field.
     *
     * @param e The {@link FutureRegistrationDateException} to be handled
     * @param request {@link HttpServletRequest} object referring to the current request.
     * @return A {@link ResponseEntity} containing the error information and a 400 Bad Request status.
     */
    @ExceptionHandler(FutureRegistrationDateException.class)
    @ResponseBody
    public ResponseEntity<ProblemDetail> handleFutureRegistrationDateException(FutureRegistrationDateException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ProblemDetail detail = this.detailBuild(e, status, request.getRequestURL(), ERROR_INVALID_REQUEST);
        detail.setProperty("errors", List.of("registrationDate"));
        logger.debug("Future registration date at {} {}: {}",
            request.getMethod(),
            request.getRequestURI(),
            e.getMessage());
        return ResponseEntity.status(status).body(detail);
    }

    /**
     * Handles {@link DuplicateHouseholdException} raised when an owner is created whose household —
     * its (last name, postcode) derived household id — already belongs to another owner, without the
     * owner declaring itself a member via {@code sharesHousehold}. Returns a 409 Conflict whose body
     * carries an {@code errors} array naming the {@code householdId}.
     *
     * @param e The {@link DuplicateHouseholdException} to be handled
     * @param request {@link HttpServletRequest} object referring to the current request.
     * @return A {@link ResponseEntity} containing the error information and a 409 Conflict status.
     */
    @ExceptionHandler(DuplicateHouseholdException.class)
    @ResponseBody
    public ResponseEntity<ProblemDetail> handleDuplicateHouseholdException(DuplicateHouseholdException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        ProblemDetail detail = this.detailBuild(e, status, request.getRequestURL(), ERROR_INVALID_REQUEST);
        detail.setProperty("errors", List.of("householdId"));
        logger.debug("Duplicate owner household at {} {}: {}",
            request.getMethod(),
            request.getRequestURI(),
            e.getMessage());
        return ResponseEntity.status(status).body(detail);
    }

    /**
     * Handles {@link CityOwnerCapacityExceededException} raised when an owner is created in a city
     * that has already reached its maximum number of owners. Returns a 409 Conflict whose body
     * carries an {@code errors} array naming the offending {@code city} field.
     *
     * @param e The {@link CityOwnerCapacityExceededException} to be handled
     * @param request {@link HttpServletRequest} object referring to the current request.
     * @return A {@link ResponseEntity} containing the error information and a 409 Conflict status.
     */
    @ExceptionHandler(CityOwnerCapacityExceededException.class)
    @ResponseBody
    public ResponseEntity<ProblemDetail> handleCityOwnerCapacityExceededException(CityOwnerCapacityExceededException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        ProblemDetail detail = this.detailBuild(e, status, request.getRequestURL(), ERROR_INVALID_REQUEST);
        detail.setProperty("errors", List.of("city"));
        logger.debug("City owner capacity exceeded at {} {}: {}",
            request.getMethod(),
            request.getRequestURI(),
            e.getMessage());
        return ResponseEntity.status(status).body(detail);
    }

    /**
     * Handles {@link DailyRegistrationLimitExceededException} raised when an owner is created after
     * the maximum number of owners allowed for the current day has already been reached. Returns a
     * 429 Too Many Requests.
     *
     * @param e The {@link DailyRegistrationLimitExceededException} to be handled
     * @param request {@link HttpServletRequest} object referring to the current request.
     * @return A {@link ResponseEntity} containing the error information and a 429 Too Many Requests status.
     */
    @ExceptionHandler(DailyRegistrationLimitExceededException.class)
    @ResponseBody
    public ResponseEntity<ProblemDetail> handleDailyRegistrationLimitExceededException(DailyRegistrationLimitExceededException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.TOO_MANY_REQUESTS;
        ProblemDetail detail = this.detailBuild(e, status, request.getRequestURL(), ERROR_INVALID_REQUEST);
        logger.debug("Daily registration limit exceeded at {} {}: {}",
            request.getMethod(),
            request.getRequestURI(),
            e.getMessage());
        return ResponseEntity.status(status).body(detail);
    }

}
