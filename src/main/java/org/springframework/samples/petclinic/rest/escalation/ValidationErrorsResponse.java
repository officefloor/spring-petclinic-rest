package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

/**
 * Response body carrying the names of the fields that failed validation, serialised
 * as {@code {"errors": ["city", ...]}}.
 */
public class ValidationErrorsResponse {

    private final List<String> errors;

    public ValidationErrorsResponse(List<String> errors) {
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}
