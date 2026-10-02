package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

/**
 * Thrown when a request to create an owner omits or blanks one or more required fields.
 * Carries the names of the offending fields so {@link MissingOwnerFieldsExceptionHandler}
 * can report them in the response's {@code errors} array. Checked so it appears in the
 * function's {@code throws} clause and is routed as an escalation.
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
