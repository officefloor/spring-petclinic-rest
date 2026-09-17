package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

/**
 * Thrown when a create-owner request omits or blanks a required field. Carries the names
 * of the offending fields so {@link MissingOwnerFieldsExceptionHandler} can report them.
 */
public class MissingOwnerFieldsException extends Exception {

    private final List<String> fields;

    public MissingOwnerFieldsException(List<String> fields) {
        super("Missing or blank required owner fields: " + fields);
        this.fields = List.copyOf(fields);
    }

    public List<String> getFields() {
        return fields;
    }
}
