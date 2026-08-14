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
 * Runs in the create-owner pipeline after {@link DefaultOwnerRegistrationDate}, before
 * {@link SaveOwner}. Assigns the owner's unified {@code memberId}, formatted
 * {@code '<REGION><FY><HASH8><CHK>'}:
 *
 * <ul>
 * <li>REGION — the version-2 region code used inside the identifiers
 * ({@link Owner#identityRegion(String)}): the plain region ({@link Owner#getLocality()}, derived from
 * the postcode, falling back to the city, else {@code 'UNKNOWN'}) with the {@code 'V2'} version tag
 * mixed in, so it differs from the plain user-facing locality;</li>
 * <li>FY — the last two digits of the fiscal year (starting 1 July) derived from the
 * business-day-adjusted registrationDate, matching the {@link Owner#getFiscalYear() fiscalYear};</li>
 * <li>HASH8 — the first eight upper-case hex characters of a SHA-256 digest over the normalized
 * telephone concatenated with the last name (the same HASH8 as the region-and-hash identity);</li>
 * <li>CHK — a single Luhn check digit over the digits of {@code <REGION><FY><HASH8>}.</li>
 * </ul>
 *
 * <p>For example 'NSWV2271A2B3C4D5'. If the computed id collides with an existing owner's
 * {@code memberId}, it is de-duplicated by appending {@code '-<n>'} with the smallest {@code n} of 2
 * or more that makes it unique (e.g. 'NSWV2271A2B3C4D5-2'). Mutates the built owner in place so later
 * steps (audit, response) derive from and store the assigned id.
 */
public class AssignMemberId {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String region = Owner.identityRegion(owner.getLocality());
        String fy = String.format("%02d", Owner.fiscalYearOf(owner.getRegistrationDate()) % 100);
        String hash8 = shaHex8(owner.getTelephone() + owner.getLastName());
        String core = region + fy + hash8;
        String base = core + luhn(core);

        Set<String> existing = new HashSet<>();
        for (Owner other : ownerRepository.findAll()) {
            if (other.getMemberId() != null) {
                existing.add(other.getMemberId());
            }
        }

        String memberId = base;
        for (int n = 2; existing.contains(memberId); n++) {
            memberId = base + "-" + n;
        }
        owner.setMemberId(memberId);
    }

    /** First eight UPPER-case hex characters of SHA-256 over the UTF-8 bytes of {@code s}. */
    private static String shaHex8(String s) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02X", b));
                if (sb.length() >= 8) {
                    break;
                }
            }
            return sb.substring(0, 8);
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /** A single Luhn check digit (0-9) over the digits contained in {@code s} (non-digits ignored). */
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
}
