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

    @Column(name = "postcode")
    @Pattern(regexp = "^[0-9]{4}$", message = "Postcode must be 4 digits")
    private String postcode;

    @Column(name = "telephone")
    @NotEmpty
    @Pattern(regexp = "^\\+[0-9]{8,15}$", message = "Phone number must be in E.164 form")
    private String telephone;

    @Column(name = "email")
    private String email;

    @Column(name = "registration_date")
    private LocalDate registrationDate;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "customer_code")
    private String customerCode;

    @Column(name = "household_id")
    private String householdId;

    @Column(name = "identity_key")
    private String identityKey;

    @Column(name = "namesake_count")
    private Integer namesakeCount;

    @Column(name = "household_member_count")
    private Integer householdMemberCount;

    @Column(name = "membership_number")
    private String membershipNumber;

    @Column(name = "membership_level_cap")
    private Integer membershipLevelCap;

    @Column(name = "possible_duplicate")
    private Boolean possibleDuplicate;

    @Column(name = "possible_duplicate_of")
    private Integer possibleDuplicateOf;

    @Column(name = "deleted", nullable = false)
    private boolean deleted;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner", fetch = FetchType.EAGER)
    private Set<Pet> pets;

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * The owner's salutation, derived on read: the {@link #getTitle() title} and last name separated
     * by a single space (e.g. {@code "DR who"}) when a title is present, or just the last name when no
     * title was supplied.
     */
    @Transient
    public String getSalutation() {
        if (this.title != null && !this.title.isBlank()) {
            return this.title + " " + getLastName();
        }
        return getLastName();
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

    public String getIdentityKey() {
        return this.identityKey;
    }

    public void setIdentityKey(String identityKey) {
        this.identityKey = identityKey;
    }

    public Integer getNamesakeCount() {
        return this.namesakeCount;
    }

    public void setNamesakeCount(Integer namesakeCount) {
        this.namesakeCount = namesakeCount;
    }

    public Integer getHouseholdMemberCount() {
        return this.householdMemberCount;
    }

    public void setHouseholdMemberCount(Integer householdMemberCount) {
        this.householdMemberCount = householdMemberCount;
    }

    public String getMembershipNumber() {
        return this.membershipNumber;
    }

    public void setMembershipNumber(String membershipNumber) {
        this.membershipNumber = membershipNumber;
    }

    /**
     * This owner's membership level ceiling: the derived {@link #getMembershipLevel() membership level}
     * is never reported above it. Assigned once when the owner is registered and stored; {@code null}
     * when no ceiling applies.
     */
    public Integer getMembershipLevelCap() {
        return this.membershipLevelCap;
    }

    public void setMembershipLevelCap(Integer membershipLevelCap) {
        this.membershipLevelCap = membershipLevelCap;
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
     * Whether this owner has been soft-deleted. A newly created owner is not deleted; the delete
     * endpoint flags this true and retains the row rather than removing it. Deleted owners are
     * ignored by the create endpoint's duplicate and identity checks.
     */
    public boolean isDeleted() {
        return this.deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    /**
     * The owner's membership points, derived on read: they start at 0, add 2 when an email is
     * present, add 1 when {@link #namesakeCount} is 0, add 2 for a household of 3 or more
     * ({@link #householdMemberCount}), and add 3 for tenure over one fiscal year (elapsed fiscal
     * years from {@link #registrationDate} to today; see {@link #hasTenureBonus(LocalDate)}).
     */
    @Transient
    public Integer getMembershipPoints() {
        return membershipPoints(this.email, this.namesakeCount, this.householdMemberCount,
            this.registrationDate);
    }

    /**
     * The owner's numeric membership level, derived on read from {@link #getMembershipPoints()}:
     * level 1 for 0-1 points, 2 for 2-3, 3 for 4-5, and 4 for 6 or more; then held down to the owner's
     * {@link #getMembershipLevelCap() membership level cap} when one has been assigned.
     */
    @Transient
    public Integer getMembershipLevel() {
        return capMembershipLevel(membershipLevel(getMembershipPoints()), this.membershipLevelCap);
    }

    /** The membership points derived from the given factors: they start at 0, add 2 when
     *  {@code email} is present, add 1 when {@code namesakeCount} is 0, add 2 for a household of 3 or
     *  more ({@code householdMemberCount}), and add 3 for sufficient tenure (see
     *  {@link #hasTenureBonus(LocalDate)}). */
    private static int membershipPoints(String email, Integer namesakeCount,
            Integer householdMemberCount, LocalDate registrationDate) {
        int points = 0;
        if (email != null && !email.isBlank()) {
            points += 2;
        }
        if (namesakeCount != null && namesakeCount == 0) {
            points += 1;
        }
        if (householdMemberCount != null && householdMemberCount >= 3) {
            points += 2;
        }
        if (hasTenureBonus(registrationDate)) {
            points += 3;
        }
        return points;
    }

    /** Whether the owner's tenure earns the membership tenure bonus: the elapsed fiscal years from
     *  {@code registrationDate} to today exceed one, i.e. the registration falls two or more fiscal
     *  years before the current one (fiscal years start 1 July). A null registration date earns no
     *  bonus. */
    private static boolean hasTenureBonus(LocalDate registrationDate) {
        return registrationDate != null
            && fiscalYear(LocalDate.now()) - fiscalYear(registrationDate) > 1;
    }

    /**
     * The fiscal year (starting 1 July) that contains {@code date}, identified by the calendar year
     * in which that fiscal year ends: a date in July-December belongs to the fiscal year ending the
     * following calendar year, and a date in January-June to the fiscal year ending that same year
     * (e.g. 2026-09-05 -> 2027, 2026-03-01 -> 2026).
     */
    public static int fiscalYear(LocalDate date) {
        return date.getMonthValue() >= 7 ? date.getYear() + 1 : date.getYear();
    }

    /**
     * The owner's fiscal year, derived on read from the (business-day-adjusted)
     * {@link #registrationDate} as {@code "FY<YY>"}, where YY is the last two digits of the fiscal
     * year (starting 1 July) that contains the registration date (e.g. a date of 2026-09-05 yields
     * {@code "FY27"}). Returns {@code null} when no registration date is available.
     */
    @Transient
    public String getFiscalYear() {
        if (this.registrationDate == null) {
            return null;
        }
        return String.format("FY%02d", fiscalYear(this.registrationDate) % 100);
    }

    /** The numeric membership level (1-4) mapped from membership points: 1 for 0-1 points, 2 for
     *  2-3, 3 for 4-5, and 4 for 6 or more. */
    private static int membershipLevel(int points) {
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

    /** The membership {@code level} held down to {@code cap}, i.e. the smaller of the two, when a cap
     *  is present; the level unchanged when {@code cap} is {@code null} and no ceiling applies. */
    private static int capMembershipLevel(int level, Integer cap) {
        return cap == null ? level : Math.min(level, cap);
    }

    /**
     * The owner's preferred contact channel, derived on read: {@code "EMAIL"} when an email is
     * present, otherwise {@code "PHONE"}.
     */
    @Transient
    public String getContactPreference() {
        return (this.email != null && !this.email.isBlank()) ? "EMAIL" : "PHONE";
    }

    /**
     * The owner's age band, derived on read from the {@link #birthDate} relative to the
     * {@link #registrationDate}: {@code "MINOR"} when under 18, {@code "ADULT"} from 18 to 64, and
     * {@code "SENIOR"} at 65 or over. Returns {@code null} when no birth date (or no registration
     * date to measure against) is available.
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

    /**
     * The single Luhn check digit (0-9) computed over the decimal digits contained in the
     * {@link #customerCode}, derived on read.
     */
    @Transient
    public Integer getCheckDigit() {
        return luhnCheckDigit(this.customerCode);
    }

    /** Region -> inclusive 4-digit postcode range {low, high}, used to derive the region from the
     *  postcode ({@code NSW 2000-2099}, {@code VIC 3000-3099}, {@code QLD 4000-4099}). */
    private static final Map<String, int[]> REGION_POSTCODE_RANGES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /**
     * The owner's region, derived on read. The postcode is preferred: a 4-digit postcode falling in a
     * known region's range ({@code NSW 2000-2099}, {@code VIC 3000-3099}, {@code QLD 4000-4099}) yields
     * that region. When the postcode is absent or in no known range, it falls back to the fixed
     * city-to-region table ({@code Sydney -> NSW}, {@code Melbourne -> VIC}, {@code Brisbane -> QLD}).
     * Returns {@code "UNKNOWN"} when neither source resolves a region.
     */
    @Transient
    public String getRegion() {
        String byPostcode = regionForPostcode(this.postcode);
        if (byPostcode != null) {
            return byPostcode;
        }
        if ("Sydney".equals(this.city)) {
            return "NSW";
        }
        if ("Melbourne".equals(this.city)) {
            return "VIC";
        }
        if ("Brisbane".equals(this.city)) {
            return "QLD";
        }
        return "UNKNOWN";
    }

    /**
     * The owner's locality, derived on read from the region-and-hash {@link #getCustomerCode()
     * customer code}: the region segment before the first '-' of the customer code
     * ('<REGION>-<HASH8>'). Falls back to the owner's {@link #getRegion() region} when no customer
     * code has been assigned yet.
     */
    @Transient
    public String getLocality() {
        if (this.customerCode != null) {
            int dash = this.customerCode.indexOf('-');
            if (dash > 0) {
                return this.customerCode.substring(0, dash);
            }
        }
        return getRegion();
    }

    /** Region -> IANA timezone name, the fixed region-to-timezone table ({@code NSW ->
     *  Australia/Sydney}, {@code VIC -> Australia/Melbourne}, {@code QLD -> Australia/Brisbane}). */
    private static final Map<String, String> REGION_TIMEZONES = Map.of(
        "NSW", "Australia/Sydney",
        "VIC", "Australia/Melbourne",
        "QLD", "Australia/Brisbane");

    /**
     * The owner's timezone, derived on read as the IANA name for the owner's
     * {@link #getLocality() locality/region} via the fixed region-to-timezone table
     * ({@code NSW -> Australia/Sydney}, {@code VIC -> Australia/Melbourne},
     * {@code QLD -> Australia/Brisbane}). Returns {@code null} when the region carries no entry in
     * the table (e.g. {@code UNKNOWN}).
     */
    @Transient
    public String getTimezone() {
        return timezoneForRegion(getLocality());
    }

    /** The IANA timezone name for {@code region} via the fixed region-to-timezone table, or
     *  {@code null} when the region carries no entry ({@code NSW -> Australia/Sydney},
     *  {@code VIC -> Australia/Melbourne}, {@code QLD -> Australia/Brisbane}). */
    public static String timezoneForRegion(String region) {
        return REGION_TIMEZONES.get(region);
    }

    /** The inclusive 4-digit postcode {@code {low, high}} range for {@code region}, or {@code null} when
     *  the region carries no range rule (i.e. {@code UNKNOWN} or any region absent from the table:
     *  {@code NSW 2000-2099}, {@code VIC 3000-3099}, {@code QLD 4000-4099}). */
    public static int[] postcodeRangeForRegion(String region) {
        return REGION_POSTCODE_RANGES.get(region);
    }

    /** The region whose postcode range contains {@code postcode}, or {@code null} when the postcode is
     *  absent, not a 4-digit value, or in no known range. */
    private static String regionForPostcode(String postcode) {
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

    /** Telephone country code -> the national-number length it requires ({@code +61} Australia => 9
     *  digits, {@code +1} NANP => 10), used to derive telephone values from the stored E.164 number.
     *  The country codes here carry no conflicting prefixes ({@code +61} does not start with
     *  {@code +1}), so a number matches at most one. */
    private static final Map<String, Integer> NATIONAL_NUMBER_LENGTHS = Map.of(
        "+61", 9, "+1", 10);

    /**
     * The stored E.164 {@link #telephone} formatted for humans, derived on read: the country code, a
     * space, then the national digits grouped in threes (e.g. {@code "+61 412 345 678"}). Falls back
     * to the raw value when it is {@code null} or begins with no recognised country code.
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
        String national = this.telephone.substring(countryCode.length());
        StringBuilder grouped = new StringBuilder(countryCode);
        for (int i = 0; i < national.length(); i++) {
            if (i % 3 == 0) {
                grouped.append(' ');
            }
            grouped.append(national.charAt(i));
        }
        return grouped.toString();
    }

    /** The recognised telephone country code that {@code telephone} begins with (e.g. {@code "+61"}),
     *  or {@code null} when it is {@code null} or begins with no recognised country code. */
    public static String countryCodeOf(String telephone) {
        if (telephone == null) {
            return null;
        }
        for (String countryCode : NATIONAL_NUMBER_LENGTHS.keySet()) {
            if (telephone.startsWith(countryCode)) {
                return countryCode;
            }
        }
        return null;
    }

    /** The national-number length required for {@code countryCode}, or {@code null} when the country
     *  code carries no length rule (i.e. any code absent from the table: {@code +61 => 9},
     *  {@code +1 => 10}). */
    public static Integer nationalNumberLength(String countryCode) {
        return NATIONAL_NUMBER_LENGTHS.get(countryCode);
    }

    /** The single Luhn check digit (0-9) over the decimal digits contained in {@code value}, scanning
     *  right to left and doubling every second digit; non-digit characters are ignored and a null or
     *  digit-free value yields {@code 0}. */
    private static int luhnCheckDigit(String value) {
        int sum = 0;
        boolean doubling = true;
        for (int i = (value == null ? 0 : value.length()) - 1; i >= 0; i--) {
            char c = value.charAt(i);
            if (c < '0' || c > '9') {
                continue;
            }
            int digit = c - '0';
            if (doubling) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            doubling = !doubling;
        }
        return (10 - (sum % 10)) % 10;
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
