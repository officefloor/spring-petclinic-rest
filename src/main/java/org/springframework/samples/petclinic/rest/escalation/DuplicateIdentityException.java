package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@link org.springframework.samples.petclinic.rest.function.owner.EnsureUniqueIdentity}
 * when an owner being created belongs to a household (same {@code householdId}, i.e. same last name
 * and postcode) that already has a member and the request did not declare it a household member.
 * Handled by {@link DuplicateIdentityExceptionHandler}, which responds 409.
 */
public class DuplicateIdentityException extends Exception {

    public DuplicateIdentityException(String message) {
        super(message);
    }
}
