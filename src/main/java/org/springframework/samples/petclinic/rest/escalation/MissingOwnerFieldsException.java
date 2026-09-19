package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

/**
 * Thrown when a create-owner request is missing or blank in one or more required fields.
 * Handled by {@link MissingOwnerFieldsExceptionHandler}, which responds 400 with the
 * offending field names.
 */
public class MissingOwnerFieldsException extends Exception {

    private final List<String> missingFields;

    public MissingOwnerFieldsException(List<String> missingFields) {
        super("Missing required owner fields: " + missingFields);
        this.missingFields = List.copyOf(missingFields);
    }

    public List<String> getMissingFields() {
        return this.missingFields;
    }
}
