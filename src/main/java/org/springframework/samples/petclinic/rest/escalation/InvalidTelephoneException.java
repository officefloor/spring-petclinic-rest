package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create request's telephone cannot be converted to a valid E.164 number (8 to 15
 * digits after the '+'). Carries the offending value so {@link InvalidTelephoneExceptionHandler} can
 * report it. Handled as a 400, distinct from bean-validation's {@code MethodArgumentNotValidException}.
 */
public class InvalidTelephoneException extends Exception {

    private final String telephone;

    public InvalidTelephoneException(String telephone) {
        super("Telephone cannot be converted to a valid E.164 number: " + telephone);
        this.telephone = telephone;
    }

    public String getTelephone() {
        return telephone;
    }
}
