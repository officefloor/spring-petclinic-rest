package org.springframework.samples.petclinic.service;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Business rule: an owner's {@code locality} is the REGION component of its region-and-hash
 * {@code customerCode} (the {@code <REGION>} in {@code <REGION>-<HASH8>}), or {@code UNKNOWN}
 * when no customer code has been assigned yet. Kept as a small, self-contained unit so the rule
 * can be applied from the read flow without adding complexity to the mapper, controller, or service.
 */
public final class OwnerLocalityPolicy {

    private OwnerLocalityPolicy() {
    }

    /**
     * Derive the {@code locality} for the given owner from its customer code's region component.
     *
     * @param owner the owner whose locality to derive
     * @return the REGION portion of the customer code, or {@code "UNKNOWN"} when it is not set
     */
    public static String locality(Owner owner) {
        String code = owner.getCustomerCode();
        if (code == null) {
            return "UNKNOWN";
        }
        int dash = code.indexOf('-');
        return dash < 0 ? code : code.substring(0, dash);
    }
}
