package org.springframework.samples.petclinic.rest.function.owner;

import java.util.List;

/**
 * Thrown by {@link RequireOwnerFields} when a create-owner request is missing or blank in any of the
 * mandatory fields. Handled globally by {@code MissingOwnerFieldsExceptionHandler}, which responds 400
 * with a JSON body whose {@code errors} array lists the offending field names.
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
