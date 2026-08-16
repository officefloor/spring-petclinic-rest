package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by the create-owner pipeline when the normalized (lower-cased) email is already
 * used by another owner.
 *
 * <p>Carries the normalized email so {@link OwnerEmailDuplicateExceptionHandler} can respond
 * 409 explaining why the create was rejected.
 */
public class OwnerEmailDuplicateException extends Exception {

    private final String email;

    public OwnerEmailDuplicateException(String email) {
        super("Email '" + email + "' is already used by another owner");
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
