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
import org.springframework.samples.petclinic.util.LocalityResolver;
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
    @Mapping(target = "membershipLevel", source = "owner", qualifiedByName = "formatMembershipLevel")
    @Mapping(target = "locality", source = "owner", qualifiedByName = "formatLocality")
    @Mapping(target = "contactPreference", source = "owner", qualifiedByName = "formatContactPreference")
    @Mapping(target = "bulkSignupWarning", ignore = true)
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
     * Derives an owner's membership level, as required by the API's read-only
     * {@code membershipLevel} field: starting at 1, plus 1 when an email address is present,
     * plus 1 when the owner has no namesakes ({@code namesakeCount} is 0), capped at 3. (Level 4
     * is reserved for tenure.)
     *
     * <p>{@code @Named} keeps this out of MapStruct's automatic method selection, so it is
     * applied only where referenced via {@code qualifiedByName} (and when called directly, e.g.
     * to record the membership level on the create audit line).
     */
    @Named("formatMembershipLevel")
    default Integer formatMembershipLevel(Owner owner) {
        if (owner == null) {
            return null;
        }
        int level = 1;
        if (owner.getEmail() != null && !owner.getEmail().isEmpty()) {
            level++;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            level++;
        }
        return Math.min(level, 3);
    }

    /**
     * Derives an owner's locality, as required by the API's read-only {@code locality}
     * field: the canonical region resolved by the postcode range first
     * ({@code NSW 2000-2099}, {@code VIC 3000-3099}, {@code QLD 4000-4099}), falling back
     * to the fixed city-to-region table ({@code Sydney -> NSW}, {@code Melbourne -> VIC},
     * {@code Brisbane -> QLD}) when the postcode is absent or in no known range, or
     * {@code 'UNKNOWN'} when neither resolves a region.
     *
     * <p>{@code @Named} keeps this out of MapStruct's automatic method selection, so it is
     * applied only where referenced via {@code qualifiedByName}.
     */
    @Named("formatLocality")
    default String formatLocality(Owner owner) {
        if (owner == null) {
            return null;
        }
        return LocalityResolver.resolve(owner.getCity(), owner.getPostcode());
    }

    /**
     * Derives an owner's preferred contact channel, as required by the API's read-only
     * {@code contactPreference} field: {@code 'EMAIL'} when an email address is present,
     * otherwise {@code 'PHONE'}.
     *
     * <p>{@code @Named} keeps this out of MapStruct's automatic method selection, so it is
     * applied only where referenced via {@code qualifiedByName}.
     */
    @Named("formatContactPreference")
    default String formatContactPreference(Owner owner) {
        if (owner == null) {
            return null;
        }
        return owner.getEmail() != null && !owner.getEmail().isEmpty() ? "EMAIL" : "PHONE";
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
