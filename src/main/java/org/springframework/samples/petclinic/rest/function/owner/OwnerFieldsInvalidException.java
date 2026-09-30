package org.springframework.samples.petclinic.rest.function.owner;

import java.util.List;

/**
 * Thrown by {@link ValidateOwnerFields} when a create request is missing or blank in one or more
 * required owner fields. Handled globally by {@code OwnerFieldsInvalidExceptionHandler}, which
 * responds 400 with a JSON body listing the offending field names.
 */
public class OwnerFieldsInvalidException extends Exception {

    private final List<String> errors;

    public OwnerFieldsInvalidException(List<String> errors) {
        super("Missing or blank owner fields: " + errors);
        this.errors = List.copyOf(errors);
    }

    public List<String> getErrors() {
        return errors;
    }
}
