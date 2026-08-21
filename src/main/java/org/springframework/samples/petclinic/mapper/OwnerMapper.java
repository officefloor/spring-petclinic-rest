package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

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
    @Mapping(target = "identityKey", expression = "java(deriveIdentityKey(owner))")
    @Mapping(target = "ageBand", expression = "java(deriveAgeBand(owner))")
    @Mapping(target = "telephoneDisplay", expression = "java(deriveTelephoneDisplay(owner))")
    @Mapping(target = "salutation", expression = "java(deriveSalutation(owner))")
    @Mapping(target = "fiscalYear", expression = "java(deriveFiscalYear(owner))")
    @Mapping(target = "ownerSegment", expression = "java(deriveOwnerSegment(owner))")
    @Mapping(target = "selfLink",
        expression = "java(owner == null || owner.getId() == null ? null : \"/api/owners/\" + owner.getId())")
    OwnerDto toOwnerDto(Owner owner);

    Owner toOwner(OwnerDto ownerDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pets", ignore = true)
    Owner toOwner(OwnerFieldsDto ownerDto);

    List<OwnerDto> toOwnerDtoCollection(Collection<Owner> ownerCollection);

    Collection<Owner> toOwners(Collection<OwnerDto> ownerDtos);

    /**
     * Derive the owner's locality (region) from its postcode, falling back to its city. This shares
     * the identity's region derivation, where the postcode range takes precedence (NSW 2000-2099, VIC
     * 3000-3099, QLD 4000-4099) and the city-to-region table (Sydney->NSW, Melbourne->VIC,
     * Brisbane->QLD) is the fallback, otherwise "UNKNOWN". This is the same region embedded as the
     * {@code REGION} prefix of the owner's {@code memberId}.
     */
    default String deriveLocality(Owner owner) {
        if (owner == null) {
            return null;
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
     * Derive the owner's marketing segment, formatted {@code '<TIER>_<AREA>'}. TIER is
     * {@code "PREMIUM"} when the owner's {@code membershipLevel} is 3 or more, otherwise
     * {@code "STANDARD"} (a missing membershipLevel counts as below 3). AREA is {@code "METRO"}
     * when the owner's locality is a known region (NSW, VIC or QLD), otherwise {@code "REGIONAL"}.
     * Returns null when the owner is absent.
     */
    default String deriveOwnerSegment(Owner owner) {
        if (owner == null) {
            return null;
        }
        Integer membershipLevel = owner.getMembershipLevel();
        String tier = (membershipLevel != null && membershipLevel >= 3) ? "PREMIUM" : "STANDARD";
        String locality = deriveLocality(owner);
        boolean metro = "NSW".equals(locality) || "VIC".equals(locality) || "QLD".equals(locality);
        return tier + "_" + (metro ? "METRO" : "REGIONAL");
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

    /**
     * Derive the owner's fiscal year from its business-day-adjusted {@code registrationDate},
     * formatted {@code 'FY<YY>'} where YY is the last two digits of the fiscal year. The fiscal
     * year starts on 1 July and is labelled by the calendar year in which it ends, so a
     * registrationDate on or after 1 July belongs to the fiscal year ending the following calendar
     * year. Returns null when the owner or its registrationDate is absent.
     */
    default String deriveFiscalYear(Owner owner) {
        if (owner == null || owner.getRegistrationDate() == null) {
            return null;
        }
        java.time.LocalDate registrationDate = owner.getRegistrationDate();
        int fiscalYear = registrationDate.getMonthValue() >= java.time.Month.JULY.getValue()
            ? registrationDate.getYear() + 1 : registrationDate.getYear();
        return String.format("FY%02d", fiscalYear % 100);
    }

    /**
     * Derive the owner's {@code identityKey}: the 64-character lower-case SHA-256 hex digest over
     * {@code normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)}. The telephone and
     * email are the owner's already-normalized stored values (E.164 telephone, lower-cased email); a
     * null email contributes an empty middle segment. Returns null when the owner is absent. This is
     * the same key duplicate detection compares on create.
     */
    default String deriveIdentityKey(Owner owner) {
        if (owner == null) {
            return null;
        }
        String telephone = owner.getTelephone() == null ? "" : owner.getTelephone();
        String email = owner.getEmail() == null ? "" : owner.getEmail();
        return sha256Hex(telephone + "|" + email + "|" + soundex(owner.getLastName()));
    }

    /**
     * The American Soundex code of a name: its first letter followed by up to three digits encoding
     * the remaining consonants (b/f/p/v->1, c/g/j/k/q/s/x/z->2, d/t->3, l->4, m/n->5, r->6), with
     * adjacent duplicate codes collapsed, vowels (and y) resetting the run, and h/w transparent. The
     * result is padded with zeros to exactly four characters. A null or letterless name yields "".
     */
    private static String soundex(String name) {
        if (name == null) {
            return "";
        }
        String letters = name.toUpperCase(Locale.ROOT).replaceAll("[^A-Z]", "");
        if (letters.isEmpty()) {
            return "";
        }
        StringBuilder code = new StringBuilder();
        code.append(letters.charAt(0));
        char prev = soundexCode(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            if (c == 'H' || c == 'W') {
                continue;
            }
            char digit = soundexCode(c);
            if (digit != '0' && digit != prev) {
                code.append(digit);
            }
            prev = digit;
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    /**
     * The Soundex digit for a single upper-case letter, or {@code '0'} for a vowel, y, h or w.
     */
    private static char soundexCode(char c) {
        return switch (c) {
            case 'B', 'F', 'P', 'V' -> '1';
            case 'C', 'G', 'J', 'K', 'Q', 'S', 'X', 'Z' -> '2';
            case 'D', 'T' -> '3';
            case 'L' -> '4';
            case 'M', 'N' -> '5';
            case 'R' -> '6';
            default -> '0';
        };
    }

    /**
     * The full lower-case SHA-256 hex digest (64 characters) of the UTF-8 bytes of {@code value}.
     */
    private static String sha256Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
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
