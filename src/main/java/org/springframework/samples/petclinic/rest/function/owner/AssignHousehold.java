package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * When the request opts in with {@code sharesHousehold: true}, gives the new owner a stable shared
 * {@code householdId} derived from the household's normalized last name and address, and back-fills
 * the same id onto any existing owner in that household that lacks one. Deterministic derivation means
 * every owner joining the same household is assigned an identical, non-blank identifier. Runs after
 * {@link EnsureUniqueHousehold} and before the owner is saved.
 */
public class AssignHousehold {

    public void service(@Val Owner owner, @Val Boolean sharesHousehold, OwnerRepository ownerRepository) {
        if (!Boolean.TRUE.equals(sharesHousehold)) {
            return;
        }
        String householdId = deriveId(owner.getLastName(), owner.getAddress());
        owner.setHouseholdId(householdId);
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getHouseholdId() == null && sameHousehold(owner, existing)) {
                existing.setHouseholdId(householdId);
                ownerRepository.save(existing);
            }
        }
    }

    private static boolean sameHousehold(Owner a, Owner b) {
        return normalize(a.getLastName()).equals(normalize(b.getLastName()))
                && normalize(a.getAddress()).equals(normalize(b.getAddress()));
    }

    /** Stable {@code HH-<12 hex>} identifier for the household keyed by normalized last name and address. */
    private static String deriveId(String lastName, String address) {
        String key = normalize(lastName) + "|" + normalize(address);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder("HH-");
            for (int i = 0; i < 6; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
