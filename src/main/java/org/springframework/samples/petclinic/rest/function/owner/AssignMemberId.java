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
 * Assigns the owner's unified {@code memberId}, formatted {@code <REGION><FY><HASH8><CHK>} (see
 * {@link MemberId}): REGION is the version-2 region code — the plain postcode/city region with the
 * fixed {@code "V2"} tag prefixed (see {@link OwnerRegion#identityRegion}); FY is the 2-digit
 * fiscal year of the business-day-adjusted
 * {@code registrationDate} (the same {@code YY} as the owner's {@code fiscalYear}); HASH8 is the
 * first 8 upper-case hex characters of SHA-256 over the normalized telephone concatenated with the
 * last name; and CHK is a single Luhn check digit over the digits of {@code <REGION><FY><HASH8>}
 * (e.g. {@code NSW261A2B3C4D5}). There are no per-city sequence numbers.
 *
 * <p>Runs after {@link BuildOwner} (so the normalized telephone, last name, postcode and
 * registration date are on the owner) and before {@link SaveOwner}. The audit record, the derived
 * locality/region and the owner segment all read this identity.
 *
 * <p>If the computed memberId collides with an existing owner's {@code memberId}, it is
 * de-duplicated by appending {@code -<n>} with the smallest {@code n} of 2 or more that makes it
 * unique.
 */
public class AssignMemberId {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String region = OwnerRegion.identityRegion(owner.getPostcode(), owner.getCity());
        String fy2 = String.format("%02d", FiscalYear.startYear(owner.getRegistrationDate()) % 100);
        String telephone = owner.getTelephone() == null ? "" : owner.getTelephone();
        String lastName = owner.getLastName() == null ? "" : owner.getLastName();
        String base = MemberId.of(region, fy2, hash8(telephone + lastName));
        owner.setMemberId(deDuplicate(base, owner, ownerRepository));
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
            String memberId = existing.getMemberId();
            if (memberId != null) {
                taken.add(memberId);
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
