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
import java.time.Month;
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

    /**
     * The version-2 owner identity contract. {@link #API_VERSION} is surfaced as the response's top-level
     * {@code apiVersion}; {@link #IDENTITY_VERSION_TAG} is the fixed tag mixed <em>inside</em> the derived
     * identifiers (the {@link #getMemberId() memberId}'s region segment, the household id and the
     * {@link #identityKey(String, String, String) identity key}) so every v2 identifier differs from its
     * v1 form and no value produced under version 1 is produced again. The tag is deliberately confined to
     * the identifiers: it never appears in the user-facing {@link #getLocality() locality},
     * {@link #getTimezone() timezone} or the {@link #getOwnerSegment() owner segment}'s derived region,
     * all of which stay the plain region code.
     */
    public static final int API_VERSION = 2;

    /** The fixed version tag mixed into the v2 identifiers (see {@link #API_VERSION}). */
    public static final String IDENTITY_VERSION_TAG = "V2";
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
    private String postcode;

    @Column(name = "telephone")
    @NotEmpty
    @Pattern(regexp = "^\\+[0-9]{8,15}$", message = "Phone number must be in E.164 form (+ then 8 to 15 digits)")
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

    @Column(name = "membership_level_cap")
    private Integer membershipLevelCap;

    @Column(name = "deleted")
    private boolean deleted;

    @Transient
    private Integer householdSize;

    @Transient
    private Boolean bulkSignupWarning;

    @Transient
    private Boolean capacityWarning;

    @Transient
    private Boolean possibleDuplicate;

    @Transient
    private Integer possibleDuplicateOf;

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

    /**
     * The first line of the structured postal address (street number and name), or {@code null} when
     * the owner was supplied with only the flat {@link #getAddress() address}. The preferred street
     * address when present; {@link #getAddress() address} holds the composed form.
     */
    public String getAddressLine1() {
        return this.addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    /**
     * The optional second line of the structured postal address (unit, suite, etc.), or {@code null}
     * when none was supplied. When present it is appended to the composed {@link #getAddress() address}.
     */
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

    /**
     * The stored {@link #getTelephone() telephone} formatted for humans by {@link TelephoneFormatter}:
     * the country code, a space, then the national digits grouped in threes (e.g. {@code +61412345678}
     * -> {@code +61 412 345 678}). The raw {@link #getTelephone() telephone} stays in E.164 form.
     */
    public String getTelephoneDisplay() {
        return TelephoneFormatter.toDisplay(this.telephone);
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
     * The owner's salutation: the {@link #getTitle() title} followed by a single space and the
     * {@link #getLastName() last name} (e.g. {@code "DR Franklin"}), or just the last name when no
     * title was supplied. Derived per response, not persisted.
     */
    public String getSalutation() {
        if (this.title == null || this.title.isBlank()) {
            return this.getLastName();
        }
        return this.title + " " + this.getLastName();
    }

    public LocalDate getRegistrationDate() {
        return this.registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }

    /**
     * The fiscal year a {@code date} belongs to, as a four-digit year. The fiscal year starts on 1 July
     * and is identified by the calendar year in which it ends: a date on or after 1 July of year
     * {@code Y} belongs to the fiscal year ending in {@code Y + 1}, while a date in January to June of
     * year {@code Y} belongs to the fiscal year ending in {@code Y}.
     */
    public static int fiscalYear(LocalDate date) {
        return date.getMonthValue() >= Month.JULY.getValue() ? date.getYear() + 1 : date.getYear();
    }

    /**
     * The owner's fiscal year, formatted {@code "FY<YY>"} where YY is the last two digits of the
     * {@link #fiscalYear(LocalDate) fiscal year} of the business-day-adjusted
     * {@link #getRegistrationDate() registrationDate} captured at creation — e.g. a registration on
     * 2026-10-02 falls in the fiscal year ending June 2027, so this reads {@code "FY27"}, the same FY the
     * {@link #getMemberId() memberId} embeds. Unlike v1 this derives directly from the registration date
     * rather than parsing the memberId, so the v2 identity tag embedded in the memberId's region segment
     * does not leak into it. {@code null} when no memberId is assigned. Derived per response.
     */
    public String getFiscalYear() {
        if (this.memberId == null || this.registrationDate == null) {
            return null;
        }
        return "FY" + String.format("%02d", fiscalYear(this.registrationDate) % 100);
    }

    public LocalDate getBirthDate() {
        return this.birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    /**
     * The owner's age band, derived from {@link #getBirthDate() birthDate} measured against the
     * {@link #getRegistrationDate() registrationDate}: {@code "MINOR"} when under 18, {@code "ADULT"}
     * when 18 to 64, or {@code "SENIOR"} when 65 or older. {@code null} when no birth date is present
     * (so the field is absent from the response) or the registration date is unknown.
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
     * The owner's member id, the single owner identity, formatted {@code <REGION><FY><HASH8><CHK>} and
     * assigned at creation (see
     * {@link org.springframework.samples.petclinic.rest.function.owner.AssignMemberId}). REGION is the
     * version-2 identifier-internal region code from {@link #identityRegion(String)} — the plain region
     * derived from the postcode (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099, else {@code "UNKNOWN"}) with
     * the fixed {@link #IDENTITY_VERSION_TAG V2 tag} mixed in (e.g. {@code "NSWV2"}); FY is the two-digit
     * fiscal year of the business-day-adjusted
     * {@link #getRegistrationDate() registrationDate}; HASH8 is the first 8 upper-case hex characters of
     * SHA-256 over the normalized (E.164) telephone concatenated with the last name; and CHK is a single
     * {@link #luhnCheckDigit(String) Luhn check digit} over the digits of {@code <REGION><FY><HASH8>}.
     * Collisions are de-duplicated by appending {@code -<n>} with the smallest {@code n} of 2 or more
     * that makes it unique. The create audit record reads this value. The {@link #getLocality() locality}
     * and {@link #getFiscalYear() fiscalYear} derive independently (from the postcode and the registration
     * date), so the embedded V2 region tag does not leak into them.
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
     * The ceiling applied to this owner's {@link #getMembershipLevel() membershipLevel}, or {@code null}
     * when none applies. Set once at creation to one above the maximum membershipLevel among the owner's
     * existing household members (see
     * {@link org.springframework.samples.petclinic.rest.function.owner.CapMembershipLevel}); a new owner
     * with no existing household member has no cap.
     *
     * <p>Unlike the other inputs to {@link #getMembershipLevel()} (points and household size), which are
     * recomputed on every read, the cap is a deliberate creation-time snapshot: the rule is about the
     * household as it stood when this owner joined, so the ceiling is persisted and reapplied on later
     * reads rather than re-derived from the household's current membership.
     */
    public Integer getMembershipLevelCap() {
        return this.membershipLevelCap;
    }

    public void setMembershipLevelCap(Integer membershipLevelCap) {
        this.membershipLevelCap = membershipLevelCap;
    }

    /**
     * Whether this owner has been soft-deleted. A {@code DELETE /api/owners/{id}} sets this true and
     * retains the record rather than removing it; a newly created owner is false. Soft-deleted owners
     * are still returned by {@code GET /api/owners/{id}} but are ignored by the create endpoint's
     * duplicate/identity checks (see {@code CheckIdentityUnique}).
     */
    public boolean isDeleted() {
        return this.deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    /**
     * Sets the number of owners that belong to this owner's household (same last name and postcode; see
     * {@link org.springframework.samples.petclinic.rest.function.owner.Household}), including this owner.
     * Computed per request and used only by {@link #getMembershipPoints()} — a household of three or more
     * contributes points. Not persisted and not serialized into the response (no getter is exposed); the
     * count is recomputed on each read because household membership changes as owners come and go.
     */
    public void setHouseholdSize(Integer householdSize) {
        this.householdSize = householdSize;
    }

    /**
     * Whether this response should carry a bulk-signup warning: {@code true} when more than 80 owners
     * have already been created for this owner's registration day. Computed per request, not persisted.
     */
    public Boolean getBulkSignupWarning() {
        return this.bulkSignupWarning;
    }

    public void setBulkSignupWarning(Boolean bulkSignupWarning) {
        this.bulkSignupWarning = bulkSignupWarning;
    }

    /**
     * Whether this response should carry a per-city capacity warning: {@code true} when this owner's city
     * already contains between 40 and 49 owners (inclusive), approaching the per-city capacity limit of 50.
     * Computed per request, not persisted.
     */
    public Boolean getCapacityWarning() {
        return this.capacityWarning;
    }

    public void setCapacityWarning(Boolean capacityWarning) {
        this.capacityWarning = capacityWarning;
    }

    /**
     * Whether this owner is a <em>possible</em> (soft) duplicate of another owner: {@code true} when some
     * other owner has a <em>different</em> {@link #getIdentityKey() identity key} yet shares both this
     * owner's last-name {@link #soundex(String) Soundex} and postcode — a near match that is not the same
     * identity. Computed per request, not persisted. See {@link #getPossibleDuplicateOf()} for the matched
     * owner.
     */
    public Boolean getPossibleDuplicate() {
        return this.possibleDuplicate;
    }

    public void setPossibleDuplicate(Boolean possibleDuplicate) {
        this.possibleDuplicate = possibleDuplicate;
    }

    /**
     * The id of the existing owner this owner possibly duplicates (same last-name Soundex and postcode, but
     * a different identity key), or {@code null} when {@link #getPossibleDuplicate() possibleDuplicate} is
     * false. When several owners match, this is the earliest (lowest id). Computed per request, not
     * persisted.
     */
    public Integer getPossibleDuplicateOf() {
        return this.possibleDuplicateOf;
    }

    public void setPossibleDuplicateOf(Integer possibleDuplicateOf) {
        this.possibleDuplicateOf = possibleDuplicateOf;
    }

    /**
     * Whether this response should carry an overall risk flag: {@code true} when any of the soft risk
     * signals hold — this owner is a {@link #getPossibleDuplicate() possible duplicate}, its email is on a
     * disposable-adjacent domain, or its city is over its soft capacity — otherwise {@code false}. A
     * roll-up of the individual signals. Computed per request, not persisted.
     */
    public Boolean getRiskFlag() {
        return this.riskFlag;
    }

    public void setRiskFlag(Boolean riskFlag) {
        this.riskFlag = riskFlag;
    }

    /**
     * The owner's membership points: start at 0; add 2 when an email is present; add 1 when
     * {@code namesakeCount} is 0; add 2 for a household of 3 or more members (see
     * {@link #setHouseholdSize(Integer)}); add 3 for a tenure of at least one elapsed fiscal year, i.e.
     * the {@code registrationDate} falls in an earlier {@link #fiscalYear(LocalDate) fiscal year} than
     * today (fiscal years start 1 July). A newly created owner has zero tenure, so a fresh owner scores
     * at most 5 points (2 + 1 + 2).
     */
    public int getMembershipPoints() {
        return membershipPoints(this.householdSize);
    }

    /** The membership points this owner would score for the given household size; see
     * {@link #getMembershipPoints()}. Read-only, so a level can be derived for a hypothetical household
     * size without mutating this owner's transient {@link #setHouseholdSize(Integer) householdSize}. */
    private int membershipPoints(Integer householdSize) {
        int points = 0;
        boolean hasEmail = this.email != null && !this.email.isBlank();
        if (hasEmail) {
            points += 2;
        }
        boolean unique = this.namesakeCount != null && this.namesakeCount == 0;
        if (unique) {
            points += 1;
        }
        boolean largeHousehold = householdSize != null && householdSize >= 3;
        if (largeHousehold) {
            points += 2;
        }
        boolean tenured = this.registrationDate != null
            && fiscalYear(LocalDate.now()) - fiscalYear(this.registrationDate) >= 1;
        if (tenured) {
            points += 3;
        }
        return points;
    }

    /**
     * The owner's membership level, derived from {@link #getMembershipPoints() membershipPoints}:
     * 1 for 0-1 points, 2 for 2-3, 3 for 4-5, and 4 for 6 or more. When a
     * {@link #getMembershipLevelCap() membershipLevelCap} applies, the derived level is capped to it, so
     * a new owner cannot exceed one above the maximum level among their household members.
     */
    public int getMembershipLevel() {
        return membershipLevelForHouseholdSize(this.householdSize);
    }

    /**
     * This owner's membership level as it would be derived for the given household size — i.e. the value
     * this owner's own {@code GET} would report when its household holds {@code householdSize} members —
     * after applying any {@link #getMembershipLevelCap() cap}. Read-only: used to size a new household
     * member's cap from the existing members' levels without mutating their transient household size.
     */
    public int membershipLevelForHouseholdSize(Integer householdSize) {
        int points = membershipPoints(householdSize);
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
     * Fixed city-to-region table mapping each known city to its canonical region string.
     */
    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW",
        "Melbourne", "VIC",
        "Brisbane", "QLD");

    /**
     * Region -> inclusive {low, high} four-digit postcode range: the single canonical home of the
     * postcode bands (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099), read via
     * {@link #postcodeRangeOf(String)} by both locality derivation and the create-time range check.
     */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /**
     * Fixed region-to-timezone table mapping each known region to its IANA timezone name
     * (NSW -> Australia/Sydney, VIC -> Australia/Melbourne, QLD -> Australia/Brisbane). The single
     * source of truth for the timezone derivation read by {@link #getTimezone()}.
     */
    private static final Map<String, String> REGION_TIMEZONE = Map.of(
        "NSW", "Australia/Sydney",
        "VIC", "Australia/Melbourne",
        "QLD", "Australia/Brisbane");

    /**
     * The canonical region for {@code city} derived from the fixed city-to-region table
     * ({@code Sydney->NSW}, {@code Melbourne->VIC}, {@code Brisbane->QLD}), or {@code "UNKNOWN"} when the
     * city is not in the table. The single source of truth for the create-time postcode range check in
     * {@link org.springframework.samples.petclinic.rest.function.owner.ValidateOwnerPostcode}.
     */
    public static String regionOf(String city) {
        return CITY_REGION.getOrDefault(city, "UNKNOWN");
    }

    /**
     * The inclusive {low, high} four-digit postcode band for {@code region} (NSW 2000-2099,
     * VIC 3000-3099, QLD 4000-4099), or {@code null} when the region has no band. This is the single
     * source of truth for the bands, shared by {@link #regionOfPostcode(String) the postcode-to-region
     * lookup} (which feeds the memberId's region, and hence the locality) and the create-time
     * postcode range check.
     */
    public static int[] postcodeRangeOf(String region) {
        return REGION_POSTCODES.get(region);
    }

    /**
     * The canonical region whose postcode band contains {@code postcode} (NSW 2000-2099, VIC 3000-3099,
     * QLD 4000-4099), or {@code null} when {@code postcode} is absent, not four digits, or in no known
     * range.
     */
    public static String regionOfPostcode(String postcode) {
        if (postcode == null || !postcode.matches("[0-9]{4}")) {
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

    /**
     * The region code embedded <em>inside</em> the version-2 identifiers: the plain region from
     * {@link #regionOfPostcode(String)} (or {@code "UNKNOWN"} when the postcode is absent or in no known
     * range) with the fixed {@link #IDENTITY_VERSION_TAG version tag} mixed in (e.g. {@code "NSWV2"}), so
     * the v2 {@link #getMemberId() memberId} differs from its v1 form and no v1 value recurs. This is the
     * identifier-internal region only; the user-facing {@link #getLocality() locality} stays the plain
     * region code.
     */
    public static String identityRegion(String postcode) {
        String region = regionOfPostcode(postcode);
        return (region == null ? "UNKNOWN" : region) + IDENTITY_VERSION_TAG;
    }

    /**
     * The owner's locality: the plain region code derived from the {@link #getPostcode() postcode} via
     * {@link #regionOfPostcode(String)} (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099), or {@code "UNKNOWN"}
     * when the postcode resolves to no known region. This is the user-facing region and is NOT an
     * identifier: it stays the plain code (e.g. {@code "NSW"}) and never carries the
     * {@link #IDENTITY_VERSION_TAG version tag} that the memberId's region segment does. It matches the
     * region the v2 memberId was built from, since both derive from the same stored postcode.
     */
    public String getLocality() {
        String region = regionOfPostcode(this.postcode);
        return region == null ? "UNKNOWN" : region;
    }

    /**
     * The owner's IANA timezone, derived from the {@link #getLocality() locality}/region via the fixed
     * region-to-timezone table (NSW -> Australia/Sydney, VIC -> Australia/Melbourne,
     * QLD -> Australia/Brisbane), or {@code null} (so the field is absent from the response) when the
     * locality maps to no known region.
     */
    public String getTimezone() {
        return REGION_TIMEZONE.get(getLocality());
    }

    /**
     * The owner's segment, formatted {@code "<TIER>_<AREA>"}: TIER is {@code "PREMIUM"} when
     * {@link #getMembershipLevel() membershipLevel} is 3 or more, otherwise {@code "STANDARD"}; AREA is
     * {@code "METRO"} when the {@link #getLocality() locality} is a known region (NSW, VIC or QLD),
     * otherwise {@code "REGIONAL"}. One of {@code PREMIUM_METRO}, {@code PREMIUM_REGIONAL},
     * {@code STANDARD_METRO} or {@code STANDARD_REGIONAL}. Derived per response.
     */
    public String getOwnerSegment() {
        String tier = getMembershipLevel() >= 3 ? "PREMIUM" : "STANDARD";
        String area = REGION_POSTCODES.containsKey(getLocality()) ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }

    /**
     * The single Luhn check digit (0-9) computed over the digits contained in {@code value}: non-digit
     * characters are ignored, each second digit from the right is doubled (subtracting 9 when the double
     * exceeds 9), the results are summed, and the check digit is {@code (10 - sum % 10) % 10}. This is
     * the CHK segment of the {@link #getMemberId() memberId}, computed over its {@code <REGION><FY><HASH8>}
     * prefix.
     */
    public static int luhnCheckDigit(String value) {
        int sum = 0;
        boolean dbl = true;
        for (int i = value.length() - 1; i >= 0; i--) {
            char c = value.charAt(i);
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
     * The owner's preferred contact channel: {@code "EMAIL"} when an email is present, otherwise
     * {@code "PHONE"}.
     */
    public String getContactPreference() {
        boolean hasEmail = this.email != null && !this.email.isBlank();
        return hasEmail ? "EMAIL" : "PHONE";
    }

    /**
     * The single canonical definition of an owner's derived identity key: the lower-case SHA-256 hex
     * digest (64 hex characters) of
     * {@code IDENTITY_VERSION_TAG + "|" + normalizedTelephone + "|" + lowerEmail + "|" + soundex(lastName)}.
     * The leading version tag is what distinguishes this v2 key from its v1 form (which hashed the same
     * components without the tag), so no v1 identity key is produced again.
     * The telephone is used as given (it is expected to already be canonical E.164); the email is
     * lower-cased here and a null/blank email or telephone contributes an empty component; the last name is
     * reduced to its {@link #soundex(String) Soundex} code so near-spellings share a key component. This is
     * the only place the key is composed and hashed; the stored-owner {@link #getIdentityKey()} routes
     * through here so the format cannot drift. The key is both a descriptive, returned field and the single
     * basis for create-time duplicate detection (two owners with an equal identity key are the same
     * identity — a 409; see {@code CheckIdentityUnique}).
     */
    public static String identityKey(String telephone, String email, String lastName) {
        String tel = (telephone == null) ? "" : telephone;
        String mail = (email == null || email.isBlank()) ? "" : email.toLowerCase(Locale.ROOT);
        return Sha256.hex(IDENTITY_VERSION_TAG + "|" + tel + "|" + mail + "|" + soundex(lastName));
    }

    /**
     * The owner's derived identity key, built from its stored canonical telephone (E.164), email and the
     * Soundex of its last name via {@link #identityKey(String, String, String)}. Both a descriptive,
     * returned value and the single basis for create-time duplicate detection (see
     * {@code CheckIdentityUnique}).
     */
    public String getIdentityKey() {
        return identityKey(this.telephone, this.email, this.getLastName());
    }

    /**
     * The American Soundex code of {@code name}: the upper-cased first letter followed by three digits
     * derived from the remaining consonants (vowels and the letters H/W are not coded; adjacent letters
     * with the same code, and same-coded letters separated only by H/W, collapse to one; a vowel between
     * same-coded letters keeps both). Padded with zeros or truncated to exactly four characters. Empty
     * when {@code name} holds no letters (or is {@code null}). Used as the last-name component of the
     * {@link #identityKey(String, String, String) identity key} so near-spellings share it.
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
        char previous = soundexDigit(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char ch = letters.charAt(i);
            if (ch == 'H' || ch == 'W') {
                continue; // acts as a separator: keep 'previous' so the flanking letters still collapse
            }
            char digit = soundexDigit(ch);
            if (digit != '0' && digit != previous) {
                code.append(digit);
            }
            previous = digit; // a vowel ('0') resets, so same-coded letters it separates are both coded
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    /** The Soundex digit for a single upper-case letter; {@code '0'} for vowels and H/W (not coded). */
    private static char soundexDigit(char ch) {
        switch (ch) {
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
            .toString();
    }
}
