package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

/**
 * Thrown by the create-owner pipeline when one or more required owner fields are missing
 * or blank. Handled globally by {@link MissingFieldsExceptionHandler}, which responds 400
 * with a JSON body whose {@code errors} array lists the offending field names.
 */
public class MissingFieldsException extends Exception {

    private final List<String> fields;

    public MissingFieldsException(List<String> fields) {
        super("Missing or blank required fields: " + String.join(", ", fields));
        this.fields = List.copyOf(fields);
    }

    public List<String> getFields() {
        return this.fields;
    }
}
