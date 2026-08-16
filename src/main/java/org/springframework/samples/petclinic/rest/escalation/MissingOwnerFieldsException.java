package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

/**
 * Thrown by {@link org.springframework.samples.petclinic.rest.function.owner.ValidateOwner}
 * when the owner body is missing or blank in any required field.
 * Handled globally by {@link MissingOwnerFieldsExceptionHandler}, which responds 400 with a
 * JSON body whose {@code errors} array lists the name of each missing field.
 */
public class MissingOwnerFieldsException extends Exception {

    private final List<String> missingFields;

    public MissingOwnerFieldsException(List<String> missingFields) {
        super("Missing or blank owner fields: " + String.join(", ", missingFields));
        this.missingFields = List.copyOf(missingFields);
    }

    public List<String> getMissingFields() {
        return this.missingFields;
    }
}
