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
import java.time.Period;
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

    @Column(name = "postcode")
    @Pattern(regexp = "^[0-9]{4}$", message = "Postcode must be 4 digits")
    private String postcode;

    @Column(name = "telephone")
    @NotEmpty
    @Pattern(regexp = "^\\+[0-9]{8,15}$",
            message = "Phone number must be E.164: '+' followed by 8 to 15 digits")
    private String telephone;

    @Column(name = "email")
    private String email;

    @Column(name = "title")
    private String title;

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

    @Column(name = "deleted")
    private Boolean deleted = Boolean.FALSE;

    @Transient
    private Boolean bulkSignupWarning;

    @Transient
    private Boolean capacityWarning;

    @Transient
    private Integer householdSize;

    @Transient
    private Integer membershipLevelCap;

    @Transient
    private Boolean riskFlag;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner", fetch = FetchType.EAGER)
    private Set<Pet> pets;

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

    public String getPostcode() {
        return this.postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getTelephone() {
        return this.telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * The owner's salutation, derived at response time from the optional {@link #title} and the
     * {@link #getLastName() lastName}: the title, a single space, then the lastName (e.g.
     * {@code DR Who}) when a title is present, otherwise just the lastName. Not persisted as a
     * separate value; composed from the stored title and last name.
     */
    public String getSalutation() {
        if (this.title == null || this.title.isBlank()) {
            return getLastName();
        }
        return this.title + " " + getLastName();
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
     * The owner's age band, derived at response time from {@link #birthDate} against
     * {@link #registrationDate}: {@code MINOR} when under 18 years old, {@code ADULT} when 18 to 64,
     * and {@code SENIOR} when 65 or over. {@code null} when the owner has no birth date (or no
     * registration date to compute against). Not persisted.
     */
    public String getAgeBand() {
        if (this.birthDate == null || this.registrationDate == null) {
            return null;
        }
        int age = Period.between(this.birthDate, this.registrationDate).getYears();
        if (age < 18) {
            return "MINOR";
        }
        if (age < 65) {
            return "ADULT";
        }
        return "SENIOR";
    }

    /**
     * The owner's unified member id, formatted {@code <REGIONV2><FY><HASH8><CHK>} — the version-2
     * {@link #regionCodeV2(String) region code} (the plain region with the {@link #IDENTITY_VERSION}
     * tag mixed in), the two-digit fiscal year, the 8 upper-case hex HASH8 of the region-and-hash
     * identity, and a single Luhn check digit over the digits of {@code <REGIONV2><FY><HASH8>}.
     * Assigned at creation (see {@code AssignOwnerMemberId}); it is the owner's single primary
     * identifier, replacing the former {@code customerCode}, {@code membershipNumber} and standalone
     * {@code checkDigit}. The user-facing {@link #getLocality() locality}, {@link #getFiscalYear()
     * fiscal year} and {@link #getOwnerSegment() owner segment} are derived independently from the
     * postcode and registration date, so the version-2 tag stays inside this identifier.
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
     * Whether this owner, when created, was a possible (soft) duplicate of an earlier owner: an
     * earlier non-deleted owner whose {@link #soundex(String) soundex(lastName)} and postcode both
     * matched this owner's, but whose {@link #getIdentityKey() identityKey} differed (so it was not a
     * hard duplicate — an exact identityKey match is rejected 409 and never reaches this flag).
     * {@code false} when no such earlier owner existed. Assigned at creation.
     */
    public Boolean getPossibleDuplicate() {
        return this.possibleDuplicate;
    }

    public void setPossibleDuplicate(Boolean possibleDuplicate) {
        this.possibleDuplicate = possibleDuplicate;
    }

    /**
     * The id of the earlier owner this owner is a possible duplicate of (the earliest such match),
     * or {@code null} when {@link #getPossibleDuplicate()} is false. Assigned at creation.
     */
    public Integer getPossibleDuplicateOf() {
        return this.possibleDuplicateOf;
    }

    public void setPossibleDuplicateOf(Integer possibleDuplicateOf) {
        this.possibleDuplicateOf = possibleDuplicateOf;
    }

    /**
     * Whether this owner has been soft-deleted via {@code DELETE /api/owners/{id}}: the record is
     * retained and still returned by {@code GET /api/owners/{id}}, but is flagged {@code true} and
     * ignored by the create endpoint's duplicate and identity checks. {@code false} for a newly
     * created owner.
     */
    public Boolean getDeleted() {
        return this.deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    /** Whether this owner is soft-deleted, treating a {@code null} flag as not deleted. */
    public boolean isDeleted() {
        return Boolean.TRUE.equals(this.deleted);
    }

    /**
     * Whether more than 80 owners had already been created on this owner's registration day,
     * derived at response time and not persisted. {@code true} triggers a bulk-signup warning
     * in the response.
     */
    public Boolean getBulkSignupWarning() {
        return this.bulkSignupWarning;
    }

    public void setBulkSignupWarning(Boolean bulkSignupWarning) {
        this.bulkSignupWarning = bulkSignupWarning;
    }

    /**
     * Whether this owner's city already holds between 40 and 49 owners (inclusive), approaching the
     * hard capacity limit of 50. Derived at response time and not persisted; {@code true} triggers a
     * capacity warning in the response.
     */
    public Boolean getCapacityWarning() {
        return this.capacityWarning;
    }

    public void setCapacityWarning(Boolean capacityWarning) {
        this.capacityWarning = capacityWarning;
    }

    /**
     * The number of owners in this owner's household (owners sharing the same
     * {@link #householdId}), derived at response time and not persisted.
     */
    public Integer getHouseholdSize() {
        return this.householdSize;
    }

    public void setHouseholdSize(Integer householdSize) {
        this.householdSize = householdSize;
    }

    /**
     * The ceiling applied to {@link #getMembershipLevel() membershipLevel}: one above the current
     * maximum membershipLevel among this owner's existing household members. Derived at response
     * time and not persisted; {@code null} means no household member exists and no cap applies.
     */
    public Integer getMembershipLevelCap() {
        return this.membershipLevelCap;
    }

    public void setMembershipLevelCap(Integer membershipLevelCap) {
        this.membershipLevelCap = membershipLevelCap;
    }

    /**
     * Whether this owner is flagged as elevated risk, derived at response time and not persisted.
     * {@code true} when any of three soft signals holds: the owner is a {@link #getPossibleDuplicate()
     * possible duplicate}; the email domain is disposable-adjacent (see
     * {@code ValidateOwnerEmailDomain.isDisposableAdjacent}); or the owner's city is over its soft
     * capacity (see {@code DeriveOwnerRiskFlag.SOFT_CAPACITY}). {@code false} when none hold.
     * Populated by {@code DeriveOwnerRiskFlag} on both the create and single-owner GET responses.
     */
    public Boolean getRiskFlag() {
        return this.riskFlag;
    }

    public void setRiskFlag(Boolean riskFlag) {
        this.riskFlag = riskFlag;
    }

    /**
     * The owner's membership points, derived at response time. Starts at {@code 0}, plus 2 when an
     * email is present, plus 1 when {@link #namesakeCount} is 0, plus 2 when the
     * {@link #getHouseholdSize() household} has 3 or more members, plus 3 when tenure exceeds one
     * elapsed fiscal year (measured from the {@link #registrationDate}; see
     * {@link #getTenureFiscalYears()}).
     */
    public Integer getMembershipPoints() {
        int points = 0;
        if (this.email != null && !this.email.isEmpty()) {
            points += 2;
        }
        if (this.namesakeCount != null && this.namesakeCount == 0) {
            points += 1;
        }
        if (this.householdSize != null && this.householdSize >= 3) {
            points += 2;
        }
        if (getTenureFiscalYears() > 1) {
            points += 3;
        }
        return points;
    }

    /**
     * The owner's numeric membership level, mapped from {@link #getMembershipPoints() membershipPoints}:
     * {@code 1} for 0-1 points, {@code 2} for 2-3 points, {@code 3} for 4-5 points, and {@code 4} for
     * 6 or more points.
     *
     * <p>When a {@link #getMembershipLevelCap() membershipLevelCap} is present the result is capped to
     * it, so a new owner's level cannot exceed one above the current maximum membershipLevel among
     * their household members. With no household member the cap is absent and the mapped level stands.
     */
    public Integer getMembershipLevel() {
        int points = getMembershipPoints();
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
        if (this.membershipLevelCap != null && level > this.membershipLevelCap) {
            return this.membershipLevelCap;
        }
        return level;
    }

    /**
     * The fiscal year containing {@code date}: fiscal years start on 1 July and are named for the
     * calendar year in which they end, so 1 July 2026 – 30 June 2027 is fiscal year {@code 2027}.
     */
    public static int fiscalYear(LocalDate date) {
        return date.getMonthValue() >= 7 ? date.getYear() + 1 : date.getYear();
    }

    /**
     * The owner's fiscal year, derived at response time from the (business-day-adjusted)
     * {@link #registrationDate} as {@code FY<YY>}, where the fiscal year starts on 1 July and
     * {@code YY} is the last two digits of the fiscal year (the calendar year in which it ends). This
     * is the same fiscal year the {@link #getMemberId() memberId} embeds, but it is computed directly
     * from the registration date rather than parsed out of the member id — so the version-2 tag mixed
     * into the member id never leaks into this value. {@code null} when the owner has no registration
     * date. Not persisted.
     */
    public String getFiscalYear() {
        if (this.registrationDate == null) {
            return null;
        }
        return "FY" + String.format(Locale.ROOT, "%02d", fiscalYear(this.registrationDate) % 100);
    }

    /**
     * The owner's tenure in elapsed fiscal years, measured from the {@link #registrationDate}'s
     * fiscal year to the current fiscal year (fiscal years start on 1 July); zero when the
     * registration date is absent or falls in a later fiscal year than today.
     */
    private long getTenureFiscalYears() {
        if (this.registrationDate == null) {
            return 0;
        }
        long years = (long) fiscalYear(LocalDate.now()) - fiscalYear(this.registrationDate);
        return Math.max(years, 0);
    }

    /**
     * The fixed identity-scheme version tag mixed into every version-2 identifier — the
     * {@link #getMemberId() memberId} (via its {@link #regionCodeV2(String) region code}), the
     * {@link #getHouseholdId() householdId} and the {@link #getIdentityKey() identityKey}. It appears
     * only inside those identifiers; the user-facing {@link #getLocality() locality},
     * {@link #getTimezone() timezone} and {@link #getOwnerSegment() owner segment} stay the plain
     * region code.
     */
    public static final String IDENTITY_VERSION = "V2";

    /** Fixed city-to-region table used to derive {@link #getLocality()}. */
    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** Region -> inclusive 4-digit postcode range {low, high} used to derive {@link #getLocality()}. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
        "NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099}, "QLD", new int[] {4000, 4099});

    /** Fixed region-to-IANA-timezone table used to derive {@link #getTimezone()}. */
    private static final Map<String, String> REGION_TIMEZONE = Map.of(
        "NSW", "Australia/Sydney", "VIC", "Australia/Melbourne", "QLD", "Australia/Brisbane");

    /**
     * The fixed city-to-region mapping ({@code Sydney -> NSW}, {@code Melbourne -> VIC},
     * {@code Brisbane -> QLD}): the region for {@code city}, or {@code null} when the city is not in
     * the table. Used only by postcode validation, which branches on "known region or not" by testing
     * for {@code null} before range-checking the postcode.
     */
    public static String regionOf(String city) {
        return CITY_REGION.get(city);
    }

    /**
     * The region whose fixed postcode range ({@code NSW 2000-2099}, {@code VIC 3000-3099},
     * {@code QLD 4000-4099}) contains {@code postcode}, or {@code null} when {@code postcode} is
     * absent, not a 4-digit number, or in no known range.
     */
    public static String regionOfPostcode(String postcode) {
        if (postcode == null || postcode.isEmpty()) {
            return null;
        }
        int value;
        try {
            value = Integer.parseInt(postcode);
        } catch (NumberFormatException ex) {
            return null;
        }
        for (Map.Entry<String, int[]> entry : REGION_POSTCODES.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * The version-2 region code embedded <em>inside</em> the identifiers: the plain region code with
     * the fixed {@link #IDENTITY_VERSION} tag appended (e.g. {@code NSWV2}, {@code UNKNOWNV2}). A
     * {@code null} region is treated as {@code UNKNOWN}. Because every version-1 identifier carried the
     * bare region code, mixing the tag in guarantees no version-2 identifier can ever reproduce a
     * version-1 value. This is an identifier-internal value only; the user-facing
     * {@link #getLocality() locality}, {@link #getTimezone() timezone} and {@link #getOwnerSegment()
     * owner segment} keep using the plain region code.
     */
    public static String regionCodeV2(String region) {
        return (region == null ? "UNKNOWN" : region) + IDENTITY_VERSION;
    }

    /**
     * The owner's region, derived at response time directly from the {@link #getPostcode() postcode}
     * via {@link #regionOfPostcode(String)} — the plain region code ({@code NSW}, {@code VIC} or
     * {@code QLD}), or {@code UNKNOWN} when the postcode maps to no known region. This is the same
     * region the {@link #getMemberId() memberId} is built from, but it is the <em>plain</em> code:
     * the version-2 {@link #IDENTITY_VERSION} tag mixed into the member id never appears here.
     */
    public String getLocality() {
        String region = regionOfPostcode(this.postcode);
        return region == null ? "UNKNOWN" : region;
    }

    /**
     * The owner's IANA timezone name, derived at response time from the {@link #getLocality()
     * locality}/region via the fixed region-to-timezone table ({@code NSW -> Australia/Sydney},
     * {@code VIC -> Australia/Melbourne}, {@code QLD -> Australia/Brisbane}), or {@code null}
     * when the region is not in the table. Not persisted.
     */
    public String getTimezone() {
        return REGION_TIMEZONE.get(getLocality());
    }

    /**
     * The owner's segment, derived at response time as {@code <TIER>_<AREA>} — one of
     * {@code PREMIUM_METRO}, {@code PREMIUM_REGIONAL}, {@code STANDARD_METRO} or
     * {@code STANDARD_REGIONAL}. TIER is {@code PREMIUM} when {@link #getMembershipLevel()
     * membershipLevel} is 3 or more, otherwise {@code STANDARD}. AREA is {@code METRO} when the
     * {@link #getLocality() locality} is a known region ({@code NSW}, {@code VIC} or {@code QLD}),
     * otherwise {@code REGIONAL}. Not persisted.
     */
    public String getOwnerSegment() {
        Integer level = getMembershipLevel();
        String tier = (level != null && level >= 3) ? "PREMIUM" : "STANDARD";
        String area = REGION_TIMEZONE.containsKey(getLocality()) ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }

    /**
     * The owner's preferred contact channel, derived at response time: {@code EMAIL} when an
     * email is present, otherwise {@code PHONE}. Not persisted.
     */
    public String getContactPreference() {
        return (this.email != null && !this.email.isEmpty()) ? "EMAIL" : "PHONE";
    }

    /**
     * The owner's deterministic identity key: the full lower-case hex SHA-256 of
     * {@code normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)}. The telephone is
     * stored in canonical E.164 form and the email lower-cased; an absent telephone or email
     * contributes an empty segment, and the last name is reduced to its {@link #soundex(String)
     * Soundex} code. Two owners share an identity exactly when this 64-character hex value is equal.
     *
     * <p>This is the single key duplicate detection keys off: a create is rejected 409 when a
     * non-deleted owner already carries the same identity key (see {@code DetectOwnerDuplicate}).
     * Because the telephone participates in the key, two owners with the same last name and postcode
     * but different telephones no longer collide here — they are flagged as a soft match instead (see
     * {@code DetectOwnerPossibleDuplicate}). Derived at response time and not persisted.
     *
     * <p>The version-2 derivation mixes the fixed {@link #IDENTITY_VERSION} tag into the hashed input,
     * so every key differs from the version-1 value while owners that share an identity still share
     * the (new) key — the collision semantics are unchanged, only the value.
     */
    public String getIdentityKey() {
        String telephone = keyPart(this.telephone);
        String email = this.email == null ? "" : this.email.toLowerCase(Locale.ROOT);
        String input = IDENTITY_VERSION + "|" + telephone + "|" + email + "|" + soundex(this.lastName);
        return sha256Hex(input);
    }

    private static String keyPart(String value) {
        return value == null ? "" : value;
    }

    /** Full lower-case hex SHA-256 of the UTF-8 bytes of {@code value}. */
    private static String sha256Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
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

    /**
     * The American Soundex code for {@code name}: the retained first letter followed by up to three
     * digits mapping the remaining consonants (b/f/p/v→1, c/g/j/k/q/s/x/z→2, d/t→3, l→4, m/n→5, r→6),
     * right-padded with zeros to length four. Adjacent duplicate codes collapse; {@code h}/{@code w}
     * do not separate repeated codes while vowels (and {@code y}) do. Non-letters are ignored. Returns
     * {@code ""} when {@code name} is null or has no letters. Two last names that sound alike share a
     * code, which is how the identity key and soft-match group surnames phonetically.
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
        char prev = soundexDigit(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            if (c == 'H' || c == 'W') {
                continue; // ignored and does not separate repeated codes
            }
            char digit = soundexDigit(c);
            if (digit != '0' && digit != prev) {
                code.append(digit);
            }
            prev = digit; // a vowel resets prev so a later same code is emitted again
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    /** The Soundex digit for an upper-case letter, {@code '0'} for vowels and {@code h}/{@code w}. */
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
     * The stored E.164 {@link #telephone} formatted for humans: the country code, a space, then the
     * national digits (everything after the country code) grouped in threes and space-separated —
     * e.g. {@code +61412345678} becomes {@code +61 412 345 678}. The country code is recognised for
     * '+61' (2 digits) and '+1' (1 digit); any other prefix falls back to a 2-digit country code.
     * {@code null} when the telephone is absent or not a valid E.164 value. Derived at response time
     * and not persisted; the raw {@link #getTelephone() telephone} stays in E.164 form.
     */
    public String getTelephoneDisplay() {
        if (this.telephone == null || !this.telephone.matches("\\+\\d{8,15}")) {
            return null;
        }
        String digits = this.telephone.substring(1);
        int countryCodeLength;
        if (digits.startsWith("61")) {
            countryCodeLength = 2;
        }
        else if (digits.startsWith("1")) {
            countryCodeLength = 1;
        }
        else {
            countryCodeLength = 2;
        }
        String countryCode = digits.substring(0, countryCodeLength);
        String national = digits.substring(countryCodeLength);
        StringBuilder display = new StringBuilder("+").append(countryCode);
        for (int i = 0; i < national.length(); i += 3) {
            display.append(' ').append(national, i, Math.min(i + 3, national.length()));
        }
        return display.toString();
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
