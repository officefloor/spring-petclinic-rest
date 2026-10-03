package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.repository.OwnerRepository;

public class BuildOwner {

    public void service(@Val OwnerFieldsDto request, @Val Household.Id householdId,
            OwnerMapper ownerMapper, OwnerRepository ownerRepository, Out<Owner> built) {
        Owner owner = ownerMapper.toOwner(request);
        // The effective registration date defaults an unsupplied value to the server's
        // current date, then rolls any weekend forward to the next Monday so derived values
        // (e.g. the memberId's fiscal year) use the adjusted business day.
        owner.setRegistrationDate(BusinessDay.effectiveRegistrationDate(owner.getRegistrationDate()));
        owner.setMemberId(deduplicate(memberId(owner), ownerRepository));
        // Record how many existing owners already share this first+last name (case-insensitive),
        // counted before this owner is persisted so the value is fixed at creation time.
        owner.setNamesakeCount(countNamesakes(owner.getFirstName(), owner.getLastName(), ownerRepository));
        // Join a shared household when the gate resolved an identifier.
        if (householdId != null && householdId.isPresent()) {
            owner.setHouseholdId(householdId.value());
        }
        // Record the household's member count (existing members plus this not-yet-persisted
        // owner) so the membership points rule can score a household of 3 or more.
        owner.setHouseholdSize(Household.size(owner.getHouseholdId(), ownerRepository) + 1);
        built.set(owner);
    }

    /**
     * Build the '<REGION><FY><HASH8><CHK>' memberId: REGION is the version-2 identity region derived
     * from the owner's postcode and city ({@link Owner#identityRegion(String, String)}, the plain
     * region prefixed with the {@link Owner#IDENTITY_VERSION} tag, e.g. 'V2NSW' or 'V2UNKNOWN'); FY is
     * the 2-digit fiscal-year segment of the business-day-adjusted registration date
     * ({@link Owner#fiscalYearSegment(java.time.LocalDate)}, e.g. '26'); HASH8 is the first 8
     * upper-case hex characters of the SHA-256 of the normalized telephone followed by the last name;
     * and CHK is a single Luhn check digit computed over the digits of '<REGION><FY><HASH8>' (e.g.
     * 'V2NSW261A2B3C4D7'). There is no sequence number, so the id is stable for an owner regardless of
     * creation order. The embedded version tag changes every memberId relative to version 1 while the
     * user-facing {@link Owner#getLocality() locality} recovers the plain region by stripping it.
     */
    private static String memberId(Owner owner) {
        String region = Owner.identityRegion(owner.getPostcode(), owner.getCity());
        String fy = Owner.fiscalYearSegment(owner.getRegistrationDate());
        String normalizedTelephone = OwnerTelephone.normalize(owner.getTelephone());
        String seed = (normalizedTelephone == null ? "" : normalizedTelephone) + owner.getLastName();
        String base = region + fy + hash8(seed);
        return base + luhn(base);
    }

    /**
     * The Luhn check digit (0-9) computed over the digits contained in {@code value}. Non-digit
     * characters (e.g. the letters of the REGION prefix) are ignored.
     */
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

    /**
     * Return a memberId that no existing owner already uses. When {@code id} collides with an
     * existing owner's memberId, append '-&lt;n&gt;' with the smallest n of 2 or more that makes
     * it unique (e.g. 'NSW261A2B3C4D7' → 'NSW261A2B3C4D7-2'). The un-suffixed id is returned unchanged
     * when it does not collide.
     */
    private static String deduplicate(String code, OwnerRepository ownerRepository) {
        Set<String> existing = new HashSet<>();
        for (Owner owner : ownerRepository.findAll()) {
            if (owner.getMemberId() != null) {
                existing.add(owner.getMemberId());
            }
        }
        if (!existing.contains(code)) {
            return code;
        }
        int n = 2;
        while (existing.contains(code + "-" + n)) {
            n++;
        }
        return code + "-" + n;
    }

    /**
     * The first 8 upper-case hex characters of the SHA-256 of the UTF-8 bytes of {@code seed}.
     */
    private static String hash8(String seed) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(seed.getBytes(StandardCharsets.UTF_8));
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

    /**
     * Count the existing owners whose first and last names both match the given names,
     * compared case-insensitively.
     */
    private static int countNamesakes(String firstName, String lastName,
            OwnerRepository ownerRepository) {
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (firstName.equalsIgnoreCase(existing.getFirstName())
                    && lastName.equalsIgnoreCase(existing.getLastName())) {
                count++;
            }
        }
        return count;
    }
}
