package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Objects;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Flags a newly built owner as a soft duplicate of an existing owner. Unlike the hard
 * {@link EnsureUniqueIdentity} check (which rejects an occupied household as a 409), a
 * soft match still creates the owner: it fires when the new owner shares an existing
 * owner's last name (case-insensitive) and postcode but has a different telephone. The new
 * owner is stamped with {@code possibleDuplicate=true} and {@code possibleDuplicateOf} set
 * to the matching owner's id; when no match exists the flag is false and the id null.
 *
 * <p>An owner created as a declared household member ({@code sharesHousehold=true}, which
 * bypassed the duplicate block) is never flagged: a declared member is not a suspected
 * duplicate.
 *
 * <p>Runs after {@link BuildOwner} (so the owner's normalized fields are set) and before
 * {@link SaveOwner} (so the new owner is not yet persisted and cannot match itself), and
 * mutates the built owner in place so the flag is stored and returned with it. The first
 * matching existing owner encountered wins.
 */
public class AssignPossibleDuplicate {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository) {
        if (Boolean.TRUE.equals(request.getSharesHousehold())) {
            owner.setPossibleDuplicate(false);
            owner.setPossibleDuplicateOf(null);
            return;
        }
        for (Owner existing : ownerRepository.findAll()) {
            if (isPossibleDuplicate(owner, existing)) {
                owner.setPossibleDuplicate(true);
                owner.setPossibleDuplicateOf(existing.getId());
                return;
            }
        }
        owner.setPossibleDuplicate(false);
        owner.setPossibleDuplicateOf(null);
    }

    /**
     * Whether {@code candidate} is a soft duplicate of {@code existing}: the same last
     * name (case-insensitive) and the same non-blank postcode, but a different telephone
     * (so it is not the same person re-registering).
     */
    private static boolean isPossibleDuplicate(Owner candidate, Owner existing) {
        return samePostcode(candidate.getPostcode(), existing.getPostcode())
            && sameLastName(candidate.getLastName(), existing.getLastName())
            && !Objects.equals(candidate.getTelephone(), existing.getTelephone());
    }

    private static boolean samePostcode(String a, String b) {
        return a != null && !a.isBlank() && a.equals(b);
    }

    private static boolean sameLastName(String a, String b) {
        return fold(a).equals(fold(b));
    }

    private static String fold(String value) {
        return value == null ? "" : value.strip().toLowerCase(Locale.ROOT);
    }
}
