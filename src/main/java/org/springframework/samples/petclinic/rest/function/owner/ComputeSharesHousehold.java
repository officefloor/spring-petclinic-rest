package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records whether this owner shares a household with an existing one: {@code true} when
 * another owner already has the same address and city at the moment of creation, else
 * {@code false}.
 *
 * <p>Address and city are compared ignoring letter case and surrounding or repeated
 * whitespace, consistent with the rest of the owner-creation pipeline. Runs before the
 * owner is saved, so the comparison excludes this owner itself.
 */
public class ComputeSharesHousehold {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String address = normalize(owner.getAddress());
        String city = normalize(owner.getCity());
        boolean shares = false;
        if (address != null && city != null) {
            for (Owner existing : ownerRepository.findAll()) {
                if (isDifferentOwner(existing, owner)
                        && address.equals(normalize(existing.getAddress()))
                        && city.equals(normalize(existing.getCity()))) {
                    shares = true;
                    break;
                }
            }
        }
        owner.setSharesHousehold(shares);
    }

    private static boolean isDifferentOwner(Owner existing, Owner candidate) {
        return existing.getId() == null || !existing.getId().equals(candidate.getId());
    }

    /**
     * Lower-cases and collapses whitespace so comparisons ignore letter case and
     * surrounding or repeated whitespace. Returns {@code null} when blank.
     */
    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }
}
