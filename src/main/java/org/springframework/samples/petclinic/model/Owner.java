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

    @Column(name = "telephone")
    @NotEmpty
    @Pattern(regexp = "^\\+[0-9]{8,15}$", message = "Phone number must be in E.164 format")
    private String telephone;

    @Column(name = "email")
    private String email;

    @Column(name = "title")
    private String title;

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

    @Column(name = "bulk_signup_warning")
    private Boolean bulkSignupWarning;

    @Column(name = "postcode")
    private String postcode;

    @Column(name = "possible_duplicate")
    private Boolean possibleDuplicate;

    @Column(name = "possible_duplicate_of")
    private Integer possibleDuplicateOf;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

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
     * The owner's salutation for display: the {@link #title} followed by a single space
     * and the last name (e.g. {@code "DR Who"}), or just the last name when no title is
     * present. Derived from the owner's own fields.
     */
    @Transient
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

    public LocalDate getBirthDate() {
        return this.birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    /**
     * The owner's age band derived from {@link #birthDate}, computed against the
     * {@link #registrationDate}: {@code "MINOR"} when under 18, {@code "ADULT"} when
     * 18 to 64 inclusive, {@code "SENIOR"} when 65 or over. Null when either the birth
     * date or the registration date is absent.
     */
    @Transient
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

    public Integer getHouseholdSize() {
        return this.householdSize;
    }

    public void setHouseholdSize(Integer householdSize) {
        this.householdSize = householdSize;
    }

    public Boolean getBulkSignupWarning() {
        return this.bulkSignupWarning;
    }

    public void setBulkSignupWarning(Boolean bulkSignupWarning) {
        this.bulkSignupWarning = bulkSignupWarning;
    }

    public String getPostcode() {
        return this.postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
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

    public Boolean getDeleted() {
        return this.deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    /**
     * The owner's name formatted for display as {@code "LastName, FirstName"}.
     */
    @Transient
    public String getDisplayName() {
        return this.getLastName() + ", " + this.getFirstName();
    }

    /**
     * The owner's initials: the upper-cased first letters of firstName and
     * lastName, dot-separated with a trailing dot (e.g. {@code "J.S."}).
     */
    @Transient
    public String getInitials() {
        return Character.toUpperCase(this.getFirstName().charAt(0)) + "."
            + Character.toUpperCase(this.getLastName().charAt(0)) + ".";
    }

    /**
     * The fiscal year (starting 1 July) that the registration date falls in, as a
     * number: a date on or after 1 July belongs to the fiscal year of its own calendar
     * year, an earlier date belongs to the previous calendar year's fiscal year. Single
     * source of truth for the fiscal year, so {@link #getFiscalYear()}, the
     * {@link #getMembershipNumber() membership number} year segment and the tenure count
     * in {@link #getMembershipPoints()} cannot drift apart.
     */
    private static int fiscalYearOf(LocalDate date) {
        int year = date.getYear();
        return date.getMonthValue() >= Month.JULY.getValue() ? year : year - 1;
    }

    /**
     * The owner's fiscal year, formatted {@code "FY<YY>"} where YY is the last two
     * digits of the fiscal year (starting 1 July) of the business-day-adjusted
     * {@link #registrationDate} (e.g. {@code "FY26"} for a date in July 2026 to
     * June 2027). Derived from the owner's own fields; null when the registration date
     * is absent.
     */
    @Transient
    public String getFiscalYear() {
        if (this.registrationDate == null) {
            return null;
        }
        return String.format("FY%02d", fiscalYearOf(this.registrationDate) % 100);
    }

    /**
     * The owner's membership number, formatted {@code "<customerCode>-M<YY>"} where
     * YY is the last two digits of the registration date's fiscal year (starting
     * 1 July; e.g. {@code "NSW-A1B2C3D4-M26"} for a date in July 2026 to June 2027).
     * Derived from the owner's own fields; null when either the customer code or
     * registration date is absent.
     */
    @Transient
    public String getMembershipNumber() {
        if (this.customerCode == null || this.registrationDate == null) {
            return null;
        }
        return String.format("%s-M%02d", this.customerCode, fiscalYearOf(this.registrationDate) % 100);
    }

    /**
     * The owner's check digit: a single Luhn check digit (0-9) computed over the digits
     * contained in the {@code customerCode}, scanning right to left and doubling the
     * rightmost digit first; non-digit characters are ignored. Derived from the owner's
     * own fields; null when the customer code is absent.
     */
    @Transient
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
     * The owner's membership points: a running score built from the owner's own fields.
     * Starts at 0 and gains 2 when an email address is present, a further 1 when the owner
     * has no namesakes ({@code namesakeCount} is 0), a further 2 when it belongs to a
     * household of 3 or more members ({@code householdSize} is 3 or more), and a further 3
     * for tenure of more than one elapsed fiscal year (the fiscal years, starting 1 July,
     * from {@link #registrationDate} to the current date). A newly created owner has zero
     * tenure, so the pre-tenure factors alone top out at 5 points. Derived from the owner's
     * own fields.
     */
    @Transient
    public Integer getMembershipPoints() {
        int points = 0;
        boolean hasEmail = this.email != null && !this.email.isBlank();
        if (hasEmail) {
            points += 2;
        }
        boolean unique = this.namesakeCount != null && this.namesakeCount == 0;
        if (unique) {
            points += 1;
        }
        boolean largeHousehold = this.householdSize != null && this.householdSize >= 3;
        if (largeHousehold) {
            points += 2;
        }
        boolean tenured = this.registrationDate != null
            && (fiscalYearOf(LocalDate.now()) - fiscalYearOf(this.registrationDate)) > 1;
        if (tenured) {
            points += 3;
        }
        return points;
    }

    /**
     * The owner's membership level: a number from 1 to 4 derived by banding
     * {@link #getMembershipPoints()} — 1 for 0-1 points, 2 for 2-3, 3 for 4-5 and 4 for 6
     * or more. Derived from the owner's own fields.
     */
    @Transient
    public Integer getMembershipLevel() {
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
     * Fixed city-to-region lookup table. Any city not listed derives locality
     * {@code "UNKNOWN"}.
     */
    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW",
        "Melbourne", "VIC",
        "Brisbane", "QLD");

    /**
     * Region -> inclusive 4-digit postcode range {low, high}: NSW 2000-2099,
     * VIC 3000-3099, QLD 4000-4099. The single source of truth for the
     * postcode-to-region mapping, consulted by {@link #regionForPostcode(String)}
     * (used by locality derivation) and {@link #hasPostcodeRange(String)} (used by
     * postcode validation).
     */
    private static final Map<String, int[]> REGION_POSTCODE_RANGES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /**
     * Fixed region-to-timezone lookup table, mapping each region to its IANA timezone
     * name: NSW->Australia/Sydney, VIC->Australia/Melbourne, QLD->Australia/Brisbane.
     * Any region not listed (including {@code "UNKNOWN"}) has no timezone.
     */
    private static final Map<String, String> REGION_TIMEZONE = Map.of(
        "NSW", "Australia/Sydney",
        "VIC", "Australia/Melbourne",
        "QLD", "Australia/Brisbane");

    /**
     * The owner's locality: the REGION component of the owner's region-and-hash identity.
     * Read off the {@code customerCode} prefix (the part before the first {@code '-'}) when
     * a customer code is present, otherwise derived straight from the postcode via
     * {@link #identityRegion(String)}. Either way this yields the region the customer code
     * was built from — NSW/VIC/QLD, or {@code "UNKNOWN"} when the postcode is absent or in
     * no known range. Derived from the owner's own fields.
     */
    @Transient
    public String getLocality() {
        if (this.customerCode != null) {
            int dash = this.customerCode.indexOf('-');
            if (dash > 0) {
                return this.customerCode.substring(0, dash);
            }
        }
        return identityRegion(this.postcode);
    }

    /**
     * The REGION component of an owner's region-and-hash identity: the region derived from
     * the postcode via {@link #regionForPostcode(String)} (NSW/VIC/QLD), or
     * {@code "UNKNOWN"} when the postcode is absent or in no known range. The single source
     * of truth for the identity's region, so the {@code customerCode} prefix and the
     * {@link #getLocality() locality} cannot drift apart.
     */
    public static String identityRegion(String postcode) {
        String region = regionForPostcode(postcode);
        return region != null ? region : "UNKNOWN";
    }

    /**
     * The canonical region for a city per the fixed city-to-region table
     * ({@code Sydney->NSW}, {@code Melbourne->VIC}, {@code Brisbane->QLD}), or
     * {@code "UNKNOWN"} when the city is not in the table. The single source of truth
     * for the city-to-region mapping, used as the city's region in postcode validation.
     */
    public static String regionForCity(String city) {
        return CITY_REGION.getOrDefault(city, "UNKNOWN");
    }

    /**
     * The canonical region for a 4-digit postcode per the fixed range table
     * (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099), or {@code null} when the postcode
     * is absent, not a well-formed integer, or in no known range. Reads the shared
     * {@link #REGION_POSTCODE_RANGES} table; used by {@link #identityRegion(String)}
     * for the identity's region and by postcode validation to range-check a submitted
     * postcode against its city's region.
     */
    public static String regionForPostcode(String postcode) {
        if (postcode == null) {
            return null;
        }
        int value;
        try {
            value = Integer.parseInt(postcode.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
        for (Map.Entry<String, int[]> entry : REGION_POSTCODE_RANGES.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Whether the region is subject to postcode range validation, i.e. has a known
     * range in the shared {@link #REGION_POSTCODE_RANGES} table. A region with no range
     * (including {@code "UNKNOWN"}) accepts any well-formed postcode.
     */
    public static boolean hasPostcodeRange(String region) {
        return REGION_POSTCODE_RANGES.containsKey(region);
    }

    /**
     * The owner's IANA timezone, derived from the owner's {@link #getLocality() locality}
     * (its region) via the fixed region-to-timezone table ({@code NSW->Australia/Sydney},
     * {@code VIC->Australia/Melbourne}, {@code QLD->Australia/Brisbane}). Null when the
     * region is not one of these (e.g. {@code "UNKNOWN"}). Derived from the owner's own
     * fields.
     */
    @Transient
    public String getTimezone() {
        return REGION_TIMEZONE.get(getLocality());
    }

    /**
     * The owner's preferred contact channel: {@code "EMAIL"} when an email address is
     * present, otherwise {@code "PHONE"}. Derived from the owner's own fields.
     */
    @Transient
    public String getContactPreference() {
        boolean hasEmail = this.email != null && !this.email.isBlank();
        return hasEmail ? "EMAIL" : "PHONE";
    }

    /**
     * The owner's identity key: a display-only composite value formed as
     * {@code normalizedTelephone + "|" + (email or empty) + "|" + (householdId or empty)}.
     * The telephone and email are already stored in their normalized forms (E.164 and
     * lower-cased), and the household id is derived from the last name and postcode (empty
     * for an owner with no postcode). It drives no logic — duplicate detection is keyed on
     * the household (same last name and postcode); this value is exposed on the API purely
     * for reference. A null or blank part contributes an empty segment. Derived from the
     * owner's own fields.
     */
    @Transient
    public String getIdentityKey() {
        return keyPart(this.telephone) + "|" + keyPart(this.email) + "|" + keyPart(this.householdId);
    }

    private static String keyPart(String value) {
        return (value == null || value.isBlank()) ? "" : value;
    }

    /**
     * The stored E.164 {@link #telephone} formatted for humans: the country code,
     * a space, then the national digits grouped in threes (e.g.
     * {@code "+61 412 345 678"}). The {@code '+'} and country code are taken from the
     * E.164 value (a {@code '+1'} NANP country code is a single digit, any other is
     * treated as two digits, matching the recognized {@code '+61'}/{@code '+1'} forms),
     * and the national digits follow in space-separated groups of three. Returns the raw
     * value unchanged when it is absent or not a {@code '+'}-prefixed run of digits.
     * Derived from the owner's own fields; the raw {@link #telephone} stays E.164.
     */
    @Transient
    public String getTelephoneDisplay() {
        if (this.telephone == null || !this.telephone.startsWith("+")) {
            return this.telephone;
        }
        String digits = this.telephone.substring(1);
        if (!digits.matches("\\d+")) {
            return this.telephone;
        }
        int countryCodeLength = digits.startsWith("1") ? 1 : 2;
        if (digits.length() <= countryCodeLength) {
            return this.telephone;
        }
        String countryCode = digits.substring(0, countryCodeLength);
        String national = digits.substring(countryCodeLength);
        StringBuilder grouped = new StringBuilder();
        for (int i = 0; i < national.length(); i += 3) {
            if (grouped.length() > 0) {
                grouped.append(' ');
            }
            grouped.append(national, i, Math.min(i + 3, national.length()));
        }
        return "+" + countryCode + " " + grouped;
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
