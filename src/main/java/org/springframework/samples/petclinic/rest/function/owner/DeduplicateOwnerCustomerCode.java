package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Set;
import java.util.stream.Collectors;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * De-duplicates the owner's {@code customerCode} against existing owners. The code assigned by
 * {@link AssignOwnerCustomerCode} is a pure function of postcode, telephone and last name, so two
 * distinct owners can in principle compute the same code. When the computed code already belongs to
 * an existing owner, this step appends {@code -<n>} with the smallest {@code n} of 2 or more that
 * makes it unique (e.g. {@code NSW-1A2B3C4D} → {@code NSW-1A2B3C4D-2}), guaranteeing distinct owners
 * always receive distinct customer codes.
 *
 * <p>Wired after {@link AssignOwnerCustomerCode} and before the create is saved, so the existing
 * owners read here never include this owner.
 */
public class DeduplicateOwnerCustomerCode {

    /** The smallest suffix appended on a collision. */
    private static final int FIRST_SUFFIX = 2;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String base = owner.getCustomerCode();
        Set<String> existing = ownerRepository.findAll().stream()
                .map(Owner::getCustomerCode)
                .collect(Collectors.toSet());
        if (!existing.contains(base)) {
            return;
        }
        String unique;
        for (int n = FIRST_SUFFIX; existing.contains(unique = CustomerCode.withSuffix(base, n)); n++) {
            // advance to the next suffix until the code is unique
        }
        owner.setCustomerCode(unique);
    }
}
