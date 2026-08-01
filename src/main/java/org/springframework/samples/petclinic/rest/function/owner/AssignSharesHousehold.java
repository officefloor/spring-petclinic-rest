package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records whether the new owner shares a household with an existing owner: {@code true} if another
 * owner already has the same address and city at the moment of creation (before it is saved),
 * otherwise {@code false}. Creation is still allowed either way. Runs after {@code normalizeCity} so
 * the comparison uses the canonical city spelling, and before {@code save}.
 */
public class AssignSharesHousehold {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String address = owner.getAddress();
        String city = owner.getCity();
        boolean shares = ownerRepository.findAll().stream()
                .anyMatch(existing -> Objects.equals(existing.getAddress(), address)
                        && Objects.equals(existing.getCity(), city));
        owner.setSharesHousehold(shares);
    }
}
