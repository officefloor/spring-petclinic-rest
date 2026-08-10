package org.springframework.samples.petclinic.mapper;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.IdentityDto;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.OwnerPageDto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * Maps Owner & OwnerDto using Mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface OwnerMapper {

    /** Fixed city-to-region table used to derive an owner's region. */
    java.util.Map<String, String> CITY_REGION =
        java.util.Map.of("Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** Fixed region-to-timezone table (IANA names) used to derive an owner's timezone. */
    java.util.Map<String, String> REGION_TIMEZONE =
        java.util.Map.of("NSW", "Australia/Sydney", "VIC", "Australia/Melbourne",
            "QLD", "Australia/Brisbane");

    @Mapping(target = "selfLink", expression = "java(selfLink(owner))")
    @Mapping(target = "displayName",
        expression = "java(owner.getLastName() + \", \" + owner.getFirstName())")
    @Mapping(target = "salutation", expression = "java(salutation(owner))")
    @Mapping(target = "initials", expression = "java(initials(owner))")
    @Mapping(target = "telephoneDisplay", expression = "java(telephoneDisplay(owner))")
    @Mapping(target = "apiVersion", expression = "java(apiVersion())")
    @Mapping(target = "identity", expression = "java(identity(owner))")
    @Mapping(target = "membershipPoints", expression = "java(membershipPoints(owner))")
    @Mapping(target = "membershipLevel", expression = "java(membershipLevel(owner))")
    @Mapping(target = "fiscalYear", expression = "java(fiscalYear(owner))")
    @Mapping(target = "locality", expression = "java(locality(owner))")
    @Mapping(target = "timezone", expression = "java(timezone(owner))")
    @Mapping(target = "contactPreference", expression = "java(contactPreference(owner))")
    @Mapping(target = "ageBand", expression = "java(ageBand(owner))")
    @Mapping(target = "ownerSegment", expression = "java(ownerSegment(owner))")
    @Mapping(target = "riskFlag", expression = "java(riskFlag(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /** The owner API contract version this representation conforms to. */
    int API_VERSION = 2;

    /**
     * The fixed version tag mixed into every version-2 identifier (the region embedded in
     * the memberId, the householdId and the identityKey) so that no value produced under
     * version 1 is produced again. It deliberately never appears in the user-facing
     * {@link #locality(Owner) locality}, {@link #timezone(Owner) timezone} or
     * {@link #ownerSegment(Owner) ownerSegment}.
     */
    String VERSION_TAG = "V2";

    /** The top-level owner API version, always {@link #API_VERSION 2} under version 2. */
    default Integer apiVersion() {
        return API_VERSION;
    }

    /**
     * The owner's version-2 identity object, grouping the three identifiers — the memberId
     * (assigned on creation), the {@link #identityKey(Owner) identityKey} and the
     * {@link #householdId(Owner) householdId} — that were previously top-level fields.
     */
    default IdentityDto identity(Owner owner) {
        IdentityDto identity = new IdentityDto();
        identity.setMemberId(owner.getMemberId());
        identity.setIdentityKey(identityKey(owner));
        identity.setHouseholdId(householdId(owner));
        return identity;
    }

    /**
     * The owner's canonical API path, formatted '/api/owners/&lt;id&gt;'. Returns
     * {@code null} when the owner has not yet been assigned an id.
     */
    default String selfLink(Owner owner) {
        return owner.getId() == null ? null : "/api/owners/" + owner.getId();
    }

    /**
     * The owner's salutation: the supplied title, a single space and the last name when a
     * title was supplied (e.g. 'DR Who'), or just the last name when no title (null or blank)
     * was given.
     */
    default String salutation(Owner owner) {
        String title = owner.getTitle();
        if (title == null || title.isBlank()) {
            return owner.getLastName();
        }
        return title + " " + owner.getLastName();
    }

    /**
     * The owner's age band, derived from the supplied birthDate as at the
     * registrationDate: 'MINOR' when under 18, 'ADULT' when 18-64, and 'SENIOR' when
     * 65 or older. Returns {@code null} when no birthDate (or registrationDate) is
     * available, so the field is omitted for owners without a supplied birth date.
     */
    default String ageBand(Owner owner) {
        if (owner.getBirthDate() == null || owner.getRegistrationDate() == null) {
            return null;
        }
        int age = java.time.Period.between(owner.getBirthDate(), owner.getRegistrationDate()).getYears();
        if (age < 18) {
            return "MINOR";
        }
        if (age < 65) {
            return "ADULT";
        }
        return "SENIOR";
    }

    /**
     * The owner's preferred contact channel: 'EMAIL' when an email address is present,
     * otherwise 'PHONE'.
     */
    default String contactPreference(Owner owner) {
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isBlank();
        return hasEmail ? "EMAIL" : "PHONE";
    }

    /**
     * The owner's region, read from the leading REGION segment of the
     * {@code <REGION><FY><HASH8><CHK>} memberId assigned on creation: the run of leading
     * letters before the first (fiscal-year) digit. Returns 'UNKNOWN' when no memberId has
     * been assigned (e.g. legacy owners predating the region-and-hash identity).
     */
    default String locality(Owner owner) {
        String id = owner.getMemberId();
        if (id == null) {
            return "UNKNOWN";
        }
        String body = identifierBody(id);
        int regionEnd = regionLength(body);
        return regionEnd == 0 ? "UNKNOWN" : body.substring(0, regionEnd);
    }

    /**
     * The plain {@code <REGION><FY><HASH8><CHK>} body of a memberId with the leading
     * version-2 {@link #VERSION_TAG 'V2'} tag removed, so the user-facing locality and
     * fiscal year read the plain region and fiscal-year segments rather than the tag. A
     * memberId that does not carry the tag (e.g. a legacy value) is returned unchanged.
     */
    private static String identifierBody(String memberId) {
        return memberId.startsWith(VERSION_TAG) ? memberId.substring(VERSION_TAG.length()) : memberId;
    }

    /**
     * The length of the leading REGION segment of a memberId body: the run of leading
     * letters before the first digit (which begins the two-digit fiscal-year segment).
     */
    private static int regionLength(String memberId) {
        int i = 0;
        while (i < memberId.length() && Character.isLetter(memberId.charAt(i))) {
            i++;
        }
        return i;
    }

    /**
     * The owner's IANA timezone, derived from its locality/region via the fixed
     * region-to-timezone table (NSW -> Australia/Sydney, VIC -> Australia/Melbourne,
     * QLD -> Australia/Brisbane). Returns {@code null} when the region is not one of
     * these, so the field is omitted for owners whose region is unknown.
     */
    default String timezone(Owner owner) {
        return REGION_TIMEZONE.get(locality(owner));
    }

    /**
     * The owner's segment, formatted '&lt;TIER&gt;_&lt;AREA&gt;'. TIER is 'PREMIUM' when the
     * {@link #membershipLevel(Owner) membershipLevel} is 3 or more, otherwise 'STANDARD'. AREA is
     * 'METRO' when the {@link #locality(Owner) locality} is a known region (NSW, VIC or QLD),
     * otherwise 'REGIONAL'. One of 'PREMIUM_METRO', 'PREMIUM_REGIONAL', 'STANDARD_METRO' or
     * 'STANDARD_REGIONAL'.
     */
    default String ownerSegment(Owner owner) {
        String tier = membershipLevel(owner) >= 3 ? "PREMIUM" : "STANDARD";
        String area = REGION_TIMEZONE.containsKey(locality(owner)) ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }

    /**
     * The second-level labels of the known disposable email providers (the label immediately
     * before the top-level domain of each blocked disposable domain). Used to recognise
     * disposable-adjacent domains - subdomains or other-TLD near-misses that share a label with
     * one of these providers.
     */
    java.util.Set<String> DISPOSABLE_PROVIDER_LABELS =
        java.util.Set.of("mailinator", "tempmail", "guerrillamail");

    /**
     * The owner's risk flag: true when the owner warrants a manual risk review because any one of
     * these holds - it is a possible duplicate ({@link Owner#getPossibleDuplicate()} is true), its
     * email domain is {@link #disposableAdjacent(String) disposable-adjacent}, or its city is over
     * its soft capacity ({@link Owner#getCapacityWarning()} is true, i.e. the city already held 40
     * or more owners when this owner was created); otherwise false.
     */
    default Boolean riskFlag(Owner owner) {
        return Boolean.TRUE.equals(owner.getPossibleDuplicate())
            || Boolean.TRUE.equals(owner.getCapacityWarning())
            || disposableAdjacent(owner.getEmail());
    }

    /**
     * Whether the given email's domain is disposable-adjacent: not itself a blocked disposable
     * domain (those are rejected on create and never stored), but a subdomain or other-TLD
     * near-miss that carries one of the known {@link #DISPOSABLE_PROVIDER_LABELS disposable
     * provider labels} as one of its dot-separated domain labels (e.g. 'mail.mailinator.com' or
     * 'mailinator.net'). Returns false when the email is absent or its domain contains no such
     * label.
     */
    default boolean disposableAdjacent(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        int at = email.lastIndexOf('@');
        if (at < 0 || at == email.length() - 1) {
            return false;
        }
        String domain = email.substring(at + 1).toLowerCase(Locale.ROOT);
        for (String label : domain.split("\\.")) {
            if (DISPOSABLE_PROVIDER_LABELS.contains(label)) {
                return true;
            }
        }
        return false;
    }

    /**
     * The owner's membership points. Starts at 0; gains 2 when an email address is present,
     * 1 when the owner has no namesakes (namesakeCount is 0), 2 for a household of 3 or more
     * (householdSize is 3 or greater), and 3 when the owner's tenure spans at least one
     * elapsed fiscal year (the current fiscal year, starting 1 July, is later than the
     * registrationDate's fiscal year). Because a newly created owner registers in the current
     * fiscal year, the tenure points are only earned once the fiscal year has rolled over
     * since registration.
     */
    default Integer membershipPoints(Owner owner) {
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
            && fiscalEndYear(java.time.LocalDate.now())
                - fiscalEndYear(owner.getRegistrationDate()) >= 1) {
            points += 3;
        }
        return points;
    }

    /**
     * The owner's numeric membership level, derived from {@link #membershipPoints(Owner)}:
     * level 1 for 0-1 points, level 2 for 2-3 points, level 3 for 4-5 points, and level 4
     * for 6 or more points.
     *
     * <p>The derived level is then capped by the owner's {@code membershipLevelCap} when one is
     * present: a new owner's level cannot exceed one above the current maximum membership level
     * among their existing household members, so the cap (recorded on the owner at creation as
     * that maximum plus one) is applied here. A {@code null} cap means the owner had no existing
     * household member, so no cap applies.
     */
    default Integer membershipLevel(Owner owner) {
        int points = membershipPoints(owner);
        int level;
        if (points <= 1) {
            level = 1;
        }
        else if (points <= 3) {
            level = 2;
        }
        else if (points <= 5) {
            level = 3;
        }
        else {
            level = 4;
        }
        Integer cap = owner.getMembershipLevelCap();
        if (cap != null && level > cap) {
            return cap;
        }
        return level;
    }

    /**
     * The fiscal year, starting on 1 July, that a date falls in, expressed as the last two
     * digits of the calendar year in which the fiscal year ends. Dates on or after 1 July
     * belong to the fiscal year ending the following calendar year (e.g. 2026-08-10 -&gt; 27);
     * dates before 1 July belong to the fiscal year ending in the same calendar year
     * (e.g. 2026-03-01 -&gt; 26).
     */
    private static int fiscalEndYear(java.time.LocalDate date) {
        return date.getMonthValue() >= java.time.Month.JULY.getValue() ? date.getYear() + 1 : date.getYear();
    }

    /**
     * The owner's fiscal year, formatted 'FY&lt;YY&gt;' where YY is the two-digit fiscal-year
     * (FY) segment of the memberId — the two digits immediately following the leading REGION
     * segment (e.g. memberId 'NSW271A2B3C4D5' -&gt; 'FY27'). Returns {@code null} when no
     * memberId has been assigned.
     */
    default String fiscalYear(Owner owner) {
        String id = owner.getMemberId();
        if (id == null) {
            return null;
        }
        String body = identifierBody(id);
        int regionEnd = regionLength(body);
        if (regionEnd + 2 > body.length()) {
            return null;
        }
        return "FY" + body.substring(regionEnd, regionEnd + 2);
    }

    /**
     * Known E.164 country calling codes used to split the stored telephone into its
     * country code and national number for display. Longer codes are matched first so a
     * '+61' number is not mistaken for a '+6...' one.
     */
    java.util.List<String> COUNTRY_CODES = java.util.List.of("61", "1");

    /**
     * The stored E.164 telephone formatted for humans: the country code, a space, and the
     * national digits grouped in threes (e.g. '+61412345678' becomes '+61 412 345 678').
     * Returns the stored value unchanged when it is {@code null} or not in the expected
     * '+'-prefixed all-digits E.164 form.
     */
    default String telephoneDisplay(Owner owner) {
        String telephone = owner.getTelephone();
        if (telephone == null || !telephone.startsWith("+")) {
            return telephone;
        }
        String digits = telephone.substring(1);
        if (digits.isEmpty() || !digits.chars().allMatch(Character::isDigit)) {
            return telephone;
        }
        String countryCode = COUNTRY_CODES.stream()
            .filter(digits::startsWith)
            .findFirst()
            .orElse(digits.substring(0, 1));
        String national = digits.substring(countryCode.length());
        StringBuilder sb = new StringBuilder("+").append(countryCode);
        for (int i = 0; i < national.length(); i++) {
            if (i % 3 == 0) {
                sb.append(' ');
            }
            sb.append(national.charAt(i));
        }
        return sb.toString();
    }

    /**
     * The upper-cased first letters of firstName and lastName, dot-separated with a
     * trailing dot, e.g. 'J.S.'.
     */
    default String initials(Owner owner) {
        return Character.toUpperCase(owner.getFirstName().charAt(0)) + "."
            + Character.toUpperCase(owner.getLastName().charAt(0)) + ".";
    }

    /**
     * A stable identifier for the owner's household, derived deterministically from the
     * normalized last name and the postcode. Every owner sharing the same last name
     * (trimmed, lower-cased, whitespace collapsed) and postcode therefore receives the
     * same identifier: the first twelve upper-case hex characters of the SHA-256 of
     * '&lt;V2&gt;|&lt;normalizedLastName&gt;|&lt;postcode&gt;', where &lt;V2&gt; is the fixed
     * version-2 {@link #VERSION_TAG tag} mixed in so no value produced under version 1 is
     * produced again. A {@code null} postcode contributes the empty string.
     */
    default String householdId(Owner owner) {
        String postcode = owner.getPostcode() == null ? "" : owner.getPostcode();
        String key = VERSION_TAG + "|" + normalizeHouseholdKey(owner.getLastName()) + "|" + postcode;
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * The owner's derived identity key: the single value onto which all duplicate
     * detection is consolidated. It is the full lower-case hex SHA-256 digest of
     * '&lt;V2&gt;|&lt;normalizedTelephone&gt;|&lt;lowerEmail&gt;|&lt;soundex(lastName)&gt;',
     * where &lt;V2&gt; is the fixed version-2 {@link #VERSION_TAG tag} mixed in so no value
     * produced under version 1 is produced again. The telephone and email are the owner's
     * stored (already normalized) values; a {@code null} email contributes the empty string,
     * and the last name is reduced to its Soundex code so surnames that sound alike share the
     * same segment. Two owners are duplicates only when their whole identity keys are equal.
     */
    default String identityKey(Owner owner) {
        String telephone = owner.getTelephone() == null ? "" : owner.getTelephone();
        String email = owner.getEmail() == null ? "" : owner.getEmail();
        String key = VERSION_TAG + "|" + telephone + "|" + email + "|" + Soundex.of(owner.getLastName());
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String normalizeHouseholdKey(String value) {
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
