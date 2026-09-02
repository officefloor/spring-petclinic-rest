package org.springframework.samples.petclinic.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.MappingTarget;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.stereotype.Component;

/**
 * Upgrades an owner's {@code membershipTier} to {@code GOLD} when the owner's household
 * (owners sharing the same {@code householdId}) has three or more members. Runs as a MapStruct
 * {@code @AfterMapping} step, so the household-size rule stays a small, self-contained unit and
 * the BRONZE/SILVER expression on {@link OwnerMapper} is left untouched.
 */
@Component
public class OwnerHouseholdTierPostProcessor {

    static final int GOLD_HOUSEHOLD_SIZE = 3;

    private final ClinicService clinicService;

    public OwnerHouseholdTierPostProcessor(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    @AfterMapping
    public void applyGoldTier(Owner owner, @MappingTarget OwnerDto ownerDto) {
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            return;
        }
        long members = clinicService.findAllOwners().stream()
            .filter(other -> householdId.equals(other.getHouseholdId()))
            .count();
        if (members >= GOLD_HOUSEHOLD_SIZE) {
            ownerDto.setMembershipTier("GOLD");
        }
    }
}
