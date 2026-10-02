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
import org.springframework.samples.petclinic.util.LuhnCheckDigit;
import org.springframework.samples.petclinic.util.TelephoneNormalizer;

import java.time.Period;
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
    @Mapping(target = "telephoneDisplay", source = "owner", qualifiedByName = "formatTelephoneDisplay")
    @Mapping(target = "checkDigit", source = "owner", qualifiedByName = "formatCheckDigit")
    @Mapping(target = "membershipNumber", source = "owner", qualifiedByName = "formatMembershipNumber")
    @Mapping(target = "membershipLevel", source = "owner", qualifiedByName = "formatMembershipLevel")
    @Mapping(target = "locality", source = "owner", qualifiedByName = "formatLocality")
    @Mapping(target = "contactPreference", source = "owner", qualifiedByName = "formatContactPreference")
    @Mapping(target = "ageBand", source = "owner", qualifiedByName = "formatAgeBand")
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
     * Formats an owner's stored E.164 telephone for human display, as required by the API's
     * read-only {@code telephoneDisplay} field: the country code (with its leading {@code '+'}),
     * a space, then the national digits grouped in threes (e.g. {@code '+61 412 345 678'}). The
     * raw {@code telephone} field keeps its canonical E.164 form. Returns {@code null} when no
     * telephone is present.
     *
     * <p>{@code @Named} keeps this out of MapStruct's automatic method selection, so it is
     * applied only where referenced via {@code qualifiedByName}.
     */
    @Named("formatTelephoneDisplay")
    default String formatTelephoneDisplay(Owner owner) {
        if (owner == null) {
            return null;
        }
        return TelephoneNormalizer.toDisplay(owner.getTelephone());
    }

    /**
     * Formats an owner's membership number as {@code '<customerCode>-M<YY>'}, where
     * {@code customerCode} is the owner's {@code '<REGION>-<HASH8>'} customer code and
     * {@code YY} is the last two digits of the registration date's year (e.g.
     * {@code 'NSW-1A2B3C4D-M26'}), as required by the API's read-only
     * {@code membershipNumber} field. Returns {@code null} when either the customer code
     * or registration date is absent.
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
     * Computes an owner's check digit, as required by the API's read-only {@code checkDigit}
     * field: a single Luhn check digit (0-9) over the digits contained in the owner's customer
     * code. Returns {@code null} when the customer code is absent.
     *
     * <p>{@code @Named} keeps this out of MapStruct's automatic method selection, so it is
     * applied only where referenced via {@code qualifiedByName}.
     */
    @Named("formatCheckDigit")
    default Integer formatCheckDigit(Owner owner) {
        if (owner == null || owner.getCustomerCode() == null) {
            return null;
        }
        return LuhnCheckDigit.compute(owner.getCustomerCode());
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
     * Derives an owner's locality, as required by the API's read-only {@code locality} field:
     * the {@code REGION} segment of the owner's {@code '<REGION>-<HASH8>'} customer code, so the
     * locality is read from the same region-and-hash identity that the customer code encodes.
     * When no customer code is present (e.g. an owner that predates the code), the region is
     * resolved directly from the postcode (falling back to the city-to-region table, else
     * {@code 'UNKNOWN'}), which yields the same region the code would have carried.
     *
     * <p>{@code @Named} keeps this out of MapStruct's automatic method selection, so it is
     * applied only where referenced via {@code qualifiedByName}.
     */
    @Named("formatLocality")
    default String formatLocality(Owner owner) {
        if (owner == null) {
            return null;
        }
        String customerCode = owner.getCustomerCode();
        if (customerCode != null) {
            int dash = customerCode.indexOf('-');
            if (dash > 0) {
                return customerCode.substring(0, dash);
            }
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

    /**
     * Derives an owner's age band, as required by the API's read-only {@code ageBand} field:
     * the owner's age in whole years at the registration date, mapped to {@code 'MINOR'} (under
     * 18), {@code 'ADULT'} (18 to 64) or {@code 'SENIOR'} (65 or over). Returns {@code null} when
     * no birth date was supplied (so the field is absent), or when no registration date is yet
     * known against which to compute the age.
     *
     * <p>{@code @Named} keeps this out of MapStruct's automatic method selection, so it is
     * applied only where referenced via {@code qualifiedByName}.
     */
    @Named("formatAgeBand")
    default String formatAgeBand(Owner owner) {
        if (owner == null || owner.getBirthDate() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        int age = Period.between(owner.getBirthDate(), owner.getRegistrationDate()).getYears();
        if (age < 18) {
            return "MINOR";
        }
        if (age < 65) {
            return "ADULT";
        }
        return "SENIOR";
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
