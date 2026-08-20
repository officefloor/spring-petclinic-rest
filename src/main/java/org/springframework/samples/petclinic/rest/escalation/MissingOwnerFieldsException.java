package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

/**
 * Thrown by {@code ValidateOwnerFields} when a create-owner request is missing or blank
 * in one or more required fields. Handled by {@link MissingOwnerFieldsExceptionHandler},
 * which responds 400 with the offending field names in an {@code errors} array.
 */
public class MissingOwnerFieldsException extends Exception {

    private final List<String> fields;

    public MissingOwnerFieldsException(List<String> fields) {
        super("Missing or blank required fields: " + String.join(", ", fields));
        this.fields = List.copyOf(fields);
    }

    public List<String> getFields() {
        return fields;
    }
}
