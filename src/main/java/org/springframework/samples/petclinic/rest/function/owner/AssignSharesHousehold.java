package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags whether the new owner shares a household with an existing one: {@code true} when another
 * owner already has the same address and city at the moment of creation, {@code false} otherwise.
 * The new owner is not yet persisted, so every stored owner is a distinct candidate. The comparison
 * ignores letter case and surrounding or repeated whitespace (see {@link DuplicateKey}). Creation is
 * always allowed regardless of the result.
 */
public class AssignSharesHousehold {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String address = DuplicateKey.normalize(owner.getAddress());
        String city = DuplicateKey.normalize(owner.getCity());
        boolean shares = false;
        if (address != null && city != null) {
            for (Owner existing : ownerRepository.findAll()) {
                if (Objects.equals(DuplicateKey.normalize(existing.getAddress()), address)
                        && Objects.equals(DuplicateKey.normalize(existing.getCity()), city)) {
                    shares = true;
                    break;
                }
            }
        }
        owner.setSharesHousehold(shares);
    }
}
