package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Derives the owner's {@code bulkSignupWarning} at response time: {@code true} when more than
 * {@value #BULK_THRESHOLD} owners have already been created on this owner's registration day,
 * otherwise {@code false}. The day count is delegated to
 * {@link RegistrationDates#countRegisteredOn(LocalDate, OwnerRepository)} — the same per-business-day
 * accumulation the daily-limit rule uses (see {@link CheckOwnerDailyLimit}) — so the warning
 * provably tracks the same population that the daily cap governs.
 *
 * <p>Wired into both the create pipeline (where the signal originates) and the single-owner GET
 * pipeline. The GET wiring is deliberate and exists purely for DTO consistency: it keeps
 * {@code bulkSignupWarning} populated (never null) on reads rather than only on the create
 * response. The flag is transient and set on the in-flight owner so the responder carries it; it
 * is not persisted.
 */
public class DeriveOwnerBulkSignupWarning {

    static final int BULK_THRESHOLD = 80;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        long registeredThatDay = RegistrationDates.countRegisteredOn(owner.getRegistrationDate(), ownerRepository);
        owner.setBulkSignupWarning(registeredThatDay > BULK_THRESHOLD);
    }
}
