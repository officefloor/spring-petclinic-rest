package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Runs in the create-owner pipeline after {@link DefaultOwnerRegistrationDate} (so the owner's
 * registration date is already resolved to its business day) and before {@link SaveOwner}. Counts
 * the existing owners already registered on that same business day — the same per-day bucket the
 * {@link EnsureDailyOwnerLimit} rule accumulates — and stamps a bulk-signup warning onto the built
 * owner in place: true when more than 80 owners had already been created for that day, otherwise
 * false. As it runs before the owner is saved, the count excludes the owner being created, and the
 * evaluated flag is fixed at creation and returned unchanged on later reads.
 */
public class AssignBulkSignupWarning {

    private static final int BULK_SIGNUP_THRESHOLD = 80;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        LocalDate businessDay = owner.getRegistrationDate();
        long count = ownerRepository.findAll().stream()
                .filter(existing -> businessDay.equals(existing.getRegistrationDate()))
                .count();
        owner.setBulkSignupWarning(count > BULK_SIGNUP_THRESHOLD);
    }
}
