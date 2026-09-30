package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.CityRegions;
import org.springframework.samples.petclinic.model.FiscalYear;
import org.springframework.samples.petclinic.model.Hashing;
import org.springframework.samples.petclinic.model.LuhnCheckDigit;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.OwnerTelephones;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code memberId}, the single unified identifier formatted
 * {@code <REGION><FY><HASH8><CHK>}:
 * <ul>
 * <li>REGION — the region derived from the postcode by the shared {@link CityRegions} table (or
 * {@code UNKNOWN} when the postcode is absent or in no known range),</li>
 * <li>FY — the 2-digit fiscal year (last two digits of the fiscal year containing the
 * business-day-adjusted {@code registrationDate}) from the shared {@link FiscalYear} rule,</li>
 * <li>HASH8 — the first 8 upper-case hex characters of SHA-256 over
 * {@code normalizedTelephone + lastName}, the same value used by the region-and-hash identity;
 * the telephone is normalized to E.164 via {@link OwnerTelephones} so the hash is stable
 * regardless of the incoming format,</li>
 * <li>CHK — a single Luhn check digit computed over the digits of {@code <REGION><FY><HASH8>}.</li>
 * </ul>
 * (e.g. {@code NSW263A7F1C2D4}).
 *
 * <p>The base member id is fully determined by the owner's own fields, but two distinct owners
 * could in principle hash to the same id. When the computed id collides with an existing owner's
 * {@code memberId}, {@code -<n>} is appended with the smallest {@code n} of 2 or more that makes it
 * unique, so distinct owners always end up with distinct member ids.
 */
public class AssignMemberId {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String region = CityRegions.identityRegion(owner.getPostcode());
        String fy = String.format("%02d", FiscalYear.endingYearOf(owner.getRegistrationDate()) % 100);
        String normalizedTelephone = OwnerTelephones.toE164OrRaw(owner.getTelephone());
        String hash8 = hash8(normalizedTelephone + owner.getLastName());
        String stem = region + fy + hash8;
        String baseId = stem + LuhnCheckDigit.of(stem);

        Set<String> existingIds = new HashSet<>();
        for (Owner existing : ownerRepository.findAll()) {
            String id = existing.getMemberId();
            if (id != null) {
                existingIds.add(id);
            }
        }

        String memberId = baseId;
        for (int n = 2; existingIds.contains(memberId); n++) {
            memberId = baseId + "-" + n;
        }
        owner.setMemberId(memberId);
    }

    /**
     * First 8 upper-case hex characters of SHA-256 over {@code value}, derived through the shared
     * {@link Hashing} primitive so this hash never diverges from the region-and-hash identity.
     */
    private static String hash8(String value) {
        return Hashing.sha256Hex(value).substring(0, 8).toUpperCase(Locale.ROOT);
    }
}
