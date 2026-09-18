package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.function.owner.OwnerIdentity;
import org.springframework.samples.petclinic.rest.function.owner.OwnerTelephone;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;
import org.springframework.samples.petclinic.util.AgeBand;
import org.springframework.samples.petclinic.util.CustomerCode;
import org.springframework.samples.petclinic.util.Luhn;
import org.springframework.samples.petclinic.util.OwnerRegion;
import org.springframework.samples.petclinic.util.RegionTimezone;

import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    @Mapping(target = "telephoneDisplay", expression = "java(telephoneDisplay(owner))")
    @Mapping(target = "displayName", expression = "java(displayName(owner))")
    @Mapping(target = "initials", expression = "java(initials(owner))")
    @Mapping(target = "membershipNumber", expression = "java(membershipNumber(owner))")
    @Mapping(target = "checkDigit", expression = "java(checkDigit(owner))")
    @Mapping(target = "locality", expression = "java(locality(owner))")
    @Mapping(target = "timezone", expression = "java(timezone(owner))")
    @Mapping(target = "contactPreference", expression = "java(contactPreference(owner))")
    @Mapping(target = "identityKey", expression = "java(identityKey(owner))")
    @Mapping(target = "ageBand", expression = "java(ageBand(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /** The owner's stored E.164 telephone formatted for people to read (country code, space,
     *  national digits grouped in threes), or null when it is not a recognised E.164 number.
     *  See {@link OwnerTelephone#toDisplay}. */
    default String telephoneDisplay(Owner owner) {
        return OwnerTelephone.toDisplay(owner.getTelephone()).orElse(null);
    }

    /** The owner's name formatted as 'LastName, FirstName' from the stored names. */
    default String displayName(Owner owner) {
        return owner.getLastName() + ", " + owner.getFirstName();
    }

    /** The upper-cased first letters of firstName and lastName, dot-separated with a
     *  trailing dot, e.g. 'J.S.'. */
    default String initials(Owner owner) {
        return Character.toUpperCase(owner.getFirstName().charAt(0)) + "."
                + Character.toUpperCase(owner.getLastName().charAt(0)) + ".";
    }

    /** The membership number '&lt;customerCode&gt;-M&lt;YY&gt;', where YY is the last two digits of
     *  the registrationDate year (e.g. 'NSW-1A2B3C4D-M26'). Null when either source field is absent. */
    default String membershipNumber(Owner owner) {
        if (owner.getCustomerCode() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        return String.format("%s-M%02d", owner.getCustomerCode(), owner.getRegistrationDate().getYear() % 100);
    }

    /** The Luhn check digit over the digits of the customerCode. Null when the customerCode
     *  is absent. */
    default Integer checkDigit(Owner owner) {
        return owner.getCustomerCode() == null ? null : Luhn.checkDigit(owner.getCustomerCode());
    }

    /** The owner's locality: the REGION segment of its customerCode, which is the canonical region
     *  the code was built from. Falls back to deriving the region straight from the postcode and
     *  city when the customerCode is absent (so an owner without one still resolves a locality). */
    default String locality(Owner owner) {
        return owner.getCustomerCode() != null
                ? CustomerCode.region(owner.getCustomerCode())
                : OwnerRegion.of(owner.getPostcode(), owner.getCity());
    }

    /** The owner's timezone: the IANA name from the fixed region-to-timezone table
     *  (NSW-&gt;Australia/Sydney, VIC-&gt;Australia/Melbourne, QLD-&gt;Australia/Brisbane), keyed
     *  off the owner's {@link #locality locality}. Null when the region has no mapped timezone. */
    default String timezone(Owner owner) {
        return RegionTimezone.of(locality(owner));
    }

    /** The owner's preferred contact method: 'EMAIL' when an email address is present,
     *  otherwise 'PHONE'. */
    default String contactPreference(Owner owner) {
        return owner.getEmail() != null && !owner.getEmail().isEmpty() ? "EMAIL" : "PHONE";
    }

    /** The owner's derived duplicate-detection identity key: normalized telephone, email and
     *  household id joined by '|'. See {@link OwnerIdentity#key}. */
    default String identityKey(Owner owner) {
        return OwnerIdentity.key(owner.getTelephone(), owner.getEmail(), owner.getHouseholdId());
    }

    /** The owner's age band derived from its birthDate against its registrationDate, or null when
     *  either date is absent. See {@link AgeBand}. */
    default OwnerDto.AgeBandEnum ageBand(Owner owner) {
        if (owner.getBirthDate() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        return OwnerDto.AgeBandEnum.fromValue(AgeBand.of(owner.getBirthDate(), owner.getRegistrationDate()));
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
