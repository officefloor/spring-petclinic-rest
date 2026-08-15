package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

/**
 * Raised when a create-owner request is missing (absent) or blank (whitespace only) in one
 * or more of the required fields. Checked so it appears in a {@code throws} clause and can be
 * routed to its escalation handler, which responds 400 with the offending field names.
 */
public class MissingOwnerFieldsException extends Exception {

    private final List<String> fields;

    public MissingOwnerFieldsException(List<String> fields) {
        super("Missing or blank required owner fields: " + String.join(", ", fields));
        this.fields = List.copyOf(fields);
    }

    /** The names of the required fields that were missing or blank, in declaration order. */
    public List<String> getFields() {
        return this.fields;
    }
}
