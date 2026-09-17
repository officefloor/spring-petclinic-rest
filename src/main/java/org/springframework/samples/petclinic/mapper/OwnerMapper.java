package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.AgeBand;
import org.springframework.samples.petclinic.model.ContactPreference;
import org.springframework.samples.petclinic.model.FiscalYear;
import org.springframework.samples.petclinic.model.Locality;
import org.springframework.samples.petclinic.model.MembershipLevel;
import org.springframework.samples.petclinic.model.MembershipPoints;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.OwnerSegment;
import org.springframework.samples.petclinic.model.Timezone;
import org.springframework.samples.petclinic.rest.function.owner.OwnerIdentities;
import org.springframework.samples.petclinic.rest.function.owner.OwnerTelephones;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;

import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    @Mapping(target = "selfLink", expression = "java(selfLink(owner))")
    @Mapping(target = "salutation", expression = "java(salutation(owner))")
    @Mapping(target = "displayName", expression = "java(displayName(owner))")
    @Mapping(target = "initials", expression = "java(initials(owner))")
    @Mapping(target = "fiscalYear", expression = "java(fiscalYear(owner))")
    @Mapping(target = "membershipPoints", expression = "java(membershipPoints(owner))")
    @Mapping(target = "membershipLevel", expression = "java(membershipLevel(owner))")
    @Mapping(target = "locality", expression = "java(locality(owner))")
    @Mapping(target = "timezone", expression = "java(timezone(owner))")
    @Mapping(target = "contactPreference", expression = "java(contactPreference(owner))")
    @Mapping(target = "ownerSegment", expression = "java(ownerSegment(owner))")
    @Mapping(target = "identityKey", expression = "java(identityKey(owner))")
    @Mapping(target = "ageBand", expression = "java(ageBand(owner))")
    @Mapping(target = "telephoneDisplay", expression = "java(telephoneDisplay(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /** The owner's canonical API path - '/api/owners/' followed by the owner's id;
     * null until the owner has been assigned an id. */
    default String selfLink(Owner owner) {
        if (owner == null || owner.getId() == null) {
            return null;
        }
        return "/api/owners/" + owner.getId();
    }

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

    /** The owner's marketing segment, formatted '&lt;TIER&gt;_&lt;AREA&gt;' - see
     * {@link OwnerSegment}. */
    default String ownerSegment(Owner owner) {
        if (owner == null) {
            return null;
        }
        return OwnerSegment.forOwner(owner);
    }

    /** The canonical region derived from the owner's postcode - the same REGION that forms the
     * memberId (the {@code <REGION><FY><HASH8><CHK>} identity), or 'UNKNOWN' when the postcode
     * resolves to no known region. */
    default String locality(Owner owner) {
        if (owner == null) {
            return null;
        }
        return Locality.forPostcode(owner.getPostcode());
    }

    /** The IANA timezone name for the owner's region (see {@link Timezone} and {@link #locality}),
     * or null when the region has no known timezone. */
    default String timezone(Owner owner) {
        if (owner == null) {
            return null;
        }
        return Timezone.forRegion(locality(owner));
    }

    /** The owner's honorific title and last name, space-separated (e.g. 'DR Who'); just the
     * last name when no title is stored. */
    default String salutation(Owner owner) {
        if (owner == null) {
            return null;
        }
        String title = owner.getTitle();
        if (title == null || title.isEmpty()) {
            return owner.getLastName();
        }
        return title + " " + owner.getLastName();
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
     * The owner's fiscal year, formatted 'FY&lt;YY&gt;' where YY is the last two digits of the
     * fiscal year (starting 1 July) of the registrationDate; null until a registration date is
     * assigned.
     *
     * @see FiscalYear
     */
    default String fiscalYear(Owner owner) {
        if (owner == null) {
            return null;
        }
        return FiscalYear.label(owner.getRegistrationDate());
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
     * The owner's numeric membership level: the value fixed at creation time (capped against the
     * household) when recorded, otherwise mapped from the owner's membership points; null when
     * there is no owner.
     *
     * @see MembershipLevel
     */
    default Integer membershipLevel(Owner owner) {
        if (owner == null) {
            return null;
        }
        return MembershipLevel.effective(owner);
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
