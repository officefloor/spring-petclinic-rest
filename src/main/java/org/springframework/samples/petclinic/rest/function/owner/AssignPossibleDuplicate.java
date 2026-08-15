package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Soft-match step of {@code POST /api/owners}. By the time it runs the request has already passed
 * {@link RejectDuplicateIdentity}, so it is <em>not</em> a hard duplicate; this step then looks for a
 * weaker signal that it might be one anyway: an existing owner that shares this owner's
 * {@code lastName} (compared case-insensitively) and {@code postcode} but has a <em>different</em>
 * telephone. Such an owner is still created, but is flagged with {@code possibleDuplicate = true} and
 * {@code possibleDuplicateOf} set to the matching owner's id (the lowest id when several match).
 * When nothing matches, {@code possibleDuplicate} is set to {@code false} and no id is recorded.
 *
 * <p>A <em>declared</em> household member — one created with {@code sharesHousehold: true} — is never
 * flagged: it already reached this step by opting past the household-duplicate block in
 * {@link RejectDuplicateIdentity}, so sharing a same-household owner's lastName and postcode is
 * expected, not a suspected duplicate.
 *
 * <p>Runs after {@link BuildOwner} so the owner carries its normalized lastName, postcode and
 * telephone, and before {@link SaveOwner} so the comparison sees only owners that existed before this
 * create — the new owner is not yet persisted and so never matches itself. {@code @Val} yields the
 * built owner, mutated in place and persisted by the save step.
 */
public class AssignPossibleDuplicate {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository) {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            // A declared household member is not a suspected duplicate.
            owner.setPossibleDuplicate(false);
            owner.setPossibleDuplicateOf(null);
            return;
        }
        String lastName = Households.normalizeName(owner.getLastName());
        String postcode = normalizePostcode(owner.getPostcode());
        String telephone = owner.getTelephone();
        Integer match = null;
        if (postcode != null) {
            for (Owner existing : ownerRepository.findAll()) {
                if (lastName.equals(Households.normalizeName(existing.getLastName()))
                        && postcode.equals(normalizePostcode(existing.getPostcode()))
                        && !equalsSafely(telephone, existing.getTelephone())
                        && existing.getId() != null
                        && (match == null || existing.getId() < match)) {
                    match = existing.getId();
                }
            }
        }
        owner.setPossibleDuplicate(match != null);
        owner.setPossibleDuplicateOf(match);
    }

    /** Trim the postcode, treating a null or blank value as absent (no possible-duplicate match). */
    private static String normalizePostcode(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static boolean equalsSafely(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }
}
