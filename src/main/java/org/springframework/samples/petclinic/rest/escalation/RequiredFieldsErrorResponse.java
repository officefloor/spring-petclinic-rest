package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

/**
 * Error body for a request rejected because required fields are missing or blank.
 * Serialises to {@code {"errors": ["field", ...]}}.
 */
public class RequiredFieldsErrorResponse {

    private final List<String> errors;

    public RequiredFieldsErrorResponse(List<String> errors) {
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}
