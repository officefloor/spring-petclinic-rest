package org.springframework.samples.petclinic.service;

import java.time.LocalDate;
import java.time.Period;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Business rule: when an owner supplies a {@code birthDate}, its {@code ageBand} is derived against
 * the registration date as 'MINOR' (under 18), 'ADULT' (18-64) or 'SENIOR' (65+). Kept as a small,
 * self-contained unit so the rule can be applied from the read flow without adding complexity to the
 * mapper, controller, or service.
 */
public final class OwnerAgeBandPolicy {

    private OwnerAgeBandPolicy() {
    }

    /**
     * Derive the {@code ageBand} for the given owner, or {@code null} when no birthDate is present.
     *
     * @param owner the owner whose age band to derive
     * @return 'MINOR', 'ADULT', 'SENIOR', or {@code null}
     */
    public static String ageBand(Owner owner) {
        LocalDate birthDate = owner.getBirthDate();
        if (birthDate == null) {
            return null;
        }
        LocalDate asOf = owner.getRegistrationDate() != null ? owner.getRegistrationDate() : LocalDate.now();
        int age = Period.between(birthDate, asOf).getYears();
        if (age < 18) {
            return "MINOR";
        }
        return age < 65 ? "ADULT" : "SENIOR";
    }
}
