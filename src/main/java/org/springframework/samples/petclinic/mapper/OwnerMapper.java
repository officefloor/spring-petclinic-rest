package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerIdentityDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;
import org.springframework.samples.petclinic.service.HouseholdMatcher;
import org.springframework.samples.petclinic.service.IdentityKey;

import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    @Mapping(target = "selfLink", expression = "java(\"/api/owners/\" + owner.getId())")
    @Mapping(target = "salutation", expression = "java(owner.getSalutation())")
    @Mapping(target = "displayName", expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials", expression = "java((owner.getFirstName().charAt(0) + \".\" + owner.getLastName().charAt(0) + \".\").toUpperCase())")
    @Mapping(target = "apiVersion", expression = "java(2)")
    @Mapping(target = "identity", expression = "java(toIdentity(owner))")
    @Mapping(target = "fiscalYear", expression = "java(org.springframework.samples.petclinic.service.FiscalYear.label(owner.getRegistrationDate()))")
    @Mapping(target = "membershipPoints", expression = "java(org.springframework.samples.petclinic.service.MembershipPoints.of(owner))")
    @Mapping(target = "membershipLevel", expression = "java(owner.getMembershipLevel() != null ? owner.getMembershipLevel() : org.springframework.samples.petclinic.service.MembershipLevel.of(owner))")
    @Mapping(target = "locality", expression = "java(org.springframework.samples.petclinic.service.RegionCode.of(owner))")
    @Mapping(target = "timezone", expression = "java(org.springframework.samples.petclinic.service.RegionTimezone.of(owner))")
    @Mapping(target = "ownerSegment", expression = "java(org.springframework.samples.petclinic.service.OwnerSegment.of(owner))")
    @Mapping(target = "contactPreference", expression = "java(owner.getEmail() != null && !owner.getEmail().isBlank() ? \"EMAIL\" : \"PHONE\")")
    @Mapping(target = "ageBand", expression = "java(org.springframework.samples.petclinic.service.AgeBand.of(owner))")
    @Mapping(target = "telephoneDisplay", expression = "java(org.springframework.samples.petclinic.service.TelephoneFormatter.display(owner.getTelephone()))")
    @Mapping(target = "riskFlag", expression = "java(org.springframework.samples.petclinic.service.RiskFlag.of(owner))")
    OwnerDto toOwnerDto(Owner owner);

    Owner toOwner(OwnerDto ownerDto);

    /** Groups the owner's version-2 identifiers (memberId, householdId, identityKey) into the
     *  nested identity object of the response. */
    default OwnerIdentityDto toIdentity(Owner owner) {
        OwnerIdentityDto identity = new OwnerIdentityDto();
        identity.setMemberId(owner.getMemberId());
        identity.setHouseholdId(HouseholdMatcher.householdId(owner));
        identity.setIdentityKey(IdentityKey.of(owner));
        return identity;
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    @Mapping(target = "telephone", qualifiedByName = "normalizeTelephone")
    @Mapping(target = "address", expression = "java(composeAddress(ownerDto))")
    @Mapping(target = "addressLine1", expression = "java(normalizeLine(ownerDto.getAddressLine1()))")
    @Mapping(target = "addressLine2", expression = "java(normalizeLine(ownerDto.getAddressLine2()))")
    @Mapping(target = "postcode", expression = "java(org.springframework.samples.petclinic.service.PostcodeValidator.validate(ownerDto.getCity(), ownerDto.getPostcode()))")
    @Mapping(target = "registrationDate", expression = "java(org.springframework.samples.petclinic.service.RegistrationDateValidator.validate(ownerDto.getRegistrationDate()))")
    @Mapping(target = "email", expression = "java(org.springframework.samples.petclinic.service.DisposableEmailDomain.validate(ownerDto.getEmail()))")
    @Mapping(target = "salutation", expression = "java(composeSalutation(ownerDto.getTitle(), ownerDto.getLastName()))")
    Owner toOwner(OwnerFieldsDto ownerDto);

    /** Salutation as 'title lastName', or just lastName when no title is given. Composed from the raw
     *  request fields so it keeps their original casing (the mapped entity fields are normalized). */
    default String composeSalutation(String title, String lastName) {
        return (title == null || title.isBlank()) ? lastName : title + " " + lastName;
    }

    /**
     * Normalise a telephone to E.164 for storage: keep an explicit leading '+' and country
     * code when present, otherwise assume '+61' and drop a single leading national '0'.
     * Spaces, dashes and brackets are stripped; the result must have 8 to 15 digits after
     * the '+', else the number is rejected as invalid.
     */
    @Named("normalizeTelephone")
    default String normalizeTelephone(String telephone) {
        if (telephone == null) {
            return null;
        }
        boolean international = telephone.trim().startsWith("+");
        String digits = telephone.replaceAll("\\D", "");
        if (!international) {
            if (digits.startsWith("0")) {
                digits = digits.substring(1);
            }
            digits = "61" + digits;
        }
        if (digits.length() < 8 || digits.length() > 15) {
            throw new IllegalArgumentException("Telephone cannot form a valid E.164 number: " + telephone);
        }
        org.springframework.samples.petclinic.service.E164NationalLength.check(digits);
        return "+" + digits;
    }

    /**
     * Prefer the structured addressLine1 (with a normalized addressLine2 appended after a
     * single space when present) over the flat address; each supplied line is normalized
     * independently. Falls back to the flat address when no addressLine1 is supplied.
     */
    default String composeAddress(OwnerFieldsDto ownerDto) {
        String line1 = ownerDto.getAddressLine1();
        if (line1 != null && !line1.isBlank()) {
            String normalized = org.springframework.samples.petclinic.service.AddressNormalizer.normalize(line1);
            String line2 = ownerDto.getAddressLine2();
            return (line2 == null || line2.isBlank()) ? normalized
                : normalized + " " + org.springframework.samples.petclinic.service.AddressNormalizer.normalize(line2);
        }
        return org.springframework.samples.petclinic.service.AddressNormalizer.normalize(ownerDto.getAddress());
    }

    /** Normalize an optional address line, returning null when it is absent or blank. */
    default String normalizeLine(String line) {
        return (line == null || line.isBlank()) ? null
            : org.springframework.samples.petclinic.service.AddressNormalizer.normalize(line);
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
