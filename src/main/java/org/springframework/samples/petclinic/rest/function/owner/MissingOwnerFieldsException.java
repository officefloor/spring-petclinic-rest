package org.springframework.samples.petclinic.rest.function.owner;

import java.util.List;

/**
 * Thrown by {@link ValidateOwnerFields} when a create request omits or blanks a required
 * owner field. Carries the names of the offending fields so the handler can list them.
 * Checked so it appears in the function's {@code throws} clause and routes to an escalation.
 */
public class MissingOwnerFieldsException extends Exception {

    private final List<String> fields;

    public MissingOwnerFieldsException(List<String> fields) {
        super("Missing or blank required owner fields: " + String.join(", ", fields));
        this.fields = List.copyOf(fields);
    }

    public List<String> getFields() {
        return fields;
    }
}
