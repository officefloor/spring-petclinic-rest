package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.util.CheckDigit;
import org.springframework.samples.petclinic.util.FiscalYear;
import org.springframework.samples.petclinic.util.LocalityResolver;
import org.springframework.samples.petclinic.util.Sha256Hash;

/**
 * Assigns the owner's {@code memberId} before it is saved. It unifies the former
 * {@code customerCode} and {@code membershipNumber} into a single value formatted
 * {@code <REGION><FY><HASH8><CHK>}, where:
 *
 * <ul>
 * <li>{@code REGION} is the region code derived from the postcode (see {@link LocalityResolver});</li>
 * <li>{@code FY} is the last two digits of the fiscal year (starting 1 July) of the
 * business-day-adjusted {@code registrationDate}, zero-padded;</li>
 * <li>{@code HASH8} is the first 8 upper-case hex characters of SHA-256 over the concatenation of
 * the normalized telephone and the last name (the same HASH8 used by the region-and-hash identity);
 * and</li>
 * <li>{@code CHK} is a single Luhn check digit computed over the digits of
 * {@code <REGION><FY><HASH8>} (see {@link CheckDigit}).</li>
 * </ul>
 *
 * (e.g. {@code NSW273F1A9C2B4}.) The telephone was normalized to E.164 by {@link ValidateNewOwner},
 * so {@code owner.getTelephone()} is already the normalized value, and {@link BuildOwner} has set the
 * business-day-adjusted {@code registrationDate}.
 *
 * <p>Should the computed id collide with an existing owner's {@code memberId}, it is de-duplicated by
 * appending {@code -<n>} with the smallest {@code n} of 2 or more that makes it unique
 * (e.g. {@code NSW273F1A9C2B4-2}). Runs after {@link BuildOwner} and before {@link SaveOwner}, so
 * {@link OwnerRepository#findAll()} returns only the pre-existing owners. Mutates the entity in place
 * so {@link SaveOwner} persists it.
 */
public class AssignMemberId {

    /** Number of upper-case hex characters taken from the SHA-256 digest. */
    private static final int HASH_LENGTH = 8;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String region = LocalityResolver.localityOf(owner.getPostcode(), owner.getCity());
        String fy = String.format("%02d", FiscalYear.yearOf(owner.getRegistrationDate()) % 100);
        String hash8 = Sha256Hash.upperHex(owner.getTelephone() + owner.getLastName(), HASH_LENGTH);
        String body = region + fy + hash8;
        String baseId = body + CheckDigit.luhnOf(body);

        Set<String> existingIds = new HashSet<>();
        for (Owner other : ownerRepository.findAll()) {
            existingIds.add(other.getMemberId());
        }

        String memberId = baseId;
        for (int n = 2; existingIds.contains(memberId); n++) {
            memberId = baseId + "-" + n;
        }
        owner.setMemberId(memberId);
    }
}
