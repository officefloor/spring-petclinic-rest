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
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.ValidationMessageDto;
import org.springframework.samples.petclinic.rest.validation.CityOwnerLimitException;
import org.springframework.samples.petclinic.rest.validation.DailyOwnerLimitException;
import org.springframework.samples.petclinic.rest.validation.DuplicateIdentityException;
import org.springframework.samples.petclinic.rest.validation.FutureRegistrationDateException;
import org.springframework.samples.petclinic.rest.validation.HouseholdDuplicateException;
import org.springframework.samples.petclinic.rest.validation.InvalidEmailException;
import org.springframework.samples.petclinic.rest.validation.InvalidPostcodeException;
import org.springframework.samples.petclinic.rest.validation.InvalidTelephoneException;
import org.springframework.samples.petclinic.rest.validation.MissingOwnerFieldsException;
import org.springframework.samples.petclinic.rest.validation.OwnerFieldsValidator;
import org.springframework.samples.petclinic.rest.validation.ValidationErrorsResponse;
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

    private final OwnerFieldsValidator ownerFieldsValidator;

    public ExceptionControllerAdvice(OwnerFieldsValidator ownerFieldsValidator) {
        this.ownerFieldsValidator = ownerFieldsValidator;
    }

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
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        BindingErrorsResponse errors = new BindingErrorsResponse();
        BindingResult bindingResult = e.getBindingResult();
        if (bindingResult.getTarget() instanceof OwnerFieldsDto ownerFields) {
            List<String> missingFields = ownerFieldsValidator.findMissingOrBlankFields(ownerFields);
            if (!missingFields.isEmpty()) {
                return ResponseEntity.status(status).body(new ValidationErrorsResponse(missingFields));
            }
        }
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
            return ResponseEntity.status(status).body(detail);
        }
        return ResponseEntity.status(status).body(detail);
    }

    /**
     * Handles {@link MissingOwnerFieldsException} raised when an owner is created with required
     * fields that are missing or blank. Returns a 400 Bad Request whose {@code errors} array lists
     * the name of each offending field.
     *
     * @param e The {@link MissingOwnerFieldsException} to be handled
     * @return A {@link ResponseEntity} containing the offending field names and a 400 Bad Request status.
     */
    @ExceptionHandler(MissingOwnerFieldsException.class)
    @ResponseBody
    public ResponseEntity<ValidationErrorsResponse> handleMissingOwnerFieldsException(MissingOwnerFieldsException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ValidationErrorsResponse(e.getFields()));
    }

    /**
     * Handles {@link InvalidTelephoneException} raised when an owner's telephone does not
     * reduce to exactly ten digits. Returns a 400 Bad Request whose {@code errors} array
     * names the offending field.
     *
     * @param e The {@link InvalidTelephoneException} to be handled
     * @return A {@link ResponseEntity} containing the offending field name and a 400 Bad Request status.
     */
    @ExceptionHandler(InvalidTelephoneException.class)
    @ResponseBody
    public ResponseEntity<ValidationErrorsResponse> handleInvalidTelephoneException(InvalidTelephoneException e) {
        logger.debug("Invalid telephone: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ValidationErrorsResponse(List.of(InvalidTelephoneException.FIELD)));
    }

    /**
     * Handles {@link InvalidEmailException} raised when an owner's email is present but is
     * not a syntactically valid address. Returns a 400 Bad Request whose {@code errors}
     * array names the offending field.
     *
     * @param e The {@link InvalidEmailException} to be handled
     * @return A {@link ResponseEntity} containing the offending field name and a 400 Bad Request status.
     */
    @ExceptionHandler(InvalidEmailException.class)
    @ResponseBody
    public ResponseEntity<ValidationErrorsResponse> handleInvalidEmailException(InvalidEmailException e) {
        logger.debug("Invalid email: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ValidationErrorsResponse(List.of(InvalidEmailException.FIELD)));
    }

    /**
     * Handles {@link InvalidPostcodeException} raised when an owner's postcode is present but is
     * not four digits, or is out of range for the region of the owner's city. Returns a 400 Bad
     * Request whose {@code errors} array names the offending field.
     *
     * @param e The {@link InvalidPostcodeException} to be handled
     * @return A {@link ResponseEntity} containing the offending field name and a 400 Bad Request status.
     */
    @ExceptionHandler(InvalidPostcodeException.class)
    @ResponseBody
    public ResponseEntity<ValidationErrorsResponse> handleInvalidPostcodeException(InvalidPostcodeException e) {
        logger.debug("Invalid postcode: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ValidationErrorsResponse(List.of(InvalidPostcodeException.FIELD)));
    }

    /**
     * Handles {@link FutureRegistrationDateException} raised when an owner is created with a
     * supplied registration date later than the server's current date. Returns a 400 Bad Request
     * whose {@code errors} array names the offending field.
     *
     * @param e The {@link FutureRegistrationDateException} to be handled
     * @return A {@link ResponseEntity} containing the offending field name and a 400 Bad Request status.
     */
    @ExceptionHandler(FutureRegistrationDateException.class)
    @ResponseBody
    public ResponseEntity<ValidationErrorsResponse> handleFutureRegistrationDateException(FutureRegistrationDateException e) {
        logger.debug("Future registration date: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ValidationErrorsResponse(List.of(FutureRegistrationDateException.FIELD)));
    }

    /**
     * Handles {@link DuplicateIdentityException} raised when an owner is created whose whole
     * derived identity key already belongs to another owner. This is the single duplicate rule
     * that replaces the former separate telephone, email and household checks. Returns a 409
     * Conflict whose {@code errors} array names the offending field.
     *
     * @param e The {@link DuplicateIdentityException} to be handled
     * @return A {@link ResponseEntity} containing the offending field name and a 409 Conflict status.
     */
    @ExceptionHandler(DuplicateIdentityException.class)
    @ResponseBody
    public ResponseEntity<ValidationErrorsResponse> handleDuplicateIdentityException(DuplicateIdentityException e) {
        logger.debug("Duplicate identity: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new ValidationErrorsResponse(List.of(DuplicateIdentityException.FIELD)));
    }

    /**
     * Handles {@link HouseholdDuplicateException} raised when an owner is created into a household
     * (same last name and postcode) that already exists without declaring {@code sharesHousehold}.
     * Returns a 409 Conflict whose {@code errors} array names the offending field.
     *
     * @param e The {@link HouseholdDuplicateException} to be handled
     * @return A {@link ResponseEntity} containing the offending field name and a 409 Conflict status.
     */
    @ExceptionHandler(HouseholdDuplicateException.class)
    @ResponseBody
    public ResponseEntity<ValidationErrorsResponse> handleHouseholdDuplicateException(HouseholdDuplicateException e) {
        logger.debug("Household duplicate: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new ValidationErrorsResponse(List.of(HouseholdDuplicateException.FIELD)));
    }

    /**
     * Handles {@link CityOwnerLimitException} raised when an owner is created in a city that
     * already contains the maximum allowed number of owners. Returns a 409 Conflict whose
     * {@code errors} array names the offending field.
     *
     * @param e The {@link CityOwnerLimitException} to be handled
     * @return A {@link ResponseEntity} containing the offending field name and a 409 Conflict status.
     */
    @ExceptionHandler(CityOwnerLimitException.class)
    @ResponseBody
    public ResponseEntity<ValidationErrorsResponse> handleCityOwnerLimitException(CityOwnerLimitException e) {
        logger.debug("City owner limit reached: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new ValidationErrorsResponse(List.of(CityOwnerLimitException.FIELD)));
    }

    /**
     * Handles {@link DailyOwnerLimitException} raised when an owner is created on a day that has
     * already reached the maximum allowed number of owner registrations. Returns a 429 Too Many
     * Requests whose {@code errors} array names the offending field.
     *
     * @param e The {@link DailyOwnerLimitException} to be handled
     * @return A {@link ResponseEntity} containing the offending field name and a 429 status.
     */
    @ExceptionHandler(DailyOwnerLimitException.class)
    @ResponseBody
    public ResponseEntity<ValidationErrorsResponse> handleDailyOwnerLimitException(DailyOwnerLimitException e) {
        logger.debug("Daily owner limit reached: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
            .body(new ValidationErrorsResponse(List.of(DailyOwnerLimitException.FIELD)));
    }

}
