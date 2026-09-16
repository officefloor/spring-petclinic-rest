package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

/**
 * Thrown when a create-owner request omits or blanks a required field.
 * Handled by {@link MissingOwnerFieldsExceptionHandler}, which responds 400 with an
 * {@code errors} array naming each missing field.
 */
public class MissingOwnerFieldsException extends Exception {

    private final List<String> fields;

    public MissingOwnerFieldsException(List<String> fields) {
        super("Owner is missing required fields: " + String.join(", ", fields));
        this.fields = List.copyOf(fields);
    }

    /** The names of the required fields that were missing or blank. */
    public List<String> getFields() {
        return fields;
    }
}
