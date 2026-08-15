package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Step of {@code POST /api/owners} that flags a bulk signup: when more than {@value
 * #BULK_SIGNUP_THRESHOLD} owners have already been created for this request's business day, counted
 * by {@link Owner#getRegistrationDate()}, the new owner is stamped with {@code bulkSignupWarning}
 * true; otherwise false. The day counted is the adjusted business day resolved by {@link
 * ResolveRegistrationDate} (weekends roll forward to Monday), the same date {@link BuildOwner}
 * stamps on the new owner — the same accumulation path as the daily create-limit rule.
 *
 * <p>Runs after {@link BuildOwner} so the owner exists to stamp, and before {@link SaveOwner} so the
 * count reflects only owners already persisted against that business day, never this owner itself.
 * {@code @Val} yields the built owner, mutated in place and persisted by the save step.
 */
public class AssignBulkSignupWarning {

    /** The new owner warns of a bulk signup once more than this many owners share its business day. */
    static final int BULK_SIGNUP_THRESHOLD = 80;

    public void service(@Val Owner owner, @Val LocalDate registrationDate,
            OwnerRepository ownerRepository) {
        long count = ownerRepository.findAll().stream()
                .map(Owner::getRegistrationDate)
                .filter(registrationDate::equals)
                .count();
        owner.setBulkSignupWarning(count > BULK_SIGNUP_THRESHOLD);
    }
}
