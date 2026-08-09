package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

/**
 * Thrown when a create-owner request is missing or blank in any required field
 * (firstName, lastName, address, city or telephone). Handled globally by
 * {@link OwnerFieldsRequiredExceptionHandler}, which responds 400 with a JSON body
 * whose {@code errors} array lists the name of each offending field.
 */
public class OwnerFieldsRequiredException extends Exception {

    private final List<String> errors;

    public OwnerFieldsRequiredException(List<String> errors) {
        super("Missing or blank required owner fields: " + errors);
        this.errors = List.copyOf(errors);
    }

    public List<String> getErrors() {
        return this.errors;
    }
}
