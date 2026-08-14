package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

/**
 * Thrown by {@link org.springframework.samples.petclinic.rest.function.owner.ValidateOwnerFields}
 * when a create-owner request is missing or blank in a required field. Handled globally by
 * {@link OwnerFieldsValidationExceptionHandler}, which responds 400 with an {@code errors} array
 * naming each offending field.
 */
public class OwnerFieldsValidationException extends Exception {

    private final List<String> errors;

    public OwnerFieldsValidationException(List<String> errors) {
        super("Missing or blank required owner fields: " + errors);
        this.errors = List.copyOf(errors);
    }

    public List<String> getErrors() {
        return errors;
    }
}
