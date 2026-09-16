package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags a high-volume signup day: sets {@link Owner#getBulkSignupWarning() bulkSignupWarning}
 * true when more than {@value #BULK_SIGNUP_THRESHOLD} owners have already been registered on
 * the new owner's (adjusted, business-day) {@link Owner#getRegistrationDate() registrationDate},
 * otherwise false. Runs after the registration date has been resolved so it buckets by the same
 * day the new owner would be stored under.
 */
public class FlagOwnerBulkSignup {

    /** Owners already registered on the day above which the warning is raised. */
    static final int BULK_SIGNUP_THRESHOLD = 80;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        long registeredThatDay = OwnerRegistrations.countOn(ownerRepository, owner.getRegistrationDate());
        owner.setBulkSignupWarning(registeredThatDay > BULK_SIGNUP_THRESHOLD);
    }
}
