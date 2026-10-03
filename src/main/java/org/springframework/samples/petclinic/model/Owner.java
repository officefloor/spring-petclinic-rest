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
import java.time.DayOfWeek;
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
    @Pattern(regexp = "^\\+[0-9]{8,15}$", message = "Phone number must be in E.164 form (a '+' followed by 8 to 15 digits)")
    private String telephone;

    @Column(name = "email")
    private String email;

    @Column(name = "registration_date", columnDefinition = "DATE")
    private LocalDate registrationDate;

    @Column(name = "birth_date", columnDefinition = "DATE")
    private LocalDate birthDate;

    @Column(name = "customer_code")
    private String customerCode;

    @Column(name = "household_id")
    private String householdId;

    @Column(name = "namesake_count")
    private Integer namesakeCount;

    @Column(name = "household_size")
    private Integer householdSize;

    @Column(name = "postcode")
    private String postcode;

    @Column(name = "bulk_signup_warning")
    private Boolean bulkSignupWarning;

    @Column(name = "possible_duplicate")
    private Boolean possibleDuplicate;

    @Column(name = "possible_duplicate_of")
    private Integer possibleDuplicateOf;

    @Column(name = "membership_level")
    private Integer membershipLevel;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner", fetch = FetchType.EAGER)
    private Set<Pet> pets;

    /**
     * Fixed city-to-region table used to derive {@link #getLocality() locality} when the postcode
     * is absent or in no known range. Any city not listed derives the locality {@code 'UNKNOWN'}.
     */
    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW",
        "Melbourne", "VIC",
        "Brisbane", "QLD");

    /**
     * Inclusive 4-digit postcode range per region, used to derive {@link #getLocality() locality}
     * from the postcode in preference to the city: NSW 2000-2099, VIC 3000-3099, QLD 4000-4099.
     */
    private static final Map<String, int[]> REGION_POSTCODE = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /**
     * Fixed region-to-timezone table used to derive {@link #getTimezone() timezone} from the owner's
     * {@link #getLocality() locality}: NSW->Australia/Sydney, VIC->Australia/Melbourne,
     * QLD->Australia/Brisbane. A locality not listed derives a {@code null} timezone.
     */
    private static final Map<String, String> REGION_TIMEZONE = Map.of(
        "NSW", "Australia/Sydney",
        "VIC", "Australia/Melbourne",
        "QLD", "Australia/Brisbane");

    /**
     * A link to this owner's own resource, {@code '/api/owners/'} followed by the owner's
     * {@link #getId() id}. Returns {@code null} before the owner has been assigned an id.
     *
     * @return the self link, or {@code null} when the owner has no id yet
     */
    public String getSelfLink() {
        return this.getId() == null ? null : "/api/owners/" + this.getId();
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * The owner's salutation: the {@link #getTitle() title}, a single space and the
     * {@link #getLastName() last name} (e.g. {@code 'DR Who'}) when a title is present, or just the
     * last name when no title was supplied.
     *
     * @return the composed salutation
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
     * Known E.164 country codes whose national-number length is fixed, used to split the stored
     * telephone into its country code and national digits for display: Australia ({@code '61'}) and
     * the North American Numbering Plan ({@code '1'}). A number whose country code is not listed is
     * split by taking its leading digit as the (minimal) country code.
     */
    private static final Map<String, Integer> COUNTRY_NATIONAL_LENGTH = Map.of("61", 9, "1", 10);

    /**
     * The stored E.164 {@link #getTelephone() telephone} formatted for humans: the country code, a
     * single space, then the national digits grouped in threes from the left (e.g. the stored
     * {@code '+61412345678'} becomes {@code '+61 412 345 678'}). The raw telephone itself stays in
     * E.164 form. Returns the stored value unchanged when it is absent or not a {@code '+'}-prefixed
     * digit string.
     *
     * @return the human-readable telephone, or the stored value when it cannot be formatted
     */
    public String getTelephoneDisplay() {
        if (this.telephone == null || !this.telephone.matches("\\+[0-9]+")) {
            return this.telephone;
        }
        String digits = this.telephone.substring(1);
        String countryCode = extractCountryCode(digits);
        String national = digits.substring(countryCode.length());
        StringBuilder display = new StringBuilder("+").append(countryCode);
        for (int i = 0; i < national.length(); i += 3) {
            display.append(' ').append(national, i, Math.min(i + 3, national.length()));
        }
        return display.toString();
    }

    /**
     * Splits the country code off an E.164 digit string (country code followed by national digits,
     * without the leading {@code '+'}). A country code listed in {@link #COUNTRY_NATIONAL_LENGTH}
     * whose required national-number length matches the remaining digits is preferred; failing that,
     * the leading digit is used as the country code.
     *
     * @param digits the E.164 digits without the leading {@code '+'}
     * @return the country-code portion of {@code digits}
     */
    private static String extractCountryCode(String digits) {
        for (Map.Entry<String, Integer> entry : COUNTRY_NATIONAL_LENGTH.entrySet()) {
            String code = entry.getKey();
            if (digits.startsWith(code) && digits.length() - code.length() == entry.getValue()) {
                return code;
            }
        }
        return digits.substring(0, 1);
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
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
     * The owner's age band, derived from the {@link #getBirthDate() birth date} as at the owner's
     * {@link #getRegistrationDate() registration date}: {@code 'MINOR'} when under 18, {@code 'ADULT'}
     * when 18 to 64, and {@code 'SENIOR'} when 65 or older. The age is the number of whole years
     * elapsed from the birth date to the registration date (defaulting to the server's current date
     * when no registration date is set yet). Returns {@code null} when no birth date was supplied.
     *
     * @return the age band, or {@code null} when the birth date is absent
     */
    public String getAgeBand() {
        if (this.birthDate == null) {
            return null;
        }
        LocalDate asAt = this.registrationDate != null ? this.registrationDate : LocalDate.now();
        int years = Period.between(this.birthDate, asAt).getYears();
        if (years < 18) {
            return "MINOR";
        }
        if (years < 65) {
            return "ADULT";
        }
        return "SENIOR";
    }

    public String getCustomerCode() {
        return this.customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    /**
     * The owner's membership number, formatted {@code '<customerCode>-M<YY>'} where {@code YY} is
     * the last two digits of the {@link #getFiscalYear() fiscal year} of the registration date
     * (e.g. {@code 'NSW-1A2B3C4D-M26'}). Derived from the owner's own customer code and registration
     * date; {@code null} when either is absent.
     *
     * @return the membership number, or {@code null} when it cannot be derived
     */
    public String getMembershipNumber() {
        if (this.customerCode == null || this.registrationDate == null) {
            return null;
        }
        return String.format("%s-M%02d", this.customerCode, fiscalYearOf(this.registrationDate) % 100);
    }

    /**
     * The owner's fiscal year, formatted {@code 'FY<YY>'} where {@code YY} is the last two digits of
     * the fiscal year of the {@link #getRegistrationDate() business-day-adjusted registration date}.
     * The fiscal year starts on 1 July and is named by the calendar year in which it ends, so a
     * registration date on or after 1 July belongs to the following calendar year's fiscal year
     * (e.g. {@code 2026-07-01} is {@code 'FY27'}) while an earlier date stays in the current one
     * (e.g. {@code 2026-06-15} is {@code 'FY26'}). Returns {@code null} when the registration date is
     * absent.
     *
     * @return the fiscal year, or {@code null} when the registration date is absent
     */
    public String getFiscalYear() {
        if (this.registrationDate == null) {
            return null;
        }
        return String.format("FY%02d", fiscalYearOf(this.registrationDate) % 100);
    }

    /**
     * The fiscal year a date falls in, as a full calendar year. The fiscal year starts on 1 July and
     * is named by the calendar year in which it ends: a date in July through December belongs to the
     * next calendar year's fiscal year, while January through June stays in the current one.
     *
     * @param date the date to classify
     * @return the fiscal year as a four-digit calendar year
     */
    private static int fiscalYearOf(LocalDate date) {
        return date.getMonthValue() >= 7 ? date.getYear() + 1 : date.getYear();
    }

    /**
     * The owner's Luhn check digit, a single digit (0-9) computed over the digits contained in the
     * {@link #getCustomerCode() customer code}. Non-digit characters (such as the separators and the
     * letter prefixes) are ignored. Returns {@code null} when the customer code is absent.
     *
     * @return the Luhn check digit, or {@code null} when the customer code is absent
     */
    public Integer getCheckDigit() {
        if (this.customerCode == null) {
            return null;
        }
        int sum = 0;
        boolean dbl = true;
        for (int i = this.customerCode.length() - 1; i >= 0; i--) {
            char c = this.customerCode.charAt(i);
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
     * The owner's membership points, the score the {@link #getMembershipLevel() membership level} is
     * derived from. The score starts at 0 and gains: 2 when an email address is present, 1 when the
     * owner has no namesakes ({@code namesakeCount} is 0), 2 for a household of 3 or more members
     * ({@code householdSize} is at least 3), and 3 when the owner's tenure spans at least one elapsed
     * fiscal year (see {@link #getTenureFiscalYears()}). Because a newly created owner has zero
     * tenure, the tenure points are never awarded at create time.
     *
     * @return the membership points, a non-negative integer
     */
    public Integer getMembershipPoints() {
        int points = 0;
        boolean hasEmail = this.email != null && !this.email.isBlank();
        if (hasEmail) {
            points += 2;
        }
        boolean hasNoNamesakes = this.namesakeCount != null && this.namesakeCount == 0;
        if (hasNoNamesakes) {
            points += 1;
        }
        if (this.householdSize != null && this.householdSize >= 3) {
            points += 2;
        }
        Long tenureFiscalYears = getTenureFiscalYears();
        if (tenureFiscalYears != null && tenureFiscalYears >= 1) {
            points += 3;
        }
        return points;
    }

    /**
     * The owner's numeric membership level. This is normally derived from the
     * {@link #getMembershipPoints() membership points} (see {@link #deriveMembershipLevel()}), but a
     * household-level cap applied at create time may store a lower value here; when such a capped level
     * has been stored it is returned in preference to the derived one.
     *
     * @return the membership level, an integer from 1 to 4
     */
    public Integer getMembershipLevel() {
        if (this.membershipLevel != null) {
            return this.membershipLevel;
        }
        return deriveMembershipLevel();
    }

    public void setMembershipLevel(Integer membershipLevel) {
        this.membershipLevel = membershipLevel;
    }

    /**
     * The owner's numeric membership level derived purely from the {@link #getMembershipPoints()
     * membership points}: level 1 for 0-1 points, level 2 for 2-3 points, level 3 for 4-5 points, and
     * level 4 for 6 or more points. This is the uncapped level, before any household-level cap.
     *
     * @return the derived membership level, an integer from 1 to 4
     */
    public Integer deriveMembershipLevel() {
        int points = getMembershipPoints();
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
     * The owner's tenure in elapsed fiscal years, the number of fiscal-year boundaries crossed from
     * the {@link #getFiscalYear() fiscal year} of the {@link #getRegistrationDate() registration date}
     * to the fiscal year of the server's current date (the fiscal year starts on 1 July). An owner
     * registered in the current fiscal year has a tenure of 0; one elapsed fiscal year or more is what
     * awards the tenure membership points. Returns {@code null} when no registration date has been set
     * yet.
     *
     * @return the tenure in elapsed fiscal years, or {@code null} when the registration date is absent
     */
    public Long getTenureFiscalYears() {
        if (this.registrationDate == null) {
            return null;
        }
        return (long) (fiscalYearOf(LocalDate.now()) - fiscalYearOf(this.registrationDate));
    }

    /**
     * The owner's locality, the canonical region string. Once the owner has a
     * {@link #getCustomerCode() customer code} the locality is the {@code <REGION>} component of that
     * code (the text before the first {@code '-'}), so the locality and the identity share a single
     * source of truth. Before the customer code is assigned (for example while a create is still
     * validating the postcode) it falls back to the region the code will be built from: the
     * {@link #getPostcode() postcode} looked up by range first (NSW 2000-2099, VIC 3000-3099,
     * QLD 4000-4099), then the fixed city-to-region table (Sydney->NSW, Melbourne->VIC,
     * Brisbane->QLD), and finally {@code 'UNKNOWN'} when neither resolves to a region.
     *
     * @return the canonical region string, or {@code 'UNKNOWN'}
     */
    public String getLocality() {
        if (this.customerCode != null) {
            int dash = this.customerCode.indexOf('-');
            return dash >= 0 ? this.customerCode.substring(0, dash) : this.customerCode;
        }
        return deriveRegion();
    }

    /**
     * The owner's IANA timezone name, derived from the owner's {@link #getLocality() locality} via the
     * fixed region-to-timezone table: NSW->Australia/Sydney, VIC->Australia/Melbourne,
     * QLD->Australia/Brisbane. Returns {@code null} when the locality is not one of these regions (for
     * example the {@code 'UNKNOWN'} locality of a city with no known region).
     *
     * @return the IANA timezone name, or {@code null} when the locality has no mapped timezone
     */
    public String getTimezone() {
        return REGION_TIMEZONE.get(getLocality());
    }

    /**
     * The region the customer code's {@code <REGION>} component is built from: the postcode looked up
     * by range first (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099), falling back to the fixed
     * city-to-region table (Sydney->NSW, Melbourne->VIC, Brisbane->QLD) when the postcode is absent or
     * in no known range, and {@code 'UNKNOWN'} when neither resolves. Preferring the postcode returns
     * the same region for known cities but disambiguates cities that share a name.
     *
     * @return the canonical region string, or {@code 'UNKNOWN'}
     */
    private String deriveRegion() {
        String region = regionFromPostcode();
        if (region != null) {
            return region;
        }
        return CITY_REGION.getOrDefault(this.city, "UNKNOWN");
    }

    /**
     * Builds this owner's customer code, formatted {@code '<REGION>-<HASH8>'}. {@code REGION} is the
     * region derived from the postcode (falling back to the city, then {@code 'UNKNOWN'}; see
     * {@link #deriveRegion()}) and {@code HASH8} is the first 8 upper-case hexadecimal characters of
     * the SHA-256 digest of the owner's normalized telephone concatenated with its last name (e.g.
     * {@code 'NSW-1A2B3C4D'}). The code carries no sequence number, so it is a pure function of the
     * owner's region, telephone and last name. Call this once at create time and store the result via
     * {@link #setCustomerCode(String)}, which thereafter backs {@link #getLocality()},
     * {@link #getMembershipNumber()} and {@link #getCheckDigit()}.
     *
     * @return the assembled customer code
     */
    public String buildCustomerCode() {
        return deriveRegion() + "-" + sha256Hex8(this.telephone + this.getLastName());
    }

    /**
     * Computes the first 8 upper-case hexadecimal characters (the first 4 bytes) of the SHA-256 digest
     * of the given value's UTF-8 bytes, the {@code HASH8} component of the customer code.
     *
     * @param value the value to hash
     * @return the 8-character upper-case hex prefix of the SHA-256 digest
     */
    private static String sha256Hex8(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(8);
            for (int i = 0; i < 4; i++) {
                hex.append(String.format("%02X", digest[i]));
            }
            return hex.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * The region whose range contains this owner's postcode, or {@code null} when the postcode is
     * absent, not a 4-digit number, or in no known range.
     *
     * @return the matching region, or {@code null}
     */
    private String regionFromPostcode() {
        if (this.postcode == null) {
            return null;
        }
        int value;
        try {
            value = Integer.parseInt(this.postcode.trim());
        }
        catch (NumberFormatException ex) {
            return null;
        }
        for (Map.Entry<String, int[]> entry : REGION_POSTCODE.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * The owner's preferred contact channel, derived from the owner's details. Returns
     * {@code 'EMAIL'} when an email address is present, otherwise {@code 'PHONE'}.
     *
     * @return {@code 'EMAIL'} when an email address is present, otherwise {@code 'PHONE'}
     */
    public String getContactPreference() {
        boolean hasEmail = this.email != null && !this.email.isBlank();
        return hasEmail ? "EMAIL" : "PHONE";
    }

    /**
     * The owner's identity key, the single derived value used for duplicate detection. It is the
     * owner's normalized telephone, email (empty when absent) and household id joined with
     * {@code '|'} ({@code '<telephone>|<email>|<householdId>'}); any absent ({@code null}) component
     * contributes an empty string. Two owners are duplicates exactly when their whole identity keys
     * are equal, so two members of the same household with different telephones differ here and are
     * both allowed.
     *
     * @return the derived identity key
     */
    public String getIdentityKey() {
        return (this.telephone == null ? "" : this.telephone) + "|"
            + (this.email == null ? "" : this.email) + "|"
            + (this.householdId == null ? "" : this.householdId);
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

    public Integer getHouseholdSize() {
        return this.householdSize;
    }

    public void setHouseholdSize(Integer householdSize) {
        this.householdSize = householdSize;
    }

    public String getPostcode() {
        return this.postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public Boolean getBulkSignupWarning() {
        return this.bulkSignupWarning;
    }

    public void setBulkSignupWarning(Boolean bulkSignupWarning) {
        this.bulkSignupWarning = bulkSignupWarning;
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

    /**
     * Whether this owner has been soft-deleted. A newly created owner is not deleted
     * ({@code false}); the DELETE endpoint marks the owner deleted ({@code true}) while retaining the
     * row, and the create endpoint's duplicate and identity checks ignore owners flagged deleted.
     *
     * @return whether the owner has been soft-deleted
     */
    public boolean isDeleted() {
        return this.deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    /**
     * Stamps the effective registration date exactly once on insert, so every newly persisted owner
     * carries a resolved, business-day registration date (see {@link #resolveRegistrationDate}).
     */
    @PrePersist
    void onCreate() {
        this.registrationDate = resolveRegistrationDate(this.registrationDate);
    }

    /**
     * Resolves the effective registration date and rolls it onto a business day. An absent
     * ({@code null}) value defaults to the server's current date; a supplied value is used as-is.
     * The resulting date is then rolled forward so it never falls on a weekend or a listed public
     * holiday: starting from the effective date, the date advances one day at a time while it lands
     * on a Saturday, a Sunday, or a date in {@link #PUBLIC_HOLIDAYS}, stopping on the first
     * non-holiday business day. A date already on a business day is returned unchanged.
     *
     * <p>This is the single definition of both the defaulting and the business-day roll, used
     * wherever the effective registration date is needed - by the {@code @PrePersist} hook that
     * stamps the owner and by the create endpoint that counts owners per business day.
     *
     * @param supplied the registration date from the request, or {@code null} when none was supplied
     * @return the effective registration date, guaranteed to fall on a non-holiday business day
     */
    public static LocalDate resolveRegistrationDate(LocalDate supplied) {
        LocalDate effective = supplied != null ? supplied : LocalDate.now();
        while (!isBusinessDay(effective)) {
            effective = effective.plusDays(1);
        }
        return effective;
    }

    /**
     * The fixed list of public holidays that the registration date rolls past, alongside weekends.
     */
    private static final Set<LocalDate> PUBLIC_HOLIDAYS = Set.of(
        LocalDate.of(2026, 1, 1),
        LocalDate.of(2026, 1, 26),
        LocalDate.of(2026, 4, 25),
        LocalDate.of(2026, 12, 25),
        LocalDate.of(2026, 12, 28));

    /**
     * A business day is any date that is neither a weekend day nor a listed public holiday.
     */
    private static boolean isBusinessDay(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY && !PUBLIC_HOLIDAYS.contains(date);
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
