package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Sha256;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the {@code memberId} formatted {@code <REGION><FY><HASH8><CHK>}, the single owner identity
 * that unifies the former customer code and membership number. REGION is the region code derived from
 * the owner's {@link Owner#getPostcode() postcode} via {@link Owner#regionOfPostcode(String)} (NSW
 * 2000-2099, VIC 3000-3099, QLD 4000-4099), or {@code "UNKNOWN"} when the postcode is absent or in no
 * known range. FY is the two-digit fiscal year (starting 1 July) of the business-day-adjusted
 * {@link Owner#getRegistrationDate() registration date} (e.g. a registration on 2026-10-02 falls in the
 * fiscal year ending June 2027, so {@code "27"}). HASH8 is the first 8 upper-case hex characters of the
 * SHA-256 of the owner's {@link OwnerTelephone normalized (E.164) telephone} concatenated with the last
 * name — the same region-and-hash identity as before. CHK is a single {@link Owner#luhnCheckDigit(String)
 * Luhn check digit} computed over the digits of {@code <REGION><FY><HASH8>}.
 *
 * <p>The {@code <REGION><FY><HASH8><CHK>} value would otherwise collide for two owners with the same
 * normalized telephone and last name in the same region and fiscal year. When the computed value matches
 * an existing owner's {@code memberId}, it is de-duplicated by appending {@code -<n>} with the smallest
 * {@code n} of 2 or more that makes it unique. Every value built from the memberId — the owner's
 * {@link Owner#getLocality() locality} and {@link Owner#getFiscalYear() fiscalYear}, and the
 * {@link AuditOwnerCreated create audit record} — reads this de-duplicated value.
 *
 * <p>Runs after {@link BuildOwner}, which defaults and business-day-adjusts the registration date the FY
 * segment is derived from.
 */
public class AssignMemberId {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String region = Owner.regionOfPostcode(owner.getPostcode());
        if (region == null) {
            region = "UNKNOWN";
        }
        String normalizedTelephone = OwnerTelephone.toE164(owner.getTelephone());
        if (normalizedTelephone == null) {
            normalizedTelephone = owner.getTelephone() == null ? "" : owner.getTelephone();
        }
        String lastName = owner.getLastName() == null ? "" : owner.getLastName();
        String fy = String.format("%02d", Owner.fiscalYear(owner.getRegistrationDate()) % 100);
        String core = region + fy + hash8(normalizedTelephone + lastName);
        String base = core + Owner.luhnCheckDigit(core);

        Set<String> existingIds = new HashSet<>();
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getMemberId() != null) {
                existingIds.add(existing.getMemberId());
            }
        }

        String memberId = base;
        for (int n = 2; existingIds.contains(memberId); n++) {
            memberId = base + "-" + n;
        }
        owner.setMemberId(memberId);
    }

    /** First 8 upper-case hex characters of the SHA-256 of {@code value}'s UTF-8 bytes. */
    private static String hash8(String value) {
        return Sha256.hex(value).substring(0, 8).toUpperCase(Locale.ROOT);
    }
}
