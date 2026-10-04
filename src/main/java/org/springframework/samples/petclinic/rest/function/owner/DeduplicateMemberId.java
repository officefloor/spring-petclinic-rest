package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * De-duplicates the owner's {@code memberId} against the ids already in the data store.
 * {@link AssignMemberId} derives the id deterministically from the owner's own fields, so two
 * distinct owners can in principle collide. When the assigned id matches an existing owner's
 * {@code memberId}, this step finds the smallest {@code n} of 2 or more that makes it unique and
 * applies {@link OwnerMemberId#withSuffix} (e.g. {@code NSW271A2B3C4D5} → {@code NSW271A2B3C4D5-2}).
 * The suffix spelling lives in {@link OwnerMemberId}, which owns the {@code memberId} format; this
 * step contributes only the orchestration — gather existing ids, pick a free {@code n}.
 *
 * <p>Runs within the create transaction after {@link AssignMemberId} and before {@link SaveOwner},
 * so every other value derived from the id sees the de-duplicated value. The owner being created has
 * not been saved yet, so it is not among the existing ids compared against.
 */
public class DeduplicateMemberId {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String base = owner.getMemberId();
        Set<String> existing = new HashSet<>();
        for (Owner other : ownerRepository.findAll()) {
            existing.add(other.getMemberId());
        }
        if (!existing.contains(base)) {
            return;
        }
        int n = 2;
        String candidate;
        do {
            candidate = OwnerMemberId.withSuffix(base, n);
            n++;
        }
        while (existing.contains(candidate));
        owner.setMemberId(candidate);
    }
}
