package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;
import org.springframework.samples.petclinic.rest.validation.BulkSignupWarningEvaluator;
import org.springframework.samples.petclinic.rest.validation.HouseholdMembershipTierEvaluator;

import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public abstract class OwnerMapper {

    @Autowired
    protected BulkSignupWarningEvaluator bulkSignupWarningEvaluator;

    @Autowired
    protected HouseholdMembershipTierEvaluator householdMembershipTierEvaluator;

    public abstract OwnerDto toOwnerDto(Owner owner);

    public abstract Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    public abstract Owner toOwner(OwnerFieldsDto ownerDto);

    public abstract List<OwnerDto> toOwnerDtoCollection(Collection<Owner> ownerCollection);

    public abstract Collection<Owner> toOwners(Collection<OwnerDto> ownerDtos);

    public OwnerPageDto toOwnerPageDto(@NonNull Page<Owner> ownerPage) {
        OwnerPageDto ownerPageDto = new OwnerPageDto();
        ownerPageDto.setContent(toOwnerDtoCollection(ownerPage.getContent()));
        ownerPageDto.setPage(ownerPage.getNumber());
        ownerPageDto.setSize(ownerPage.getSize());
        ownerPageDto.setTotalElements(ownerPage.getTotalElements());
        ownerPageDto.setTotalPages(ownerPage.getTotalPages());
        return ownerPageDto;
    }

    /**
     * Stamp the response-only bulk-signup warning onto every mapped owner, since it reflects the
     * current day's registration volume rather than any stored owner field.
     */
    @AfterMapping
    protected void applyBulkSignupWarning(@MappingTarget OwnerDto ownerDto) {
        ownerDto.setBulkSignupWarning(this.bulkSignupWarningEvaluator.isWarranted());
    }

    /**
     * Upgrade the owner's membership tier to {@code GOLD} when its household is large enough,
     * superseding the individual {@code BRONZE}/{@code SILVER} tier already mapped from the entity.
     * The household size depends on how many owners currently share the household id, so it is
     * evaluated per response rather than stored on the owner.
     */
    @AfterMapping
    protected void applyHouseholdMembershipTier(@MappingTarget OwnerDto ownerDto) {
        if (this.householdMembershipTierEvaluator.qualifiesForGold(ownerDto.getHouseholdId())) {
            ownerDto.setMembershipTier(HouseholdMembershipTierEvaluator.GOLD_TIER);
        }
    }
}
