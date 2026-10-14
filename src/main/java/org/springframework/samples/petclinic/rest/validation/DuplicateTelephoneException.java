package org.springframework.samples.petclinic.rest.validation;

/**
 * Thrown when an owner's normalized telephone number is already used by another owner.
 */
public class DuplicateTelephoneException extends RuntimeException {

    private final String telephone;

    public DuplicateTelephoneException(String telephone) {
        super("Telephone number is already used by another owner");
        this.telephone = telephone;
    }

    public String getTelephone() {
        return telephone;
    }
}
