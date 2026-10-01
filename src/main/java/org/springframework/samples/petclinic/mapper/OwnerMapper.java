package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.OwnerRegion;
import org.springframework.samples.petclinic.rest.function.owner.OwnerAgeBand;
import org.springframework.samples.petclinic.rest.function.owner.OwnerFiscalYear;
import org.springframework.samples.petclinic.rest.function.owner.OwnerIdentity;
import org.springframework.samples.petclinic.rest.function.owner.OwnerRiskFlag;
import org.springframework.samples.petclinic.rest.function.owner.OwnerSegment;
import org.springframework.samples.petclinic.rest.function.owner.OwnerTelephone;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerIdentityDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;

import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    @Mapping(target = "selfLink", source = "owner", qualifiedByName = "selfLink")
    @Mapping(target = "displayName", source = "owner", qualifiedByName = "displayName")
    @Mapping(target = "salutation", source = "owner", qualifiedByName = "salutation")
    @Mapping(target = "initials", source = "owner", qualifiedByName = "initials")
    @Mapping(target = "telephoneDisplay", source = "owner", qualifiedByName = "telephoneDisplay")
    @Mapping(target = "locality", source = "owner", qualifiedByName = "locality")
    @Mapping(target = "timezone", source = "owner", qualifiedByName = "timezone")
    @Mapping(target = "contactPreference", source = "owner", qualifiedByName = "contactPreference")
    @Mapping(target = "apiVersion", constant = "2")
    @Mapping(target = "identity", source = "owner", qualifiedByName = "identity")
    @Mapping(target = "ageBand", source = "owner", qualifiedByName = "ageBand")
    @Mapping(target = "fiscalYear", source = "owner", qualifiedByName = "fiscalYear")
    @Mapping(target = "ownerSegment", source = "owner", qualifiedByName = "ownerSegment")
    @Mapping(target = "riskFlag", source = "owner", qualifiedByName = "riskFlag")
    OwnerDto toOwnerDto(Owner owner);

    @Named("fiscalYear")
    default String fiscalYear(Owner owner) {
        // The fiscal year is derived directly from the owner's business-day-adjusted
        // registrationDate. It is user-facing, so it must not read back the V2-tagged region that the
        // memberId now carries inside it.
        return OwnerFiscalYear.label(owner);
    }

    @Named("identity")
    default OwnerIdentityDto identity(Owner owner) {
        // The three derived identifiers, grouped. memberId and householdId are the version-2 values
        // stored on the owner; identityKey is recomputed from the owner's current fields.
        OwnerIdentityDto identity = new OwnerIdentityDto();
        identity.setMemberId(owner.getMemberId());
        identity.setHouseholdId(owner.getHouseholdId());
        identity.setIdentityKey(OwnerIdentity.identityKey(owner));
        return identity;
    }

    @Named("ageBand")
    default OwnerDto.AgeBandEnum ageBand(Owner owner) {
        return OwnerAgeBand.ageBand(owner);
    }

    @Named("ownerSegment")
    default OwnerDto.OwnerSegmentEnum ownerSegment(Owner owner) {
        return OwnerSegment.segment(owner);
    }

    @Named("riskFlag")
    default Boolean riskFlag(Owner owner) {
        return OwnerRiskFlag.riskFlag(owner);
    }

    @Named("selfLink")
    default String selfLink(Owner owner) {
        return "/api/owners/" + owner.getId();
    }

    @Named("displayName")
    default String displayName(Owner owner) {
        return owner.getLastName() + ", " + owner.getFirstName();
    }

    @Named("salutation")
    default String salutation(Owner owner) {
        String title = owner.getTitle();
        return (title != null && !title.isBlank())
            ? title + " " + owner.getLastName()
            : owner.getLastName();
    }

    @Named("telephoneDisplay")
    default String telephoneDisplay(Owner owner) {
        return OwnerTelephone.toDisplay(owner.getTelephone());
    }

    @Named("initials")
    default String initials(Owner owner) {
        return Character.toUpperCase(owner.getFirstName().charAt(0)) + "."
            + Character.toUpperCase(owner.getLastName().charAt(0)) + ".";
    }

    @Named("locality")
    default String locality(Owner owner) {
        // The locality is the plain region derived from the owner's city/postcode. It is user-facing,
        // so it must stay the plain region code (e.g. 'NSW') and never carry the 'V2' version tag the
        // identity algorithm mixes into the region inside the identifiers.
        return OwnerRegion.localityOf(owner.getCity(), owner.getPostcode());
    }

    @Named("timezone")
    default String timezone(Owner owner) {
        // The timezone is the IANA name for the owner's locality/region, from the fixed
        // region-to-timezone table; derive the region exactly as locality(owner) does.
        return OwnerRegion.timezoneOf(locality(owner));
    }

    @Named("contactPreference")
    default OwnerDto.ContactPreferenceEnum contactPreference(Owner owner) {
        String email = owner.getEmail();
        return (email != null && !email.isBlank())
            ? OwnerDto.ContactPreferenceEnum.EMAIL
            : OwnerDto.ContactPreferenceEnum.PHONE;
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
