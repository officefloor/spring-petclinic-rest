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
 * Assigns the owner's unified {@code memberId} on create, formatted
 * {@code <IDREGION><FY><HASH8><CHK>} where {@code IDREGION} is the version-2 identity
 * region code (the postcode-derived region prefixed with the fixed {@code V2} version
 * tag, e.g. {@code V2NSW} — see {@link OwnerRegion#identityRegionOf}), {@code FY} is the
 * two-digit fiscal year of the (business-day-adjusted) registration date (see
 * {@link OwnerFiscalYear}), {@code HASH8} is the first 8 upper-case hex characters of
 * SHA-256 over ({@code normalizedTelephone + lastName}) and {@code CHK} is a single Luhn
 * check digit computed over the digits of {@code <IDREGION><FY><HASH8>} (e.g.
 * {@code V2NSW261A2B3C4D7}). The {@code V2} tag means no memberId matches a value the
 * same owner produced under version 1.
 *
 * <p>Runs after the telephone has been normalized to E.164 and the registration date has
 * been rolled to a business day, so both the hash and the fiscal year are stable over the
 * stored values. The base id is fully determined by those inputs; should it collide with
 * an existing owner's {@code memberId}, {@code -<n>} is appended with the smallest
 * {@code n} of 2 or more that makes it unique.
 */
public class AssignOwnerMemberId {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String region = OwnerRegion.identityRegionOf(owner);
        String fy = String.format("%02d",
                OwnerFiscalYear.fiscalYearOf(owner.getRegistrationDate()) % 100);
        String hash8 = sha256Hex8(owner.getTelephone() + owner.getLastName());
        String withoutCheck = region + fy + hash8;
        String base = withoutCheck + luhnCheckDigit(withoutCheck);

        Set<String> existing = new HashSet<>();
        for (Owner other : ownerRepository.findAll()) {
            if (other.getId() != null && other.getId().equals(owner.getId())) {
                continue;
            }
            String memberId = other.getMemberId();
            if (memberId != null) {
                existing.add(memberId);
            }
        }

        String memberId = base;
        for (int n = 2; existing.contains(memberId); n++) {
            memberId = base + "-" + n;
        }
        owner.setMemberId(memberId);
    }

    /** First 8 upper-case hex characters of SHA-256 over the UTF-8 bytes of {@code s}. */
    private static String sha256Hex8(String s) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(8);
            for (int i = 0; i < 4; i++) { // 4 bytes -> 8 hex characters
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /** Single Luhn check digit (0-9) computed over the digits contained in {@code s}. */
    private static int luhnCheckDigit(String s) {
        int sum = 0;
        boolean dbl = true;
        for (int i = s.length() - 1; i >= 0; i--) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') {
                continue;
            }
            int d = c - '0';
            if (dbl) {
                d *= 2;
                if (d > 9) {
                    d -= 9;
                }
            }
            sum += d;
            dbl = !dbl;
        }
        return (10 - (sum % 10)) % 10;
    }
}
