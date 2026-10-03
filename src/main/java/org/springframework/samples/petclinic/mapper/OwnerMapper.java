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
import org.springframework.samples.petclinic.util.AddressNormalizer;
import org.springframework.samples.petclinic.util.TelephoneNormalizer;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    @Mapping(target = "displayName", expression = "java(toDisplayName(owner))")
    @Mapping(target = "initials", expression = "java(toInitials(owner))")
    @Mapping(target = "membershipTier", expression = "java(toMembershipTier(owner))")
    @Mapping(target = "locality", expression = "java(toLocality(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /** Fixed city-to-region table used to derive an owner's {@code locality}. */
    Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /**
     * Format an owner's stored names as {@code 'LastName, FirstName'} for the
     * read-only {@code displayName} field returned on the Owner DTO.
     */
    default String toDisplayName(Owner owner) {
        return owner.getLastName() + ", " + owner.getFirstName();
    }

    /**
     * Build the owner's initials as the upper-cased first letters of firstName and
     * lastName, dot-separated with a trailing dot (e.g. {@code 'J.S.'}) for the
     * read-only {@code initials} field returned on the Owner DTO.
     */
    default String toInitials(Owner owner) {
        return Character.toUpperCase(owner.getFirstName().charAt(0)) + "."
            + Character.toUpperCase(owner.getLastName().charAt(0)) + ".";
    }

    /**
     * Derive the owner's read-only {@code membershipTier}: {@code 'SILVER'} when the owner's name was
     * unique at creation time ({@code namesakeCount} is 0) and an email is present, otherwise
     * {@code 'BRONZE'}.
     */
    default String toMembershipTier(Owner owner) {
        boolean uniqueName = Integer.valueOf(0).equals(owner.getNamesakeCount());
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        return uniqueName && hasEmail ? "SILVER" : "BRONZE";
    }

    /**
     * Derive the owner's read-only {@code locality}: the canonical region looked up from the
     * owner's city in the fixed city-to-region table ({@code Sydney->NSW, Melbourne->VIC,
     * Brisbane->QLD}), or {@code 'UNKNOWN'} when the city is not in the table.
     */
    default String toLocality(Owner owner) {
        return CITY_REGION.getOrDefault(owner.getCity(), "UNKNOWN");
    }

    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    @Mapping(target = "address", source = "address", qualifiedByName = "normalizeAddress")
    @Mapping(target = "telephone", source = "telephone", qualifiedByName = "normalizeTelephone")
    @Mapping(target = "email", source = "email", qualifiedByName = "normalizeEmail")
    Owner toOwner(OwnerFieldsDto ownerDto);

    /**
     * Normalize a postal address to its canonical form (trimmed and whitespace-collapsed,
     * upper-cased, with common street-type abbreviations expanded) so it is stored, returned and
     * compared in one shape. Blank-after-normalization is rejected separately by
     * {@code AddressValidation} on the inbound DTO, so this only performs the transform the stored
     * value needs.
     */
    @Named("normalizeAddress")
    default String normalizeAddress(String address) {
        return AddressNormalizer.normalize(address);
    }

    /**
     * Normalize a telephone number to canonical E.164 form (a leading {@code '+'}, country
     * code and national digits) so it is stored and returned in one comparable form and
     * duplicates can be detected by comparing these values. Format validity is enforced
     * separately by {@code TelephoneValidation} on the inbound DTO, so this only performs
     * the transform the stored value needs.
     */
    @Named("normalizeTelephone")
    default String normalizeTelephone(String telephone) {
        return TelephoneNormalizer.toE164(telephone);
    }

    /**
     * Normalize an email address by trimming surrounding whitespace and lower-casing
     * it so it is stored and returned in a canonical form. Syntactic validity is
     * enforced separately by {@code @Email} on the inbound DTO, so this only performs
     * the transform the stored value needs.
     */
    @Named("normalizeEmail")
    default String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
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
