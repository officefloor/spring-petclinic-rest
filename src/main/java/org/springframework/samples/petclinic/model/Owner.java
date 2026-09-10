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
    @Pattern(regexp = "^\\+[0-9]{8,15}$", message = "Phone number must be in E.164 form: '+' followed by 8 to 15 digits")
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

    @Column(name = "possible_duplicate")
    private Boolean possibleDuplicate;

    @Column(name = "possible_duplicate_of")
    private Integer possibleDuplicateOf;

    @Column(name = "membership_level_cap")
    private Integer membershipLevelCap;

    @Column(name = "deleted")
    private Boolean deleted = Boolean.FALSE;

    @Transient
    private Integer householdMemberCount;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner", fetch = FetchType.EAGER)
    private Set<Pet> pets;

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns this owner's salutation: the honorific {@code title}, a single space and the
     * {@code lastName} (for example {@code 'DR Who'}), or just the {@code lastName} when the owner
     * has no title (null or blank). This is the single definition of an owner's salutation.
     *
     * @return the composed salutation
     */
    @Transient
    public String getSalutation() {
        if (this.title == null || this.title.isBlank()) {
            return getLastName();
        }
        return this.title + " " + getLastName();
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
     * Returns the stored E.164 {@code telephone} formatted for humans: the recognized country code
     * (with its leading {@code '+'}), a space, then the national digits grouped in threes from the
     * left and separated by spaces (for example {@code '+61412345678'} renders as
     * {@code '+61 412 345 678'}). The split into country code and national number reuses
     * {@link #countryCodeOf(String)}, the single definition of that split. Returns {@code null} when
     * the owner has no telephone, and returns the raw E.164 value unchanged when it carries no
     * recognized country code. This is the single definition of an owner's human-readable telephone.
     *
     * @return the human-formatted telephone, or {@code null} when the owner has no telephone
     */
    @Transient
    public String getTelephoneDisplay() {
        if (this.telephone == null) {
            return null;
        }
        String countryCode = countryCodeOf(this.telephone);
        if (countryCode == null) {
            return this.telephone;
        }
        String national = this.telephone.substring(1 + countryCode.length());
        StringBuilder display = new StringBuilder("+").append(countryCode);
        for (int i = 0; i < national.length(); i += 3) {
            display.append(' ').append(national, i, Math.min(i + 3, national.length()));
        }
        return display.toString();
    }

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

    public Boolean getPossibleDuplicate() {
        return this.possibleDuplicate;
    }

    public void setPossibleDuplicate(Boolean possibleDuplicate) {
        this.possibleDuplicate = possibleDuplicate;
    }

    public Integer getPossibleDuplicateOf() {
        return this.possibleDuplicateOf;
    }

    public void setPossibleDuplicateOf(Integer possibleDuplicateOf) {
        this.possibleDuplicateOf = possibleDuplicateOf;
    }

    public Integer getMembershipLevelCap() {
        return this.membershipLevelCap;
    }

    public void setMembershipLevelCap(Integer membershipLevelCap) {
        this.membershipLevelCap = membershipLevelCap;
    }

    public Boolean getDeleted() {
        return this.deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    /**
     * Reports whether an owner is flagged deleted (soft-deleted). A {@code null} flag is treated as
     * not deleted. This is the single definition of an owner being deleted, shared by every rule
     * that must ignore soft-deleted owners.
     *
     * @param deleted the owner's {@code deleted} flag, or {@code null}
     * @return {@code true} when the owner is flagged deleted
     */
    public static boolean isDeleted(Boolean deleted) {
        return Boolean.TRUE.equals(deleted);
    }

    public Integer getHouseholdMemberCount() {
        return this.householdMemberCount;
    }

    public void setHouseholdMemberCount(Integer householdMemberCount) {
        this.householdMemberCount = householdMemberCount;
    }

    /**
     * Builds the derived identity key that consolidates all owner duplicate detection into a
     * single value: the SHA-256 hex digest (see {@link #sha256Hex(String)}) over the normalized
     * E.164 {@code telephone}, the lower-cased {@code email} (empty when none) and the Soundex
     * code of the {@code lastName} (see {@link #soundex(String)}), joined by {@code '|'}. Two
     * owners are duplicates exactly when their whole identity keys are equal, so a difference in
     * any of the three parts — for example two household mates (same last name, so same Soundex)
     * with different telephones — yields distinct keys. A {@code null} telephone or email
     * contributes the empty string. The result is a 64-character lower-case hex string.
     *
     * @param telephone the normalized E.164 telephone
     * @param email the lower-cased email, or {@code null} when none
     * @param lastName the owner's last name, reduced to its Soundex code
     * @return the derived identity key, a 64-character lower-case hex SHA-256 digest
     */
    public static String identityKey(String telephone, String email, String lastName) {
        String key = (telephone == null ? "" : telephone) + "|"
            + (email == null ? "" : email.toLowerCase(Locale.ROOT)) + "|"
            + soundex(lastName);
        return sha256Hex(key);
    }

    /**
     * Returns this owner's derived identity key, see
     * {@link #identityKey(String, String, String)}.
     *
     * @return the derived identity key for this owner
     */
    @Transient
    public String getIdentityKey() {
        return identityKey(this.telephone, this.email, this.lastName);
    }

    /**
     * Returns this owner's identity hash: the leading 8 upper-case hex characters (see
     * {@link #shaHexPrefix(String, int)}) of the SHA-256 digest over the owner's normalized E.164
     * {@code telephone} concatenated with its {@code lastName}. This is the single definition of the
     * {@code HASH8} segment the region-and-hash identity is built from (see {@link #getRegionCode()}),
     * shared by every derived code that carries it.
     *
     * @return the owner's 8-character upper-case hex identity hash
     */
    @Transient
    public String getIdentityHash() {
        return shaHexPrefix(this.telephone + this.lastName, 8);
    }

    /**
     * Computes the American Soundex code of a name: the retained first letter followed by up to
     * three digits encoding the remaining consonants ({@code B,F,P,V => 1}; {@code C,G,J,K,Q,S,X,Z
     * => 2}; {@code D,T => 3}; {@code L => 4}; {@code M,N => 5}; {@code R => 6}), with vowels and
     * {@code Y} acting as separators, {@code H} and {@code W} transparent (they do not break a run
     * of same-coded consonants), adjacent same-coded letters collapsed to a single digit, and the
     * code zero-padded to length 4. Non-letters are ignored. A {@code null} or letter-free name
     * yields the empty string. This is the single definition of the Soundex reduction shared by the
     * identity key ({@link #identityKey(String, String, String)}).
     *
     * @param name the name to encode, or {@code null}
     * @return the 4-character Soundex code, or the empty string when {@code name} has no letters
     */
    public static String soundex(String name) {
        if (name == null) {
            return "";
        }
        String letters = name.toUpperCase(Locale.ROOT).replaceAll("[^A-Z]", "");
        if (letters.isEmpty()) {
            return "";
        }
        StringBuilder code = new StringBuilder();
        code.append(letters.charAt(0));
        char previousCode = soundexCode(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            if (c == 'H' || c == 'W') {
                continue;
            }
            char digit = soundexCode(c);
            if (digit != '0' && digit != previousCode) {
                code.append(digit);
            }
            previousCode = digit;
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    /**
     * Maps a single upper-case letter to its Soundex digit, or {@code '0'} for a vowel, {@code Y},
     * {@code H} or {@code W} (the letters that do not contribute a digit). See
     * {@link #soundex(String)} for the digit groups.
     *
     * @param c the upper-case letter to encode
     * @return the Soundex digit {@code '1'}-{@code '6'}, or {@code '0'} when the letter contributes none
     */
    private static char soundexCode(char c) {
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
     * Computes the full lower-case hex SHA-256 digest of a string: the input is hashed as UTF-8
     * bytes and each of the 32 digest bytes is rendered as two lower-case hex digits, yielding a
     * 64-character string. This is the single definition of the SHA-256 hex derivation shared by
     * every rule that hashes owner fields (for example the customer code and household identifier,
     * each formed from the leading upper-case characters of this digest). SHA-256 is a required
     * platform algorithm; were it ever unavailable an {@link IllegalStateException} is thrown.
     *
     * @param input the string to hash
     * @return the 64-character lower-case hex SHA-256 digest of {@code input}
     */
    public static String sha256Hex(String input) {
        try {
            byte[] digest = java.security.MessageDigest.getInstance("SHA-256")
                .digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required but unavailable", e);
        }
    }

    /**
     * Returns the leading {@code hexChars} upper-case hex characters of the SHA-256 digest of a
     * string (see {@link #sha256Hex(String)}). This is the single definition of the truncated
     * upper-case hex form the derived owner codes are built from — the region-and-hash identity's
     * {@code HASH8} (see {@link #getIdentityHash()}) and the household identifier each take a leading
     * slice of a digest — so {@code hexChars} characters cover the leading {@code ceil(hexChars / 2)}
     * bytes of the digest.
     *
     * @param input the string to hash
     * @param hexChars the number of leading upper-case hex characters to return
     * @return the first {@code hexChars} upper-case hex characters of the SHA-256 digest of {@code input}
     */
    public static String shaHexPrefix(String input, int hexChars) {
        return sha256Hex(input).substring(0, hexChars).toUpperCase(Locale.ROOT);
    }

    /**
     * Fixed telephone country-code table: the exact national-number length (the count of digits
     * following the country code) each country code this application recognizes admits
     * ({@code '61'} Australia => 9, {@code '1'} NANP => 10). Keyed by the country-code digits
     * without the leading {@code '+'}. A country code absent from this table is not recognized.
     */
    private static final Map<String, Integer> COUNTRY_CODE_NATIONAL_LENGTHS = Map.of("61", 9, "1", 10);

    /**
     * Resolves the recognized country code a normalized E.164 telephone carries: the longest entry
     * of the fixed country-code table (see {@link #COUNTRY_CODE_NATIONAL_LENGTHS}) whose digits the
     * number begins with, so a shorter code can never shadow a longer one. Returns the country-code
     * digits without the leading {@code '+'} (for example {@code '61'} for {@code '+61412345678'}),
     * or {@code null} when the value is {@code null}, carries no leading {@code '+'}, or begins with
     * no recognized country code. This is the single definition of how an E.164 telephone splits
     * from its country code, shared by every rule that reasons about a telephone's country code.
     *
     * @param telephone the normalized E.164 telephone (a {@code '+'} followed by digits), or {@code null}
     * @return the recognized country-code digits, or {@code null} when none is recognized
     */
    public static String countryCodeOf(String telephone) {
        if (telephone == null || !telephone.startsWith("+")) {
            return null;
        }
        String digits = telephone.substring(1);
        return COUNTRY_CODE_NATIONAL_LENGTHS.keySet().stream()
            .filter(digits::startsWith)
            .max(Comparator.comparingInt(String::length))
            .orElse(null);
    }

    /**
     * Resolves the exact national-number length (the count of digits following the country code) a
     * recognized country code admits using the fixed country-code table ({@code '61'} => 9,
     * {@code '1'} => 10, see {@link #COUNTRY_CODE_NATIONAL_LENGTHS}). Returns the length, or
     * {@code null} when the country code is {@code null} or not recognized (it then carries no
     * per-country length rule). This is the single definition of the per-country national-number
     * lengths, shared by every rule that relates a telephone's national number to its country code.
     *
     * @param countryCode the country-code digits without the leading {@code '+'}, or {@code null}
     * @return the required national-number length, or {@code null} when the code is not recognized
     */
    public static Integer nationalNumberLength(String countryCode) {
        return countryCode == null ? null : COUNTRY_CODE_NATIONAL_LENGTHS.get(countryCode);
    }

    /**
     * Fixed city-to-region table: the canonical region each known city belongs to
     * ({@code Sydney -> NSW}, {@code Melbourne -> VIC}, {@code Brisbane -> QLD}). A city
     * absent from this table has no known region.
     */
    private static final Map<String, String> CITY_REGIONS = Map.of(
        "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /**
     * Resolves the canonical region a city belongs to using the fixed city-to-region table
     * ({@code Sydney -> NSW}, {@code Melbourne -> VIC}, {@code Brisbane -> QLD}). Returns the
     * region string, or {@code null} when the city has no known region. This is the single
     * definition of the city-to-region mapping, shared by every rule that derives a region
     * from an owner's city.
     *
     * @param city the city to resolve
     * @return the canonical region, or {@code null} when the city has no known region
     */
    public static String regionForCity(String city) {
        return CITY_REGIONS.get(city);
    }

    /**
     * Fixed region-to-postcode table: the inclusive {@code {low, high}} 4-digit postcode range
     * each region admits ({@code NSW 2000-2099}, {@code VIC 3000-3099}, {@code QLD 4000-4099}).
     * A region absent from this table has no fixed range.
     */
    private static final Map<String, int[]> REGION_POSTCODE_RANGES = Map.of(
        "NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099}, "QLD", new int[] {4000, 4099});

    /**
     * Resolves the inclusive {@code {low, high}} 4-digit postcode range a region admits using the
     * fixed region-to-postcode table ({@code NSW 2000-2099}, {@code VIC 3000-3099},
     * {@code QLD 4000-4099}). Returns the range, or {@code null} when the region is {@code null} or
     * has no fixed range (any 4-digit postcode is then admitted). This is the single definition of
     * the region-to-postcode ranges, shared by every rule that relates a postcode to a region.
     *
     * @param region the region to resolve, or {@code null}
     * @return the inclusive {@code {low, high}} range, or {@code null} when the region has none
     */
    public static int[] postcodeRangeForRegion(String region) {
        return region == null ? null : REGION_POSTCODE_RANGES.get(region);
    }

    /**
     * Resolves the canonical region a postcode belongs to by finding the fixed region-to-postcode
     * range that contains it ({@code NSW 2000-2099}, {@code VIC 3000-3099}, {@code QLD 4000-4099}).
     * Returns the region string, or {@code null} when the postcode is {@code null}, not a 4-digit
     * value, or falls in no known range. This is the single definition of the postcode-to-region
     * mapping, shared by every rule that derives a region from a postcode.
     *
     * @param postcode the postcode to resolve
     * @return the canonical region, or {@code null} when the postcode maps to no known region
     */
    public static String regionForPostcode(String postcode) {
        if (postcode == null || !postcode.matches("^[0-9]{4}$")) {
            return null;
        }
        int value = Integer.parseInt(postcode);
        for (Map.Entry<String, int[]> entry : REGION_POSTCODE_RANGES.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Returns the canonical region this owner belongs to. The owner's {@code postcode} is preferred:
     * when it falls in a known region range (see {@link #regionForPostcode(String)}) that region is
     * used, disambiguating cities that share a name. Only when the postcode is absent or in no known
     * range does derivation fall back to the fixed city-to-region table (see
     * {@link #regionForCity(String)}). Returns {@code null} when neither the postcode nor the city
     * resolves to a known region. This is the single definition of an owner's region, shared by
     * every rule that derives a region from an owner.
     *
     * @return the owner's canonical region, or {@code null} when it has no known region
     */
    @Transient
    public String getRegion() {
        String region = regionForPostcode(this.postcode);
        return region != null ? region : regionForCity(this.city);
    }

    /**
     * Returns the region code carried by this owner's derived identity: the owner's canonical region
     * (see {@link #getRegion()}), or the sentinel {@code 'UNKNOWN'} when the owner has no known
     * region. Unlike {@link #getRegion()} this never returns {@code null}, so it is the single
     * definition of the {@code REGION} segment the region-and-hash identity leads with (see
     * {@link #getIdentityHash()}).
     *
     * @return the owner's region code, or {@code 'UNKNOWN'} when it has no known region
     */
    @Transient
    public String getRegionCode() {
        String region = getRegion();
        return region != null ? region : "UNKNOWN";
    }

    /**
     * Resolves whether an {@code email} counts as present for membership purposes: the value is
     * non-null and not blank. Returns {@code false} otherwise. This is the single definition of an
     * owner "having an email", shared by every rule that turns email presence into a derived value
     * (its membership factor and its {@code contactPreference}).
     *
     * @param email the owner's email, or {@code null}
     * @return {@code true} when the email is present and not blank
     */
    public static boolean hasEmail(String email) {
        return email != null && !email.isBlank();
    }

    /**
     * Resolves whether an owner with the given {@code namesakeCount} has no namesakes: the count is
     * present and equal to {@code 0}. Returns {@code false} when the count is {@code null} (unknown)
     * or positive. This is the single definition of the "no namesakes" membership factor, shared by
     * every rule that rewards an owner for being unique by name.
     *
     * @param namesakeCount the owner's namesake count, or {@code null}
     * @return {@code true} when the owner is known to have no namesakes
     */
    public static boolean hasNoNamesakes(Integer namesakeCount) {
        return namesakeCount != null && namesakeCount == 0;
    }

    /**
     * Resolves the fiscal year a date falls in. The fiscal year starts on 1 July, so a date in July
     * through December belongs to the fiscal year of the following calendar year, while a date in
     * January through June belongs to the fiscal year of its own calendar year (for example both
     * 2025-08-01 and 2026-03-01 fall in fiscal year 2026). This is the single definition of the
     * date-to-fiscal-year mapping, shared by every value derived on a fiscal-year basis (the
     * {@code fiscalYear}, the membership number's year segment and tenure).
     *
     * @param date the date to resolve, never {@code null}
     * @return the fiscal year the date falls in
     */
    public static int fiscalYearOf(LocalDate date) {
        return date.getMonthValue() >= 7 ? date.getYear() + 1 : date.getYear();
    }

    /**
     * Renders the two-digit fiscal-year segment ({@code YY}) of a date: the last two digits of the
     * fiscal year the date falls in (see {@link #fiscalYearOf(LocalDate)}), zero-padded to two
     * characters (for example both 2025-08-01 and 2026-03-01 render {@code '26'}). This is the single
     * definition of the {@code YY} fiscal-year segment, shared by every value that carries it (the
     * {@code fiscalYear} and the membership number's year segment).
     *
     * @param date the date to resolve, never {@code null}
     * @return the two-digit fiscal-year segment the date falls in
     */
    public static String fiscalYearSegment(LocalDate date) {
        return String.format("%02d", fiscalYearOf(date) % 100);
    }

    /**
     * Resolves whether an owner registered on {@code registrationDate} has long tenure as of
     * {@code asOf}: the number of elapsed fiscal years between the two dates (see
     * {@link #fiscalYearOf(LocalDate)}) exceeds {@code 1}. Returns {@code false} when
     * {@code registrationDate} is absent, so the owner has no derivable tenure; because a newly
     * created owner has zero elapsed fiscal years, it never counts as long-tenured. This is the
     * single definition of the "long tenure" membership factor, shared by every rule that rewards
     * an owner for tenure beyond a fiscal year.
     *
     * @param registrationDate the owner's registration date, or {@code null}
     * @param asOf the date the tenure is measured against
     * @return {@code true} when the owner's tenure exceeds one elapsed fiscal year
     */
    public static boolean hasLongTenure(LocalDate registrationDate, LocalDate asOf) {
        return registrationDate != null && fiscalYearOf(asOf) - fiscalYearOf(registrationDate) > 1;
    }

    /**
     * Resolves whether an owner with the given {@code householdMemberCount} belongs to a large
     * household: the count is present and at least {@code 3}. Returns {@code false} when the count is
     * {@code null} (unknown) or smaller. This is the single definition of the "large household"
     * membership factor, shared by every rule that rewards an owner for a household of three or more.
     *
     * @param householdMemberCount the number of owners sharing the household, or {@code null}
     * @return {@code true} when the owner is known to belong to a household of three or more
     */
    public static boolean hasLargeHousehold(Integer householdMemberCount) {
        return householdMemberCount != null && householdMemberCount >= 3;
    }

    /**
     * Returns this owner's membership points. Points start at {@code 0} and accumulate the membership
     * factors: {@code 2} when an email is present, {@code 1} when the owner has no namesakes
     * (namesakeCount is 0), {@code 2} when the owner belongs to a household of three or more, and
     * {@code 3} when the owner's tenure exceeds one elapsed fiscal year. Because a newly created
     * owner has zero tenure, the tenure points are never awarded on creation. This is the single source the
     * membership level is derived from (see {@link #getMembershipLevel()}).
     *
     * @return the owner's membership points
     */
    @Transient
    public Integer getMembershipPoints() {
        LocalDate today = LocalDate.now();
        int points = 0;
        if (hasEmail(this.email)) {
            points += 2;
        }
        if (hasNoNamesakes(this.namesakeCount)) {
            points += 1;
        }
        if (hasLargeHousehold(this.householdMemberCount)) {
            points += 2;
        }
        if (hasLongTenure(this.registrationDate, today)) {
            points += 3;
        }
        return points;
    }

    /**
     * Resolves the numeric membership level a given number of membership points earns: {@code 1} for
     * {@code 0-1} points, {@code 2} for {@code 2-3}, {@code 3} for {@code 4-5}, and {@code 4} for
     * {@code 6} or more points. This is the single definition of the points-to-level mapping, shared
     * by every rule that derives a membership level from points (see {@link #getMembershipLevel()}).
     *
     * @param points the membership points to map
     * @return the membership level those points earn
     */
    public static int membershipLevelForPoints(int points) {
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
     * Applies an owner's household level ceiling to a membership level: the level is returned
     * unchanged when {@code cap} is {@code null} (no ceiling applies), otherwise it is capped at
     * {@code cap} so it can never exceed it. This is the single definition of how the household
     * level ceiling caps a membership level, shared by every rule that reads a capped membership
     * level (see {@link #getMembershipLevel()}).
     *
     * @param level the uncapped membership level derived from the owner's points
     * @param cap the owner's household level ceiling, or {@code null} when no ceiling applies
     * @return the membership level after applying the ceiling
     */
    public static int cappedMembershipLevel(int level, Integer cap) {
        return cap == null ? level : Math.min(level, cap);
    }

    /**
     * Returns this owner's numeric membership level, derived from its membership points (see
     * {@link #getMembershipPoints()}) by the single points-to-level mapping (see
     * {@link #membershipLevelForPoints(int)}): {@code 1} for {@code 0-1} points, {@code 2} for
     * {@code 2-3}, {@code 3} for {@code 4-5}, and {@code 4} for {@code 6} or more points. The
     * result is then held to the owner's household level ceiling {@code membershipLevelCap} (see
     * {@link #cappedMembershipLevel(int, Integer)}): a new owner joining a household may earn a
     * level at most one above the highest level among its existing household members, and no
     * ceiling applies when the owner joined an empty household.
     *
     * @return the owner's membership level, capped by its household level ceiling
     */
    @Transient
    public Integer getMembershipLevel() {
        return cappedMembershipLevel(membershipLevelForPoints(getMembershipPoints()), this.membershipLevelCap);
    }

    /**
     * Returns this owner's fiscal year formatted as {@code 'FY<YY>'}, where YY is the two-digit
     * fiscal-year segment carried by the owner's {@code memberId} (see {@link #getMemberId()}), the
     * segment immediately following the leading region code. Because the {@code memberId} is the
     * unified identity the fiscal year is now read from, this returns {@code null} when the owner has
     * no {@code memberId}, so the fiscal year cannot be derived. The two-digit segment is the last two
     * digits of the fiscal year (starting 1 July, see {@link #fiscalYearOf(LocalDate)}) the owner's
     * business-day-adjusted {@code registrationDate} fell in when the {@code memberId} was assigned,
     * e.g. a {@code memberId} of {@code 'NSW26A1B2C3D45'} yields {@code 'FY26'}. This is the single
     * definition of an owner's fiscal year.
     *
     * @return the owner's fiscal year, or {@code null} when it cannot be derived
     */
    @Transient
    public String getFiscalYear() {
        if (this.memberId == null) {
            return null;
        }
        String core = this.memberId;
        int dash = core.indexOf('-');
        if (dash >= 0) {
            core = core.substring(0, dash);
        }
        return "FY" + core.substring(core.length() - 11, core.length() - 9);
    }

    /**
     * Computes the single Luhn check digit (0-9) over the digits contained in a string: reading from
     * the right, every second digit is doubled (subtracting 9 when the result exceeds 9), the digits
     * are summed and the check digit is the amount that rounds the sum up to the next multiple of ten.
     * Non-digit characters are skipped, so a code mixing letters and digits contributes only its
     * digits. This is the single definition of the Luhn check digit, shared by every value that
     * carries one (the {@code CHK} segment of the unified member id, see {@link #getMemberId()}).
     *
     * @param code the string whose digits the check digit is computed over
     * @return the Luhn check digit (0-9) of {@code code}
     */
    public static int luhnCheckDigit(String code) {
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
     * Returns this owner's age band, derived from {@code birthDate} computed against
     * {@code registrationDate}: the whole number of years between the two dates places the owner in
     * {@code 'MINOR'} (under 18), {@code 'ADULT'} (18 to 64 inclusive) or {@code 'SENIOR'} (65 or
     * older). Returns {@code null} when the owner has no birth date or no registration date, so the
     * band cannot be derived. This is the single definition of an owner's age band.
     *
     * @return the owner's age band, or {@code null} when it cannot be derived
     */
    @Transient
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
