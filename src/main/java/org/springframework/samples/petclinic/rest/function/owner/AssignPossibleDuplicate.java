package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Step of {@code POST /api/owners} that flags a soft duplicate. The create has already passed
 * {@link RequireUniqueIdentity}, so it is not a hard identity duplicate. This step additionally
 * marks the owner when an existing owner shares its {@code lastName} (compared case-insensitively
 * with collapsed whitespace) and its {@code postcode} but has a different telephone (normalized to
 * E.164, see {@link TelephoneE164}).
 *
 * <p>When such an owner exists, {@code possibleDuplicate} is set true and {@code possibleDuplicateOf}
 * to that owner's id — the earliest (lowest-id) match when several qualify; otherwise
 * {@code possibleDuplicate} is false and {@code possibleDuplicateOf} is left unset. Runs before
 * {@link SaveOwner}, so the owner being created is not matched against itself. An owner with no
 * postcode never soft-matches.
 */
public class AssignPossibleDuplicate {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        owner.setPossibleDuplicate(false);
        String postcode = owner.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            return;
        }
        String lastName = normalize(owner.getLastName());
        String telephone = normalizeTelephone(owner.getTelephone());
        Owner match = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (!postcode.equals(existing.getPostcode())) {
                continue;
            }
            if (!lastName.equals(normalize(existing.getLastName()))) {
                continue;
            }
            if (telephone.equals(normalizeTelephone(existing.getTelephone()))) {
                continue; // same telephone would be a hard duplicate, not a soft match
            }
            if (match == null || (existing.getId() != null && match.getId() != null
                    && existing.getId() < match.getId())) {
                match = existing;
            }
        }
        if (match != null) {
            owner.setPossibleDuplicate(true);
            owner.setPossibleDuplicateOf(match.getId());
        }
    }

    /** Trim, collapse internal whitespace runs to a single space, and lower-case for comparison. */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /** Canonical E.164 telephone, falling back to the raw value when it cannot be parsed. */
    private static String normalizeTelephone(String telephone) {
        String e164 = TelephoneE164.toE164(telephone);
        return e164 != null ? e164 : (telephone == null ? "" : telephone);
    }
}
