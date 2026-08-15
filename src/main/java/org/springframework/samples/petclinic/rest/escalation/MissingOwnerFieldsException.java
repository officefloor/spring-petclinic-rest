package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

/**
 * Thrown when a create-owner request is missing or blank in one or more required fields
 * (firstName, lastName, address, city or telephone). Handled globally by
 * {@link MissingOwnerFieldsExceptionHandler}, which responds 400 with an 'errors' array
 * naming each offending field.
 */
public class MissingOwnerFieldsException extends Exception {

    private final List<String> fields;

    public MissingOwnerFieldsException(List<String> fields) {
        super("Missing or blank required owner fields: " + fields);
        this.fields = List.copyOf(fields);
    }

    public List<String> getFields() {
        return fields;
    }
}
