package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.AgeBand;
import org.springframework.samples.petclinic.model.ContactPreference;
import org.springframework.samples.petclinic.model.Locality;
import org.springframework.samples.petclinic.model.MembershipLevel;
import org.springframework.samples.petclinic.model.MembershipPoints;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.function.owner.OwnerIdentities;
import org.springframework.samples.petclinic.rest.function.owner.OwnerTelephones;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;
import org.springframework.samples.petclinic.util.Luhn;

import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    @Mapping(target = "displayName", expression = "java(displayName(owner))")
    @Mapping(target = "initials", expression = "java(initials(owner))")
    @Mapping(target = "membershipNumber", expression = "java(membershipNumber(owner))")
    @Mapping(target = "checkDigit", expression = "java(checkDigit(owner))")
    @Mapping(target = "membershipPoints", expression = "java(membershipPoints(owner))")
    @Mapping(target = "membershipLevel", expression = "java(membershipLevel(owner))")
    @Mapping(target = "locality", expression = "java(locality(owner))")
    @Mapping(target = "contactPreference", expression = "java(contactPreference(owner))")
    @Mapping(target = "identityKey", expression = "java(identityKey(owner))")
    @Mapping(target = "ageBand", expression = "java(ageBand(owner))")
    @Mapping(target = "telephoneDisplay", expression = "java(telephoneDisplay(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /** The owner's stored E.164 telephone formatted for humans - see {@link OwnerTelephones#toDisplay}. */
    default String telephoneDisplay(Owner owner) {
        if (owner == null) {
            return null;
        }
        return OwnerTelephones.toDisplay(owner.getTelephone());
    }

    /** The owner's age band derived from birthDate against registrationDate - see {@link AgeBand};
     * null when either date is absent. */
    default String ageBand(Owner owner) {
        if (owner == null) {
            return null;
        }
        AgeBand ageBand = AgeBand.forOwner(owner);
        return ageBand == null ? null : ageBand.name();
    }

    /** The owner's derived duplicate-detection key - see {@link OwnerIdentities}. */
    default String identityKey(Owner owner) {
        if (owner == null) {
            return null;
        }
        return OwnerIdentities.of(owner);
    }

    /** The owner's preferred contact channel - 'EMAIL' when an email is present, otherwise 'PHONE'. */
    default String contactPreference(Owner owner) {
        if (owner == null) {
            return null;
        }
        return ContactPreference.forOwner(owner).name();
    }

    /** The canonical region derived from the owner's postcode - the same REGION that forms the
     * customer code (see {@link OwnerIdentities} and the {@code <REGION>-<HASH8>} customer code),
     * or 'UNKNOWN' when the postcode resolves to no known region. */
    default String locality(Owner owner) {
        if (owner == null) {
            return null;
        }
        return Locality.forPostcode(owner.getPostcode());
    }

    /** Format the stored names as 'LastName, FirstName'. */
    default String displayName(Owner owner) {
        if (owner == null) {
            return null;
        }
        return owner.getLastName() + ", " + owner.getFirstName();
    }

    /** The upper-cased first letters of firstName and lastName, dot-separated with a trailing dot (e.g. 'J.S.'). */
    default String initials(Owner owner) {
        if (owner == null) {
            return null;
        }
        return initial(owner.getFirstName()) + initial(owner.getLastName());
    }

    private static String initial(String name) {
        if (name == null || name.isEmpty()) {
            return "";
        }
        return Character.toUpperCase(name.charAt(0)) + ".";
    }

    /**
     * The owner's membership number, formatted '&lt;customerCode&gt;-M&lt;YY&gt;' where YY is the
     * last two digits of the registrationDate year (e.g. 'NSW-1A2B3C4D-M26'). Derived from the owner's
     * own fields; null until both the customer code and registration date are assigned.
     */
    default String membershipNumber(Owner owner) {
        if (owner == null || owner.getCustomerCode() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        return owner.getCustomerCode() + "-M" + String.format("%02d", owner.getRegistrationDate().getYear() % 100);
    }

    /** The Luhn check digit over the digits of the owner's customerCode; null until a code is assigned. */
    default Integer checkDigit(Owner owner) {
        if (owner == null || owner.getCustomerCode() == null) {
            return null;
        }
        return Luhn.checkDigit(owner.getCustomerCode());
    }

    /**
     * The owner's membership points, scored from the owner's own fields; null when there
     * is no owner.
     *
     * @see MembershipPoints
     */
    default Integer membershipPoints(Owner owner) {
        if (owner == null) {
            return null;
        }
        return MembershipPoints.of(owner);
    }

    /**
     * The owner's numeric membership level, mapped from the owner's membership points; null
     * when there is no owner.
     *
     * @see MembershipLevel
     */
    default Integer membershipLevel(Owner owner) {
        if (owner == null) {
            return null;
        }
        return MembershipLevel.of(owner);
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
