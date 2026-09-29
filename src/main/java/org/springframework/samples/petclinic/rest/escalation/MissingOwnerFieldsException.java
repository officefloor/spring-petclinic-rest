package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

/**
 * Thrown by {@code RequireOwnerFields} when a create-owner request is blank in any of the required
 * fields (firstName, lastName, address, city, telephone). Carries the offending field names so
 * {@link MissingOwnerFieldsExceptionHandler} can report each one.
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
