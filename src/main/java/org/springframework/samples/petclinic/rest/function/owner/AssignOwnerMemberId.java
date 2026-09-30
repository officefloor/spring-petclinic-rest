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
 * Assigns the owner's unified {@code memberId}, formatted {@code <REGIONV2><FY><HASH8><CHK>} where:
 * <ul>
 * <li>REGIONV2 is the version-2 {@link Owner#regionCodeV2(String) region code} — the plain region
 * derived from the owner's postcode (see {@link Owner#regionOfPostcode(String)}, or {@code UNKNOWN}
 * when the postcode maps to no known region) with the fixed {@link Owner#IDENTITY_VERSION} tag mixed
 * in (e.g. {@code NSWV2}),</li>
 * <li>FY is the two-digit (zero-padded) last two digits of the fiscal year (starting 1 July) of the
 * business-day-adjusted {@code registrationDate},</li>
 * <li>HASH8 is the first 8 upper-case hex characters of the SHA-256 of the normalized telephone
 * concatenated with the lastName — the same region-and-hash identity used elsewhere, and</li>
 * <li>CHK is a single Luhn check digit computed over the digits of {@code <REGIONV2><FY><HASH8>}.</li>
 * </ul>
 * e.g. {@code NSWV2273F9A1C7E4}. Because the version-2 tag is mixed into the region code, no
 * version-2 member id can reproduce a version-1 value. This single value unifies the former
 * {@code customerCode} and {@code membershipNumber} and subsumes the standalone {@code checkDigit}.
 * When the computed id collides with an existing owner's {@code memberId}, {@code -<n>} is appended
 * with the smallest {@code n} of 2 or more that makes it unique (e.g. {@code NSWV2273F9A1C7E4-2}).
 * Runs after {@link BuildOwner} builds the entity (so the telephone is in canonical E.164 form and
 * the business-day-adjusted registration date is set) and before {@link SaveOwner} persists it,
 * mutating the built owner in place so the saved and returned record carries the id. The user-facing
 * {@link Owner#getLocality() locality}, {@link Owner#getFiscalYear() fiscal year} and owner segment
 * are derived independently from the postcode and registration date, so they stay the plain region
 * code and never carry the version-2 tag.
 */
public class AssignOwnerMemberId {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String region = Owner.regionCodeV2(Owner.regionOfPostcode(owner.getPostcode()));
        String fy = String.format(Locale.ROOT, "%02d", Owner.fiscalYear(owner.getRegistrationDate()) % 100);
        String hash8 = sha256Hex8(owner.getTelephone() + owner.getLastName());
        String identity = region + fy + hash8;
        String base = identity + luhnCheckDigit(identity);
        owner.setMemberId(deduplicate(base, owner, ownerRepository));
    }

    /**
     * The unique member id for the owner: {@code base} when no other owner already carries it,
     * otherwise {@code base-<n>} with the smallest {@code n} of 2 or more that is not yet taken.
     * The owner is not yet persisted, so its own id is excluded by identity.
     */
    private static String deduplicate(String base, Owner owner, OwnerRepository ownerRepository) {
        Set<String> taken = new HashSet<>();
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getId() != null && existing.getId().equals(owner.getId())) {
                continue;
            }
            String id = existing.getMemberId();
            if (id != null) {
                taken.add(id);
            }
        }
        if (!taken.contains(base)) {
            return base;
        }
        int n = 2;
        while (taken.contains(base + "-" + n)) {
            n++;
        }
        return base + "-" + n;
    }

    /**
     * The first 8 upper-case hex characters of the SHA-256 digest of {@code value}, taken over its
     * UTF-8 bytes — i.e. the first 4 digest bytes rendered as hex.
     */
    private static String sha256Hex8(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(8);
            for (int i = 0; i < 4; i++) {
                sb.append(String.format(Locale.ROOT, "%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    /**
     * A single Luhn check digit (0-9) over the digits contained in {@code value} (non-digit
     * characters are ignored): doubling every second digit from the right, subtracting 9 from any
     * result over 9, and taking {@code (10 - sum % 10) % 10}.
     */
    private static int luhnCheckDigit(String value) {
        int sum = 0;
        boolean dbl = true;
        for (int i = value.length() - 1; i >= 0; i--) {
            char c = value.charAt(i);
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
