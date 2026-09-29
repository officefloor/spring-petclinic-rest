package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Set;
import java.util.stream.Collectors;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * De-duplicates the owner's {@code memberId} against existing owners. The id assigned by
 * {@link AssignOwnerMemberId} is (apart from its check digit) a pure function of postcode, registration
 * date, telephone and last name, so two distinct owners can in principle compute the same id. When the
 * computed id already belongs to an existing owner, this step appends {@code -<n>} with the smallest
 * {@code n} of 2 or more that makes it unique (e.g. {@code NSW261A2B3C4D7} → {@code NSW261A2B3C4D7-2}),
 * guaranteeing distinct owners always receive distinct member ids.
 *
 * <p>Wired after {@link AssignOwnerMemberId} and before the create is saved, so the existing owners read
 * here never include this owner.
 */
public class DeduplicateOwnerMemberId {

    /** The smallest suffix appended on a collision. */
    private static final int FIRST_SUFFIX = 2;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String base = owner.getMemberId();
        Set<String> existing = ownerRepository.findAll().stream()
                .map(Owner::getMemberId)
                .collect(Collectors.toSet());
        if (!existing.contains(base)) {
            return;
        }
        String unique;
        for (int n = FIRST_SUFFIX; existing.contains(unique = MemberId.withSuffix(base, n)); n++) {
            // advance to the next suffix until the id is unique
        }
        owner.setMemberId(unique);
    }
}
