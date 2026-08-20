package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

/**
 * Error body for a rejected create-owner request: the names of the required fields that
 * were missing or blank, serialized as {@code {"errors": [...]}}.
 */
public class OwnerValidationErrors {

    private final List<String> errors;

    public OwnerValidationErrors(List<String> errors) {
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}
