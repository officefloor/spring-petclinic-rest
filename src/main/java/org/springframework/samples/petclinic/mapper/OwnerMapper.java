package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;
import org.springframework.samples.petclinic.service.MembershipLevels;
import org.springframework.samples.petclinic.rest.validation.IdentityKeyNormalizer;
import org.springframework.samples.petclinic.rest.validation.RegistrationDateNormalizer;
import org.springframework.samples.petclinic.rest.validation.TelephoneNormalizer;

import java.time.LocalDate;
import java.time.Period;
import java.util.Collection;
import java.util.List;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    @Mapping(target = "displayName", expression = "java(displayName(owner))")
    @Mapping(target = "salutation", expression = "java(salutation(owner))")
    @Mapping(target = "initials", expression = "java(initials(owner))")
    @Mapping(target = "telephoneDisplay", expression = "java(telephoneDisplay(owner))")
    @Mapping(target = "checkDigit", expression = "java(checkDigit(owner))")
    @Mapping(target = "membershipNumber", expression = "java(membershipNumber(owner))")
    @Mapping(target = "fiscalYear", expression = "java(fiscalYear(owner))")
    @Mapping(target = "membershipPoints", expression = "java(membershipPoints(owner))")
    @Mapping(target = "membershipLevel", expression = "java(membershipLevel(owner))")
    @Mapping(target = "locality", expression = "java(locality(owner))")
    @Mapping(target = "timezone", expression = "java(timezone(owner))")
    @Mapping(target = "bulkSignupWarning", expression = "java(bulkSignupWarning(owner))")
    @Mapping(target = "capacityWarning", expression = "java(capacityWarning(owner))")
    @Mapping(target = "possibleDuplicate", expression = "java(possibleDuplicate(owner))")
    @Mapping(target = "contactPreference", expression = "java(contactPreference(owner))")
    @Mapping(target = "identityKey", expression = "java(identityKey(owner))")
    @Mapping(target = "ageBand", expression = "java(ageBand(owner))")
    @Mapping(target = "selfLink", expression = "java(selfLink(owner))")
    OwnerDto toOwnerDto(Owner owner);

    Owner toOwner(OwnerDto ownerDto);

    /**
     * Formats an owner's stored names as {@code 'LastName, FirstName'}.
     */
    default String displayName(Owner owner) {
        if (owner == null) {
            return null;
        }
        String lastName = owner.getLastName() == null ? "" : owner.getLastName();
        String firstName = owner.getFirstName() == null ? "" : owner.getFirstName();
        return lastName + ", " + firstName;
    }

    /**
     * Composes the owner's salutation as {@code '<title> <lastName>'} — the title, a single
     * space and the last name — or just the last name when no (blank/absent) title is given.
     */
    default String salutation(Owner owner) {
        if (owner == null) {
            return null;
        }
        String lastName = owner.getLastName() == null ? "" : owner.getLastName();
        String title = owner.getTitle();
        if (title == null || title.isBlank()) {
            return lastName;
        }
        return title + " " + lastName;
    }

    /**
     * Returns the upper-cased first letters of firstName and lastName,
     * dot-separated with a trailing dot, e.g. {@code 'J.S.'}.
     */
    default String initials(Owner owner) {
        if (owner == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        String firstName = owner.getFirstName();
        String lastName = owner.getLastName();
        if (firstName != null && !firstName.isEmpty()) {
            sb.append(Character.toUpperCase(firstName.charAt(0))).append('.');
        }
        if (lastName != null && !lastName.isEmpty()) {
            sb.append(Character.toUpperCase(lastName.charAt(0))).append('.');
        }
        return sb.toString();
    }

    /**
     * Formats the owner's stored E.164 {@code telephone} for human display: the country code,
     * a space, and the national digits grouped in threes separated by spaces, e.g.
     * {@code '+61412345678'} becomes {@code '+61 412 345 678'}. The raw {@code telephone} is
     * left unchanged in E.164 form.
     */
    default String telephoneDisplay(Owner owner) {
        if (owner == null) {
            return null;
        }
        return TelephoneNormalizer.toDisplay(owner.getTelephone());
    }

    /**
     * Formats the owner's membership number as {@code '<customerCode>-M<YY>'}: the customer
     * code, the literal {@code '-M'}, and the last two digits of the fiscal year derived from
     * the (business-day-adjusted) registrationDate. Returns {@code null} unless both the
     * customer code and registration date are present.
     */
    default String membershipNumber(Owner owner) {
        if (owner == null || owner.getCustomerCode() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        return owner.getCustomerCode() + "-M" + fiscalYearYY(owner.getRegistrationDate());
    }

    /**
     * Derives the owner's {@code fiscalYear}, formatted {@code 'FY<YY>'}: the literal {@code 'FY'}
     * and the last two digits of the fiscal year derived from the (business-day-adjusted)
     * registrationDate. The fiscal year starts on 1 July. Returns {@code null} when no registration
     * date is present.
     */
    default String fiscalYear(Owner owner) {
        if (owner == null || owner.getRegistrationDate() == null) {
            return null;
        }
        return "FY" + fiscalYearYY(owner.getRegistrationDate());
    }

    /**
     * Formats the last two digits (zero-padded) of the fiscal year that {@code date} falls in.
     */
    private String fiscalYearYY(LocalDate date) {
        return String.format("%02d", RegistrationDateNormalizer.fiscalYear(date) % 100);
    }

    /**
     * Computes the owner's {@code checkDigit}: a single Luhn check digit (0-9) over the digits
     * contained in the {@code customerCode}. Returns {@code null} when no customer code is present.
     */
    default Integer checkDigit(Owner owner) {
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
     * The owner's membership points; see {@link MembershipLevels#points(Owner)}.
     */
    default Integer membershipPoints(Owner owner) {
        return MembershipLevels.points(owner);
    }

    /**
     * The owner's capped numeric membership level; see {@link MembershipLevels#level(Owner)}.
     */
    default Integer membershipLevel(Owner owner) {
        return MembershipLevels.level(owner);
    }

    /**
     * Derives the owner's locality (region) from the {@code customerCode}, whose form is
     * {@code '<REGION>-<HASH8>'}: the locality is the {@code REGION} segment before the first
     * hyphen. Returns {@code "UNKNOWN"} when the owner has no customer code yet.
     */
    default String locality(Owner owner) {
        if (owner == null) {
            return null;
        }
        String code = owner.getCustomerCode();
        if (code == null) {
            return "UNKNOWN";
        }
        int dash = code.indexOf('-');
        return dash >= 0 ? code.substring(0, dash) : code;
    }

    /**
     * Derives the owner's IANA {@code timezone} from its locality/region using the fixed
     * region-to-timezone table ({@code NSW -> Australia/Sydney}, {@code VIC -> Australia/Melbourne},
     * {@code QLD -> Australia/Brisbane}). Returns {@code null} when the region is not in the table.
     */
    default String timezone(Owner owner) {
        if (owner == null) {
            return null;
        }
        String region = locality(owner);
        return switch (region) {
            case "NSW" -> "Australia/Sydney";
            case "VIC" -> "Australia/Melbourne";
            case "QLD" -> "Australia/Brisbane";
            default -> null;
        };
    }

    /**
     * Whether the owner carries the bulk-signup warning (more than 80 owners already created
     * on its registration day at creation time). A {@code null} stored value is treated as
     * {@code false}, so the field is always present in the response.
     */
    default Boolean bulkSignupWarning(Owner owner) {
        if (owner == null) {
            return null;
        }
        return Boolean.TRUE.equals(owner.getBulkSignupWarning());
    }

    /**
     * Whether the owner carries the capacity warning (its city already held between 40 and 49
     * owners, approaching the capacity limit of 50, at creation time). A {@code null} stored value
     * is treated as {@code false}, so the field is always present in the response.
     */
    default Boolean capacityWarning(Owner owner) {
        if (owner == null) {
            return null;
        }
        return Boolean.TRUE.equals(owner.getCapacityWarning());
    }

    /**
     * Whether the owner was flagged as a possible (soft) duplicate at creation time — not a hard
     * duplicate, but sharing an existing owner's last-name Soundex code and postcode while differing
     * on the identity key (typically a different telephone). A {@code null} stored value is treated
     * as {@code false}, so the field is always present in the response.
     */
    default Boolean possibleDuplicate(Owner owner) {
        if (owner == null) {
            return null;
        }
        return Boolean.TRUE.equals(owner.getPossibleDuplicate());
    }

    /**
     * Derives the owner's preferred contact channel: {@code EMAIL} when an email address is
     * present, otherwise {@code PHONE}.
     */
    default OwnerDto.ContactPreferenceEnum contactPreference(Owner owner) {
        if (owner == null) {
            return null;
        }
        String email = owner.getEmail();
        return (email != null && !email.isEmpty())
            ? OwnerDto.ContactPreferenceEnum.EMAIL
            : OwnerDto.ContactPreferenceEnum.PHONE;
    }

    /**
     * Derives the owner's {@code identityKey}, the single value into which duplicate detection is
     * consolidated: the SHA-256 hex over
     * {@code normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)}.
     */
    default String identityKey(Owner owner) {
        return IdentityKeyNormalizer.derive(owner);
    }

    /**
     * Derives the owner's {@code ageBand} from {@code birthDate}, measured as of the owner's
     * {@code registrationDate}: {@code MINOR} when under 18, {@code ADULT} when 18 to 64, and
     * {@code SENIOR} when 65 or older. Returns {@code null} when no birth date is known (or when
     * no reference registration date is available), so the field is absent from the response.
     */
    default OwnerDto.AgeBandEnum ageBand(Owner owner) {
        if (owner == null || owner.getBirthDate() == null) {
            return null;
        }
        LocalDate reference = owner.getRegistrationDate();
        if (reference == null) {
            reference = LocalDate.now();
        }
        int age = Period.between(owner.getBirthDate(), reference).getYears();
        if (age < 18) {
            return OwnerDto.AgeBandEnum.MINOR;
        }
        if (age < 65) {
            return OwnerDto.AgeBandEnum.ADULT;
        }
        return OwnerDto.AgeBandEnum.SENIOR;
    }

    /**
     * Derives the owner's {@code selfLink}, the canonical API path for the owner, formed as
     * {@code '/api/owners/'} followed by the owner's id. Returns {@code null} until the owner
     * has an id, so the field is absent from the response.
     */
    default String selfLink(Owner owner) {
        if (owner == null || owner.getId() == null) {
            return null;
        }
        return "/api/owners/" + owner.getId();
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    Owner toOwner(OwnerFieldsDto ownerDto);

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
