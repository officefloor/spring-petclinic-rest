package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

/**
 * Thrown when a create-owner request omits or blanks a required field.
 * Carries the names of the offending fields; handled by
 * {@link MissingOwnerFieldsExceptionHandler}, which responds 400 with an {@code errors} array.
 */
public class MissingOwnerFieldsException extends Exception {

    private final List<String> fields;

    public MissingOwnerFieldsException(List<String> fields) {
        super("Missing or blank required owner fields: " + String.join(", ", fields));
        this.fields = List.copyOf(fields);
    }

    public List<String> getFields() {
        return this.fields;
    }
}
