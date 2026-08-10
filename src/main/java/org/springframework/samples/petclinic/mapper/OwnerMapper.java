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

    @Mapping(target = "selfLink", expression = "java(selfLink(owner))")
    @Mapping(target = "salutation", expression = "java(salutation(owner))")
    @Mapping(target = "displayName",
        expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials",
        expression = "java(Character.toUpperCase(owner.getFirstName().charAt(0)) + \".\" "
            + "+ Character.toUpperCase(owner.getLastName().charAt(0)) + \".\")")
    @Mapping(target = "householdId", expression = "java(householdId(owner))")
    @Mapping(target = "identityKey", expression = "java(identityKey(owner))")
    @Mapping(target = "membershipNumber", expression = "java(membershipNumber(owner))")
    @Mapping(target = "fiscalYear", expression = "java(fiscalYear(owner))")
    @Mapping(target = "checkDigit", expression = "java(checkDigit(owner))")
    @Mapping(target = "membershipPoints", expression = "java(membershipPoints(owner))")
    @Mapping(target = "membershipLevel", expression = "java(membershipLevel(owner))")
    @Mapping(target = "locality", expression = "java(locality(owner))")
    @Mapping(target = "timezone", expression = "java(timezone(owner))")
    @Mapping(target = "telephoneDisplay", expression = "java(telephoneDisplay(owner))")
    @Mapping(target = "contactPreference", expression = "java(contactPreference(owner))")
    @Mapping(target = "ageBand", expression = "java(ageBand(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /**
     * Derives a link to the owner's own resource, formatted {@code /api/owners/<id>} where
     * {@code <id>} is the owner's id. Returns {@code null} when the owner has no id yet, so the
     * field is omitted from the response.
     *
     * @param owner the owner to derive the self link for
     * @return the {@code /api/owners/<id>} self link, or {@code null} when the owner has no id
     */
    default String selfLink(Owner owner) {
        return owner.getId() == null ? null : "/api/owners/" + owner.getId();
    }

    /**
     * Derives an owner's salutation from its optional title and last name: {@code "<title> <lastName>"}
     * when a non-blank title is present, or just the last name when no title was supplied.
     *
     * @param owner the owner to derive the salutation for
     * @return the composed salutation
     */
    default String salutation(Owner owner) {
        String title = owner.getTitle();
        String lastName = owner.getLastName();
        if (title == null || title.isBlank()) {
            return lastName;
        }
        return title + " " + lastName;
    }

    /**
     * Derives an owner's age band from its birth date, computed as the owner's age on its
     * {@link Owner#getRegistrationDate() registration date}: {@code "MINOR"} when under {@code 18},
     * {@code "ADULT"} when {@code 18} to {@code 64}, and {@code "SENIOR"} when {@code 65} or older.
     * Returns {@code null} when the owner has no birth date, so the field is omitted from the
     * response.
     *
     * @param owner the owner to derive the age band for
     * @return {@code "MINOR"}, {@code "ADULT"} or {@code "SENIOR"}, or {@code null} when no birth date
     *     is held
     */
    default String ageBand(Owner owner) {
        java.time.LocalDate birthDate = owner.getBirthDate();
        java.time.LocalDate registrationDate = owner.getRegistrationDate();
        if (birthDate == null || registrationDate == null) {
            return null;
        }
        int age = java.time.Period.between(birthDate, registrationDate).getYears();
        if (age < 18) {
            return "MINOR";
        }
        return age < 65 ? "ADULT" : "SENIOR";
    }

    /**
     * Derives an owner's preferred contact channel: {@code "EMAIL"} when the owner has a non-blank
     * email, otherwise {@code "PHONE"}.
     *
     * @param owner the owner to derive the contact preference for
     * @return {@code "EMAIL"} when an email is present, otherwise {@code "PHONE"}
     */
    default String contactPreference(Owner owner) {
        String email = owner.getEmail();
        return (email != null && !email.isBlank()) ? "EMAIL" : "PHONE";
    }

    /**
     * Known telephone country codes, matched longest-prefix first so {@code "61"} is preferred over
     * the {@code "1"} it starts with. Mirrors the country codes the create endpoint recognizes when
     * normalizing telephones into E.164 form.
     */
    java.util.List<String> TELEPHONE_COUNTRY_CODES = java.util.List.of("61", "1");

    /**
     * Formats an owner's stored E.164 telephone for humans: the country code, a space, then the
     * national digits grouped in threes (e.g. {@code +61412345678} becomes {@code +61 412 345 678}).
     * The country code is taken as the longest recognized prefix (see
     * {@link #TELEPHONE_COUNTRY_CODES}), falling back to the leading digit; the raw
     * {@link Owner#getTelephone() telephone} stays in E.164 form. A {@code null} or non-E.164 value
     * is returned unchanged.
     *
     * @param owner the owner to format the telephone display for
     * @return the human-formatted telephone, or the raw value when it is not a {@code +}-prefixed
     *     digit string
     */
    default String telephoneDisplay(Owner owner) {
        String telephone = owner.getTelephone();
        if (telephone == null || !telephone.startsWith("+")) {
            return telephone;
        }
        String digits = telephone.substring(1);
        if (!digits.matches("[0-9]+")) {
            return telephone;
        }
        String countryCode = TELEPHONE_COUNTRY_CODES.stream()
            .filter(digits::startsWith)
            .max(java.util.Comparator.comparingInt(String::length))
            .orElseGet(() -> digits.substring(0, 1));
        String national = digits.substring(countryCode.length());
        StringBuilder sb = new StringBuilder("+").append(countryCode);
        for (int i = 0; i < national.length(); i += 3) {
            sb.append(' ').append(national, i, Math.min(i + 3, national.length()));
        }
        return sb.toString();
    }

    /**
     * Derives an owner's locality from the region-and-hash customer code: the {@code <REGION>}
     * segment (everything before the first {@code '-'}) of the owner's {@code customerCode}. Because
     * the customer code's region is itself derived from the postcode (falling back to the city), the
     * locality now moves with the identity rather than being computed independently. The result is
     * {@code "UNKNOWN"} when no customer code is present.
     *
     * @param owner the owner to derive the locality for
     * @return the region segment of the customer code, or {@code "UNKNOWN"}
     */
    default String locality(Owner owner) {
        String code = owner.getCustomerCode();
        if (code == null || code.isEmpty()) {
            return "UNKNOWN";
        }
        int dash = code.indexOf('-');
        return dash < 0 ? code : code.substring(0, dash);
    }

    /**
     * Fixed region-to-timezone table mapping a canonical region to its IANA timezone name:
     * {@code NSW->Australia/Sydney}, {@code VIC->Australia/Melbourne}, {@code QLD->Australia/Brisbane}.
     */
    java.util.Map<String, String> REGION_TIMEZONES = java.util.Map.of(
        "NSW", "Australia/Sydney", "VIC", "Australia/Melbourne", "QLD", "Australia/Brisbane");

    /**
     * Derives an owner's IANA timezone name from its {@link #locality(Owner) locality/region} using
     * the fixed region-to-timezone table ({@code NSW->Australia/Sydney}, {@code VIC->Australia/Melbourne},
     * {@code QLD->Australia/Brisbane}). Returns {@code null} when the region has no known timezone, so
     * the field is omitted from the response.
     *
     * @param owner the owner to derive the timezone for
     * @return the region's IANA timezone name, or {@code null} when the region has no known timezone
     */
    default String timezone(Owner owner) {
        return REGION_TIMEZONES.get(locality(owner));
    }

    /**
     * Derives an owner's membership points. Starts at {@code 0}, plus {@code 2} when an email is
     * present (non-blank), plus {@code 1} when the owner's namesake count is {@code 0}, plus
     * {@code 2} for a household of {@code 3} or more members, plus {@code 3} for a tenure of more
     * than one elapsed fiscal year - the number of fiscal years between the owner's registration
     * date and the current server date (the fiscal year starts on 1 July). Because a newly created
     * owner registers on the current date, its tenure is zero, so a new owner never earns the tenure
     * points.
     *
     * @param owner the owner to derive the membership points for
     * @return the membership points, {@code 0} or more
     */
    default Integer membershipPoints(Owner owner) {
        int points = 0;
        String email = owner.getEmail();
        if (email != null && !email.isBlank()) {
            points += 2;
        }
        Integer namesakeCount = owner.getNamesakeCount();
        if (namesakeCount != null && namesakeCount == 0) {
            points += 1;
        }
        Integer householdMemberCount = owner.getHouseholdMemberCount();
        if (householdMemberCount != null && householdMemberCount >= 3) {
            points += 2;
        }
        java.time.LocalDate registrationDate = owner.getRegistrationDate();
        long tenureFiscalYears = registrationDate == null ? 0
            : fiscalYearValue(java.time.LocalDate.now()) - fiscalYearValue(registrationDate);
        if (tenureFiscalYears > 1) {
            points += 3;
        }
        return points;
    }

    /**
     * Derives an owner's numeric membership level from its {@link #membershipPoints(Owner) membership
     * points}: level {@code 1} for {@code 0}-{@code 1} points, {@code 2} for {@code 2}-{@code 3},
     * {@code 3} for {@code 4}-{@code 5}, and {@code 4} for {@code 6} or more.
     *
     * @param owner the owner to derive the membership level for
     * @return the membership level, between {@code 1} and {@code 4}
     */
    default Integer membershipLevel(Owner owner) {
        int points = membershipPoints(owner);
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
     * Derives an owner's membership number, formatted {@code <customerCode>-M<YY>} where
     * {@code <customerCode>} is the owner's region-and-hash customer code and {@code YY} is the last
     * two digits (zero-padded) of the owner's {@link #fiscalYear(Owner) fiscal year} - so the year
     * segment matches the returned {@code fiscalYear} (e.g. {@code NSW-1A2B3C4D-M27}).
     *
     * @param owner the owner to derive the membership number for
     * @return the formatted membership number
     */
    default String membershipNumber(Owner owner) {
        String yy = String.format("%02d", fiscalYearValue(owner.getRegistrationDate()) % 100);
        return owner.getCustomerCode() + "-M" + yy;
    }

    /**
     * Derives an owner's fiscal year, formatted {@code FY<YY>} where {@code YY} is the last two
     * digits (zero-padded) of the fiscal year that the owner's business-day-adjusted registration
     * date falls in. The fiscal year starts on 1 July and is labelled by the calendar year in which
     * it ends, so a registration date on or after 1 July belongs to the next year's fiscal year
     * (e.g. {@code 2026-08-11 -> FY27}, {@code 2026-01-05 -> FY26}).
     *
     * @param owner the owner to derive the fiscal year for
     * @return the formatted fiscal year
     */
    default String fiscalYear(Owner owner) {
        return String.format("FY%02d", fiscalYearValue(owner.getRegistrationDate()) % 100);
    }

    /**
     * Computes the fiscal year a date falls in as a full four-digit year. The fiscal year starts on
     * 1 July and is labelled by the calendar year in which it ends, so a date in July through
     * December belongs to the following calendar year's fiscal year and a date in January through
     * June belongs to its own calendar year's fiscal year.
     *
     * @param date the date to compute the fiscal year for
     * @return the fiscal year as a four-digit calendar year
     */
    private int fiscalYearValue(java.time.LocalDate date) {
        return date.getMonthValue() >= 7 ? date.getYear() + 1 : date.getYear();
    }

    /**
     * Derives an owner's check digit: a single Luhn check digit ({@code 0}-{@code 9}) computed over
     * the digits contained in the owner's customer code. Non-digit characters in the customer code
     * are ignored.
     *
     * @param owner the owner to derive the check digit for
     * @return the Luhn check digit, between {@code 0} and {@code 9}
     */
    default Integer checkDigit(Owner owner) {
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
     * Derives an owner's household identifier: the first 12 hex characters of the SHA-256 digest
     * over {@code <normalizedLastName>|<postcode>}, where the last name is normalized
     * case-insensitively with collapsed whitespace and a missing postcode contributes the empty
     * string. Because it is derived deterministically from the last name and postcode, every owner
     * that shares those two fields receives the same value - including owners created via the
     * {@code sharesHousehold} flag, which by definition have a matching last name and postcode.
     *
     * @param owner the owner to derive the household identifier for
     * @return the 12 upper-case hex character household identifier, never blank
     */
    default String householdId(Owner owner) {
        String postcode = owner.getPostcode() == null ? "" : owner.getPostcode();
        String key = normalizeHouseholdField(owner.getLastName()) + '|' + postcode;
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(12);
            for (int i = 0; i < 6; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    /**
     * Derives an owner's identity key, into which all duplicate detection is consolidated. The key
     * is {@code <normalizedTelephone>|<email or empty>|<householdId>}: the owner's (already E.164
     * normalized) telephone, its (already lower-cased) email or the empty string when none is held,
     * and its {@link #householdId(Owner) household id}. Two owners are duplicates only when their
     * whole identity keys are equal; because the telephone is part of the key, household members with
     * different telephones have different keys.
     *
     * @param owner the owner to derive the identity key for
     * @return the {@code telephone|email|householdId} identity key
     */
    default String identityKey(Owner owner) {
        String telephone = owner.getTelephone() == null ? "" : owner.getTelephone();
        String email = owner.getEmail() == null ? "" : owner.getEmail();
        return telephone + '|' + email + '|' + householdId(owner);
    }

    /**
     * Collapses surrounding and internal whitespace and lower-cases a value so household fields can
     * be compared case-insensitively with collapsed whitespace. A {@code null} value normalizes to
     * the empty string. Mirrors the normalization used when enforcing the household rule on create.
     *
     * <p>Declared {@code private} so MapStruct does not treat it as an implicit String-to-String
     * conversion and apply it to unrelated String properties (last name, address, ...).
     */
    private String normalizeHouseholdField(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    Owner toOwner(OwnerDto ownerDto);

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
