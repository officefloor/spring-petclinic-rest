package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;
import org.springframework.samples.petclinic.rest.controller.OwnerIdentity;
import org.springframework.samples.petclinic.service.BulkSignupWarningEvaluator;
import org.springframework.samples.petclinic.service.MembershipLevelEvaluator;

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
    protected MembershipLevelEvaluator membershipLevelEvaluator;

    @Autowired
    protected OwnerIdentity ownerIdentity;

    @Mapping(target = "identityKey", expression = "java(owner == null ? null : ownerIdentity.key(owner))")
    @Mapping(target = "displayName", expression = "java(formatDisplayName(owner))")
    @Mapping(target = "initials", expression = "java(formatInitials(owner))")
    @Mapping(target = "membershipNumber", expression = "java(formatMembershipNumber(owner))")
    @Mapping(target = "membershipLevel", expression = "java(membershipLevelEvaluator.levelFor(owner))")
    @Mapping(target = "locality", expression = "java(org.springframework.samples.petclinic.model.Locality.forOwner(owner))")
    @Mapping(target = "bulkSignupWarning", expression = "java(bulkSignupWarningEvaluator.isWarranted(owner))")
    @Mapping(target = "contactPreference", expression = "java(org.springframework.samples.petclinic.model.ContactPreference.forOwner(owner))")
    public abstract OwnerDto toOwnerDto(Owner owner);

    /**
     * Formats an owner's membership number as {@code "<customerCode>-M<YY>"}, where
     * {@code YY} is the last two digits of the registration date's year
     * (e.g. {@code "LON-SMI-0007-M26"}). Returns {@code null} until both the customer code
     * and registration date have been assigned.
     */
    protected String formatMembershipNumber(Owner owner) {
        if (owner == null || owner.getCustomerCode() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        return String.format("%s-M%02d", owner.getCustomerCode(), owner.getRegistrationDate().getYear() % 100);
    }

    /**
     * Formats an owner's stored names as {@code "LastName, FirstName"} for display.
     */
    protected String formatDisplayName(Owner owner) {
        if (owner == null) {
            return null;
        }
        return owner.getLastName() + ", " + owner.getFirstName();
    }

    /**
     * Builds an owner's initials as the upper-cased first letters of the first and last
     * name, dot-separated with a trailing dot (e.g. {@code "J.S."}).
     */
    protected String formatInitials(Owner owner) {
        if (owner == null) {
            return null;
        }
        return initial(owner.getFirstName()) + initial(owner.getLastName());
    }

    private String initial(String name) {
        return Character.toUpperCase(name.charAt(0)) + ".";
    }

    public abstract Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    @Mapping(target = "customerCode", ignore = true)
    @Mapping(target = "householdId", ignore = true)
    @Mapping(target = "namesakeCount", ignore = true)
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
}
