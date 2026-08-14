package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Step of {@code POST /api/owners} that records the owner's {@code bulkSignupWarning}: {@code true}
 * when more than 80 owners had already been created on this request's adjusted business day (the
 * effective {@code registrationDate} resolved by {@link ResolveRegistrationDate}) before this owner,
 * otherwise {@code false}.
 *
 * <p>Shares the per-day accumulation path with {@link EnsureDailyLimit} and, like it, runs before
 * {@link SaveOwner}, so the owner being created is not yet persisted and never counts itself.
 */
public class AssignBulkSignupWarning {

    private static final int BULK_THRESHOLD = 80;

    public void service(@Val Owner owner, @Val LocalDate registrationDate,
            OwnerRepository ownerRepository) {
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (registrationDate.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        owner.setBulkSignupWarning(count > BULK_THRESHOLD);
    }
}
