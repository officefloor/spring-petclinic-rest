package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code customerCode}, formatted {@code <REGION>-<HASH8>} where REGION is the
 * region code derived from the postcode (see {@link OwnerLocality}) and HASH8 is the first eight
 * upper-case hex characters of SHA-256 over {@code normalizedTelephone + lastName}. The telephone
 * is normalized to E.164 (see {@link TelephoneE164}) exactly as the owner is stored, so the hash is
 * stable regardless of the telephone's input format. The identity is fully determined by the
 * owner's region, telephone and surname.
 *
 * <p>Should that computed code collide with an existing owner's {@code customerCode}, the code is
 * de-duplicated by appending {@code -<n>} with the smallest {@code n} of 2 or more that makes it
 * unique. Runs before {@link SaveOwner}, so the owner being created is not matched against itself.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String region = OwnerLocality.of(owner.getCity(), owner.getPostcode());
        String normalizedTelephone = normalizedTelephone(owner.getTelephone());
        String hash8 = sha256Hex8(normalizedTelephone + owner.getLastName());
        String baseCode = region + "-" + hash8;
        owner.setCustomerCode(deduplicate(baseCode, ownerRepository));
    }

    /**
     * Returns {@code baseCode} if no existing owner already uses it; otherwise appends {@code -<n>}
     * with the smallest {@code n >= 2} that is not taken by any existing owner.
     */
    private static String deduplicate(String baseCode, OwnerRepository ownerRepository) {
        Set<String> existing = new HashSet<>();
        for (Owner other : ownerRepository.findAll()) {
            String code = other.getCustomerCode();
            if (code != null) {
                existing.add(code);
            }
        }
        if (!existing.contains(baseCode)) {
            return baseCode;
        }
        for (int n = 2; ; n++) {
            String candidate = baseCode + "-" + n;
            if (!existing.contains(candidate)) {
                return candidate;
            }
        }
    }

    /** The owner's telephone in the same canonical E.164 form it is stored with. */
    private static String normalizedTelephone(String telephone) {
        String e164 = TelephoneE164.toE164(telephone);
        return e164 != null ? e164 : (telephone == null ? "" : telephone);
    }

    /** First eight upper-case hex characters of SHA-256 over the UTF-8 bytes of {@code value}. */
    private static String sha256Hex8(String value) {
        byte[] digest;
        try {
            digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex); // never on a standard JRE
        }
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 4; i++) {
            sb.append(String.format("%02X", digest[i]));
        }
        return sb.toString().toUpperCase(Locale.ROOT);
    }
}
