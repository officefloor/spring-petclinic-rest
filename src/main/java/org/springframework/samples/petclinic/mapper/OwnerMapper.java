package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;

import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = {PetMapper.class})
public interface OwnerMapper {

    @Mapping(target = "displayName", expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "telephoneDisplay", expression = "java(org.springframework.samples.petclinic.service.OwnerTelephoneDisplayPolicy.telephoneDisplay(owner))")
    @Mapping(target = "checkDigit", expression = "java(org.springframework.samples.petclinic.service.OwnerCheckDigitPolicy.checkDigit(owner))")
    @Mapping(target = "membershipNumber", expression = "java(org.springframework.samples.petclinic.service.OwnerMembershipPolicy.membershipNumber(owner))")
    @Mapping(target = "membershipPoints", expression = "java(org.springframework.samples.petclinic.service.OwnerMembershipLevelPolicy.membershipPoints(owner))")
    @Mapping(target = "membershipLevel", expression = "java(org.springframework.samples.petclinic.service.OwnerMembershipLevelPolicy.membershipLevel(owner))")
    @Mapping(target = "locality", expression = "java(org.springframework.samples.petclinic.service.OwnerLocalityPolicy.locality(owner))")
    @Mapping(target = "timezone", expression = "java(org.springframework.samples.petclinic.service.OwnerTimezonePolicy.timezone(owner))")
    @Mapping(target = "bulkSignupWarning", expression = "java(org.springframework.samples.petclinic.service.OwnerBulkSignupPolicy.bulkSignupWarning(owner))")
    @Mapping(target = "contactPreference", expression = "java(org.springframework.samples.petclinic.service.OwnerContactPreferencePolicy.contactPreference(owner))")
    @Mapping(target = "identityKey", expression = "java(org.springframework.samples.petclinic.service.OwnerIdentityPolicy.identityKey(owner))")
    @Mapping(target = "ageBand", expression = "java(org.springframework.samples.petclinic.service.OwnerAgeBandPolicy.ageBand(owner))")
    OwnerDto toOwnerDto(Owner owner);

    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    @Mapping(target = "address", expression = "java(composeAddress(ownerDto))")
    @Mapping(target = "addressLine1", source = "addressLine1", qualifiedByName = "normalizeAddress")
    @Mapping(target = "addressLine2", source = "addressLine2", qualifiedByName = "normalizeAddress")
    @Mapping(target = "telephone", source = "telephone", qualifiedByName = "normalizeTelephone")
    @Mapping(target = "email", source = "email", qualifiedByName = "normalizeEmail")
    Owner toOwner(OwnerFieldsDto ownerDto);

    /**
     * Compose the stored/returned address, preferring the structured fields: when a non-blank
     * {@code addressLine1} is supplied it is the normalized line 1 with the normalized
     * {@code addressLine2} appended after a single space (when present); otherwise fall back to
     * the normalized flat {@code address} for backward compatibility.
     */
    default String composeAddress(OwnerFieldsDto ownerDto) {
        String line1 = normalizeAddress(ownerDto.getAddressLine1());
        if (line1 == null || line1.isBlank()) {
            return normalizeAddress(ownerDto.getAddress());
        }
        String line2 = normalizeAddress(ownerDto.getAddressLine2());
        return (line2 == null || line2.isBlank()) ? line1 : line1 + " " + line2;
    }

    /**
     * Canonicalise the address on create: trim, collapse whitespace runs, upper-case, and expand
     * the common street-type abbreviations. The normalized value is what gets stored, returned, and
     * compared by the household rules.
     */
    @Named("normalizeAddress")
    default String normalizeAddress(String address) {
        if (address == null) {
            return null;
        }
        return address.trim().replaceAll("\\s+", " ").toUpperCase()
            .replaceAll("\\bST\\b", "STREET")
            .replaceAll("\\bRD\\b", "ROAD")
            .replaceAll("\\bAVE\\b", "AVENUE");
    }

    /** Store the telephone in E.164 form (see {@link E164Telephone#toE164}). */
    @Named("normalizeTelephone")
    default String normalizeTelephone(String telephone) {
        return E164Telephone.toE164(telephone);
    }

    /** Lower-case the email so it is stored and returned in canonical form. */
    @Named("normalizeEmail")
    default String normalizeEmail(String email) {
        return email == null ? null : email.toLowerCase();
    }

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
