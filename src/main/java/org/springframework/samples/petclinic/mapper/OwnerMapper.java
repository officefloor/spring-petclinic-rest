package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;
import org.springframework.samples.petclinic.util.AgeBand;
import org.springframework.samples.petclinic.util.ContactPreference;
import org.springframework.samples.petclinic.util.CustomerCode;
import org.springframework.samples.petclinic.util.FiscalYear;
import org.springframework.samples.petclinic.util.IdentityKey;
import org.springframework.samples.petclinic.util.Luhn;
import org.springframework.samples.petclinic.util.MembershipLevel;
import org.springframework.samples.petclinic.util.MembershipPoints;
import org.springframework.samples.petclinic.util.OwnerSegment;
import org.springframework.samples.petclinic.util.RegionTimezone;
import org.springframework.samples.petclinic.util.Salutation;
import org.springframework.samples.petclinic.util.Telephone;

import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class, imports = IdentityKey.class)
public interface OwnerMapper {

    @Mapping(target = "salutation", expression = "java(salutation(owner))")
    @Mapping(target = "displayName",
        expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials", expression = "java(initials(owner))")
    @Mapping(target = "membershipPoints", expression = "java(membershipPoints(owner))")
    @Mapping(target = "membershipLevel", expression = "java(membershipLevel(owner))")
    @Mapping(target = "ownerSegment", expression = "java(ownerSegment(owner))")
    @Mapping(target = "checkDigit", expression = "java(checkDigit(owner))")
    @Mapping(target = "locality", expression = "java(locality(owner))")
    @Mapping(target = "timezone", expression = "java(timezone(owner))")
    @Mapping(target = "contactPreference", expression = "java(contactPreference(owner))")
    @Mapping(target = "telephoneDisplay", expression = "java(telephoneDisplay(owner))")
    @Mapping(target = "ageBand", expression = "java(ageBand(owner))")
    @Mapping(target = "fiscalYear", expression = "java(fiscalYear(owner))")
    @Mapping(target = "identityKey", expression = "java(IdentityKey.of(owner))")
    @Mapping(target = "selfLink", expression = "java(selfLink(owner))")
    // Not derivable from a single owner; the responder sets it from the daily registration count.
    @Mapping(target = "bulkSignupWarning", ignore = true)
    // Not derivable from a single owner; the responder sets it from the owner's city count.
    @Mapping(target = "capacityWarning", ignore = true)
    OwnerDto toOwnerDto(Owner owner);

    /**
     * The owner's locality: the REGION segment of its customer-code identity (see
     * {@link CustomerCode#regionOf(String)}), which is the region derived from the
     * owner's postcode then city when the code was assigned.
     */
    default String locality(Owner owner) {
        return CustomerCode.regionOf(owner.getCustomerCode());
    }

    /**
     * The owner's timezone: the IANA name for its locality, from the pinned
     * region-to-timezone table, or {@code null} when the locality has no entry.
     *
     * @see RegionTimezone#of(String)
     */
    default String timezone(Owner owner) {
        return RegionTimezone.of(locality(owner));
    }

    /**
     * The owner's preferred contact channel: "EMAIL" when an email address is present,
     * otherwise "PHONE".
     *
     * @see ContactPreference#of(Owner)
     */
    default String contactPreference(Owner owner) {
        return ContactPreference.of(owner);
    }

    /**
     * The owner's salutation: the title and last name separated by a space, or just the
     * last name when no title is present.
     *
     * @see Salutation#of(Owner)
     */
    default String salutation(Owner owner) {
        return Salutation.of(owner);
    }

    /**
     * The owner's stored E.164 telephone formatted for humans: the country code, a
     * space, then the national digits grouped in threes (e.g. "+61 412 345 678"). The
     * raw {@code telephone} stays in E.164 form.
     *
     * @see Telephone#display(String)
     */
    default String telephoneDisplay(Owner owner) {
        return Telephone.display(owner.getTelephone());
    }

    /**
     * The owner's age band, derived from its birth date against its registration date.
     *
     * @see AgeBand#of(Owner)
     */
    default String ageBand(Owner owner) {
        return AgeBand.of(owner);
    }

    /**
     * The owner's fiscal year, an {@code FY<YY>} label derived from its
     * business-day-adjusted registration date; {@code null} when no date is present.
     *
     * @see FiscalYear#label(java.time.LocalDate)
     */
    default String fiscalYear(Owner owner) {
        return owner.getRegistrationDate() == null ? null
                : FiscalYear.label(owner.getRegistrationDate());
    }

    /**
     * The owner's membership points, scored from email, unique name, household size and
     * tenure.
     *
     * @see MembershipPoints#of(Owner)
     */
    default Integer membershipPoints(Owner owner) {
        return MembershipPoints.of(owner);
    }

    /**
     * The owner's membership level from 1 to 4, derived from its membership points.
     *
     * @see MembershipLevel#of(Owner)
     */
    default Integer membershipLevel(Owner owner) {
        return MembershipLevel.of(owner);
    }

    /**
     * The owner's segment, formatted {@code <TIER>_<AREA>}, derived from its membership
     * level and locality.
     *
     * @see OwnerSegment#of(int, String)
     */
    default String ownerSegment(Owner owner) {
        return OwnerSegment.of(membershipLevel(owner), locality(owner));
    }

    /**
     * The Luhn check digit (0-9) over the digits of the owner's customer code.
     *
     * @see Luhn#checkDigit(String)
     */
    default Integer checkDigit(Owner owner) {
        return Luhn.checkDigit(owner.getCustomerCode());
    }

    /**
     * The upper-cased first letters of firstName and lastName, dot-separated with a
     * trailing dot (e.g. "J.S.").
     */
    default String initials(Owner owner) {
        return firstInitial(owner.getFirstName()) + firstInitial(owner.getLastName());
    }

    private static String firstInitial(String name) {
        return Character.toUpperCase(name.charAt(0)) + ".";
    }

    /**
     * The owner's canonical URI: {@code /api/owners/} followed by its id.
     */
    default String selfLink(Owner owner) {
        return "/api/owners/" + owner.getId();
    }

    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    Owner toOwner(OwnerFieldsDto ownerDto);

    List<OwnerDto> toOwnerDtoCollection(Collection<Owner> ownerCollection);

    Collection<Owner> toOwners(Collection<OwnerDto> ownerDtos);

    default OwnerPageDto toOwnerPageDto(@NonNull Page<Owner> ownerPage) {
        OwnerPageDto ownerPageDto = new OwnerPageDto();
        ownerPageDto.setContent(toOwnerDtoCollection(ownerPage.getContent()));
        ownerPageDto.setPage(ownerPage.getNumber());
        ownerPageDto.setSize(ownerPage.getSize());
        ownerPageDto.setTotalElements(ownerPage.getTotalElements());
        ownerPageDto.setTotalPages(ownerPage.getTotalPages());
        return ownerPageDto;
    }
}
