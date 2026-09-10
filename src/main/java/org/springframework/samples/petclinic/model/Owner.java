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

    @Column(name = "customer_code")
    private String customerCode;

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

    public String getCustomerCode() {
        return this.customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
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
     * single value: the normalized E.164 {@code telephone}, the lower-cased {@code email}
     * (empty when none) and the {@code householdId} (empty when the owner shares no household),
     * joined by {@code '|'}. Two owners are duplicates exactly when their whole identity keys are
     * equal, so a difference in any of the three parts (for example two household mates with
     * different telephones) yields distinct keys. A {@code null} part contributes the empty string.
     *
     * @param telephone the normalized E.164 telephone
     * @param email the lower-cased email, or {@code null} when none
     * @param householdId the shared household identifier, or {@code null} when none
     * @return the derived identity key
     */
    public static String identityKey(String telephone, String email, String householdId) {
        return (telephone == null ? "" : telephone) + "|"
            + (email == null ? "" : email) + "|"
            + (householdId == null ? "" : householdId);
    }

    /**
     * Returns this owner's derived identity key, see
     * {@link #identityKey(String, String, String)}.
     *
     * @return the derived identity key for this owner
     */
    @Transient
    public String getIdentityKey() {
        return identityKey(this.telephone, this.email, this.householdId);
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
     * Returns this owner's fiscal year formatted as {@code 'FY<YY>'}, where YY is the last two digits
     * of the fiscal year (starting 1 July, see {@link #fiscalYearOf(LocalDate)}) the owner's
     * business-day-adjusted {@code registrationDate} falls in, e.g. {@code 'FY26'}. Returns
     * {@code null} when the owner has no registration date, so the fiscal year cannot be derived.
     * This is the single definition of an owner's fiscal year, shared by every value derived on a
     * fiscal-year basis (see {@link #getMembershipNumber()}).
     *
     * @return the owner's fiscal year, or {@code null} when it cannot be derived
     */
    @Transient
    public String getFiscalYear() {
        if (this.registrationDate == null) {
            return null;
        }
        return String.format("FY%02d", fiscalYearOf(this.registrationDate) % 100);
    }

    /**
     * Returns this owner's membership number formatted as {@code '<customerCode>-M<YY>'}, where YY is
     * the last two digits of the fiscal year (see {@link #fiscalYearOf(LocalDate)}) the owner's
     * business-day-adjusted {@code registrationDate} falls in, e.g. {@code 'NSW-A1B2C3D4-M26'}.
     * Returns {@code null} when the owner has no customer code or registration date.
     *
     * @return the owner's membership number, or {@code null} when it cannot be derived
     */
    @Transient
    public String getMembershipNumber() {
        if (this.customerCode == null || this.registrationDate == null) {
            return null;
        }
        return String.format("%s-M%02d", this.customerCode, fiscalYearOf(this.registrationDate) % 100);
    }

    /**
     * Returns this owner's check digit: a single Luhn check digit (0-9) over the digits contained in
     * the owner's customerCode. Returns {@code null} when the owner has no customer code.
     *
     * @return the owner's check digit, or {@code null} when it has no customer code
     */
    @Transient
    public Integer getCheckDigit() {
        if (this.customerCode == null) {
            return null;
        }
        String code = this.customerCode;
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
