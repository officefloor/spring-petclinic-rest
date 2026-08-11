package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.Month;
import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code memberId}, formatted {@code <REGION><FY><HASH8><CHK>} where REGION is
 * the version-2 region code that appears inside the identifiers — the plain region derived from the
 * owner's postcode with the fixed {@code 'V2'} tag mixed in (see {@link OwnerRegion#identityRegion},
 * e.g. {@code NSWV2}), so the memberId changes and can never equal a v1 one. The user-facing
 * {@code locality} keeps the plain region. FY is the 2-digit fiscal year of the owner's registrationDate (the same YY
 * as the {@code fiscalYear} field), HASH8 is the first 8 upper-case hex characters of SHA-256 over
 * the owner's normalized (E.164) telephone concatenated with the last name, and CHK is a single Luhn
 * check digit computed over the digits of {@code <REGION><FY><HASH8>}.
 *
 * <p>The identity is therefore a stable function of who the owner is, not of how many owners already
 * exist — the old per-city sequence number is gone. This unifies what used to be the separate
 * {@code customerCode}, {@code membershipNumber} and {@code checkDigit}.
 *
 * <p>Should the computed memberId collide with an existing owner's {@code memberId}, it is
 * de-duplicated by appending {@code -<n>} with the smallest {@code n} of 2 or more that makes it
 * unique, so distinct owners always end up with distinct member ids.
 */
public class AssignMemberId {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String region = OwnerRegion.identityRegion(owner);
        String fy = fiscalYear2(owner.getRegistrationDate());
        String hash8 = hash8(normalizedTelephone(owner.getTelephone()) + owner.getLastName());
        String body = region + fy + hash8;
        String base = body + luhn(body);
        owner.setMemberId(deduplicate(base, existingMemberIds(ownerRepository)));
    }

    /** The {@code memberId}s already assigned to other owners in the data store. */
    private static Set<String> existingMemberIds(OwnerRepository ownerRepository) {
        Set<String> ids = new HashSet<>();
        for (Owner existing : ownerRepository.findAll()) {
            String id = existing.getMemberId();
            if (id != null) {
                ids.add(id);
            }
        }
        return ids;
    }

    /**
     * Returns {@code base} if it is not already taken; otherwise appends {@code -<n>} with the
     * smallest {@code n} of 2 or more that makes it unique.
     */
    private static String deduplicate(String base, Set<String> taken) {
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

    /**
     * The last two digits of the fiscal year (1 July - 30 June, named by the calendar year in which
     * it ends) of {@code date}, zero-padded. Falls back to the current date's fiscal year when no
     * registration date is present.
     */
    private static String fiscalYear2(LocalDate date) {
        LocalDate d = date != null ? date : LocalDate.now();
        int fy = d.getMonthValue() >= Month.JULY.getValue() ? d.getYear() + 1 : d.getYear();
        return String.format("%02d", Math.floorMod(fy, 100));
    }

    /** Luhn check digit (0-9) over the digits contained in {@code s}. */
    private static int luhn(String s) {
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

    /**
     * The owner's telephone normalized to E.164 (the same way {@link OwnerIdentityKey} does),
     * falling back to the trimmed raw value when it cannot form a valid E.164 number. On the create
     * path the telephone was already normalized upstream, so this is idempotent.
     */
    private static String normalizedTelephone(String raw) {
        if (raw == null) {
            return "";
        }
        String normalized = E164Telephone.normalize(raw);
        return normalized != null ? normalized : raw.trim();
    }

    /** First 8 upper-case hex characters of SHA-256 over the UTF-8 bytes of {@code s}. */
    private static String hash8(String s) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02X", b));
                if (sb.length() >= 8) {
                    break;
                }
            }
            return sb.substring(0, 8);
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}
