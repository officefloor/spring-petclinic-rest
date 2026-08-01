package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Emits a WARN to the dedicated {@code AUDIT} logger when a newly created owner's
 * telephone shares its area code (the first three digits) with five or more existing
 * owners, flagging a possible bulk signup. Runs after the owner has been saved, so the
 * new owner is excluded from the count by its id.
 */
public class WarnBulkAreaCodeSignup {

    /** Threshold of existing owners sharing an area code that triggers the warning. */
    private static final int BULK_THRESHOLD = 5;

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String areaCode = areaCode(owner.getTelephone());
        if (areaCode == null) {
            return;
        }
        int shared = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (isSameOwner(existing, owner)) {
                continue;
            }
            if (areaCode.equals(areaCode(existing.getTelephone()))) {
                shared++;
            }
        }
        if (shared >= BULK_THRESHOLD) {
            AUDIT.warn("Possible bulk signup: new owner id={} shares area code {} with {} existing owners",
                    owner.getId(), areaCode, shared);
        }
    }

    /**
     * Returns the first three digits of the normalised telephone, or {@code null} when the
     * telephone is absent or has fewer than three digits.
     */
    private static String areaCode(String telephone) {
        String digits = TelephoneNormalizer.digitsOnly(telephone);
        if (digits == null || digits.length() < 3) {
            return null;
        }
        return digits.substring(0, 3);
    }

    private static boolean isSameOwner(Owner existing, Owner candidate) {
        return existing.getId() != null && existing.getId().equals(candidate.getId());
    }
}
