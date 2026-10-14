package org.springframework.samples.petclinic.rest.validation;

import java.util.List;

/**
 * Thrown when a request body is missing (or has blank values for) required fields.
 */
public class MissingRequiredFieldsException extends RuntimeException {

    private final List<String> fields;

    public MissingRequiredFieldsException(List<String> fields) {
        super("Missing required fields: " + fields);
        this.fields = List.copyOf(fields);
    }

    public List<String> getFields() {
        return fields;
    }
}
