package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

/**
 * Thrown when a create request omits or blanks required fields. Handled by
 * {@link MissingFieldsExceptionHandler}, which responds 400 with the offending field names.
 */
public class MissingFieldsException extends Exception {

    private final List<String> fields;

    public MissingFieldsException(List<String> fields) {
        super("Missing required fields: " + fields);
        this.fields = fields;
    }

    public List<String> getFields() {
        return fields;
    }
}
