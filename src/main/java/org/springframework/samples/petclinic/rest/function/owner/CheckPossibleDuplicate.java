package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags a soft (non-hard) duplicate: a <em>suspected</em>, undeclared duplicate across
 * households. By the time this step runs the request has already passed
 * {@link CheckOwnerIdentityUnique}, so it is neither a full-identity nor an (undeclared)
 * household duplicate. This step records a weaker signal: an existing owner that shares this
 * owner's {@code lastName} (compared case-insensitively) and {@code postcode} but has a
 * <em>different</em> telephone <em>and a different {@code householdId}</em>. Such an owner is
 * still created, but with {@code possibleDuplicate = true} and {@code possibleDuplicateOf} set
 * to the matching owner's id (the lowest matching id when several match). When nothing matches,
 * {@code possibleDuplicate} is {@code false} and {@code possibleDuplicateOf} stays absent.
 *
 * <p>Because {@code householdId} is now computed from last name and postcode, any owner that
 * shares both with an existing one is in the <em>same</em> household. Such a pairing is handled
 * by the household-duplicate block (rejected, or admitted as a declared member): a declared
 * member is not a suspected duplicate, so the same-household exclusion keeps it unflagged.
 *
 * <p>Runs after {@link CheckOwnerIdentityUnique} and before {@link SaveOwner} (so the new
 * owner is not yet among {@code findAll()} and cannot match itself), mutating the built
 * {@link Owner} in place.
 */
public class CheckPossibleDuplicate {

    public void service(@Val Owner built, OwnerRepository ownerRepository) {
        Integer matchId = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue; // a soft-deleted owner is not a suspected duplicate
            }
            if (sameLastName(built, existing) && samePostcode(built, existing)
                    && differentTelephone(built, existing) && differentHousehold(built, existing)) {
                Integer id = existing.getId();
                if (id != null && (matchId == null || id < matchId)) {
                    matchId = id;
                }
            }
        }
        built.setPossibleDuplicate(matchId != null);
        built.setPossibleDuplicateOf(matchId);
    }

    private static boolean sameLastName(Owner a, Owner b) {
        return normalize(a.getLastName()).equals(normalize(b.getLastName()));
    }

    private static boolean samePostcode(Owner a, Owner b) {
        String pa = a.getPostcode();
        String pb = b.getPostcode();
        return pa != null && !pa.isBlank() && pa.equals(pb);
    }

    private static boolean differentTelephone(Owner a, Owner b) {
        String ta = a.getTelephone();
        String tb = b.getTelephone();
        return ta == null ? tb != null : !ta.equals(tb);
    }

    /** A declared household member (same computed householdId) is not a suspected duplicate. */
    private static boolean differentHousehold(Owner a, Owner b) {
        String ha = a.getHouseholdId();
        return ha == null || !ha.equals(b.getHouseholdId());
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
