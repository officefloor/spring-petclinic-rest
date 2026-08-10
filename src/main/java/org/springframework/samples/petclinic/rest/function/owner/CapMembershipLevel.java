package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.util.OptionalInt;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.util.MembershipLevel;

/**
 * Runs after {@link CountNamesakes} and {@link CountHouseholdMembers} (so the owner's
 * {@code namesakeCount} and {@code householdSize} are set) and before the owner is saved. Derives the
 * new owner's membership level and caps it at one above the highest membership level currently held by
 * an existing member of the same household — the owners already sharing this owner's deterministic
 * {@code householdId} (keyed on lastName + postcode, the same way {@link CountHouseholdMembers} finds
 * them). With no existing household member no cap applies. The (possibly capped) level is stored on the
 * owner via {@code @Val} so {@code SaveOwner} persists it and the response mapper returns it verbatim.
 */
public class CapMembershipLevel {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        LocalDate today = LocalDate.now();
        int level = levelOf(owner, today);
        String householdId = owner.getHouseholdId();
        if (householdId != null) {
            OptionalInt maxExisting = ownerRepository.findAll().stream()
                    .filter(existing -> householdId.equals(
                            OwnerIdentity.householdId(existing.getLastName(), existing.getPostcode())))
                    .mapToInt(existing -> levelOf(existing, today))
                    .max();
            if (maxExisting.isPresent()) {
                level = Math.min(level, maxExisting.getAsInt() + 1);
            }
        }
        owner.setMembershipLevel(level);
    }

    /** An owner's membership level: the stored value when already assigned, otherwise derived from its
     *  fields the same way the response mapper derives it. */
    private static int levelOf(Owner owner, LocalDate today) {
        Integer stored = owner.getMembershipLevel();
        if (stored != null) {
            return stored;
        }
        return MembershipLevel.level(MembershipLevel.points(owner.getEmail(), owner.getNamesakeCount(),
                owner.getHouseholdSize(), owner.getRegistrationDate(), today));
    }
}
