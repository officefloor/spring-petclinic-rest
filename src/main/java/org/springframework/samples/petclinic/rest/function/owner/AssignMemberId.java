package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the unified {@code memberId} formatted {@code <REGION><FY><HASH8><CHK>}, where REGION is the
 * version-2 region code (see {@link RegionCode#v2(String, String)}: the plain region derived from the
 * owner's postcode — preferring the postcode's range, falling back to the city table and finally
 * {@code "UNKNOWN"} — with the fixed {@code "V2"} tag mixed in); FY is the two-digit fiscal
 * year (starting 1 July) of the business-day-adjusted {@code registrationDate}; HASH8 is the first 8
 * upper-case hex characters of SHA-256 over the normalized telephone concatenated with the last name;
 * and CHK is a single Luhn check digit computed over
 * the decimal digits of {@code <REGION><FY><HASH8>} (e.g. {@code NSWV2261A2B3C4D5}). The telephone has
 * already been normalized to E.164 form by {@link NormalizeTelephone} earlier in the pipeline, so the
 * stored value is used directly.
 * <p>
 * When the computed member id collides with an existing owner's {@code memberId}, {@code -<n>} is
 * appended with the smallest {@code n} of 2 or more that makes it unique (e.g.
 * {@code NSWV2261A2B3C4D5-2}). Runs before the owner is saved, so {@link OwnerRepository#findAll()}
 * sees only the owners that existed before this create.
 */
public class AssignMemberId {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String region = RegionCode.v2(owner.getCity(), owner.getPostcode());
        String telephone = owner.getTelephone() == null ? "" : owner.getTelephone();
        String lastName = owner.getLastName() == null ? "" : owner.getLastName();
        String hash8 = Sha256.hex(telephone + lastName).substring(0, 8).toUpperCase(Locale.ROOT);
        String fy = String.format("%02d", FiscalYear.yy(owner.getRegistrationDate()));
        String core = region + fy + hash8;
        String base = core + CheckDigit.of(core);

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
}
