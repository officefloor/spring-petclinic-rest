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
import java.util.Locale;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    @Mapping(target = "displayName", source = "owner", qualifiedByName = "formatDisplayName")
    @Mapping(target = "initials", source = "owner", qualifiedByName = "formatInitials")
    @Mapping(target = "membershipNumber", source = "owner", qualifiedByName = "formatMembershipNumber")
    @Mapping(target = "membershipTier", source = "owner", qualifiedByName = "formatMembershipTier")
    OwnerDto toOwnerDto(Owner owner);

    /**
     * Formats an owner's name as {@code 'LastName, FirstName'} from the stored first and
     * last names, as required by the API's read-only {@code displayName} field.
     *
     * <p>{@code @Named} keeps this out of MapStruct's automatic method selection, so it is
     * applied only where referenced via {@code qualifiedByName}.
     */
    @Named("formatDisplayName")
    default String formatDisplayName(Owner owner) {
        if (owner == null) {
            return null;
        }
        return owner.getLastName() + ", " + owner.getFirstName();
    }

    /**
     * Formats an owner's initials as the upper-cased first letters of the first and last
     * names, dot-separated with a trailing dot (e.g. {@code 'J.S.'}), as required by the
     * API's read-only {@code initials} field.
     *
     * <p>{@code @Named} keeps this out of MapStruct's automatic method selection, so it is
     * applied only where referenced via {@code qualifiedByName}.
     */
    @Named("formatInitials")
    default String formatInitials(Owner owner) {
        if (owner == null) {
            return null;
        }
        return initial(owner.getFirstName()) + initial(owner.getLastName());
    }

    /**
     * Formats an owner's membership number as {@code '<customerCode>-M<YY>'}, where
     * {@code customerCode} is the owner's customer code and {@code YY} is the last two
     * digits of the registration date's year (e.g. {@code 'SMI-0007-M26'}), as required
     * by the API's read-only {@code membershipNumber} field. Returns {@code null} when
     * either the customer code or registration date is absent.
     *
     * <p>{@code @Named} keeps this out of MapStruct's automatic method selection, so it is
     * applied only where referenced via {@code qualifiedByName}.
     */
    @Named("formatMembershipNumber")
    default String formatMembershipNumber(Owner owner) {
        if (owner == null || owner.getCustomerCode() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        return String.format("%s-M%02d", owner.getCustomerCode(), owner.getRegistrationDate().getYear() % 100);
    }

    /**
     * Derives an owner's membership tier, as required by the API's read-only
     * {@code membershipTier} field: {@code SILVER} when the owner has no namesakes
     * ({@code namesakeCount} is 0) and an email address is present, otherwise
     * {@code BRONZE}.
     *
     * <p>{@code @Named} keeps this out of MapStruct's automatic method selection, so it is
     * applied only where referenced via {@code qualifiedByName}.
     */
    @Named("formatMembershipTier")
    default OwnerDto.MembershipTierEnum formatMembershipTier(Owner owner) {
        if (owner == null) {
            return null;
        }
        boolean noNamesakes = owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0;
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isEmpty();
        return noNamesakes && hasEmail ? OwnerDto.MembershipTierEnum.SILVER : OwnerDto.MembershipTierEnum.BRONZE;
    }

    private static String initial(String name) {
        if (name == null || name.isEmpty()) {
            return "";
        }
        return Character.toUpperCase(name.charAt(0)) + ".";
    }

    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    @Mapping(target = "address", source = "address", qualifiedByName = "normalizeAddress")
    @Mapping(target = "telephone", source = "telephone", qualifiedByName = "normalizeTelephone")
    @Mapping(target = "email", source = "email", qualifiedByName = "normalizeEmail")
    Owner toOwner(OwnerFieldsDto ownerDto);

    /**
     * Normalizes a postal address into a canonical form: surrounding whitespace is trimmed and
     * internal runs of whitespace collapsed to a single space, the value is upper-cased and common
     * abbreviations are expanded as whole tokens ({@code ST -> STREET}, {@code RD -> ROAD},
     * {@code AVE -> AVENUE}). So {@code '  12  main  st '} is stored and returned as
     * {@code '12 MAIN STREET'}.
     *
     * <p>{@code @Named} keeps this out of MapStruct's automatic method selection, so it is applied
     * only where referenced via {@code qualifiedByName}.
     */
    @Named("normalizeAddress")
    default String normalizeAddress(String address) {
        return AddressNormalizer.normalize(address);
    }

    /**
     * Normalizes a telephone number into canonical E.164 form: a leading {@code '+'} and country
     * code are kept when present, otherwise country code {@code '+61'} is assumed and a single
     * leading {@code '0'} is dropped from the national digits; spaces, dashes and brackets are
     * stripped and the result must contain 8 to 15 digits after the {@code '+'}. So
     * {@code '0412 345 678'} is stored and returned as {@code '+61412345678'}.
     *
     * <p>{@code @Named} keeps this out of MapStruct's automatic method selection, so it is applied
     * only where referenced via {@code qualifiedByName} (and when called directly, e.g. on update).
     *
     * @throws org.springframework.samples.petclinic.util.InvalidTelephoneException if the number
     *         cannot form a valid E.164 value (handled as a 400 Bad Request).
     */
    @Named("normalizeTelephone")
    default String normalizeTelephone(String telephone) {
        return TelephoneNormalizer.toE164(telephone);
    }

    /**
     * Normalizes an email address by lower-casing it, so it is stored and returned in a
     * canonical form. The schema-level {@code format: email} on {@code OwnerFieldsDto.email}
     * guarantees any value present is a syntactically valid address.
     *
     * <p>{@code @Named} keeps this out of MapStruct's automatic method selection, so it is
     * applied only where referenced via {@code qualifiedByName} (and when called directly,
     * e.g. on update).
     */
    @Named("normalizeEmail")
    default String normalizeEmail(String email) {
        return email == null ? null : email.toLowerCase(Locale.ROOT);
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
