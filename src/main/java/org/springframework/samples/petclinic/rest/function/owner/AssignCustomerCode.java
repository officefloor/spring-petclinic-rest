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
 * Assigns the owner's {@code customerCode}, formatted {@code <REGION>-<HASH8>} where REGION is
 * the region code derived from the postcode (see {@link OwnerRegion}) and HASH8 is the first 8
 * upper-case hex characters of the SHA-256 digest over the normalized telephone concatenated with
 * the last name (e.g. {@code NSW-1A2B3C4D}). There are no per-city sequence numbers.
 *
 * <p>Runs after {@link BuildOwner} (so the normalized telephone, last name and postcode are on the
 * owner) and before {@link SaveOwner}. The membership number, its Luhn check digit, the create
 * audit line and the derived locality all read this identity.
 *
 * <p>If the computed code collides with an existing owner's {@code customerCode}, it is
 * de-duplicated by appending {@code -<n>} with the smallest {@code n} of 2 or more that makes it
 * unique.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String region = OwnerRegion.fromPostcodeOrCity(owner.getPostcode(), owner.getCity());
        String telephone = owner.getTelephone() == null ? "" : owner.getTelephone();
        String lastName = owner.getLastName() == null ? "" : owner.getLastName();
        String base = region + "-" + hash8(telephone + lastName);
        owner.setCustomerCode(deDuplicate(base, owner, ownerRepository));
    }

    /**
     * Returns {@code base} if no existing owner already uses it, otherwise {@code base-<n>} with the
     * smallest {@code n >= 2} that is not already taken by an existing owner.
     */
    private static String deDuplicate(String base, Owner owner, OwnerRepository ownerRepository) {
        Set<String> taken = new HashSet<>();
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getId() != null && existing.getId().equals(owner.getId())) {
                continue; // never compare the new owner against itself
            }
            String code = existing.getCustomerCode();
            if (code != null) {
                taken.add(code);
            }
        }
        if (!taken.contains(base)) {
            return base;
        }
        for (int n = 2; ; n++) {
            String candidate = base + "-" + n;
            if (!taken.contains(candidate)) {
                return candidate;
            }
        }
    }

    /** First 8 upper-case hex characters of SHA-256 over the UTF-8 bytes of {@code value}. */
    private static String hash8(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(16);
            for (int i = 0; i < 4; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
