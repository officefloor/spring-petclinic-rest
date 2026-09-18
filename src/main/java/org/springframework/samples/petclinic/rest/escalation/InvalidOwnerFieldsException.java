package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

/**
 * Thrown by the create-owner pipeline when the request body fails field validation
 * (a required field is missing/blank, or a present field breaks its format rule).
 * Carries the offending field names so {@link InvalidOwnerFieldsExceptionHandler}
 * can report them. Handled as 400 Bad Request.
 */
public class InvalidOwnerFieldsException extends Exception {

    private final List<String> fields;

    public InvalidOwnerFieldsException(List<String> fields) {
        super("Invalid or missing owner fields: " + fields);
        this.fields = List.copyOf(fields);
    }

    public List<String> getFields() {
        return this.fields;
    }
}
