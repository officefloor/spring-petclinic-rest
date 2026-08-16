package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by the create-owner pipeline when the normalized (stripped to 10 digits) telephone
 * is already used by another owner.
 *
 * <p>Carries the normalized telephone so {@link OwnerTelephoneDuplicateExceptionHandler}
 * can respond 409 explaining why the create was rejected.
 */
public class OwnerTelephoneDuplicateException extends Exception {

    private final String telephone;

    public OwnerTelephoneDuplicateException(String telephone) {
        super("Telephone '" + telephone + "' is already used by another owner");
        this.telephone = telephone;
    }

    public String getTelephone() {
        return telephone;
    }
}
