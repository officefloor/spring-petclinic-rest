package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;
import org.springframework.samples.petclinic.rest.function.owner.IdentityKey;
import org.springframework.samples.petclinic.rest.function.owner.TelephoneNormalizer;
import org.springframework.samples.petclinic.util.AgeBand;
import org.springframework.samples.petclinic.util.CheckDigit;
import org.springframework.samples.petclinic.util.ContactPreference;
import org.springframework.samples.petclinic.util.CustomerCode;
import org.springframework.samples.petclinic.util.Locality;
import org.springframework.samples.petclinic.util.MembershipLevel;
import org.springframework.samples.petclinic.util.MembershipPoints;
import org.springframework.samples.petclinic.util.RegionTimezone;

import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    @Mapping(target = "displayName", expression = "java(displayName(owner))")
    @Mapping(target = "initials", expression = "java(initials(owner))")
    @Mapping(target = "locality", expression = "java(locality(owner))")
    @Mapping(target = "timezone", expression = "java(timezone(owner))")
    @Mapping(target = "membershipNumber", expression = "java(membershipNumber(owner))")
    @Mapping(target = "checkDigit", expression = "java(checkDigit(owner))")
    @Mapping(target = "membershipPoints", expression = "java(membershipPoints(owner))")
    @Mapping(target = "membershipLevel", expression = "java(membershipLevel(owner))")
    @Mapping(target = "contactPreference", expression = "java(contactPreference(owner))")
    @Mapping(target = "identityKey", expression = "java(identityKey(owner))")
    @Mapping(target = "ageBand", expression = "java(ageBand(owner))")
    @Mapping(target = "telephoneDisplay", expression = "java(telephoneDisplay(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /** The owner's name formatted as 'LastName, FirstName'. */
    default String displayName(Owner owner) {
        return owner == null ? null : owner.getLastName() + ", " + owner.getFirstName();
    }

    /**
     * The canonical region taken from the owner's region-and-hash identity: the REGION portion of the
     * customerCode (see {@link CustomerCode}). Falls back to deriving the region directly from the
     * city and postcode (see {@link Locality}) for an owner that has no customerCode yet.
     */
    default String locality(Owner owner) {
        if (owner == null) {
            return null;
        }
        String region = CustomerCode.regionOf(owner.getCustomerCode());
        return region != null ? region : Locality.of(owner.getCity(), owner.getPostcode());
    }

    /**
     * The owner's IANA timezone (see {@link RegionTimezone}), derived from the owner's
     * {@link #locality(Owner) locality} (region): NSW->Australia/Sydney, VIC->Australia/Melbourne,
     * QLD->Australia/Brisbane. {@code null} when the region has no timezone.
     */
    default String timezone(Owner owner) {
        return owner == null ? null : RegionTimezone.of(locality(owner));
    }

    /** The upper-cased first letters of the first and last name, e.g. 'J.S.'. */
    default String initials(Owner owner) {
        return owner == null ? null
            : Character.toUpperCase(owner.getFirstName().charAt(0)) + "."
            + Character.toUpperCase(owner.getLastName().charAt(0)) + ".";
    }

    /**
     * The owner's membership number, formatted '&lt;customerCode&gt;-M&lt;YY&gt;' where YY is the
     * last two digits of the registration date's year, e.g. 'NSW-1A2B3C4D-M26'.
     */
    default String membershipNumber(Owner owner) {
        if (owner == null || owner.getCustomerCode() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        return String.format("%s-M%02d", owner.getCustomerCode(), owner.getRegistrationDate().getYear() % 100);
    }

    /**
     * The owner's check digit (see {@link CheckDigit}): the Luhn check digit over the digits of the
     * customer code.
     */
    default Integer checkDigit(Owner owner) {
        return owner == null ? null : CheckDigit.of(owner.getCustomerCode());
    }

    /**
     * The owner's membership points (see {@link MembershipPoints}): a score that starts at 0 and
     * gains 2 for a present email address, 1 for a unique name (namesakeCount is zero), 2 for a
     * household of 3 or more and 3 for tenure over a year.
     */
    default Integer membershipPoints(Owner owner) {
        return MembershipPoints.of(owner);
    }

    /**
     * The owner's membership level (see {@link MembershipLevel}): the {@link #membershipPoints(Owner)
     * membership points} banded into a number from 1 to 4 (1 for 0-1 points, 2 for 2-3, 3 for 4-5,
     * 4 for 6 or more).
     */
    default Integer membershipLevel(Owner owner) {
        return MembershipLevel.forPoints(MembershipPoints.of(owner));
    }

    /**
     * The owner's preferred contact channel (see {@link ContactPreference}): 'EMAIL' when an email
     * address is present, otherwise 'PHONE'.
     */
    default OwnerDto.ContactPreferenceEnum contactPreference(Owner owner) {
        ContactPreference preference = ContactPreference.of(owner);
        return preference == null ? null : OwnerDto.ContactPreferenceEnum.valueOf(preference.name());
    }

    /**
     * The owner's derived identity key (see {@link IdentityKey}): the canonical telephone, email and
     * household id joined by '|'. A read-only summary of the owner's contact identity; duplicate
     * detection itself keys off the household id.
     */
    default String identityKey(Owner owner) {
        return owner == null ? null : IdentityKey.forOwner(owner);
    }

    /**
     * The owner's age band (see {@link AgeBand}): 'MINOR' under 18, 'ADULT' from 18 to 64 and
     * 'SENIOR' at 65 or older, measured from the birth date against the registration date, or
     * {@code null} when no birth date was given.
     */
    default OwnerDto.AgeBandEnum ageBand(Owner owner) {
        AgeBand band = AgeBand.of(owner);
        return band == null ? null : OwnerDto.AgeBandEnum.valueOf(band.name());
    }

    /**
     * The stored E.164 telephone formatted for humans (see
     * {@link TelephoneNormalizer#toDisplay(String)}): the country code, a space, then the national
     * digits grouped in threes, e.g. '+61 412 345 678'. The raw telephone stays in E.164 form.
     */
    default String telephoneDisplay(Owner owner) {
        return owner == null ? null : TelephoneNormalizer.toDisplay(owner.getTelephone());
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
