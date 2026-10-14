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
import org.springframework.samples.petclinic.rest.validation.CityCapacityExceededException;
import org.springframework.samples.petclinic.rest.validation.DailyOwnerLimitExceededException;
import org.springframework.samples.petclinic.rest.validation.DuplicateHouseholdException;
import org.springframework.samples.petclinic.rest.validation.DuplicateIdentityException;
import org.springframework.samples.petclinic.rest.validation.InvalidEmailException;
import org.springframework.samples.petclinic.rest.validation.InvalidPostcodeException;
import org.springframework.samples.petclinic.rest.validation.InvalidRegistrationDateException;
import org.springframework.samples.petclinic.rest.validation.InvalidTelephoneException;
import org.springframework.samples.petclinic.rest.validation.MissingRequiredFieldsException;
import org.springframework.samples.petclinic.rest.validation.RequiredOwnerFields;
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
            if (bindingResult.getTarget() instanceof OwnerFieldsDto owner) {
                List<String> missingFields = RequiredOwnerFields.missingFields(owner);
                if (!missingFields.isEmpty()) {
                    detail.setProperty("errors", missingFields);
                }
            }
            return ResponseEntity.status(status).body(detail);
        }
        return ResponseEntity.status(status).body(detail);
    }

    /**
     * Handles {@link MissingRequiredFieldsException} thrown when required fields are missing or blank.
     *
     * @param e The {@link MissingRequiredFieldsException} to be handled
     * @param request {@link HttpServletRequest} object referring to the current request.
     * @return A {@link ResponseEntity} whose {@code errors} property lists the missing fields, with a 400 Bad Request status.
     */
    @ExceptionHandler(MissingRequiredFieldsException.class)
    @ResponseBody
    public ResponseEntity<ProblemDetail> handleMissingRequiredFieldsException(MissingRequiredFieldsException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ProblemDetail detail = this.detailBuild(e, status, request.getRequestURL(), ERROR_INVALID_REQUEST);
        detail.setProperty("errors", e.getFields());
        return ResponseEntity.status(status).body(detail);
    }

    /**
     * Handles {@link InvalidTelephoneException} thrown when an owner's telephone number is invalid.
     *
     * @param e The {@link InvalidTelephoneException} to be handled
     * @param request {@link HttpServletRequest} object referring to the current request.
     * @return A {@link ResponseEntity} describing the rejected telephone number, with a 400 Bad Request status.
     */
    @ExceptionHandler(InvalidTelephoneException.class)
    @ResponseBody
    public ResponseEntity<ProblemDetail> handleInvalidTelephoneException(InvalidTelephoneException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ProblemDetail detail = this.detailBuild(e, status, request.getRequestURL(), ERROR_INVALID_REQUEST);
        String rejectedValue = Objects.toString(e.getRejectedValue(), "null");
        ValidationMessageDto message = new ValidationMessageDto(
            "Field 'telephone' %s (rejected value: %s)".formatted(e.getMessage(), rejectedValue))
            .putAdditionalProperty("field", "telephone")
            .putAdditionalProperty("rejectedValue", rejectedValue)
            .putAdditionalProperty("defaultMessage", e.getMessage());
        detail.setProperty("schemaValidationErrors", List.of(message));
        return ResponseEntity.status(status).body(detail);
    }

    /**
     * Handles {@link InvalidPostcodeException} thrown when an owner's postcode is invalid for the owner's city.
     *
     * @param e The {@link InvalidPostcodeException} to be handled
     * @param request {@link HttpServletRequest} object referring to the current request.
     * @return A {@link ResponseEntity} describing the rejected postcode, with a 400 Bad Request status.
     */
    @ExceptionHandler(InvalidPostcodeException.class)
    @ResponseBody
    public ResponseEntity<ProblemDetail> handleInvalidPostcodeException(InvalidPostcodeException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ProblemDetail detail = this.detailBuild(e, status, request.getRequestURL(), ERROR_INVALID_REQUEST);
        String rejectedValue = Objects.toString(e.getRejectedValue(), "null");
        ValidationMessageDto message = new ValidationMessageDto(
            "Field 'postcode' %s (rejected value: %s)".formatted(e.getMessage(), rejectedValue))
            .putAdditionalProperty("field", "postcode")
            .putAdditionalProperty("rejectedValue", rejectedValue)
            .putAdditionalProperty("defaultMessage", e.getMessage());
        detail.setProperty("schemaValidationErrors", List.of(message));
        return ResponseEntity.status(status).body(detail);
    }

    /**
     * Handles {@link InvalidEmailException} thrown when an owner's email address is at a disposable domain.
     *
     * @param e The {@link InvalidEmailException} to be handled
     * @param request {@link HttpServletRequest} object referring to the current request.
     * @return A {@link ResponseEntity} describing the rejected email address, with a 400 Bad Request status.
     */
    @ExceptionHandler(InvalidEmailException.class)
    @ResponseBody
    public ResponseEntity<ProblemDetail> handleInvalidEmailException(InvalidEmailException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ProblemDetail detail = this.detailBuild(e, status, request.getRequestURL(), ERROR_INVALID_REQUEST);
        String rejectedValue = Objects.toString(e.getRejectedValue(), "null");
        ValidationMessageDto message = new ValidationMessageDto(
            "Field 'email' %s (rejected value: %s)".formatted(e.getMessage(), rejectedValue))
            .putAdditionalProperty("field", "email")
            .putAdditionalProperty("rejectedValue", rejectedValue)
            .putAdditionalProperty("defaultMessage", e.getMessage());
        detail.setProperty("schemaValidationErrors", List.of(message));
        return ResponseEntity.status(status).body(detail);
    }

    /**
     * Handles {@link InvalidRegistrationDateException} thrown when an owner's supplied registration date is invalid.
     *
     * @param e The {@link InvalidRegistrationDateException} to be handled
     * @param request {@link HttpServletRequest} object referring to the current request.
     * @return A {@link ResponseEntity} describing the rejected registration date, with a 400 Bad Request status.
     */
    @ExceptionHandler(InvalidRegistrationDateException.class)
    @ResponseBody
    public ResponseEntity<ProblemDetail> handleInvalidRegistrationDateException(InvalidRegistrationDateException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ProblemDetail detail = this.detailBuild(e, status, request.getRequestURL(), ERROR_INVALID_REQUEST);
        String rejectedValue = Objects.toString(e.getRejectedValue(), "null");
        ValidationMessageDto message = new ValidationMessageDto(
            "Field 'registrationDate' %s (rejected value: %s)".formatted(e.getMessage(), rejectedValue))
            .putAdditionalProperty("field", "registrationDate")
            .putAdditionalProperty("rejectedValue", rejectedValue)
            .putAdditionalProperty("defaultMessage", e.getMessage());
        detail.setProperty("schemaValidationErrors", List.of(message));
        return ResponseEntity.status(status).body(detail);
    }

    /**
     * Handles {@link DuplicateIdentityException} thrown when an owner with the same identity key already exists.
     *
     * @param e The {@link DuplicateIdentityException} to be handled
     * @param request {@link HttpServletRequest} object referring to the current request.
     * @return A {@link ResponseEntity} describing the conflicting identity key, with a 409 Conflict status.
     */
    @ExceptionHandler(DuplicateIdentityException.class)
    @ResponseBody
    public ResponseEntity<ProblemDetail> handleDuplicateIdentityException(DuplicateIdentityException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        ProblemDetail detail = this.detailBuild(e, status, request.getRequestURL(), e.getMessage());
        detail.setProperty("identityKey", e.getIdentityKey());
        return ResponseEntity.status(status).body(detail);
    }

    /**
     * Handles {@link DuplicateHouseholdException} thrown when an owner of the same household already exists
     * and the new owner does not declare that it shares the household.
     *
     * @param e The {@link DuplicateHouseholdException} to be handled
     * @param request {@link HttpServletRequest} object referring to the current request.
     * @return A {@link ResponseEntity} describing the conflicting household, with a 409 Conflict status.
     */
    @ExceptionHandler(DuplicateHouseholdException.class)
    @ResponseBody
    public ResponseEntity<ProblemDetail> handleDuplicateHouseholdException(DuplicateHouseholdException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        ProblemDetail detail = this.detailBuild(e, status, request.getRequestURL(), e.getMessage());
        detail.setProperty("householdId", e.getHouseholdId());
        return ResponseEntity.status(status).body(detail);
    }

    /**
     * Handles {@link CityCapacityExceededException} thrown when the owner's city is already at capacity.
     *
     * @param e The {@link CityCapacityExceededException} to be handled
     * @param request {@link HttpServletRequest} object referring to the current request.
     * @return A {@link ResponseEntity} describing the full city, with a 409 Conflict status.
     */
    @ExceptionHandler(CityCapacityExceededException.class)
    @ResponseBody
    public ResponseEntity<ProblemDetail> handleCityCapacityExceededException(CityCapacityExceededException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        ProblemDetail detail = this.detailBuild(e, status, request.getRequestURL(), e.getMessage());
        detail.setProperty("city", e.getCity());
        detail.setProperty("capacity", e.getCapacity());
        return ResponseEntity.status(status).body(detail);
    }

    /**
     * Handles {@link DailyOwnerLimitExceededException} thrown when the daily owner creation limit has been reached.
     *
     * @param e The {@link DailyOwnerLimitExceededException} to be handled
     * @param request {@link HttpServletRequest} object referring to the current request.
     * @return A {@link ResponseEntity} describing the exceeded limit, with a 429 Too Many Requests status.
     */
    @ExceptionHandler(DailyOwnerLimitExceededException.class)
    @ResponseBody
    public ResponseEntity<ProblemDetail> handleDailyOwnerLimitExceededException(DailyOwnerLimitExceededException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.TOO_MANY_REQUESTS;
        ProblemDetail detail = this.detailBuild(e, status, request.getRequestURL(), e.getMessage());
        detail.setProperty("limit", e.getLimit());
        return ResponseEntity.status(status).body(detail);
    }

}
