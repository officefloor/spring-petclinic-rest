package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags a possible bulk signup to the dedicated {@code AUDIT} logger. When the newly
 * created owner's telephone area code (its first three digits) is already shared by
 * {@value #BULK_THRESHOLD} or more existing owners, a WARN is emitted in addition to
 * the normal creation audit.
 */
public class AuditBulkAreaCode {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    /** Number of existing owners sharing the area code that triggers the warning. */
    static final int BULK_THRESHOLD = 5;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String areaCode = areaCode(owner.getTelephone());
        if (areaCode == null) {
            return;
        }
        Integer ownerId = owner.getId();
        long sharing = ownerRepository.findAll().stream()
                .filter(existing -> ownerId == null || !ownerId.equals(existing.getId()))
                .map(existing -> areaCode(existing.getTelephone()))
                .filter(areaCode::equals)
                .count();
        if (sharing >= BULK_THRESHOLD) {
            AUDIT.warn("Possible bulk signup: telephone area code {} shared by {} existing owners; new owner id={}",
                    areaCode, sharing, ownerId);
        }
    }

    /**
     * @return the first three digits of the normalized telephone, or {@code null}
     *         when there are fewer than three digits.
     */
    private static String areaCode(String telephone) {
        String normalized = Telephone.normalize(telephone);
        if (normalized == null || normalized.length() < 3) {
            return null;
        }
        return normalized.substring(0, 3);
    }
}
