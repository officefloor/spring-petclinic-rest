package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.CheckDigits;
import org.springframework.samples.petclinic.mapper.Localities;
import org.springframework.samples.petclinic.model.FiscalYears;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code memberId}, formatted {@code <REGION><FY><HASH8><CHK>} where
 * REGION is the version-2 region code derived from the postcode (the shared derivation in
 * {@link Localities#identityRegion(String, String)}, the plain region code with the fixed
 * {@code V2} version tag mixed in, postcode-range first, city fallback), FY is
 * the two-digit fiscal year of the (business-day-adjusted) registrationDate, HASH8 is the
 * first eight upper-case hex characters of the SHA-256 digest over the normalized
 * telephone concatenated with the last name, and CHK is a single Luhn check digit computed
 * over the digits of {@code <REGION><FY><HASH8>}. The telephone is already in its
 * normalized E.164 form by this step (see {@link ValidateOwnerFields}).
 *
 * <p>The base id is a pure function of the owner's identity and registration date, but two
 * distinct owners can still hash to the same {@code memberId}. When the computed id
 * collides with an existing owner's {@code memberId}, {@code -<n>} is appended with the
 * smallest {@code n} of 2 or more that makes it unique. The new owner is not yet saved, so
 * it is not among the existing owners scanned here.
 */
public class AssignMemberId {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String region = Localities.identityRegion(owner.getCity(), owner.getPostcode());
        String fy = String.format("%02d", FiscalYears.startYear(owner.getRegistrationDate()) % 100);
        String hash8 = sha256Hex8(owner.getTelephone() + owner.getLastName());
        String withoutCheck = region + fy + hash8;
        String base = withoutCheck + CheckDigits.luhn(withoutCheck);

        Set<String> taken = new HashSet<>();
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getMemberId() != null) {
                taken.add(existing.getMemberId());
            }
        }

        String memberId = base;
        for (int n = 2; taken.contains(memberId); n++) {
            memberId = base + "-" + n;
        }
        owner.setMemberId(memberId);
    }

    /** The first eight upper-case hex characters of the SHA-256 digest of {@code s}. */
    private static String sha256Hex8(String s) {
        byte[] digest;
        try {
            digest = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; sb.length() < 8; i++) {
            sb.append(String.format("%02X", digest[i]));
        }
        return sb.substring(0, 8);
    }
}
