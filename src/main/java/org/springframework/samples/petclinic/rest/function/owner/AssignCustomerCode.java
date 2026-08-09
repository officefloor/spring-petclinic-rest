package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code customerCode}, formatted {@code '<REGION>-<HASH8>'} where REGION is the
 * region code derived from the owner's postcode (preferred), falling back to the city and then to
 * {@code UNKNOWN}, and HASH8 is the first eight UPPER-case hex characters of SHA-256 over
 * {@code normalizedTelephone + lastName} (e.g. 'NSW-1A2B3C4D').
 *
 * <p>The base code is a pure function of the region and hash, so two distinct owners can in principle
 * derive the same code. When the computed code collides with an existing owner's {@code customerCode},
 * this step de-duplicates it by appending {@code '-<n>'} with the smallest {@code n} of 2 or more that
 * makes it unique (e.g. 'NSW-1A2B3C4D-2', then 'NSW-1A2B3C4D-3'), so every persisted owner keeps a
 * distinct code.
 *
 * <p>Runs after {@link BuildOwner} and {@link NormalizeOwnerTelephone} (so the telephone is already
 * in canonical E.164 form) and before {@link SaveOwner}, mutating the not-yet-persisted owner in
 * place.
 */
public class AssignCustomerCode {

    /** Fixed city-to-region table, mirroring the read-time locality derivation. */
    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** Region to its inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
        "NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099}, "QLD", new int[] {4000, 4099});

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String base = region(owner) + "-" + hash8(owner);
        owner.setCustomerCode(deduplicate(base, owner, ownerRepository));
    }

    /**
     * Returns {@code base} when no existing owner already uses it, otherwise the first of
     * {@code base-2, base-3, ...} that is free — so distinct owners keep distinct codes.
     */
    private static String deduplicate(String base, Owner owner, OwnerRepository ownerRepository) {
        Set<String> taken = new HashSet<>();
        for (Owner existing : ownerRepository.findAll()) {
            if (owner.getId() != null && owner.getId().equals(existing.getId())) {
                continue;
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

    /** Region derived from the postcode when it falls in a known range, else the city, else UNKNOWN. */
    private static String region(Owner owner) {
        String byPostcode = regionFromPostcode(owner.getPostcode());
        if (byPostcode != null) {
            return byPostcode;
        }
        return CITY_REGION.getOrDefault(owner.getCity(), "UNKNOWN");
    }

    private static String regionFromPostcode(String postcode) {
        if (postcode == null || !postcode.matches("[0-9]{4}")) {
            return null;
        }
        int value = Integer.parseInt(postcode);
        for (Map.Entry<String, int[]> entry : REGION_POSTCODES.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return null;
    }

    /** First eight UPPER-case hex characters of SHA-256 over (normalized telephone + last name). */
    private static String hash8(Owner owner) {
        String telephone = owner.getTelephone() == null ? "" : owner.getTelephone();
        String lastName = owner.getLastName() == null ? "" : owner.getLastName();
        byte[] digest;
        try {
            digest = MessageDigest.getInstance("SHA-256")
                .digest((telephone + lastName).getBytes(StandardCharsets.UTF_8));
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 unavailable", ex);
        }
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 4; i++) {
            sb.append(String.format("%02X", digest[i]));
        }
        return sb.toString();
    }
}
