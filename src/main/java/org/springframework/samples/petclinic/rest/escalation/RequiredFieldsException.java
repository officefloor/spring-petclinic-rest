package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

/**
 * Thrown when a create/update request is missing or blank in one or more required
 * fields. Carries the offending field names, which {@link RequiredFieldsExceptionHandler}
 * reports as a 400 with an {@code errors} array.
 */
public class RequiredFieldsException extends Exception {

    private final List<String> errors;

    public RequiredFieldsException(List<String> errors) {
        super("Missing or blank required fields: " + errors);
        this.errors = List.copyOf(errors);
    }

    public List<String> getErrors() {
        return errors;
    }
}
