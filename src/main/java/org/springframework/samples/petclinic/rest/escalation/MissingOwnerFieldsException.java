package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

/**
 * Thrown when a create-owner request omits or blanks a required field.
 * Handled by {@link MissingOwnerFieldsExceptionHandler}, which responds 400 with the
 * offending field names in an {@code errors} array.
 */
public class MissingOwnerFieldsException extends Exception {

    private final List<String> missingFields;

    public MissingOwnerFieldsException(List<String> missingFields) {
        super("Missing or blank required owner fields: " + String.join(", ", missingFields));
        this.missingFields = List.copyOf(missingFields);
    }

    public List<String> getMissingFields() {
        return this.missingFields;
    }
}
