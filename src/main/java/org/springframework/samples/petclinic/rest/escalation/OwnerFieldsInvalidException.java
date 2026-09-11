package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

/**
 * Thrown when a create/update request omits or blanks a required owner field.
 * Handled by {@link OwnerFieldsInvalidExceptionHandler}, which responds 400 with a JSON
 * body whose {@code errors} array lists the offending field names.
 */
public class OwnerFieldsInvalidException extends Exception {

    private final List<String> errors;

    public OwnerFieldsInvalidException(List<String> errors) {
        super("Invalid owner fields: " + errors);
        this.errors = List.copyOf(errors);
    }

    public List<String> getErrors() {
        return errors;
    }
}
