package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.OwnerRegion;
import org.springframework.samples.petclinic.rest.function.owner.OwnerAgeBand;
import org.springframework.samples.petclinic.rest.function.owner.OwnerCheckDigit;
import org.springframework.samples.petclinic.rest.function.owner.OwnerCustomerCode;
import org.springframework.samples.petclinic.rest.function.owner.OwnerFiscalYear;
import org.springframework.samples.petclinic.rest.function.owner.OwnerIdentity;
import org.springframework.samples.petclinic.rest.function.owner.OwnerTelephone;
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

    @Mapping(target = "selfLink", source = "owner", qualifiedByName = "selfLink")
    @Mapping(target = "displayName", source = "owner", qualifiedByName = "displayName")
    @Mapping(target = "salutation", source = "owner", qualifiedByName = "salutation")
    @Mapping(target = "initials", source = "owner", qualifiedByName = "initials")
    @Mapping(target = "telephoneDisplay", source = "owner", qualifiedByName = "telephoneDisplay")
    @Mapping(target = "locality", source = "owner", qualifiedByName = "locality")
    @Mapping(target = "timezone", source = "owner", qualifiedByName = "timezone")
    @Mapping(target = "contactPreference", source = "owner", qualifiedByName = "contactPreference")
    @Mapping(target = "identityKey", source = "owner", qualifiedByName = "identityKey")
    @Mapping(target = "checkDigit", source = "owner", qualifiedByName = "checkDigit")
    @Mapping(target = "ageBand", source = "owner", qualifiedByName = "ageBand")
    @Mapping(target = "fiscalYear", source = "owner", qualifiedByName = "fiscalYear")
    OwnerDto toOwnerDto(Owner owner);

    @Named("fiscalYear")
    default String fiscalYear(Owner owner) {
        return OwnerFiscalYear.label(owner);
    }

    @Named("identityKey")
    default String identityKey(Owner owner) {
        return OwnerIdentity.identityKey(owner);
    }

    @Named("checkDigit")
    default Integer checkDigit(Owner owner) {
        return OwnerCheckDigit.checkDigit(owner);
    }

    @Named("ageBand")
    default OwnerDto.AgeBandEnum ageBand(Owner owner) {
        return OwnerAgeBand.ageBand(owner);
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
        // The locality is the REGION segment of the owner's customerCode (the region-and-hash
        // identity), falling back to the city/postcode table for any owner without a code.
        String region = OwnerCustomerCode.region(owner.getCustomerCode());
        return region != null ? region : OwnerRegion.localityOf(owner.getCity(), owner.getPostcode());
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
