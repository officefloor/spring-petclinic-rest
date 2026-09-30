package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Derives the owner's {@code capacityWarning} at response time: {@code true} when the owner's city
 * already holds between {@value #WARN_THRESHOLD} and {@link CheckOwnerCityCapacity#CITY_CAPACITY}-1
 * owners (inclusive) — the band approaching the hard capacity limit that
 * {@link CheckOwnerCityCapacity} enforces — otherwise {@code false}. The per-city count is
 * delegated to {@link CheckOwnerCityCapacity#countInCity(OwnerRepository, String)} — the same unit
 * the 409 gate rejects on — so the warning provably tracks the same population that gate governs.
 *
 * <p>Wired into both the create pipeline (where the signal originates) and the single-owner GET
 * pipeline, mirroring {@link DeriveOwnerBulkSignupWarning}, so {@code capacityWarning} is populated
 * (never null) on reads as well as on the create response. The flag is transient and set on the
 * in-flight owner so the responder carries it; it is not persisted.
 */
public class DeriveOwnerCapacityWarning {

    static final int WARN_THRESHOLD = 40;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        long inCity = CheckOwnerCityCapacity.countInCity(ownerRepository, owner.getCity());
        owner.setCapacityWarning(inCity >= WARN_THRESHOLD && inCity < CheckOwnerCityCapacity.CITY_CAPACITY);
    }
}
