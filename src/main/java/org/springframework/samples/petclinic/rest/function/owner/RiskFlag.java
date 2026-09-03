package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Set;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Combined risk signal for an owner: true when the owner is a possible duplicate, its
 * email domain is disposable-adjacent, or its city is over soft capacity; false
 * otherwise. Recomputed from persisted state whenever the owner is mapped, so the GET
 * read-back and the create response agree even though the create-time flags are transient.
 */
public final class RiskFlag {

    /** Domains adjacent to a known disposable provider: the same second-level label as one
     *  of {@link CheckEmailDomain}'s blocked domains, regardless of the TLD used. */
    private static final Set<String> DISPOSABLE_LABELS = Set.of(
            "mailinator", "tempmail", "guerrillamail");

    private RiskFlag() {
    }

    public static boolean of(Owner owner, OwnerRepository ownerRepository) {
        return isPossibleDuplicate(owner, ownerRepository)
                || isDisposableAdjacent(owner.getEmail())
                || isOverSoftCapacity(owner, ownerRepository);
    }

    /** Mirrors {@link FlagPossibleDuplicate}: a non-household owner sharing another owner's
     *  postcode and phonetic lastName but with a different identityKey. */
    private static boolean isPossibleDuplicate(Owner owner, OwnerRepository ownerRepository) {
        String postcode = owner.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            return false;
        }
        String soundex = Soundex.code(owner.getLastName());
        String key = CheckUniqueIdentity.identityKey(owner);
        for (Owner other : ownerRepository.findAll()) {
            if (!other.getId().equals(owner.getId())
                    && postcode.equals(other.getPostcode())
                    && soundex.equals(Soundex.code(other.getLastName()))
                    && !key.equals(CheckUniqueIdentity.identityKey(other))
                    && !sharesHousehold(owner, other)) {
                return true;
            }
        }
        return false;
    }

    private static boolean sharesHousehold(Owner owner, Owner other) {
        return owner.getHouseholdId() != null && owner.getHouseholdId().equals(other.getHouseholdId());
    }

    /** Over soft capacity once the city holds at least {@link FlagCapacityWarning#WARN_THRESHOLD}
     *  other owners (the warning band below {@link CheckCityCapacity}'s hard limit). */
    private static boolean isOverSoftCapacity(Owner owner, OwnerRepository ownerRepository) {
        String city = CheckCityCapacity.normalize(owner.getCity());
        long count = ownerRepository.findAll().stream()
                .filter(other -> !other.getId().equals(owner.getId()))
                .filter(other -> city.equals(CheckCityCapacity.normalize(other.getCity())))
                .count();
        return count >= FlagCapacityWarning.WARN_THRESHOLD;
    }

    private static boolean isDisposableAdjacent(String email) {
        if (email == null) {
            return false;
        }
        int at = email.lastIndexOf('@');
        if (at < 0) {
            return false;
        }
        String domain = email.substring(at + 1).toLowerCase();
        int dot = domain.indexOf('.');
        String label = dot < 0 ? domain : domain.substring(0, dot);
        return DISPOSABLE_LABELS.contains(label);
    }
}
