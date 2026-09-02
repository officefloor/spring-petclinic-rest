package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

/**
 * Thrown when a create-owner request omits or blanks a required field.
 * Handled by {@link MissingOwnerFieldsExceptionHandler}, which responds 400 with the field names.
 */
public class MissingOwnerFieldsException extends Exception {

    private final List<String> fields;

    public MissingOwnerFieldsException(List<String> fields) {
        super("Missing required fields: " + fields);
        this.fields = fields;
    }

    public List<String> getFields() {
        return fields;
    }
}
