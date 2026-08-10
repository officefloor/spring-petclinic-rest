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
        expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "initials",
        expression = "java(Character.toUpperCase(owner.getFirstName().charAt(0)) + \".\" "
            + "+ Character.toUpperCase(owner.getLastName().charAt(0)) + \".\")")
    @Mapping(target = "householdId", expression = "java(householdId(owner))")
    @Mapping(target = "identityKey", expression = "java(identityKey(owner))")
    @Mapping(target = "membershipNumber", expression = "java(membershipNumber(owner))")
    @Mapping(target = "checkDigit", expression = "java(checkDigit(owner))")
    @Mapping(target = "membershipLevel", expression = "java(membershipLevel(owner))")
    @Mapping(target = "locality", expression = "java(locality(owner))")
    @Mapping(target = "telephoneDisplay", expression = "java(telephoneDisplay(owner))")
    @Mapping(target = "contactPreference", expression = "java(contactPreference(owner))")
    @Mapping(target = "ageBand", expression = "java(ageBand(owner))")
    OwnerDto toOwnerDto(Owner owner);

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
     * Derives an owner's numeric membership level. Starts at {@code 1}, plus {@code 1} when an email
     * is present (non-blank), plus {@code 1} when the owner's namesake count is {@code 0}; these
     * pre-tenure factors are capped at {@code 3}. Level {@code 4} additionally requires a tenure of
     * more than {@code 365} days - the number of days between the owner's registration date and the
     * current server date. Because a newly created owner registers on the current date, its tenure is
     * zero, so a new owner never exceeds level {@code 3} even with an email and a namesake count of
     * {@code 0}.
     *
     * @param owner the owner to derive the membership level for
     * @return the membership level, between {@code 1} and {@code 4}
     */
    default Integer membershipLevel(Owner owner) {
        int level = 1;
        String email = owner.getEmail();
        if (email != null && !email.isBlank()) {
            level++;
        }
        Integer namesakeCount = owner.getNamesakeCount();
        if (namesakeCount != null && namesakeCount == 0) {
            level++;
        }
        level = Math.min(level, 3);
        java.time.LocalDate registrationDate = owner.getRegistrationDate();
        long tenureDays = registrationDate == null ? 0
            : java.time.temporal.ChronoUnit.DAYS.between(registrationDate, java.time.LocalDate.now());
        if (level == 3 && tenureDays > 365) {
            level = 4;
        }
        return level;
    }

    /**
     * Derives an owner's membership number, formatted {@code <customerCode>-M<YY>} where
     * {@code <customerCode>} is the owner's region-and-hash customer code and {@code YY} is the last
     * two digits (zero-padded) of the owner's registration date year (e.g. {@code NSW-1A2B3C4D-M26}).
     *
     * @param owner the owner to derive the membership number for
     * @return the formatted membership number
     */
    default String membershipNumber(Owner owner) {
        String yy = String.format("%02d", owner.getRegistrationDate().getYear() % 100);
        return owner.getCustomerCode() + "-M" + yy;
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
