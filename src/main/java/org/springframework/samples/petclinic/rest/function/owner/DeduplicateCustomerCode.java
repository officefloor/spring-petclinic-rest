package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * De-duplicates the owner's {@code customerCode} against the codes already in the data store.
 * {@link AssignCustomerCode} derives the code deterministically from the owner's own fields, so two
 * distinct owners can in principle collide. When the assigned code matches an existing owner's
 * {@code customerCode}, this step finds the smallest {@code n} of 2 or more that makes it unique and
 * applies {@link OwnerCustomerCode#withSuffix} (e.g. {@code NSW-1A2B3C4D} → {@code NSW-1A2B3C4D-2}).
 * The suffix spelling lives in {@link OwnerCustomerCode}, which owns the {@code customerCode} format;
 * this step contributes only the orchestration — gather existing codes, pick a free {@code n}.
 *
 * <p>Runs within the create transaction after {@link AssignCustomerCode} and before
 * {@link AssignMembershipNumber}, so the membership number and every other value derived from the
 * code see the de-duplicated value. The owner being created has not been saved yet, so it is not
 * among the existing codes compared against.
 */
public class DeduplicateCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String base = owner.getCustomerCode();
        Set<String> existing = new HashSet<>();
        for (Owner other : ownerRepository.findAll()) {
            existing.add(other.getCustomerCode());
        }
        if (!existing.contains(base)) {
            return;
        }
        int n = 2;
        String candidate;
        do {
            candidate = OwnerCustomerCode.withSuffix(base, n);
            n++;
        }
        while (existing.contains(candidate));
        owner.setCustomerCode(candidate);
    }
}
