package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

/**
 * Thrown by {@link org.springframework.samples.petclinic.rest.function.owner.BuildOwner}
 * when a required owner field is missing or blank (whitespace-only). Handled by
 * {@link MissingOwnerFieldsExceptionHandler}, which responds 400 with an {@code errors}
 * array naming each offending field.
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
