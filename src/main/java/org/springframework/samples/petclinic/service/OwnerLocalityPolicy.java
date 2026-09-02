package org.springframework.samples.petclinic.service;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Business rule: an owner's {@code locality} is the REGION component of its {@code memberId} (the
 * leading letters before the fiscal-year digits in {@code <REGION><FY><HASH8><CHK>}), or
 * {@code UNKNOWN} when no member id has been assigned yet. Kept as a small, self-contained unit so the
 * rule can be applied from the read flow without adding complexity to the mapper, controller, or service.
 */
public final class OwnerLocalityPolicy {

    private OwnerLocalityPolicy() {
    }

    /**
     * Derive the {@code locality} for the given owner from its member id's region component.
     *
     * @param owner the owner whose locality to derive
     * @return the REGION portion of the member id, or {@code "UNKNOWN"} when it is not set
     */
    public static String locality(Owner owner) {
        String id = owner.getMemberId();
        if (id == null) {
            return "UNKNOWN";
        }
        if (id.startsWith("V2")) {
            id = id.substring(2);
        }
        int end = 0;
        while (end < id.length() && Character.isLetter(id.charAt(end))) {
            end++;
        }
        return id.substring(0, end);
    }
}
