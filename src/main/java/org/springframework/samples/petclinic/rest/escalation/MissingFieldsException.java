package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

/**
 * Thrown when a create/update request omits or blanks a required owner field.
 * Handled by {@link MissingFieldsExceptionHandler}, which responds 400 with a body
 * whose {@code errors} array lists the offending field names.
 */
public class MissingFieldsException extends Exception {

    private final List<String> fields;

    public MissingFieldsException(List<String> fields) {
        super("Missing or blank required fields: " + fields);
        this.fields = List.copyOf(fields);
    }

    public List<String> getFields() {
        return this.fields;
    }
}
