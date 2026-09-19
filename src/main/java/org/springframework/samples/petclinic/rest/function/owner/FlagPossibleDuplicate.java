package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags a soft duplicate at creation time. A hard duplicate (same {@link OwnerIdentity identity
 * key}) has already been rejected by {@link EnsureUniqueIdentity}, so any collision here is a
 * softer one: an existing owner that shares this owner's last name (case-insensitively) and
 * postcode but has a different telephone. Such an owner is still created, but flagged
 * {@code possibleDuplicate = true} with {@code possibleDuplicateOf} set to the matching owner's
 * id; otherwise {@code possibleDuplicate = false}.
 *
 * <p>Runs after {@link BuildOwner} has produced the entity and before {@link SaveOwner} persists
 * it, so {@code findAll()} sees only the owners that existed before this create. The flags are
 * stored on the owner and returned on every later read.
 */
public class FlagPossibleDuplicate {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        owner.setPossibleDuplicate(false);
        owner.setPossibleDuplicateOf(null);

        String postcode = owner.getPostcode();
        if (owner.getLastName() == null || postcode == null || postcode.isBlank()) {
            return;
        }
        String telephone = TelephoneNormalizer.toE164(owner.getTelephone());
        for (Owner existing : ownerRepository.findAll()) {
            if (owner.getLastName().equalsIgnoreCase(existing.getLastName())
                    && postcode.equals(existing.getPostcode())
                    && !equalsIgnoreNull(telephone, TelephoneNormalizer.toE164(existing.getTelephone()))) {
                owner.setPossibleDuplicate(true);
                owner.setPossibleDuplicateOf(existing.getId());
                return;
            }
        }
    }

    private static boolean equalsIgnoreNull(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }
}
