package org.springframework.samples.petclinic.rest.function.owner;

import java.util.List;

/**
 * Thrown by {@link ValidateOwnerFields} when an owner create request omits or blanks a
 * required field. Checked so it appears in the {@code throws} clause and is routed to its
 * escalation handler, which responds 400 with the offending field names.
 */
public class MissingOwnerFieldsException extends Exception {

    private final List<String> fields;

    public MissingOwnerFieldsException(List<String> fields) {
        super("Missing required owner fields: " + fields);
        this.fields = List.copyOf(fields);
    }

    public List<String> getFields() {
        return this.fields;
    }
}
