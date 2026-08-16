package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;
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
    @Mapping(target = "identityKey",
        expression = "java(org.springframework.samples.petclinic.rest.function.owner.OwnerIdentity.identityKey(owner.getTelephone(), owner.getEmail(), owner.getLastName()))")
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
     * Derives the owner's locality (region) from the unified {@code memberId}: its leading
     * {@code <REGION>} segment (see
     * {@link org.springframework.samples.petclinic.rest.function.owner.MemberId#region}). The region
     * embedded in the memberId is itself derived (postcode preferred, then city) when the owner is
     * created, so a postcode in a known range still wins over the city. When no memberId is present
     * (e.g. legacy owners), falls back to deriving the region live from the postcode/city via
     * {@link OwnerRegion}.
     */
    default String deriveLocality(Owner owner) {
        String region = org.springframework.samples.petclinic.rest.function.owner.MemberId
            .region(owner.getMemberId());
        if (region != null) {
            return region;
        }
        return OwnerRegion.fromPostcodeOrCity(owner.getPostcode(), owner.getCity());
    }

    /**
     * Derives the {@code FY<YY>} fiscal-year label from the unified {@code memberId}: its 2-digit
     * {@code <FY>} segment (see
     * {@link org.springframework.samples.petclinic.rest.function.owner.MemberId#fiscalYear2}). When no
     * memberId is present (e.g. legacy owners), falls back to computing the label live from the
     * registration date via {@link org.springframework.samples.petclinic.rest.function.owner.FiscalYear}.
     */
    default String deriveFiscalYear(Owner owner) {
        String fy2 = org.springframework.samples.petclinic.rest.function.owner.MemberId
            .fiscalYear2(owner.getMemberId());
        if (fy2 != null) {
            return "FY" + fy2;
        }
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
