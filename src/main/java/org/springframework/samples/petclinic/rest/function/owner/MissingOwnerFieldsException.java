package org.springframework.samples.petclinic.rest.function.owner;

import java.util.List;

/**
 * Thrown by {@link ValidateOwnerFields} when a create-owner request is missing or blank in
 * any required field. Handled by {@code MissingOwnerFieldsExceptionHandler}, which responds
 * 400 with an {@code errors} array naming each offending field.
 */
public class MissingOwnerFieldsException extends Exception {

    private final List<String> fields;

    public MissingOwnerFieldsException(List<String> fields) {
        super("Missing or blank owner fields: " + fields);
        this.fields = List.copyOf(fields);
    }

    public List<String> getFields() {
        return this.fields;
    }
}
