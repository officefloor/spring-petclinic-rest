package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's {@code bulkSignupWarning}: true when more than 80 owners have
 * already been registered on this owner's registration date, otherwise false. Reads the
 * per-day total published by {@link CountDailyRegistrations} — the same accumulation the
 * daily-registration-limit rule checks — which is taken before the owner is saved, so the
 * count excludes the owner being created. The flag is stamped on the owner so it is
 * persisted and returned on read, and never changes afterwards.
 */
public class AssignBulkSignupWarning {

    /** Threshold above which a create is flagged as part of a bulk signup. */
    private static final int BULK_SIGNUP_THRESHOLD = 80;

    public void service(@Val Owner owner, @Val Integer dailyRegistrationCount) {
        owner.setBulkSignupWarning(dailyRegistrationCount > BULK_SIGNUP_THRESHOLD);
    }
}
