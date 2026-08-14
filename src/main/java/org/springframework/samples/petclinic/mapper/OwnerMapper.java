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
    @Mapping(target = "salutation", expression = "java(salutation(owner))")
    @Mapping(target = "initials", expression = "java(initials(owner))")
    @Mapping(target = "fiscalYear", expression = "java(fiscalYear(owner))")
    @Mapping(target = "membershipPoints", expression = "java(membershipPoints(owner))")
    @Mapping(target = "membershipLevel", expression = "java(membershipLevel(owner))")
    @Mapping(target = "ownerSegment", expression = "java(ownerSegment(owner))")
    @Mapping(target = "locality", expression = "java(locality(owner))")
    @Mapping(target = "timezone", expression = "java(timezone(owner))")
    @Mapping(target = "contactPreference", expression = "java(contactPreference(owner))")
    @Mapping(target = "telephoneDisplay", expression = "java(telephoneDisplay(owner))")
    @Mapping(target = "apiVersion", expression = "java(apiVersion())")
    @Mapping(target = "identity", expression = "java(identity(owner))")
    @Mapping(target = "ageBand", expression = "java(ageBand(owner))")
    @Mapping(target = "riskFlag", expression = "java(riskFlag(owner))")
    @Mapping(target = "selfLink", expression = "java(selfLink(owner))")
    OwnerDto toOwnerDto(Owner owner);

    /**
     * Second-level labels of the known disposable email providers (the label immediately before the
     * top-level domain of each entry in the create endpoint's disposable-domain blocklist:
     * {@code mailinator.com}, {@code tempmail.com}, {@code guerrillamail.com}). An email whose domain
     * shares one of these labels is considered <em>disposable-adjacent</em> (see
     * {@link #isDisposableAdjacentEmail(String)}).
     */
    java.util.Set<String> DISPOSABLE_EMAIL_LABELS =
        java.util.Set.of("mailinator", "tempmail", "guerrillamail");

    /**
     * Whether the owner is flagged for review. Returns {@code true} when any of the following hold:
     * the owner is a possible duplicate ({@link Owner#getPossibleDuplicate()} is {@code true}), the
     * owner's email domain is disposable-adjacent (see {@link #isDisposableAdjacentEmail(String)}), or
     * the owner's city is over its soft capacity ({@link Owner#getCapacityWarning()} is {@code true},
     * i.e. the city had already reached the capacity-warning threshold when the owner was created).
     * Otherwise returns {@code false}.
     */
    default Boolean riskFlag(Owner owner) {
        boolean possibleDuplicate = Boolean.TRUE.equals(owner.getPossibleDuplicate());
        boolean overSoftCapacity = Boolean.TRUE.equals(owner.getCapacityWarning());
        boolean disposableAdjacent = isDisposableAdjacentEmail(owner.getEmail());
        return possibleDuplicate || disposableAdjacent || overSoftCapacity;
    }

    /**
     * Whether an email address is <em>disposable-adjacent</em>: its domain's second-level label (the
     * label immediately before the top-level domain) matches one of the known disposable providers'
     * labels ({@link #DISPOSABLE_EMAIL_LABELS}). This catches domains related to a disposable provider
     * without being an exact blocklist entry, such as the same provider under a different top-level
     * domain ({@code mailinator.net}) or a subdomain of it ({@code inbox.mailinator.com}). A
     * {@code null}, blank, or structurally invalid domain (fewer than two dot-separated labels) is not
     * disposable-adjacent.
     */
    default boolean isDisposableAdjacentEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        int at = email.lastIndexOf('@');
        if (at < 0 || at == email.length() - 1) {
            return false;
        }
        String domain = email.substring(at + 1).toLowerCase(java.util.Locale.ROOT);
        String[] labels = domain.split("\\.");
        if (labels.length < 2) {
            return false;
        }
        String secondLevel = labels[labels.length - 2];
        return DISPOSABLE_EMAIL_LABELS.contains(secondLevel);
    }

    /**
     * Builds the owner's self link, formatted {@code '/api/owners/<id>'} where {@code id} is the
     * owner's id. Returns {@code null} when the owner has no id.
     */
    default String selfLink(Owner owner) {
        Integer id = owner.getId();
        if (id == null) {
            return null;
        }
        return "/api/owners/" + id;
    }

    /**
     * Derives the owner's age band from the birth date, computed as the owner's age on the
     * registration date: {@code MINOR} when under 18, {@code ADULT} from 18 to 64, and
     * {@code SENIOR} at 65 or older. Returns {@code null} when the birth date or registration
     * date is absent, so an owner created without a birth date carries no age band.
     */
    default org.springframework.samples.petclinic.rest.dto.OwnerDto.AgeBandEnum ageBand(Owner owner) {
        java.time.LocalDate birthDate = owner.getBirthDate();
        java.time.LocalDate registrationDate = owner.getRegistrationDate();
        if (birthDate == null || registrationDate == null) {
            return null;
        }
        int age = java.time.Period.between(birthDate, registrationDate).getYears();
        if (age < 18) {
            return org.springframework.samples.petclinic.rest.dto.OwnerDto.AgeBandEnum.MINOR;
        }
        if (age < 65) {
            return org.springframework.samples.petclinic.rest.dto.OwnerDto.AgeBandEnum.ADULT;
        }
        return org.springframework.samples.petclinic.rest.dto.OwnerDto.AgeBandEnum.SENIOR;
    }

    /**
     * The fixed version tag mixed into the region code used inside the version-2 identifiers, so every
     * identifier changes and no value produced under version 1 is produced again. It is only ever mixed
     * into the identifiers' derivation; it never appears in the user-facing {@code locality},
     * {@code timezone} or owner segment (see {@link #regionCodeV2(Owner)}).
     */
    String IDENTITY_VERSION_TAG = "V2";

    /**
     * The API version of the owner identity contract carried by every owner response: {@code 2}.
     */
    default Integer apiVersion() {
        return 2;
    }

    /**
     * Derives the owner's plain region code from the postcode first and then the city, mirroring the
     * region used to build the member id: the region whose {@link #REGION_POSTCODES} range contains the
     * (4-digit) postcode, else the fixed {@link #CITY_REGION} city table, else {@code "UNKNOWN"}. This is
     * the plain, un-tagged region (e.g. {@code "NSW"}); the user-facing {@code locality} equals it.
     */
    default String regionCode(Owner owner) {
        String postcode = owner.getPostcode();
        if (postcode != null) {
            try {
                int value = Integer.parseInt(postcode.trim());
                for (java.util.Map.Entry<String, int[]> entry : REGION_POSTCODES.entrySet()) {
                    int[] range = entry.getValue();
                    if (value >= range[0] && value <= range[1]) {
                        return entry.getKey();
                    }
                }
            }
            catch (NumberFormatException ex) {
                // Not a numeric postcode; fall back to the city table below.
            }
        }
        return CITY_REGION.getOrDefault(owner.getCity(), "UNKNOWN");
    }

    /**
     * Derives the version-2 region code used inside the identifiers: the plain {@link #regionCode(Owner)
     * region} with the fixed {@link #IDENTITY_VERSION_TAG 'V2'} version tag prefixed (e.g. {@code "V2NSW"}).
     * This tagged value is mixed into the member id, household id and identity key so every identifier
     * differs from its version-1 form, while the plain region alone continues to feed {@code locality},
     * {@code timezone} and the owner segment.
     */
    default String regionCodeV2(Owner owner) {
        return IDENTITY_VERSION_TAG + regionCode(owner);
    }

    /**
     * Derives the owner's version-2 duplicate-detection identity key: the lower-case SHA-256 hex digest
     * (64 hex characters) of {@code '<regionCodeV2>|<normalizedTelephone>|<lowerEmail>|<soundex(lastName)>'}.
     * The leading segment is the {@link #regionCodeV2(Owner) version-2 region code}, which mixes in the
     * {@code 'V2'} tag so the key differs from the version-1 key. The telephone and email segments are
     * empty when the respective field is absent, and the last-name segment is the {@link #soundex(String)
     * Soundex code} of the owner's last name. The telephone and email are already stored in their
     * normalized (E.164 / lower-cased) form, so the stored values are used directly. This is the single
     * key against which owner duplicates are detected: two owners collide only when their whole keys match.
     */
    default String identityKey(Owner owner) {
        String telephone = owner.getTelephone() == null ? "" : owner.getTelephone();
        String email = owner.getEmail() == null ? "" : owner.getEmail();
        String lastNameCode = OwnerIdentity.soundex(owner.getLastName());
        return OwnerIdentity.sha256Hex(regionCodeV2(owner) + "|" + telephone + "|" + email + "|" + lastNameCode);
    }

    /**
     * Groups the owner's version-2 identifiers (member id, household id and identity key) into the nested
     * {@code identity} object of the owner response. The member id and household id are read from the
     * owner as assigned at creation; the identity key is derived on read via {@link #identityKey(Owner)}.
     */
    default org.springframework.samples.petclinic.rest.dto.OwnerIdentityDto identity(Owner owner) {
        org.springframework.samples.petclinic.rest.dto.OwnerIdentityDto identity =
            new org.springframework.samples.petclinic.rest.dto.OwnerIdentityDto();
        identity.setMemberId(owner.getMemberId());
        identity.setHouseholdId(owner.getHouseholdId());
        identity.setIdentityKey(identityKey(owner));
        return identity;
    }

    /**
     * Fixed city-to-region table used to derive an owner's locality.
     */
    java.util.Map<String, String> CITY_REGION =
        java.util.Map.of("Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /**
     * Inclusive 4-digit postcode range {@code [low, high]} owned by each region: NSW 2000-2099,
     * VIC 3000-3099, QLD 4000-4099. Used to resolve an owner's region from the postcode first.
     */
    java.util.Map<String, int[]> REGION_POSTCODES =
        java.util.Map.of("NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099}, "QLD", new int[] {4000, 4099});

    /**
     * Derives the owner's locality, the leading {@code REGION} segment of the owner's member id (the
     * unified identity {@code '<REGION><FY><HASH8><CHK>'}, whose region is the run of leading letters
     * before the two-digit fiscal year). Only when no member id is present does it fall back to deriving
     * the region directly, preferring the postcode's {@link #REGION_POSTCODES} range and then the fixed
     * {@link #CITY_REGION} city table. Returns the canonical region string, or {@code "UNKNOWN"} when
     * neither source resolves a region.
     */
    default String locality(Owner owner) {
        String region = memberIdRegion(owner);
        if (region != null && !region.isEmpty()) {
            return region;
        }
        String postcode = owner.getPostcode();
        if (postcode != null) {
            try {
                int value = Integer.parseInt(postcode.trim());
                for (java.util.Map.Entry<String, int[]> entry : REGION_POSTCODES.entrySet()) {
                    int[] range = entry.getValue();
                    if (value >= range[0] && value <= range[1]) {
                        return entry.getKey();
                    }
                }
            }
            catch (NumberFormatException ex) {
                // Not a numeric postcode; fall back to the city table below.
            }
        }
        return CITY_REGION.getOrDefault(owner.getCity(), "UNKNOWN");
    }

    /**
     * Fixed region-to-timezone table used to derive an owner's timezone as an IANA name.
     */
    java.util.Map<String, String> REGION_TIMEZONE =
        java.util.Map.of("NSW", "Australia/Sydney", "VIC", "Australia/Melbourne", "QLD", "Australia/Brisbane");

    /**
     * Derives the owner's timezone as an IANA name from the owner's {@link #locality(Owner) locality}
     * (region) via the fixed {@link #REGION_TIMEZONE} table: {@code NSW -> Australia/Sydney},
     * {@code VIC -> Australia/Melbourne}, {@code QLD -> Australia/Brisbane}. Returns {@code null} when
     * the region does not resolve to a known timezone (e.g. the locality is {@code "UNKNOWN"}).
     */
    default String timezone(Owner owner) {
        return REGION_TIMEZONE.get(locality(owner));
    }

    /**
     * Known E.164 country calling codes, longest first, used to split a stored E.164 telephone into
     * its country code and national digits when formatting {@link #telephoneDisplay(Owner)}.
     */
    String[] TELEPHONE_COUNTRY_CODES = {"61", "1"};

    /**
     * Formats the owner's stored E.164 telephone for humans: the country code, a space, then the
     * national digits grouped in threes (e.g. {@code "+61412345678"} becomes {@code "+61 412 345 678"}).
     * The country code is recognised from the {@link #TELEPHONE_COUNTRY_CODES} table; when none matches,
     * all digits after the {@code '+'} are treated as the national number. Returns {@code null} when the
     * telephone is absent, and the value unchanged when it is not a {@code '+'}-prefixed E.164 number.
     */
    default String telephoneDisplay(Owner owner) {
        String telephone = owner.getTelephone();
        if (telephone == null) {
            return null;
        }
        if (!telephone.startsWith("+")) {
            return telephone;
        }
        String digits = telephone.substring(1);
        if (!digits.chars().allMatch(Character::isDigit)) {
            return telephone;
        }
        String countryCode = "";
        for (String code : TELEPHONE_COUNTRY_CODES) {
            if (digits.startsWith(code) && digits.length() > code.length()) {
                countryCode = code;
                break;
            }
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
     * Derives the owner's preferred contact channel: {@code "EMAIL"} when an email address is
     * present (non-blank), otherwise {@code "PHONE"}.
     */
    default String contactPreference(Owner owner) {
        String email = owner.getEmail();
        return email != null && !email.isBlank() ? "EMAIL" : "PHONE";
    }

    /**
     * Computes the owner's membership points. Starts at {@code 0} and adds {@code 2} when an email
     * is present, {@code 1} when the owner has no namesakes ({@code namesakeCount} is {@code 0}),
     * {@code 2} for a household of {@code 3} or more ({@code householdSize}), and {@code 3} when the
     * owner's tenure spans at least one elapsed fiscal year (the {@code registrationDate}'s fiscal
     * year is earlier than the current date's fiscal year).
     */
    default Integer membershipPoints(Owner owner) {
        Integer namesakeCount = owner.getNamesakeCount();
        String email = owner.getEmail();
        Integer householdSize = owner.getHouseholdSize();
        int points = 0;
        if (email != null && !email.isBlank()) {
            points += 2;
        }
        if (namesakeCount != null && namesakeCount == 0) {
            points += 1;
        }
        if (householdSize != null && householdSize >= 3) {
            points += 2;
        }
        java.time.LocalDate registrationDate = owner.getRegistrationDate();
        boolean hasTenure = registrationDate != null
            && fiscalYearValue(java.time.LocalDate.now()) - fiscalYearValue(registrationDate) >= 1;
        if (hasTenure) {
            points += 3;
        }
        return points;
    }

    /**
     * Derives the owner's membership level, a number from 1 to 4, from the owner's
     * {@link #membershipPoints(Owner) membership points}: level {@code 1} for {@code 0-1} points,
     * {@code 2} for {@code 2-3}, {@code 3} for {@code 4-5}, and {@code 4} for {@code 6} or more.
     *
     * <p>The derived level is then capped by the owner's {@code membershipLevelCap} when one was
     * snapshotted at creation: a new owner's membership level cannot exceed one above the highest
     * membership level among their existing household members. When no cap is present (the owner had no
     * existing household member) the derived level is returned unchanged.
     */
    default Integer membershipLevel(Owner owner) {
        int points = membershipPoints(owner);
        int level;
        if (points <= 1) {
            level = 1;
        } else if (points <= 3) {
            level = 2;
        } else if (points <= 5) {
            level = 3;
        } else {
            level = 4;
        }
        Integer cap = owner.getMembershipLevelCap();
        if (cap != null && level > cap) {
            return cap;
        }
        return level;
    }

    /**
     * Known regions considered {@code METRO} for the owner's segment: NSW, VIC and QLD.
     */
    java.util.Set<String> METRO_REGIONS = java.util.Set.of("NSW", "VIC", "QLD");

    /**
     * Derives the owner's segment, formatted {@code '<TIER>_<AREA>'}. {@code TIER} is {@code PREMIUM}
     * when the owner's {@link #membershipLevel(Owner) membership level} is {@code 3} or more, otherwise
     * {@code STANDARD}. {@code AREA} is {@code METRO} when the owner's {@link #locality(Owner) locality}
     * is a known region (NSW, VIC or QLD), otherwise {@code REGIONAL}.
     */
    default org.springframework.samples.petclinic.rest.dto.OwnerDto.OwnerSegmentEnum ownerSegment(Owner owner) {
        Integer level = membershipLevel(owner);
        String tier = level != null && level >= 3 ? "PREMIUM" : "STANDARD";
        String area = METRO_REGIONS.contains(locality(owner)) ? "METRO" : "REGIONAL";
        return org.springframework.samples.petclinic.rest.dto.OwnerDto.OwnerSegmentEnum
            .fromValue(tier + "_" + area);
    }

    /**
     * Derives the owner's fiscal year, formatted {@code 'FY<YY>'} where {@code YY} is the two-digit
     * fiscal year. The value is read from the {@code FY} segment of the owner's member id (the two
     * digits following the leading region of {@code '<REGION><FY><HASH8><CHK>'}), so it references the
     * same fiscal year embedded in the member id. Only when no member id is present does it fall back to
     * computing the fiscal year from the business-day-adjusted registration date. Returns {@code null}
     * when neither a member id nor a registration date is available.
     */
    default String fiscalYear(Owner owner) {
        String fy = memberIdFiscalYear(owner);
        if (fy != null) {
            return "FY" + fy;
        }
        java.time.LocalDate registrationDate = owner.getRegistrationDate();
        if (registrationDate == null) {
            return null;
        }
        return String.format("FY%02d", fiscalYearValue(registrationDate) % 100);
    }

    /**
     * Extracts the leading {@code REGION} segment from the owner's member id: the run of leading letters
     * before the two-digit fiscal year in {@code '<REGION><FY><HASH8><CHK>'}. Returns {@code null} when
     * the owner has no member id.
     */
    default String memberIdRegion(Owner owner) {
        String memberId = owner.getMemberId();
        if (memberId == null) {
            return null;
        }
        int i = 0;
        while (i < memberId.length() && Character.isLetter(memberId.charAt(i))) {
            i++;
        }
        return memberId.substring(0, i);
    }

    /**
     * Extracts the two-digit {@code FY} segment from the owner's member id: the two digits immediately
     * following the leading region in {@code '<REGION><FY><HASH8><CHK>'}. Returns {@code null} when the
     * owner has no member id or the segment is not present.
     */
    default String memberIdFiscalYear(Owner owner) {
        String region = memberIdRegion(owner);
        if (region == null) {
            return null;
        }
        String memberId = owner.getMemberId();
        if (memberId.length() < region.length() + 2) {
            return null;
        }
        String fy = memberId.substring(region.length(), region.length() + 2);
        if (fy.chars().allMatch(Character::isDigit)) {
            return fy;
        }
        return null;
    }

    /**
     * Computes the fiscal year, as a full four-digit year, of the given date. The fiscal year starts
     * on 1 July and is named by the calendar year in which it ends, so a date on or after 1 July
     * belongs to the following calendar year's fiscal year.
     */
    default int fiscalYearValue(java.time.LocalDate date) {
        return date.getMonthValue() >= java.time.Month.JULY.getValue() ? date.getYear() + 1 : date.getYear();
    }

    /**
     * Builds the owner's salutation: the supplied title and last name joined by a single space
     * (e.g. {@code "DR Franklin"}) when a title is present (non-blank), otherwise just the last
     * name. Returns {@code null} when the owner has no last name.
     */
    default String salutation(Owner owner) {
        String lastName = owner.getLastName();
        String title = owner.getTitle();
        if (title == null || title.isBlank()) {
            return lastName;
        }
        return title + " " + lastName;
    }

    /**
     * Builds the owner's initials as the upper-cased first letters of the first and last
     * names, dot-separated with a trailing dot (e.g. {@code "J.S."}).
     */
    default String initials(Owner owner) {
        String first = owner.getFirstName();
        String last = owner.getLastName();
        StringBuilder sb = new StringBuilder();
        if (first != null && !first.isEmpty()) {
            sb.append(Character.toUpperCase(first.charAt(0))).append('.');
        }
        if (last != null && !last.isEmpty()) {
            sb.append(Character.toUpperCase(last.charAt(0))).append('.');
        }
        return sb.toString();
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
