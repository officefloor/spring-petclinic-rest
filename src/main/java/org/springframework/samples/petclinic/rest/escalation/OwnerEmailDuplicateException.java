package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@link org.springframework.samples.petclinic.rest.function.owner.CheckOwnerEmailUnique}
 * when the lower-cased owner email being created already belongs to another owner. Handled by
 * {@link OwnerEmailDuplicateExceptionHandler}, which responds 409.
 */
public class OwnerEmailDuplicateException extends Exception {

    private final String email;

    public OwnerEmailDuplicateException(String email) {
        super("Email already in use by another owner: " + email);
        this.email = email;
    }

    public String getEmail() {
        return this.email;
    }
}
