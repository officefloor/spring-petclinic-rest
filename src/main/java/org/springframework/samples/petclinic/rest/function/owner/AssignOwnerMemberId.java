package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns a newly created owner the unified {@code memberId} formatted
 * {@code '<REGION><FY><HASH8><CHK>'} (no separators): REGION is the region code derived from the
 * owner's postcode (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099, otherwise {@code UNKNOWN}); FY is
 * the two-digit fiscal year of the (business-day-adjusted) registrationDate (the fiscal year
 * starting 1 July); HASH8 is the first eight UPPER-case hex characters of the SHA-256 digest over
 * the version-2 region code (the plain region combined with a fixed {@code 'V2'} tag) followed by the
 * normalized (E.164) telephone and the owner's last name, so the identity is rederived for version 2
 * while the REGION prefix stays the plain region; and CHK is a single Luhn check digit over the digits of
 * {@code <REGION><FY><HASH8>}. This unifies the former customerCode, membershipNumber and standalone
 * checkDigit into one identifier.
 *
 * <p>Mutates the {@link Owner} in place so it is persisted and returned with the memberId, and so
 * every value derived from it (audit line, owner segment, locality) reflects the new identity.
 *
 * <p>When the computed memberId collides with an existing owner's {@code memberId}, it is
 * de-duplicated by appending {@code '-<n>'} with the smallest {@code n} of 2 or more that makes it
 * unique, so distinct owners always receive distinct memberIds.
 */
public class AssignOwnerMemberId {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        // Plain region code is the visible REGION prefix (and feeds locality); the
        // version-2 region code is mixed into HASH8 so the whole memberId is rederived.
        String region = PostcodeRegions.regionCode(owner.getPostcode());
        String regionV2 = PostcodeRegions.regionCodeV2(owner.getPostcode());
        String telephone = owner.getTelephone() == null ? "" : owner.getTelephone();
        String lastName = owner.getLastName() == null ? "" : owner.getLastName();
        String fy = owner.getRegistrationDate() == null ? "00"
                : String.format("%02d", OwnerMapper.fiscalYearOf(owner.getRegistrationDate()) % 100);
        String base = region + fy + hash8(regionV2 + "|" + telephone + lastName);
        String memberId = base + luhn(base);
        owner.setMemberId(deduplicate(memberId, owner, ownerRepository));
    }

    /**
     * Return {@code base} if no existing owner already holds it, otherwise
     * {@code base + "-" + n} with the smallest {@code n >= 2} that is unused.
     */
    private static String deduplicate(String base, Owner owner, OwnerRepository ownerRepository) {
        Set<String> taken = new HashSet<>();
        for (Owner existing : ownerRepository.findAll()) {
            if (owner.getId() != null && owner.getId().equals(existing.getId())) {
                continue; // never collide with the owner itself
            }
            if (existing.getMemberId() != null) {
                taken.add(existing.getMemberId());
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

    /** Single Luhn check digit (0-9) over the digits contained in {@code value}. */
    private static int luhn(String value) {
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

    /** First eight UPPER-case hex characters (four bytes) of SHA-256 over the UTF-8 bytes. */
    private static String hash8(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(8);
            for (int i = 0; i < 4; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}
