package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

/**
 * Thrown when a create-owner request is missing or blank in one or more required
 * fields (firstName, lastName, address, city, telephone). Handled by
 * {@link MissingOwnerFieldsHandler}, which responds 400 with a JSON body whose
 * {@code errors} array lists the offending field names.
 */
public class MissingOwnerFieldsException extends Exception {

    private final List<String> fields;

    public MissingOwnerFieldsException(List<String> fields) {
        super("Missing or blank required fields: " + fields);
        this.fields = List.copyOf(fields);
    }

    public List<String> getFields() {
        return this.fields;
    }
}
