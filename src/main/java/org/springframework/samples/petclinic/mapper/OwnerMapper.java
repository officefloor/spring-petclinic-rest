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
import org.springframework.samples.petclinic.rest.validation.AddressNormalizer;
import org.springframework.samples.petclinic.rest.validation.TelephoneNormalizer;

import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    @Mapping(target = "displayName", expression = "java(formatDisplayName(owner))")
    @Mapping(target = "initials", expression = "java(formatInitials(owner))")
    @Mapping(target = "membershipNumber", expression = "java(formatMembershipNumber(owner))")
    @Mapping(target = "membershipTier", expression = "java(resolveMembershipTier(owner))")
    OwnerDto toOwnerDto(Owner owner);

    Owner toOwner(OwnerDto ownerDto);

    /**
     * Formats the owner's stored names for display as {@code "LastName, FirstName"}.
     */
    default String formatDisplayName(Owner owner) {
        return owner.getLastName() + ", " + owner.getFirstName();
    }

    /**
     * Formats the owner's initials as the upper-cased first letters of firstName and
     * lastName, dot-separated with a trailing dot, e.g. {@code "J.S."}.
     */
    default String formatInitials(Owner owner) {
        return initial(owner.getFirstName()) + initial(owner.getLastName());
    }

    /** The upper-cased first letter of {@code name} followed by a dot. */
    private String initial(String name) {
        return Character.toUpperCase(name.charAt(0)) + ".";
    }

    /**
     * Formats the owner's membership number as {@code "<customerCode>-M<YY>"}, where YY is the
     * last two digits of the registration date year, e.g. {@code "LON-SMI-0007-M26"}.
     */
    default String formatMembershipNumber(Owner owner) {
        return String.format("%s-M%02d", owner.getCustomerCode(), owner.getRegistrationDate().getYear() % 100);
    }

    /**
     * Resolves the owner's membership tier: {@code SILVER} when the owner has no namesakes
     * (namesakeCount is 0) and an email is present, otherwise {@code BRONZE}.
     */
    default OwnerDto.MembershipTierEnum resolveMembershipTier(Owner owner) {
        boolean noNamesakes = Integer.valueOf(0).equals(owner.getNamesakeCount());
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        return noNamesakes && hasEmail ? OwnerDto.MembershipTierEnum.SILVER : OwnerDto.MembershipTierEnum.BRONZE;
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    @Mapping(target = "householdId", ignore = true)
    @Mapping(target = "telephone", source = "telephone", qualifiedByName = "normalizeTelephone")
    @Mapping(target = "address", source = "address", qualifiedByName = "normalizeAddress")
    Owner toOwner(OwnerFieldsDto ownerDto);

    /**
     * Store the address in its canonical form (trimmed, whitespace-collapsed, upper-cased and with
     * common abbreviations expanded), so it is always persisted, returned and compared normalized.
     */
    @Named("normalizeAddress")
    default String normalizeAddress(String address) {
        return AddressNormalizer.normalize(address);
    }

    /**
     * Store the telephone in E.164 form. Validation ({@code @Telephone}) has already guaranteed
     * the raw value normalizes to a valid E.164 number.
     */
    @Named("normalizeTelephone")
    default String normalizeTelephone(String telephone) {
        return TelephoneNormalizer.toE164(telephone);
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
