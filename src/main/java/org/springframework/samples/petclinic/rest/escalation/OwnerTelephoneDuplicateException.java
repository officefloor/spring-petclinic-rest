package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@link org.springframework.samples.petclinic.rest.function.owner.CheckOwnerTelephoneUnique}
 * when the normalized owner telephone being created already belongs to another owner. Handled by
 * {@link OwnerTelephoneDuplicateExceptionHandler}, which responds 409.
 */
public class OwnerTelephoneDuplicateException extends Exception {

    private final String telephone;

    public OwnerTelephoneDuplicateException(String telephone) {
        super("Telephone already in use by another owner: " + telephone);
        this.telephone = telephone;
    }

    public String getTelephone() {
        return this.telephone;
    }
}
