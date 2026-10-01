package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

/**
 * Thrown by {@link org.springframework.samples.petclinic.rest.function.owner.ValidateOwner}
 * when a required owner field is missing or blank. Handled by
 * {@link OwnerFieldsMissingExceptionHandler}, which responds 400 with a JSON body whose
 * {@code errors} array lists the name of each offending field.
 */
public class OwnerFieldsMissingException extends Exception {

    private final List<String> fields;

    public OwnerFieldsMissingException(List<String> fields) {
        super("Missing or blank required owner fields: " + String.join(", ", fields));
        this.fields = List.copyOf(fields);
    }

    public List<String> getFields() {
        return this.fields;
    }
}
