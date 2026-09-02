package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

/**
 * Thrown by {@code RequireOwnerFields} when a create request is missing or blank in one of the
 * mandatory owner fields. Handled by {@link MissingOwnerFieldsExceptionHandler}, which responds
 * 400 with the offending field names.
 */
public class MissingOwnerFieldsException extends Exception {

    private final List<String> fields;

    public MissingOwnerFieldsException(List<String> fields) {
        super("Missing or blank owner fields: " + fields);
        this.fields = fields;
    }

    public List<String> getFields() {
        return fields;
    }
}
