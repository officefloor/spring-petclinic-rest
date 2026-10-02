package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

/**
 * Thrown by the owner-create pipeline when one or more required owner fields
 * (firstName, lastName, address, city, telephone) are missing or blank.
 *
 * <p>Handled by {@link MissingOwnerFieldsExceptionHandler}, which responds 400 with a
 * JSON body whose {@code errors} array lists the name of each offending field.
 */
public class MissingOwnerFieldsException extends Exception {

    private final List<String> fields;

    public MissingOwnerFieldsException(List<String> fields) {
        super("Missing or blank required owner fields: " + fields);
        this.fields = List.copyOf(fields);
    }

    public List<String> getFields() {
        return this.fields;
    }
}
