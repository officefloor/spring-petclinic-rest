package org.springframework.samples.petclinic.rest.escalation;

import java.util.List;

/**
 * Thrown by the create-owner pipeline when one or more required owner fields
 * (firstName, lastName, address, city, telephone) are missing or blank.
 *
 * <p>Carries the names of the offending fields so {@link OwnerFieldsMissingExceptionHandler}
 * can respond 400 with an {@code errors} array listing each one.
 */
public class OwnerFieldsMissingException extends Exception {

    private final List<String> errors;

    public OwnerFieldsMissingException(List<String> errors) {
        super("Missing or blank owner fields: " + String.join(", ", errors));
        this.errors = List.copyOf(errors);
    }

    public List<String> getErrors() {
        return errors;
    }
}
