package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns a shared {@code householdId} when the request set {@code sharesHousehold} true
 * (published by {@link BuildOwner}). A household is defined by {@link HouseholdKey}.
 *
 * <p>The identifier is derived deterministically from the household key, so every owner in a
 * household computes the same value and it is stable across requests. It is assigned to this
 * (new) owner and also back-filled onto the existing same-household owner(s), so all members
 * return the same {@code householdId}. Runs before {@link RejectDuplicateIdentity} because the
 * {@code householdId} is part of the owner's {@code identityKey}.
 *
 * <p>Does nothing when {@code sharesHousehold} is not true, leaving {@code householdId} absent.
 */
public class AssignHousehold {

    public void service(@Val Owner owner, @Val Boolean sharesHousehold, OwnerRepository ownerRepository) {
        if (!Boolean.TRUE.equals(sharesHousehold)) {
            return;
        }
        HouseholdKey household = HouseholdKey.of(owner);
        String householdId = householdId(household);
        owner.setHouseholdId(householdId);
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getId() != null && existing.getId().equals(owner.getId())) {
                continue;
            }
            if (household.matches(existing) && !householdId.equals(existing.getHouseholdId())) {
                existing.setHouseholdId(householdId);
                ownerRepository.save(existing);
            }
        }
    }

    /** Stable id {@code H-<16 upper-hex>} derived from the household key. */
    private static String householdId(HouseholdKey household) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(household.canonical().getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return "H-" + sb;
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}
