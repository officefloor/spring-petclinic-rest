package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Runs after the owner is built but before it is saved. By this point the request has already passed
 * {@link EnsureUniqueOwnerIdentity}, so it is <em>not</em> a hard duplicate. This step records a
 * <em>soft</em> match: when the new owner shares an existing owner's {@code lastName} (case-insensitive)
 * and {@code postcode} but has a different (normalized) telephone, it is still created, with the
 * owner's {@code possibleDuplicate} flag set to true and {@code possibleDuplicateOf} set to the matching
 * owner's id. Otherwise {@code possibleDuplicate} is false and {@code possibleDuplicateOf} is null. Both
 * values are mutated in place via {@code @Val} for the save/respond steps to persist and return.
 *
 * <p>A declared household member (the request opted in with {@code sharesHousehold: true}) is
 * <em>never</em> flagged: it shares an existing owner's household by design, so it is not a suspected
 * duplicate.
 *
 * <p>When more than one existing owner soft-matches, the lowest id is recorded.
 */
public class FlagPossibleDuplicate {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository) {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            owner.setPossibleDuplicate(false);
            owner.setPossibleDuplicateOf(null);
            return;
        }
        String lastName = owner.getLastName();
        String postcode = owner.getPostcode();
        String telephone = owner.getTelephone();
        Integer matchId = null;
        if (postcode != null && !postcode.isBlank()) {
            for (Owner existing : ownerRepository.findAll()) {
                if (Boolean.TRUE.equals(existing.getDeleted())) {
                    continue;
                }
                if (equalsIgnoreCase(lastName, existing.getLastName())
                        && postcode.equals(existing.getPostcode())
                        && !equalsNullSafe(telephone, existing.getTelephone())
                        && existing.getId() != null
                        && (matchId == null || existing.getId() < matchId)) {
                    matchId = existing.getId();
                }
            }
        }
        owner.setPossibleDuplicate(matchId != null);
        owner.setPossibleDuplicateOf(matchId);
    }

    private static boolean equalsIgnoreCase(String a, String b) {
        return a == null ? b == null : a.equalsIgnoreCase(b);
    }

    private static boolean equalsNullSafe(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }
}
