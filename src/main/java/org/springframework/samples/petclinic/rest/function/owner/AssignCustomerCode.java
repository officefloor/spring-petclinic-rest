package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code customerCode}, formatted {@code <REGION>-<HASH8>} where REGION is the
 * region derived from the owner's postcode (see {@link CustomerCodeRegion}) and HASH8 is the first
 * eight upper-case hex characters of SHA-256 over the concatenation of the owner's normalized (E.164)
 * telephone and last name (e.g. {@code NSW-1A2B3C4D}). There is no sequence number, so the code is
 * stable for a given telephone/last-name/region and does not depend on how many owners already exist.
 * Every value built from the customerCode — the membership number, its check digit, the create audit
 * line and the derived locality — follows this region-and-hash identity.
 *
 * <p>Should the computed code collide with an already-assigned {@code customerCode} (a hash
 * collision, or the same region and hash on distinct owners), it is de-duplicated by appending
 * {@code -<n>} with the smallest {@code n} of 2 or more that makes it unique among existing owners.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String code = CustomerCodeRegion.of(owner) + "-" + hash8(owner);
        owner.setCustomerCode(deduplicate(code, ownerRepository));
    }

    /**
     * Returns {@code code} unchanged when no existing owner already uses it, otherwise
     * {@code code-<n>} with the smallest {@code n >= 2} that is not already in use.
     */
    private static String deduplicate(String code, OwnerRepository ownerRepository) {
        Set<String> existing = new HashSet<>();
        for (Owner other : ownerRepository.findAll()) {
            if (other.getCustomerCode() != null) {
                existing.add(other.getCustomerCode());
            }
        }
        if (!existing.contains(code)) {
            return code;
        }
        for (int n = 2; ; n++) {
            String candidate = code + "-" + n;
            if (!existing.contains(candidate)) {
                return candidate;
            }
        }
    }

    /** First eight upper-case hex characters of SHA-256(normalizedTelephone + lastName). */
    private static String hash8(Owner owner) {
        // The stored telephone is already E.164 (see NormalizeOwnerTelephone); re-normalizing is
        // idempotent and falls back to the stored value if it somehow cannot be parsed.
        String normalizedTelephone = E164Telephone.toE164OrNull(owner.getTelephone());
        if (normalizedTelephone == null) {
            normalizedTelephone = owner.getTelephone() == null ? "" : owner.getTelephone();
        }
        String lastName = owner.getLastName() == null ? "" : owner.getLastName();
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest((normalizedTelephone + lastName).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(8);
            for (int i = 0; i < 4; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
