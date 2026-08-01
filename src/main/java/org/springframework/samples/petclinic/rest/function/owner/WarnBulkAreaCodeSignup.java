package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags a possible bulk signup: when the newly created owner's telephone shares
 * its area code (the first three digits) with five or more existing owners, a
 * WARN is emitted to the dedicated {@code AUDIT} logger.
 */
public class WarnBulkAreaCodeSignup {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    private static final int AREA_CODE_LENGTH = 3;

    private static final long BULK_THRESHOLD = 5;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String areaCode = areaCode(owner.getTelephone());
        if (areaCode == null) {
            return;
        }
        Integer newOwnerId = owner.getId();
        long existingSharing = ownerRepository.findAll().stream()
                .filter(o -> newOwnerId == null || !newOwnerId.equals(o.getId()))
                .filter(o -> areaCode.equals(areaCode(o.getTelephone())))
                .count();
        if (existingSharing >= BULK_THRESHOLD) {
            AUDIT.warn("Possible bulk signup: area code {} shared by {} existing owners; new owner id={}",
                    areaCode, existingSharing, newOwnerId);
        }
    }

    private static String areaCode(String telephone) {
        if (telephone == null || telephone.length() < AREA_CODE_LENGTH) {
            return null;
        }
        return telephone.substring(0, AREA_CODE_LENGTH);
    }
}
