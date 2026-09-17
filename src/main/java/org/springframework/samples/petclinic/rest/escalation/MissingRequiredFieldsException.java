package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

/**
 * Thrown when a create/update request omits or blanks a required field. Carries the names of the
 * offending fields so {@link MissingRequiredFieldsExceptionHandler} can report each one. Handled as a
 * 400, distinct from bean-validation's {@code MethodArgumentNotValidException}.
 */
public class MissingRequiredFieldsException extends Exception {

    private final List<String> fields;

    public MissingRequiredFieldsException(List<String> fields) {
        super("Missing or blank required fields: " + fields);
        this.fields = List.copyOf(fields);
    }

    public List<String> getFields() {
        return fields;
    }
}
