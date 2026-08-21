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
        expression = "java(owner == null ? null : owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials",
        expression = "java(owner == null ? null : "
            + "Character.toUpperCase(owner.getFirstName().charAt(0)) + \".\" "
            + "+ Character.toUpperCase(owner.getLastName().charAt(0)) + \".\")")
    @Mapping(target = "locality", expression = "java(deriveLocality(owner))")
    @Mapping(target = "timezone", expression = "java(deriveTimezone(owner))")
    @Mapping(target = "contactPreference",
        expression = "java(owner == null ? null : "
            + "(owner.getEmail() != null && !owner.getEmail().isEmpty() ? \"EMAIL\" : \"PHONE\"))")
    @Mapping(target = "identityKey",
        expression = "java(owner == null ? null : owner.getTelephone() + \"|\" "
            + "+ (owner.getEmail() == null ? \"\" : owner.getEmail()) + \"|\" "
            + "+ (owner.getHouseholdId() == null ? \"\" : owner.getHouseholdId()))")
    @Mapping(target = "checkDigit", expression = "java(deriveCheckDigit(owner))")
    @Mapping(target = "ageBand", expression = "java(deriveAgeBand(owner))")
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
     * Derive the owner's locality (region) from its region-and-hash {@code customerCode}: the
     * {@code REGION} portion is the segment before the first {@code '-'} (e.g. {@code "NSW-1A2B3C4D"}
     * yields {@code "NSW"}). This shares the identity's region derivation, where the postcode range
     * takes precedence (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099) and the city-to-region table is
     * the fallback. For an owner without a {@code customerCode} the same region is recomputed directly
     * from the postcode then city, otherwise "UNKNOWN".
     */
    default String deriveLocality(Owner owner) {
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
        String postcode = owner.getPostcode();
        if (postcode != null) {
            try {
                int pc = Integer.parseInt(postcode.trim());
                if (pc >= 2000 && pc <= 2099) {
                    return "NSW";
                }
                if (pc >= 3000 && pc <= 3099) {
                    return "VIC";
                }
                if (pc >= 4000 && pc <= 4099) {
                    return "QLD";
                }
            }
            catch (NumberFormatException ignored) {
                // not a numeric postcode; fall back to the city-to-region table
            }
        }
        String city = owner.getCity();
        if ("Sydney".equals(city)) {
            return "NSW";
        }
        if ("Melbourne".equals(city)) {
            return "VIC";
        }
        if ("Brisbane".equals(city)) {
            return "QLD";
        }
        return "UNKNOWN";
    }

    /**
     * Derive the owner's IANA timezone from its locality/region using the fixed region-to-timezone
     * table (NSW->Australia/Sydney, VIC->Australia/Melbourne, QLD->Australia/Brisbane). The region is
     * the same value reported as {@code locality}. Returns null when the owner is absent or the region
     * has no known timezone.
     */
    default String deriveTimezone(Owner owner) {
        if (owner == null) {
            return null;
        }
        String region = deriveLocality(owner);
        if ("NSW".equals(region)) {
            return "Australia/Sydney";
        }
        if ("VIC".equals(region)) {
            return "Australia/Melbourne";
        }
        if ("QLD".equals(region)) {
            return "Australia/Brisbane";
        }
        return null;
    }

    /**
     * Derive the owner's check digit: a single Luhn check digit (0-9) computed over the
     * digits contained in the owner's customerCode. Non-digit characters are ignored.
     * Returns null when the owner or its customerCode is absent.
     */
    default Integer deriveCheckDigit(Owner owner) {
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
     * Derive the owner's age band from its birthDate against the registrationDate: 'MINOR' when
     * under 18, 'ADULT' from 18 to 64, and 'SENIOR' at 65 or older. The age is the number of whole
     * years between birthDate and registrationDate (falling back to the current date when the owner
     * has no registrationDate). Returns null when the owner or its birthDate is absent, so no
     * ageBand is reported.
     */
    default String deriveAgeBand(Owner owner) {
        if (owner == null || owner.getBirthDate() == null) {
            return null;
        }
        java.time.LocalDate reference = owner.getRegistrationDate() != null
            ? owner.getRegistrationDate() : java.time.LocalDate.now();
        int age = java.time.Period.between(owner.getBirthDate(), reference).getYears();
        if (age < 18) {
            return "MINOR";
        }
        if (age < 65) {
            return "ADULT";
        }
        return "SENIOR";
    }

    /**
     * Known E.164 country calling codes, longest first, used to split a stored telephone into its
     * country code and national digits. A number whose code is not listed falls back to a two-digit
     * country code, the most common E.164 length.
     */
    String[] KNOWN_CALLING_CODES = { "61", "1" };

    /**
     * Format the owner's stored E.164 {@code telephone} for humans: the country code, a space, then
     * the national digits grouped in threes (e.g. {@code "+61412345678"} becomes
     * {@code "+61 412 345 678"}). Returns null when the owner or its telephone is absent, and returns
     * the value unchanged when it is not in E.164 form.
     */
    default String deriveTelephoneDisplay(Owner owner) {
        if (owner == null || owner.getTelephone() == null) {
            return null;
        }
        String telephone = owner.getTelephone();
        if (!telephone.startsWith("+")) {
            return telephone;
        }
        String digits = telephone.substring(1);
        int codeLength = 2;
        for (String code : KNOWN_CALLING_CODES) {
            if (digits.startsWith(code) && digits.length() > code.length()) {
                codeLength = code.length();
                break;
            }
        }
        if (digits.length() <= codeLength) {
            return telephone;
        }
        String national = digits.substring(codeLength);
        StringBuilder display = new StringBuilder("+").append(digits, 0, codeLength);
        for (int i = 0; i < national.length(); i += 3) {
            display.append(' ').append(national, i, Math.min(i + 3, national.length()));
        }
        return display.toString();
    }

    /**
     * Derive the owner's salutation: the honorific {@code title} followed by a single space and the
     * {@code lastName} (e.g. {@code "DR Who"}). When the owner has no title (null or blank) the
     * salutation is just the {@code lastName}. Returns null when the owner is absent.
     */
    default String deriveSalutation(Owner owner) {
        if (owner == null) {
            return null;
        }
        String title = owner.getTitle();
        String lastName = owner.getLastName();
        if (title == null || title.isBlank()) {
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
