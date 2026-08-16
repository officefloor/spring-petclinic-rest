package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerIdentityDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;
import org.springframework.samples.petclinic.rest.function.owner.OwnerIdentity;
import org.springframework.samples.petclinic.rest.function.owner.OwnerRegion;

import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    @Mapping(target = "displayName",
        expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials",
        expression = "java(Character.toUpperCase(owner.getFirstName().charAt(0)) + \".\" + Character.toUpperCase(owner.getLastName().charAt(0)) + \".\")")
    @Mapping(target = "salutation",
        expression = "java(composeSalutation(owner))")
    @Mapping(target = "locality",
        expression = "java(deriveLocality(owner))")
    @Mapping(target = "timezone",
        expression = "java(org.springframework.samples.petclinic.rest.function.owner.OwnerTimezone.fromRegion(deriveLocality(owner)))")
    @Mapping(target = "contactPreference",
        expression = "java(owner.getEmail() != null && !owner.getEmail().isBlank() ? \"EMAIL\" : \"PHONE\")")
    @Mapping(target = "apiVersion",
        expression = "java(Integer.valueOf(org.springframework.samples.petclinic.rest.function.owner.OwnerIdentityVersion.API_VERSION))")
    @Mapping(target = "identity",
        expression = "java(toOwnerIdentityDto(owner))")
    @Mapping(target = "ageBand",
        expression = "java(org.springframework.samples.petclinic.rest.function.owner.AgeBand.of(owner.getBirthDate(), owner.getRegistrationDate()))")
    @Mapping(target = "fiscalYear",
        expression = "java(deriveFiscalYear(owner))")
    @Mapping(target = "ownerSegment",
        expression = "java(org.springframework.samples.petclinic.rest.function.owner.OwnerSegment.of(owner.getMembershipLevel(), deriveLocality(owner)))")
    @Mapping(target = "telephoneDisplay",
        expression = "java(org.springframework.samples.petclinic.rest.function.owner.TelephoneDisplay.of(owner.getTelephone()))")
    @Mapping(target = "riskFlag",
        expression = "java(org.springframework.samples.petclinic.rest.function.owner.RiskFlag.of(owner))")
    @Mapping(target = "selfLink",
        expression = "java(\"/api/owners/\" + owner.getId())")
    OwnerDto toOwnerDto(Owner owner);

    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    Owner toOwner(OwnerFieldsDto ownerDto);

    List<OwnerDto> toOwnerDtoCollection(Collection<Owner> ownerCollection);

    Collection<Owner> toOwners(Collection<OwnerDto> ownerDtos);

    /**
     * Builds the owner's version-2 {@code identity} object — the {@code memberId}, {@code householdId}
     * and {@code identityKey}. The memberId and householdId are read from the persisted owner; the
     * identityKey is (re)derived with the version-2 algorithm (see
     * {@link OwnerIdentity#identityKey}). Every value carries the fixed {@code "V2"} version tag.
     */
    default OwnerIdentityDto toOwnerIdentityDto(Owner owner) {
        OwnerIdentityDto identity = new OwnerIdentityDto();
        identity.setMemberId(owner.getMemberId());
        identity.setHouseholdId(owner.getHouseholdId());
        identity.setIdentityKey(
            OwnerIdentity.identityKey(owner.getTelephone(), owner.getEmail(), owner.getLastName()));
        return identity;
    }

    /**
     * Derives the owner's user-facing {@code locality} — the plain region code (postcode preferred,
     * then city, else {@code "UNKNOWN"}) via {@link OwnerRegion}. This is deliberately independent of
     * the {@code memberId}, whose embedded region now carries the {@code "V2"} identity version tag:
     * the locality, timezone and owner segment must stay the plain region.
     */
    default String deriveLocality(Owner owner) {
        return OwnerRegion.fromPostcodeOrCity(owner.getPostcode(), owner.getCity());
    }

    /**
     * Derives the {@code FY<YY>} fiscal-year label live from the owner's business-day-adjusted
     * registration date via
     * {@link org.springframework.samples.petclinic.rest.function.owner.FiscalYear}.
     */
    default String deriveFiscalYear(Owner owner) {
        return org.springframework.samples.petclinic.rest.function.owner.FiscalYear
            .label(owner.getRegistrationDate());
    }

    /**
     * Composes the owner's salutation: the honorific {@code title} followed by a single space and
     * the {@code lastName} when a title is supplied, or just the {@code lastName} when no title is
     * given (null or blank).
     */
    default String composeSalutation(Owner owner) {
        String title = owner.getTitle();
        if (title == null || title.isBlank()) {
            return owner.getLastName();
        }
        return title + " " + owner.getLastName();
    }

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
