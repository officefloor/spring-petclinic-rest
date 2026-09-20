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
import org.springframework.samples.petclinic.rest.controller.CityCapacityWarningEvaluator;
import org.springframework.samples.petclinic.rest.controller.OwnerIdentity;
import org.springframework.samples.petclinic.rest.controller.TelephoneFormatter;
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
    protected CityCapacityWarningEvaluator cityCapacityWarningEvaluator;

    @Autowired
    protected OwnerIdentity ownerIdentity;

    @Autowired
    protected TelephoneFormatter telephoneFormatter;

    @Mapping(target = "selfLink", expression = "java(formatSelfLink(owner))")
    @Mapping(target = "identityKey", expression = "java(owner == null ? null : ownerIdentity.key(owner))")
    @Mapping(target = "displayName", expression = "java(formatDisplayName(owner))")
    @Mapping(target = "salutation", expression = "java(org.springframework.samples.petclinic.model.Salutation.forOwner(owner))")
    @Mapping(target = "initials", expression = "java(formatInitials(owner))")
    @Mapping(target = "telephoneDisplay", expression = "java(owner == null ? null : telephoneFormatter.format(owner.getTelephone()))")
    @Mapping(target = "fiscalYear", expression = "java(formatFiscalYear(owner))")
    @Mapping(target = "membershipNumber", expression = "java(org.springframework.samples.petclinic.model.MembershipNumber.forOwner(owner))")
    @Mapping(target = "checkDigit", expression = "java(org.springframework.samples.petclinic.model.CheckDigit.forOwner(owner))")
    @Mapping(target = "membershipPoints", expression = "java(membershipLevelEvaluator.pointsFor(owner))")
    @Mapping(target = "membershipLevel", expression = "java(membershipLevelEvaluator.levelFor(owner))")
    @Mapping(target = "locality", expression = "java(formatLocality(owner))")
    @Mapping(target = "timezone", expression = "java(formatTimezone(owner))")
    @Mapping(target = "bulkSignupWarning", expression = "java(bulkSignupWarningEvaluator.isWarranted(owner))")
    @Mapping(target = "capacityWarning", expression = "java(cityCapacityWarningEvaluator.isWarranted(owner))")
    @Mapping(target = "contactPreference", expression = "java(org.springframework.samples.petclinic.model.ContactPreference.forOwner(owner))")
    @Mapping(target = "ageBand", expression = "java(org.springframework.samples.petclinic.model.AgeBand.forOwner(owner))")
    @Mapping(target = "ownerSegment", expression = "java(formatOwnerSegment(owner))")
    public abstract OwnerDto toOwnerDto(Owner owner);

    /**
     * Builds an owner's canonical API path, {@code "/api/owners/<id>"}. Returns
     * {@code null} for a {@code null} owner or one that has not been assigned an id yet.
     */
    protected String formatSelfLink(Owner owner) {
        if (owner == null || owner.getId() == null) {
            return null;
        }
        return "/api/owners/" + owner.getId();
    }

    /**
     * Derives an owner's fiscal year (starting 1 July) from its business-day-adjusted
     * registration date, formatted {@code "FY<YY>"}. Returns {@code null} for a {@code null}
     * owner or one whose registration date has not been assigned yet.
     */
    protected String formatFiscalYear(Owner owner) {
        if (owner == null || owner.getRegistrationDate() == null) {
            return null;
        }
        return org.springframework.samples.petclinic.model.FiscalYear
            .containing(owner.getRegistrationDate()).label();
    }

    /**
     * Derives an owner's locality from its region-and-hash identity: the {@code <REGION>}
     * segment of the assigned {@code customerCode}. Owners without a code yet fall back to
     * deriving the region straight from their own fields.
     */
    protected String formatLocality(Owner owner) {
        if (owner == null) {
            return null;
        }
        if (owner.getCustomerCode() != null) {
            return org.springframework.samples.petclinic.model.Locality.fromCustomerCode(owner.getCustomerCode());
        }
        return org.springframework.samples.petclinic.model.Locality.forOwner(owner);
    }

    /**
     * Derives an owner's IANA timezone from its locality region using the fixed
     * region-to-timezone table. Returns {@code null} for a {@code null} owner or when
     * the region has no known timezone.
     */
    protected String formatTimezone(Owner owner) {
        if (owner == null) {
            return null;
        }
        return org.springframework.samples.petclinic.model.RegionTimezone.forRegion(formatLocality(owner));
    }

    /**
     * Derives an owner's segment, {@code "<TIER>_<AREA>"}, from its membership level and
     * locality. Returns {@code null} for a {@code null} owner.
     */
    protected String formatOwnerSegment(Owner owner) {
        if (owner == null) {
            return null;
        }
        return org.springframework.samples.petclinic.model.OwnerSegment
            .of(membershipLevelEvaluator.levelFor(owner), formatLocality(owner));
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
    @Mapping(target = "possibleDuplicate", ignore = true)
    @Mapping(target = "possibleDuplicateOf", ignore = true)
    @Mapping(target = "deleted", ignore = true)
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
