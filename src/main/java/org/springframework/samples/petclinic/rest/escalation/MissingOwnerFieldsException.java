package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

/**
 * Raised when a create-owner request omits or blanks one of the required fields
 * (firstName, lastName, address, city or telephone). Carries the offending field
 * names so the handler can list each in the 400 response body's {@code errors} array.
 */
public class MissingOwnerFieldsException extends Exception {

    private final List<String> fields;

    public MissingOwnerFieldsException(List<String> fields) {
        super("Missing or blank required owner fields: " + String.join(", ", fields));
        this.fields = List.copyOf(fields);
    }

    public List<String> getFields() {
        return fields;
    }
}
