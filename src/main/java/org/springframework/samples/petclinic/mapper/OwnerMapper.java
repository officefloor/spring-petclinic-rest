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
import org.springframework.samples.petclinic.rest.validation.E164Telephone;
import org.springframework.samples.petclinic.util.AddressNormalizer;

import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    @Mapping(target = "displayName", expression = "java(formatDisplayName(owner))")
    @Mapping(target = "initials", expression = "java(formatInitials(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /**
     * Format an owner's name as {@code "LastName, FirstName"} from the stored names.
     */
    default String formatDisplayName(Owner owner) {
        if (owner == null) {
            return null;
        }
        return owner.getLastName() + ", " + owner.getFirstName();
    }

    /**
     * Format an owner's initials as the upper-cased first letters of the first and last
     * names, dot-separated with a trailing dot, e.g. {@code "J.S."}.
     */
    default String formatInitials(Owner owner) {
        if (owner == null) {
            return null;
        }
        return Character.toUpperCase(owner.getFirstName().charAt(0)) + "."
            + Character.toUpperCase(owner.getLastName().charAt(0)) + ".";
    }

    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    @Mapping(target = "householdId", ignore = true)
    @Mapping(target = "namesakeCount", ignore = true)
    @Mapping(target = "address", source = "address", qualifiedByName = "normalizeAddress")
    @Mapping(target = "telephone", source = "telephone", qualifiedByName = "normalizeTelephone")
    @Mapping(target = "email", source = "email", qualifiedByName = "normalizeEmail")
    Owner toOwner(OwnerFieldsDto ownerDto);

    /**
     * Normalize a postal address for storage into its canonical form (see
     * {@link AddressNormalizer}), so it is persisted and returned the same way regardless of
     * the formatting supplied by the caller. {@code @Named} keeps this out of MapStruct's
     * automatic method selection, so it only runs where a mapping explicitly references it via
     * {@code qualifiedByName}.
     */
    @Named("normalizeAddress")
    default String normalizeAddress(String address) {
        return AddressNormalizer.normalize(address);
    }

    /**
     * Normalize a telephone number for storage into E.164 form (see {@link E164Telephone}).
     * The requirement that a valid E.164 number can be formed is enforced separately by
     * {@code TelephoneValidation}, so for already-validated input this returns the E.164
     * string; it is idempotent, so re-normalizing a stored value yields the same value.
     * {@code @Named} keeps this out of MapStruct's automatic method selection, so it only
     * runs where a mapping explicitly references it via {@code qualifiedByName}.
     */
    @Named("normalizeTelephone")
    default String normalizeTelephone(String telephone) {
        return E164Telephone.normalize(telephone);
    }

    /**
     * Normalize an email address for storage by lower-casing it, so it is persisted and
     * returned in a canonical form regardless of the casing supplied by the caller.
     * Syntactic validity is enforced separately by the {@code @Email} constraint on the DTO.
     * {@code @Named} keeps this out of MapStruct's automatic method selection, so it only
     * runs where a mapping explicitly references it via {@code qualifiedByName}.
     */
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
