package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

/**
 * Thrown when creating an owner whose firstName, lastName, address, city or telephone
 * is missing or blank. Handled by {@link MissingOwnerFieldsHandler}, which responds 400
 * with an {@code errors} array naming each offending field.
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
