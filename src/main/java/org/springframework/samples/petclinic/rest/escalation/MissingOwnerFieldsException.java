package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

/**
 * Thrown by {@link org.springframework.samples.petclinic.rest.function.owner.RequireOwnerFields}
 * when a create-owner request is missing or blank in any required field. Handled by
 * {@link MissingOwnerFieldsExceptionHandler}, which responds 400 with the offending field names.
 */
public class MissingOwnerFieldsException extends Exception {

    private final List<String> fields;

    public MissingOwnerFieldsException(List<String> fields) {
        super("Missing or blank required fields: " + fields);
        this.fields = fields;
    }

    public List<String> getFields() {
        return fields;
    }
}
