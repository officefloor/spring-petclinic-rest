package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

/**
 * Thrown by {@code ValidateOwnerFields} when a create-owner request is missing or blank in any
 * required field. Handled globally by {@link MissingOwnerFieldsExceptionHandler}, which responds
 * 400 with a JSON body whose {@code errors} array lists the offending field names.
 */
public class MissingOwnerFieldsException extends Exception {

    private final List<String> fields;

    public MissingOwnerFieldsException(List<String> fields) {
        super("Missing or blank owner fields: " + String.join(", ", fields));
        this.fields = List.copyOf(fields);
    }

    public List<String> getFields() {
        return this.fields;
    }
}
