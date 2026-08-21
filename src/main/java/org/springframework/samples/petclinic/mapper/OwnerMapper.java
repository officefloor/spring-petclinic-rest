package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
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
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    @Mapping(target = "displayName",
        expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials",
        expression = "java(Character.toUpperCase(owner.getFirstName().charAt(0)) + \".\" "
            + "+ Character.toUpperCase(owner.getLastName().charAt(0)) + \".\")")
    @Mapping(target = "membershipPoints", expression = "java(membershipPoints(owner))")
    @Mapping(target = "membershipLevel", expression = "java(membershipLevel(membershipPoints(owner)))")
    @Mapping(target = "locality", expression = "java(deriveLocality(owner))")
    @Mapping(target = "timezone", expression = "java(deriveTimezone(owner))")
    @Mapping(target = "ageBand", expression = "java(deriveAgeBand(owner))")
    @Mapping(target = "checkDigit", expression = "java(luhnCheckDigit(owner.getCustomerCode()))")
    @Mapping(target = "contactPreference",
        expression = "java(owner.getEmail() != null && !owner.getEmail().isEmpty() "
            + "? org.springframework.samples.petclinic.rest.dto.OwnerDto.ContactPreferenceEnum.EMAIL "
            + ": org.springframework.samples.petclinic.rest.dto.OwnerDto.ContactPreferenceEnum.PHONE)")
    @Mapping(target = "telephoneDisplay", expression = "java(deriveTelephoneDisplay(owner))")
    @Mapping(target = "salutation", expression = "java(deriveSalutation(owner))")
    OwnerDto toOwnerDto(Owner owner);

    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    Owner toOwner(OwnerFieldsDto ownerDto);

    List<OwnerDto> toOwnerDtoCollection(Collection<Owner> ownerCollection);

    Collection<Owner> toOwners(Collection<OwnerDto> ownerDtos);

    /**
     * Derive the locality region for an owner from its region-and-hash customer code. The customer code
     * is {@code <REGION>-<HASH8>}, so the locality is the region segment before the first hyphen (the
     * same region the identity itself was built from: postcode range first, then the city-to-region
     * table, otherwise "UNKNOWN"). When no customer code is present, the locality is "UNKNOWN".
     */
    default String deriveLocality(Owner owner) {
        String customerCode = owner.getCustomerCode();
        if (customerCode != null) {
            int dash = customerCode.indexOf('-');
            if (dash > 0) {
                return customerCode.substring(0, dash);
            }
        }
        return "UNKNOWN";
    }

    /**
     * Region -> IANA timezone name, the pinned region-to-timezone table:
     * NSW->Australia/Sydney, VIC->Australia/Melbourne, QLD->Australia/Brisbane.
     */
    java.util.Map<String, String> REGION_TIMEZONE = java.util.Map.of(
        "NSW", "Australia/Sydney", "VIC", "Australia/Melbourne", "QLD", "Australia/Brisbane");

    /**
     * Derive the owner's IANA timezone from its locality/region via the fixed region-to-timezone
     * table (NSW->Australia/Sydney, VIC->Australia/Melbourne, QLD->Australia/Brisbane). Returns
     * {@code null} when the region has no mapping (e.g. "UNKNOWN"), so the field is absent from
     * the response.
     */
    default String deriveTimezone(Owner owner) {
        return REGION_TIMEZONE.get(deriveLocality(owner));
    }

    /**
     * Derive the owner's age band from its birth date, computed against the registration date. The age
     * is the number of whole years between the birth date and the effective registration date (falling
     * back to the current date when no registration date is present): {@code MINOR} when under 18,
     * {@code ADULT} for 18-64 and {@code SENIOR} for 65 and over. Returns {@code null} when no birth
     * date was supplied, so the field is absent from the response.
     */
    default OwnerDto.AgeBandEnum deriveAgeBand(Owner owner) {
        java.time.LocalDate birthDate = owner.getBirthDate();
        if (birthDate == null) {
            return null;
        }
        java.time.LocalDate reference = owner.getRegistrationDate() != null
            ? owner.getRegistrationDate() : java.time.LocalDate.now();
        int age = java.time.Period.between(birthDate, reference).getYears();
        if (age < 18) {
            return OwnerDto.AgeBandEnum.MINOR;
        }
        if (age < 65) {
            return OwnerDto.AgeBandEnum.ADULT;
        }
        return OwnerDto.AgeBandEnum.SENIOR;
    }

    /**
     * Compute the owner's membership points: starting at 0, add 2 when an email is present, add 1
     * when {@code namesakeCount} is 0, add 2 for a household of 3 or more members, and add 3 for a
     * tenure of more than 365 days.
     */
    default int membershipPoints(Owner owner) {
        int points = 0;
        if (owner.getEmail() != null && !owner.getEmail().isEmpty()) {
            points += 2;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            points += 1;
        }
        if (owner.getHouseholdSize() != null && owner.getHouseholdSize() >= 3) {
            points += 2;
        }
        if (tenureDays(owner) > 365) {
            points += 3;
        }
        return points;
    }

    /**
     * Map membership points onto the numeric membership level: 1 for 0-1 points, 2 for 2-3 points,
     * 3 for 4-5 points and 4 for 6 or more points.
     */
    default int membershipLevel(int points) {
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
     * Compute the owner's tenure in whole days: the number of days between the owner's registration
     * date and today. A newly created owner registered today therefore has a tenure of zero, and an
     * owner with no registration date is treated as having zero tenure. Used to gate the tenure
     * membership points, which require a tenure of more than 365 days.
     */
    default long tenureDays(Owner owner) {
        java.time.LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(registrationDate, java.time.LocalDate.now());
    }

    /**
     * Compute the single Luhn check digit (0-9) over the digits contained in the given value.
     * Non-digit characters (such as the hyphens in a customer code) are ignored. A null or
     * digit-free value yields a check digit of 0.
     */
    default Integer luhnCheckDigit(String value) {
        if (value == null) {
            return 0;
        }
        int sum = 0;
        boolean doubleDigit = true;
        for (int i = value.length() - 1; i >= 0; i--) {
            char c = value.charAt(i);
            if (c < '0' || c > '9') {
                continue;
            }
            int digit = c - '0';
            if (doubleDigit) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            doubleDigit = !doubleDigit;
        }
        return (10 - (sum % 10)) % 10;
    }

    /**
     * Known country calling codes, ordered longest-prefix-first, used to split a stored E.164
     * telephone into its country code and national digits when building {@code telephoneDisplay}.
     * Mirrors the country codes the create endpoint recognises ({@code +61} and {@code +1}).
     */
    List<String> TELEPHONE_COUNTRY_CODES = List.of("61", "1");

    /**
     * Format the owner's stored E.164 telephone for humans: the country code, a single space, then
     * the national digits grouped in threes (e.g. {@code +61412345678} becomes
     * {@code +61 412 345 678}). Returns the raw value unchanged when it is null, not in E.164 form,
     * or carries an unrecognised country code.
     */
    default String deriveTelephoneDisplay(Owner owner) {
        String telephone = owner.getTelephone();
        if (telephone == null || !telephone.startsWith("+")) {
            return telephone;
        }
        String digits = telephone.substring(1);
        String countryCode = null;
        for (String code : TELEPHONE_COUNTRY_CODES) {
            if (digits.startsWith(code) && digits.length() > code.length()) {
                countryCode = code;
                break;
            }
        }
        if (countryCode == null) {
            return telephone;
        }
        String national = digits.substring(countryCode.length());
        StringBuilder grouped = new StringBuilder();
        for (int i = 0; i < national.length(); i++) {
            if (i > 0 && i % 3 == 0) {
                grouped.append(' ');
            }
            grouped.append(national.charAt(i));
        }
        return "+" + countryCode + " " + grouped;
    }

    /**
     * Compose the owner's salutation from its title and last name: the title and last name separated
     * by a single space (e.g. {@code DR who}) when a title is present, or just the last name when no
     * title (null or blank) was supplied.
     */
    default String deriveSalutation(Owner owner) {
        String lastName = owner.getLastName();
        String title = owner.getTitle();
        if (title == null || title.isEmpty()) {
            return lastName;
        }
        return title + " " + lastName;
    }

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
