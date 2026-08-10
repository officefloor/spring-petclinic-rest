package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

/**
 * Thrown by {@code ValidateOwnerFields} when a create-owner request is missing or blank in any
 * required field. Handled by {@link OwnerFieldsInvalidExceptionHandler}, which responds 400 with a
 * body whose {@code errors} array names each offending field.
 */
public class OwnerFieldsInvalidException extends Exception {

    private final List<String> errors;

    public OwnerFieldsInvalidException(List<String> errors) {
        super("Missing or blank required owner fields: " + errors);
        this.errors = List.copyOf(errors);
    }

    public List<String> getErrors() {
        return errors;
    }
}
