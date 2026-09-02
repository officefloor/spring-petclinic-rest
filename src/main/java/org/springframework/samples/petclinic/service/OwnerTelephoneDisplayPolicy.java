package org.springframework.samples.petclinic.service;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Business rule: an owner's {@code telephoneDisplay} is its stored E.164 {@code telephone}
 * rendered for humans as the country code, a space, then the national digits grouped in threes
 * (e.g. {@code +61412345678} becomes {@code +61 412 345 678}). Kept as a small, self-contained
 * unit so the rule can be applied from the read flow without adding complexity to the mapper or
 * the owner model. A telephone that is absent or not in E.164 form is returned unchanged.
 */
public final class OwnerTelephoneDisplayPolicy {

    private OwnerTelephoneDisplayPolicy() {
    }

    /**
     * Derive the human-readable {@code telephoneDisplay} for the given owner.
     *
     * @param owner the owner whose telephone to format
     * @return the formatted telephone, or the raw value when it is absent or not E.164
     */
    public static String telephoneDisplay(Owner owner) {
        String telephone = owner.getTelephone();
        if (telephone == null || !telephone.startsWith("+")) {
            return telephone;
        }
        String digits = telephone.substring(1);
        int split = digits.startsWith("61") ? 2 : 1;
        String national = digits.substring(split).replaceAll("(\\d{3})(?=\\d)", "$1 ");
        return "+" + digits.substring(0, split) + " " + national;
    }
}
