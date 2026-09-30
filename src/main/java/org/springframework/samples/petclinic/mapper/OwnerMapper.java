package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;
import org.springframework.samples.petclinic.rest.validation.AddressNormalizer;
import org.springframework.samples.petclinic.rest.validation.TelephoneNormalizer;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    @Mapping(target = "displayName", source = "owner", qualifiedByName = "formatDisplayName")
    @Mapping(target = "initials", source = "owner", qualifiedByName = "formatInitials")
    @Mapping(target = "telephoneDisplay", source = "owner", qualifiedByName = "formatTelephoneDisplay")
    @Mapping(target = "membershipNumber", source = "owner", qualifiedByName = "formatMembershipNumber")
    @Mapping(target = "fiscalYear", source = "owner", qualifiedByName = "formatFiscalYear")
    @Mapping(target = "checkDigit", source = "owner", qualifiedByName = "formatCheckDigit")
    @Mapping(target = "membershipPoints", source = "owner", qualifiedByName = "formatMembershipPoints")
    @Mapping(target = "membershipLevel", source = "owner", qualifiedByName = "formatMembershipLevel")
    @Mapping(target = "locality", source = "owner", qualifiedByName = "formatLocality")
    @Mapping(target = "timezone", source = "owner", qualifiedByName = "formatTimezone")
    @Mapping(target = "contactPreference", source = "owner", qualifiedByName = "formatContactPreference")
    @Mapping(target = "selfLink", source = "owner", qualifiedByName = "formatSelfLink")
    @Mapping(target = "bulkSignupWarning", ignore = true)
    OwnerDto toOwnerDto(Owner owner);

    /**
     * Formats an owner's canonical resource URL as {@code '/api/owners/'} followed by its id, for the
     * read-only {@code selfLink} field. Null when the owner or its id is absent.
     */
    @Named("formatSelfLink")
    default String formatSelfLink(Owner owner) {
        if (owner == null || owner.getId() == null) {
            return null;
        }
        return "/api/owners/" + owner.getId();
    }

    /**
     * Derives an owner's preferred contact channel for the read-only {@code contactPreference}
     * field: {@code 'EMAIL'} when an email address is present, otherwise {@code 'PHONE'}.
     */
    @Named("formatContactPreference")
    default String formatContactPreference(Owner owner) {
        if (owner == null) {
            return null;
        }
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        return hasEmail ? "EMAIL" : "PHONE";
    }

    /**
     * Derives an owner's {@code locality} from its region-and-hash customer code: the region is the
     * segment of the {@code '<REGION>-<HASH8>'} code before the first {@code '-'}, so the locality
     * always agrees with the identity's region (which itself prefers the postcode over the city,
     * defaulting to {@code 'UNKNOWN'}). Returns {@code 'UNKNOWN'} when no customer code is present.
     */
    @Named("formatLocality")
    default String formatLocality(Owner owner) {
        if (owner == null) {
            return null;
        }
        String code = owner.getCustomerCode();
        if (code == null || code.isBlank()) {
            return "UNKNOWN";
        }
        int dash = code.indexOf('-');
        return dash < 0 ? code : code.substring(0, dash);
    }

    /**
     * Fixed region-to-timezone table mapping each canonical region to its IANA timezone name.
     */
    Map<String, String> REGION_TIMEZONE = Map.of(
        "NSW", "Australia/Sydney",
        "VIC", "Australia/Melbourne",
        "QLD", "Australia/Brisbane");

    /**
     * Derives an owner's {@code timezone} as the IANA timezone name for its locality/region, using
     * the fixed region-to-timezone table (NSW-&gt;Australia/Sydney, VIC-&gt;Australia/Melbourne,
     * QLD-&gt;Australia/Brisbane). The region is taken from {@link #formatLocality(Owner)}, so the
     * timezone always agrees with the reported locality. Returns {@code null} when the region is not
     * in the table (e.g. {@code 'UNKNOWN'}).
     */
    @Named("formatTimezone")
    default String formatTimezone(Owner owner) {
        if (owner == null) {
            return null;
        }
        return REGION_TIMEZONE.get(formatLocality(owner));
    }

    /**
     * Computes an owner's membership points for the read-only {@code membershipPoints} field. Points
     * start at 0 and accumulate: 2 when an email address is present, 1 when the owner had no
     * namesakes at creation (namesakeCount is 0), 2 for a household of 3 or more members, and 3 for
     * tenure of one or more elapsed fiscal years (the number of 1-July fiscal-year boundaries
     * between the registration date and the current date).
     */
    @Named("formatMembershipPoints")
    default Integer formatMembershipPoints(Owner owner) {
        if (owner == null) {
            return null;
        }
        int points = 0;
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        if (hasEmail) {
            points += 2;
        }
        boolean noNamesakes = owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0;
        if (noNamesakes) {
            points += 1;
        }
        if (owner.getHouseholdSize() != null && owner.getHouseholdSize() >= 3) {
            points += 2;
        }
        if (owner.getRegistrationDate() != null
            && Owner.fiscalYear(LocalDate.now()) - Owner.fiscalYear(owner.getRegistrationDate()) >= 1) {
            points += 3;
        }
        return points;
    }

    /**
     * Derives an owner's numeric membership level for the read-only {@code membershipLevel} field
     * from its membership points: 1 for 0-1 points, 2 for 2-3 points, 3 for 4-5 points, and 4 for
     * 6 or more points.
     */
    @Named("formatMembershipLevel")
    default Integer formatMembershipLevel(Owner owner) {
        Integer points = formatMembershipPoints(owner);
        if (points == null) {
            return null;
        }
        if (points <= 1) {
            return 1;
        }
        if (points <= 3) {
            return 2;
        }
        if (points <= 5) {
            return 3;
        }
        return 4;
    }

    /**
     * Formats an owner's membership number as {@code '<customerCode>-M<YY>'}, where YY is the last
     * two digits of the fiscal year derived from the business-day-adjusted registration date (e.g.
     * {@code 'NSW-1A2B3C4D-M27'}), for the read-only {@code membershipNumber} field. Null when the
     * customer code or registration date is absent.
     */
    @Named("formatMembershipNumber")
    default String formatMembershipNumber(Owner owner) {
        if (owner == null || owner.getCustomerCode() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        return String.format("%s-M%02d", owner.getCustomerCode(), Owner.fiscalYear(owner.getRegistrationDate()) % 100);
    }

    /**
     * Formats an owner's fiscal year as {@code 'FY<YY>'}, where YY is the last two digits of the
     * fiscal year derived from the business-day-adjusted registration date (e.g. {@code 'FY27'}),
     * for the read-only {@code fiscalYear} field. Each fiscal year starts on 1 July and is named by
     * the calendar year in which it ends. Null when the registration date is absent.
     */
    @Named("formatFiscalYear")
    default String formatFiscalYear(Owner owner) {
        if (owner == null || owner.getRegistrationDate() == null) {
            return null;
        }
        return String.format("FY%02d", Owner.fiscalYear(owner.getRegistrationDate()) % 100);
    }

    /**
     * Computes the Luhn check digit (0-9) over the digits contained in the owner's
     * {@code customerCode} for the read-only {@code checkDigit} field. Null when the
     * customer code is absent.
     */
    @Named("formatCheckDigit")
    default Integer formatCheckDigit(Owner owner) {
        if (owner == null || owner.getCustomerCode() == null) {
            return null;
        }
        String code = owner.getCustomerCode();
        int sum = 0;
        boolean dbl = true;
        for (int i = code.length() - 1; i >= 0; i--) {
            char c = code.charAt(i);
            if (c < '0' || c > '9') {
                continue;
            }
            int d = c - '0';
            if (dbl) {
                d *= 2;
                if (d > 9) {
                    d -= 9;
                }
            }
            sum += d;
            dbl = !dbl;
        }
        return (10 - (sum % 10)) % 10;
    }

    /**
     * Formats an owner's stored names as {@code 'LastName, FirstName'} for the read-only
     * {@code displayName} field.
     */
    @Named("formatDisplayName")
    default String formatDisplayName(Owner owner) {
        return owner == null ? null : owner.getLastName() + ", " + owner.getFirstName();
    }

    /**
     * Formats an owner's initials as the upper-cased first letters of firstName and
     * lastName, dot-separated with a trailing dot (e.g. {@code 'J.S.'}), for the
     * read-only {@code initials} field.
     */
    @Named("formatInitials")
    default String formatInitials(Owner owner) {
        return owner == null ? null
            : Character.toUpperCase(owner.getFirstName().charAt(0)) + "."
                + Character.toUpperCase(owner.getLastName().charAt(0)) + ".";
    }

    /**
     * Formats the owner's stored E.164 telephone for human display as the country code, a space
     * and the national digits grouped in threes (e.g. {@code '+61412345678'} becomes
     * {@code '+61 412 345 678'}), for the read-only {@code telephoneDisplay} field. The raw
     * {@code telephone} field stays in E.164 form. Null when no telephone is present.
     */
    @Named("formatTelephoneDisplay")
    default String formatTelephoneDisplay(Owner owner) {
        return owner == null ? null : TelephoneNormalizer.toDisplay(owner.getTelephone());
    }

    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    @Mapping(target = "telephone", source = "telephone", qualifiedByName = "normalizeTelephone")
    @Mapping(target = "addressLine1", source = "addressLine1", qualifiedByName = "normalizeAddress")
    @Mapping(target = "addressLine2", source = "addressLine2", qualifiedByName = "normalizeAddress")
    @Mapping(target = "address", ignore = true)
    Owner toOwner(OwnerFieldsDto ownerDto);

    /**
     * Resolves the owner's stored {@code address} after the field mapping runs, from the already
     * normalized {@code addressLine1}/{@code addressLine2} fields set on the target (preferred when
     * present) with the normalized flat {@code address} as the fallback. See
     * {@link AddressNormalizer#compose(String, String, String)} for the composition rule.
     */
    @AfterMapping
    default void composeAddress(OwnerFieldsDto ownerDto, @MappingTarget Owner owner) {
        owner.setAddress(AddressNormalizer.compose(
            owner.getAddressLine1(), owner.getAddressLine2(), normalizeAddress(ownerDto.getAddress())));
    }

    /**
     * Normalizes a postal address for persistence: whitespace is trimmed and collapsed, the
     * text is upper-cased and common street-type abbreviations are expanded (e.g.
     * {@code '  12  main  st '} becomes {@code '12 MAIN STREET'}). The required-field rule is
     * enforced separately; an address blank after normalization is rejected.
     */
    @Named("normalizeAddress")
    default String normalizeAddress(String address) {
        return AddressNormalizer.normalize(address);
    }

    /**
     * Normalizes a telephone number for persistence into canonical E.164 form (e.g.
     * {@code '0412 345 678'} becomes {@code '+61412345678'}). The formability rule is
     * enforced separately by {@code TelephoneValidation}.
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
