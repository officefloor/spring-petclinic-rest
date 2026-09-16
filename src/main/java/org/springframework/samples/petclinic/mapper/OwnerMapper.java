package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.IdentityDto;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;
import org.springframework.samples.petclinic.rest.validation.BulkSignupWarningEvaluator;
import org.springframework.samples.petclinic.rest.validation.CityCapacityWarningEvaluator;
import org.springframework.samples.petclinic.rest.validation.RiskFlagEvaluator;

import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public abstract class OwnerMapper {

    /** The version of the owner identity contract every response is shaped by. */
    private static final int API_VERSION = 2;

    @Autowired
    protected BulkSignupWarningEvaluator bulkSignupWarningEvaluator;

    @Autowired
    protected CityCapacityWarningEvaluator cityCapacityWarningEvaluator;

    @Autowired
    protected RiskFlagEvaluator riskFlagEvaluator;

    @Mapping(target = "identity", ignore = true)
    @Mapping(target = "apiVersion", ignore = true)
    public abstract OwnerDto toOwnerDto(Owner owner);

    @Mapping(target = "memberId", source = "identity.memberId")
    @Mapping(target = "householdId", source = "identity.householdId")
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
     * Group the owner's derived identifiers under the response's nested {@code identity} object and
     * stamp the {@code apiVersion}. These are the version-2 identity fields, no longer exposed at the
     * top level.
     */
    @AfterMapping
    protected void applyIdentity(Owner owner, @MappingTarget OwnerDto ownerDto) {
        IdentityDto identity = new IdentityDto();
        identity.setMemberId(owner.getMemberId());
        identity.setHouseholdId(owner.getHouseholdId());
        identity.setIdentityKey(owner.getIdentityKey());
        ownerDto.setIdentity(identity);
        ownerDto.setApiVersion(API_VERSION);
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
     * Stamp the response-only capacity warning onto every mapped owner, since it reflects the
     * current owner count of the owner's city rather than any stored owner field.
     */
    @AfterMapping
    protected void applyCapacityWarning(@MappingTarget OwnerDto ownerDto) {
        ownerDto.setCapacityWarning(this.cityCapacityWarningEvaluator.isWarranted(ownerDto.getCity()));
    }

    /**
     * Stamp the response-only risk flag onto every mapped owner. It combines the owner's stored
     * possible-duplicate state with the per-response disposable-adjacent and capacity signals, so it
     * is evaluated from the source owner rather than any single stored field.
     */
    @AfterMapping
    protected void applyRiskFlag(Owner owner, @MappingTarget OwnerDto ownerDto) {
        ownerDto.setRiskFlag(this.riskFlagEvaluator.isFlagged(owner));
    }
}
