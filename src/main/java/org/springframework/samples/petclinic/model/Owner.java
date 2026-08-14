/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.model;

import org.springframework.core.style.ToStringCreator;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.*;

/**
 * Simple JavaBean domain object representing an owner.
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Michael Isvy
 */
@Entity
@Table(name = "owners")
public class Owner extends Person {
    @Column(name = "title")
    private String title;

    @Column(name = "address")
    @NotEmpty
    private String address;

    @Column(name = "address_line1")
    private String addressLine1;

    @Column(name = "address_line2")
    private String addressLine2;

    @Column(name = "city")
    @NotEmpty
    private String city;

    @Column(name = "telephone")
    @NotEmpty
    @Pattern(regexp = "^\\+[0-9]{8,15}$", message = "Phone number must be in E.164 form")
    private String telephone;

    @Column(name = "email")
    private String email;

    @Column(name = "postcode")
    private String postcode;

    @Column(name = "registration_date")
    private LocalDate registrationDate;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "member_id")
    private String memberId;

    @Column(name = "household_id")
    private String householdId;

    @Column(name = "namesake_count")
    private Integer namesakeCount;

    @Column(name = "bulk_signup_warning")
    private Boolean bulkSignupWarning;

    @Column(name = "capacity_warning")
    private Boolean capacityWarning;

    @Column(name = "household_size")
    private Integer householdSize;

    @Column(name = "membership_level_cap")
    private Integer membershipLevelCap;

    @Column(name = "possible_duplicate")
    private Boolean possibleDuplicate;

    @Column(name = "possible_duplicate_of")
    private Integer possibleDuplicateOf;

    @Column(name = "deleted")
    private Boolean deleted;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner", fetch = FetchType.EAGER)
    private Set<Pet> pets;

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * The owner's salutation: the {@link #title} followed by a single space and the last name
     * (e.g. {@code "DR Franklin"}), or just the last name when no title has been supplied.
     *
     * @return the formatted salutation
     */
    public String getSalutation() {
        if (this.title == null || this.title.isBlank()) {
            return this.getLastName();
        }
        return this.title + " " + this.getLastName();
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddressLine1() {
        return this.addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return this.addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getCity() {
        return this.city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getTelephone() {
        return this.telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    /**
     * The stored E.164 {@link #telephone} formatted for humans as the country code, a single
     * space, then the national digits (the digits after the country code) grouped in threes from
     * the left, e.g. {@code "+61412345678"} becomes {@code "+61 412 345 678"}. The country code is
     * a single digit for the {@code +1} and {@code +7} zones and two digits otherwise.
     *
     * @return the human-formatted telephone, or the raw {@link #telephone} unchanged when it is
     *         {@code null} or not a {@code +} followed by digits
     */
    public String getTelephoneDisplay() {
        if (this.telephone == null || !this.telephone.matches("\\+[0-9]+")) {
            return this.telephone;
        }
        String digits = this.telephone.substring(1);
        int countryCodeLength = ONE_DIGIT_CALLING_CODES.contains(digits.substring(0, 1)) ? 1 : 2;
        if (digits.length() <= countryCodeLength) {
            return this.telephone;
        }
        String countryCode = digits.substring(0, countryCodeLength);
        String national = digits.substring(countryCodeLength);
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
     * The E.164 calling codes that are a single digit ({@code +1} for the North American Numbering
     * Plan and {@code +7} for the Russia/Kazakhstan zone); every other calling code is two digits.
     */
    private static final Set<String> ONE_DIGIT_CALLING_CODES = Set.of("1", "7");

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPostcode() {
        return this.postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public LocalDate getRegistrationDate() {
        return this.registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }

    public LocalDate getBirthDate() {
        return this.birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    /**
     * The owner's age band, derived from the {@link #birthDate} measured in completed years
     * against the {@link #registrationDate}: {@code "MINOR"} when under 18, {@code "ADULT"}
     * from 18 to 64, and {@code "SENIOR"} at 65 and over.
     *
     * @return the age band, or {@code null} when either the birth date or the registration
     *         date has not been assigned
     */
    public String getAgeBand() {
        if (this.birthDate == null || this.registrationDate == null) {
            return null;
        }
        int age = java.time.Period.between(this.birthDate, this.registrationDate).getYears();
        if (age < 18) {
            return "MINOR";
        }
        if (age < 65) {
            return "ADULT";
        }
        return "SENIOR";
    }

    /**
     * The owner's canonical API path, formed as {@code "/api/owners/"} followed by the owner's
     * {@link #getId() id}.
     *
     * @return the self link, or {@code null} when the owner has not yet been assigned an id
     */
    public String getSelfLink() {
        if (this.getId() == null) {
            return null;
        }
        return "/api/owners/" + this.getId();
    }

    /**
     * The owner's member id, the single unified identifier formatted
     * {@code '<REGION><FY><HASH8><CHK>'}: the region code, the 2-digit fiscal year, the 8-character
     * upper-hex HASH8 of the region-and-hash identity, and a single Luhn check digit computed over
     * the digits of {@code <REGION><FY><HASH8>}. It is assigned on creation (with collision handling)
     * and unifies the former customer code, membership number and check digit.
     *
     * @return the member id, or {@code null} if it has not been assigned
     */
    public String getMemberId() {
        return this.memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getHouseholdId() {
        return this.householdId;
    }

    public void setHouseholdId(String householdId) {
        this.householdId = householdId;
    }

    public Integer getNamesakeCount() {
        return this.namesakeCount;
    }

    public void setNamesakeCount(Integer namesakeCount) {
        this.namesakeCount = namesakeCount;
    }

    /**
     * Whether more than 80 owners had already been created today at the time this owner
     * was created, indicating an unusually high signup volume.
     *
     * @return {@code true} when the bulk-signup threshold was exceeded, otherwise
     *         {@code false} (never {@code null})
     */
    public Boolean getBulkSignupWarning() {
        return this.bulkSignupWarning != null && this.bulkSignupWarning;
    }

    public void setBulkSignupWarning(Boolean bulkSignupWarning) {
        this.bulkSignupWarning = bulkSignupWarning;
    }

    /**
     * Whether the owner's city was approaching its capacity limit when this owner
     * was created: the city already held between 40 and 49 owners (inclusive), just
     * short of the hard limit of 50.
     *
     * @return {@code true} when the city was approaching capacity, otherwise
     *         {@code false} (never {@code null})
     */
    public Boolean getCapacityWarning() {
        return this.capacityWarning != null && this.capacityWarning;
    }

    public void setCapacityWarning(Boolean capacityWarning) {
        this.capacityWarning = capacityWarning;
    }

    /**
     * The number of owners in this owner's household (owners sharing the same
     * {@code householdId}) as of when this owner was created, used to derive the GOLD
     * membership tier.
     *
     * @return the household member count, or {@code null} if it has not been assigned
     */
    public Integer getHouseholdSize() {
        return this.householdSize;
    }

    public void setHouseholdSize(Integer householdSize) {
        this.householdSize = householdSize;
    }

    /**
     * The upper bound applied to this owner's {@link #getMembershipLevel() membership level}: one
     * above the highest membership level held by their household members at the time this owner was
     * created. It is assigned only to an owner admitted into an already-populated household, and is
     * {@code null} (no cap) for a household founder or a declared household member, so their level is
     * not restricted.
     *
     * @return the membership-level cap, or {@code null} when no cap applies
     */
    public Integer getMembershipLevelCap() {
        return this.membershipLevelCap;
    }

    public void setMembershipLevelCap(Integer membershipLevelCap) {
        this.membershipLevelCap = membershipLevelCap;
    }

    /**
     * Whether this owner, though not a hard duplicate, shares an existing owner's last name and
     * postcode while carrying a different telephone, flagging a likely duplicate registration.
     *
     * @return {@code true} when a soft-duplicate match was found on creation, otherwise
     *         {@code false} (never {@code null})
     */
    public Boolean getPossibleDuplicate() {
        return this.possibleDuplicate != null && this.possibleDuplicate;
    }

    public void setPossibleDuplicate(Boolean possibleDuplicate) {
        this.possibleDuplicate = possibleDuplicate;
    }

    /**
     * The id of the existing owner this owner may duplicate (same last name and postcode,
     * different telephone), or {@code null} when this owner is not a possible duplicate.
     *
     * @return the matching owner id, or {@code null}
     */
    public Integer getPossibleDuplicateOf() {
        return this.possibleDuplicateOf;
    }

    public void setPossibleDuplicateOf(Integer possibleDuplicateOf) {
        this.possibleDuplicateOf = possibleDuplicateOf;
    }

    /**
     * The base labels (the label before the first dot) of the known disposable email domains. An
     * owner's email domain is judged disposable-adjacent when its own base label is one of these,
     * so a near-variant of a blocklisted disposable domain under a different top-level domain
     * (e.g. {@code mailinator.net} for {@code mailinator.com}) still trips the risk flag.
     */
    private static final Set<String> DISPOSABLE_EMAIL_BASE_LABELS =
        Set.of("mailinator", "tempmail", "guerrillamail");

    /**
     * Whether this owner's email domain is disposable-adjacent: the domain's base label (the label
     * before its first dot, e.g. {@code "mailinator"} in {@code "mailinator.net"}) matches the base
     * label of a known disposable domain. An owner with no email carries no disposable-adjacent
     * domain.
     *
     * @return {@code true} when the email domain is disposable-adjacent, otherwise {@code false}
     */
    private boolean hasDisposableAdjacentEmail() {
        if (this.email == null) {
            return false;
        }
        int at = this.email.lastIndexOf('@');
        if (at < 0) {
            return false;
        }
        String domain = this.email.substring(at + 1).toLowerCase(Locale.ROOT);
        int dot = domain.indexOf('.');
        String baseLabel = dot < 0 ? domain : domain.substring(0, dot);
        return DISPOSABLE_EMAIL_BASE_LABELS.contains(baseLabel);
    }

    /**
     * Whether this owner warrants a manual risk review. It is {@code true} when any of three signals
     * holds: the owner is a {@link #getPossibleDuplicate() possible duplicate}, the owner's email
     * domain is {@link #hasDisposableAdjacentEmail() disposable-adjacent}, or the owner's city is
     * over its soft capacity - the same 40-owner threshold that raises the
     * {@link #getCapacityWarning() capacity warning}; otherwise {@code false}.
     *
     * @return {@code true} when any risk signal holds, otherwise {@code false} (never {@code null})
     */
    public Boolean getRiskFlag() {
        return getPossibleDuplicate() || hasDisposableAdjacentEmail() || getCapacityWarning();
    }

    /**
     * Whether this owner has been soft-deleted. A newly created owner is not deleted; deleting an
     * owner via {@code DELETE /api/owners/{id}} flags it {@code true} while retaining the record.
     * A deleted owner is ignored by the create endpoint's duplicate and identity checks.
     *
     * @return {@code true} when the owner has been soft-deleted, otherwise {@code false} (never
     *         {@code null})
     */
    public Boolean getDeleted() {
        return this.deleted != null && this.deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    /**
     * The owner's name formatted as {@code "LastName, FirstName"} from the stored names.
     *
     * @return the formatted display name
     */
    public String getDisplayName() {
        return this.getLastName() + ", " + this.getFirstName();
    }

    /**
     * The owner's initials: the upper-cased first letters of the first and last
     * names, dot-separated with a trailing dot (e.g. {@code "J.S."}).
     *
     * @return the formatted initials
     */
    public String getInitials() {
        return Character.toUpperCase(this.getFirstName().charAt(0)) + "."
            + Character.toUpperCase(this.getLastName().charAt(0)) + ".";
    }

    /**
     * The fiscal year of the (business-day-adjusted) {@link #registrationDate}, formatted as
     * {@code "FY<YY>"}. The fiscal year starts on 1 July: a registration date on or after 1 July
     * belongs to that calendar year's fiscal year, an earlier date belongs to the previous year's,
     * and {@code YY} is the last two digits of the year the fiscal year started (e.g. a date in
     * August 2026 yields {@code "FY26"}, a date in March 2026 yields {@code "FY25"}).
     *
     * @return the formatted fiscal year, or {@code null} if the registration date has not been
     *         assigned
     */
    public String getFiscalYear() {
        if (this.registrationDate == null) {
            return null;
        }
        return String.format("FY%02d", fiscalYearOf(this.registrationDate) % 100);
    }

    /**
     * The starting calendar year of the fiscal year (which begins on 1 July) that the given date
     * falls in: the date's own year when it is on or after 1 July, otherwise the previous year.
     *
     * @param date the date whose fiscal year is derived (must not be {@code null})
     * @return the starting calendar year of the fiscal year
     */
    private static int fiscalYearOf(LocalDate date) {
        return date.getMonthValue() >= java.time.Month.JULY.getValue()
            ? date.getYear() : date.getYear() - 1;
    }

    /**
     * The owner's membership points, starting at {@code 0}. The owner gains {@code 2} when
     * they carry an email address, {@code 1} when they have no namesakes
     * ({@code namesakeCount} is {@code 0}), {@code 2} for a household of {@code 3} or more
     * members ({@code householdSize}), and {@code 3} for tenure of at least one elapsed
     * fiscal year (the {@code registrationDate}'s fiscal year precedes the current fiscal
     * year, the fiscal year starting on 1 July). Because a newly created owner is still in
     * the current fiscal year, a new owner can score at most {@code 5} points.
     *
     * @return the membership points
     */
    public Integer getMembershipPoints() {
        int points = 0;
        boolean hasEmail = this.email != null && !this.email.isBlank();
        if (hasEmail) {
            points += 2;
        }
        boolean noNamesakes = this.namesakeCount != null && this.namesakeCount == 0;
        if (noNamesakes) {
            points += 1;
        }
        if (this.householdSize != null && this.householdSize >= 3) {
            points += 2;
        }
        if (this.registrationDate != null
            && fiscalYearOf(LocalDate.now()) - fiscalYearOf(this.registrationDate) >= 1) {
            points += 3;
        }
        return points;
    }

    /**
     * The owner's numeric membership level, from {@code 1} to {@code 4}, derived from the
     * {@link #getMembershipPoints() membership points}: {@code 1} for {@code 0-1} points,
     * {@code 2} for {@code 2-3} points, {@code 3} for {@code 4-5} points, and {@code 4} for
     * {@code 6} or more points. Because a newly created owner has zero tenure, a new owner
     * scores at most {@code 5} points and so never exceeds level {@code 3}; level {@code 4}
     * is reserved for owners whose tenure spans at least one elapsed fiscal year.
     *
     * <p>When a {@link #getMembershipLevelCap() membership-level cap} has been assigned the
     * points-derived level is capped to it, so an owner admitted into an existing household can never
     * exceed one level above their household's highest member.
     *
     * @return the membership level
     */
    public Integer getMembershipLevel() {
        int level = pointsMembershipLevel();
        if (this.membershipLevelCap != null && level > this.membershipLevelCap) {
            return this.membershipLevelCap;
        }
        return level;
    }

    /**
     * The membership level derived purely from the {@link #getMembershipPoints() membership points},
     * before any {@link #getMembershipLevelCap() household cap} is applied.
     *
     * @return the uncapped, points-derived membership level
     */
    private int pointsMembershipLevel() {
        int points = getMembershipPoints();
        if (points >= 6) {
            return 4;
        }
        if (points >= 4) {
            return 3;
        }
        if (points >= 2) {
            return 2;
        }
        return 1;
    }

    /**
     * The owner's preferred contact channel: {@code "EMAIL"} when the owner carries an
     * email address, otherwise {@code "PHONE"}.
     *
     * @return {@code "EMAIL"} when an email is present, otherwise {@code "PHONE"}
     */
    public String getContactPreference() {
        boolean hasEmail = this.email != null && !this.email.isBlank();
        return hasEmail ? "EMAIL" : "PHONE";
    }

    /**
     * The owner's segment, formatted {@code "<TIER>_<AREA>"}, one of {@code "PREMIUM_METRO"},
     * {@code "PREMIUM_REGIONAL"}, {@code "STANDARD_METRO"} or {@code "STANDARD_REGIONAL"}. The tier
     * is {@code "PREMIUM"} when the {@link #getMembershipLevel() membership level} is {@code 3} or
     * more, otherwise {@code "STANDARD"}. The area is {@code "METRO"} when the
     * {@link #getLocality() locality} is a known region ({@code NSW}, {@code VIC} or {@code QLD}),
     * otherwise {@code "REGIONAL"}.
     *
     * @return the formatted owner segment
     */
    public String getOwnerSegment() {
        String tier = getMembershipLevel() >= 3 ? "PREMIUM" : "STANDARD";
        String area = REGION_TIMEZONE.containsKey(getLocality()) ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }

    /**
     * The owner's identity key: the single derived value used for duplicate detection. It is the
     * lower-case, 64-character hex SHA-256 digest over
     * {@code normalizedTelephone + "|" + lowerEmail + "|" + soundex(lastName)}, where the telephone
     * is the already-normalized stored value, the email is lower-cased (a {@code null} email is
     * rendered as the empty string) and the last name is reduced to its Soundex code. Two owners are
     * duplicates only when their whole identity keys are equal; because the telephone is part of the
     * key, two owners sharing a last name (Soundex) but with different telephones have different
     * identity keys and so are not hard duplicates.
     *
     * @return the derived identity key
     */
    public String getIdentityKey() {
        String telephonePart = this.telephone == null ? "" : this.telephone;
        String emailPart = this.email == null ? "" : this.email.toLowerCase(Locale.ROOT);
        String soundexPart = soundex(getLastName());
        return sha256Hex(telephonePart + "|" + emailPart + "|" + soundexPart);
    }

    /**
     * Returns the lower-case, 64-character hex SHA-256 digest of the UTF-8 bytes of {@code input}.
     *
     * @param input the value to hash
     * @return the full hex digest
     */
    private static String sha256Hex(String input) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * Computes the American Soundex code of a name: the first letter followed by three digits that
     * phonetically encode the remaining consonants. Non-letters are ignored, {@code h} and {@code w}
     * are transparent (letters either side of them are treated as adjacent) and vowels reset the
     * run so a repeated code across a vowel is encoded twice. A {@code null} or letter-free value
     * yields the empty string.
     *
     * @param value the name to encode
     * @return the Soundex code, or the empty string when there is no letter to encode
     */
    public static String soundex(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder letters = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = Character.toUpperCase(value.charAt(i));
            if (c >= 'A' && c <= 'Z') {
                letters.append(c);
            }
        }
        if (letters.length() == 0) {
            return "";
        }
        StringBuilder code = new StringBuilder();
        char first = letters.charAt(0);
        code.append(first);
        char last = soundexDigit(first);
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            if (c == 'H' || c == 'W') {
                // Transparent: leave the previous code in place so letters either side stay adjacent.
                continue;
            }
            char digit = soundexDigit(c);
            if (digit != '0') {
                if (digit != last) {
                    code.append(digit);
                }
                last = digit;
            }
            else {
                // A vowel breaks the run so a following letter with the same code is encoded again.
                last = '0';
            }
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    /**
     * Maps a single upper-case letter to its Soundex digit, or {@code '0'} for vowels and the
     * uncoded letters {@code h} and {@code w}.
     */
    private static char soundexDigit(char c) {
        switch (c) {
            case 'B': case 'F': case 'P': case 'V':
                return '1';
            case 'C': case 'G': case 'J': case 'K': case 'Q': case 'S': case 'X': case 'Z':
                return '2';
            case 'D': case 'T':
                return '3';
            case 'L':
                return '4';
            case 'M': case 'N':
                return '5';
            case 'R':
                return '6';
            default:
                return '0';
        }
    }

    /**
     * City -> canonical region, the fixed ground truth for deriving locality.
     */
    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /**
     * Region -> inclusive 4-digit postcode range {@code {low, high}}, the fixed ground truth for
     * deriving locality from the postcode ahead of the city.
     */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
        "NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099}, "QLD", new int[] {4000, 4099});

    /**
     * The owner's locality: the canonical region resolved by the postcode range first
     * ({@code NSW 2000-2099}, {@code VIC 3000-3099}, {@code QLD 4000-4099}), falling back to the
     * fixed city-to-region table ({@code Sydney->NSW}, {@code Melbourne->VIC},
     * {@code Brisbane->QLD}) when the postcode is absent or in no known range, or {@code "UNKNOWN"}
     * when the city is not in the table either. The postcode takes precedence so cities that share a
     * name are disambiguated by their postcode's region.
     *
     * @return the canonical region string, or {@code "UNKNOWN"}
     */
    public String getLocality() {
        String byPostcode = regionForPostcode(this.postcode);
        if (byPostcode != null) {
            return byPostcode;
        }
        return CITY_REGION.getOrDefault(this.city, "UNKNOWN");
    }

    /**
     * Region -> IANA timezone name, the fixed ground truth for deriving the owner's timezone from
     * its locality.
     */
    private static final Map<String, String> REGION_TIMEZONE = Map.of(
        "NSW", "Australia/Sydney", "VIC", "Australia/Melbourne", "QLD", "Australia/Brisbane");

    /**
     * The owner's timezone: the IANA name resolved from the {@link #getLocality() locality} via the
     * fixed region-to-timezone table ({@code NSW->Australia/Sydney}, {@code VIC->Australia/Melbourne},
     * {@code QLD->Australia/Brisbane}), or {@code null} when the locality is not one of these regions
     * (e.g. {@code "UNKNOWN"}).
     *
     * @return the IANA timezone name, or {@code null}
     */
    public String getTimezone() {
        return REGION_TIMEZONE.get(getLocality());
    }

    /**
     * Region whose postcode range contains the given postcode, or {@code null} when the postcode is
     * absent, not a 4-digit number, or in no known range.
     *
     * @param postcode the owner's postcode (may be {@code null})
     * @return the matching region, or {@code null}
     */
    private static String regionForPostcode(String postcode) {
        if (postcode == null || !postcode.matches("\\d{4}")) {
            return null;
        }
        int value = Integer.parseInt(postcode);
        for (Map.Entry<String, int[]> entry : REGION_POSTCODES.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return null;
    }

    protected Set<Pet> getPetsInternal() {
        if (this.pets == null) {
            this.pets = new HashSet<>();
        }
        return this.pets;
    }

    protected void setPetsInternal(Set<Pet> pets) {
        this.pets = pets;
    }

    public List<Pet> getPets() {
        List<Pet> sortedPets = new ArrayList<>(getPetsInternal());
        sortedPets.sort(Comparator.comparing(Pet::getName, String.CASE_INSENSITIVE_ORDER));
        return Collections.unmodifiableList(sortedPets);
    }

    public void setPets(List<Pet> pets) {
        this.pets = new HashSet<>(pets);
    }

    public void addPet(Pet pet) {
        getPetsInternal().add(pet);
        pet.setOwner(this);
    }

    /**
     * Return the Pet with the given name, or null if none found for this Owner.
     *
     * @param name to test
     * @return true if pet name is already in use
     */
    public Pet getPet(String name) {
        return getPet(name, false);
    }

    /**
     * Return the Pet with the given name, or null if none found for this Owner.
     *
     * @param name to test
     * @return true if pet name is already in use
     */
    public Pet getPet(String name, boolean ignoreNew) {
        name = name.toLowerCase();
        for (Pet pet : getPetsInternal()) {
            if (!ignoreNew || !pet.isNew()) {
                String compName = pet.getName();
                compName = compName.toLowerCase();
                if (compName.equals(name)) {
                    return pet;
                }
            }
        }
        return null;
    }

    public Pet getPet(Integer petId) {
        return getPetsInternal().stream().filter(p -> p.getId().equals(petId)).findFirst().orElse(null);
    }

    @Override
    public String toString() {
        return new ToStringCreator(this)

            .append("id", this.getId())
            .append("new", this.isNew())
            .append("lastName", this.getLastName())
            .append("firstName", this.getFirstName())
            .append("address", this.address)
            .append("city", this.city)
            .append("telephone", this.telephone)
            .append("email", this.email)
            .toString();
    }
}
