package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create request's telephone does not contain exactly ten digits after every non-digit
 * character is stripped. Carries the offending value so {@link InvalidTelephoneExceptionHandler} can
 * report it. Handled as a 400, distinct from bean-validation's {@code MethodArgumentNotValidException}.
 */
public class InvalidTelephoneException extends Exception {

    private final String telephone;

    public InvalidTelephoneException(String telephone) {
        super("Telephone must contain exactly 10 digits: " + telephone);
        this.telephone = telephone;
    }

    public String getTelephone() {
        return telephone;
    }
}
