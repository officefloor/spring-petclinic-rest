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
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
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
    private String postcode;

    @Column(name = "telephone")
    @NotEmpty
    @Pattern(regexp = "^\\+[0-9]{8,15}$", message = "Phone number must be in E.164 form: a '+' followed by 8 to 15 digits")
    private String telephone;

    @Column(name = "email")
    @Email
    private String email;

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

    @Column(name = "bulk_signup_warning")
    private Boolean bulkSignupWarning;

    @Column(name = "household_size")
    private Integer householdSize;

    @Column(name = "possible_duplicate")
    private Boolean possibleDuplicate;

    @Column(name = "possible_duplicate_of")
    private Integer possibleDuplicateOf;

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
        this.email = email == null ? null : email.toLowerCase();
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
     * The owner's age band derived from {@link #birthDate} relative to the
     * {@link #registrationDate}: {@code 'MINOR'} when under 18, {@code 'ADULT'} from 18 to 64,
     * and {@code 'SENIOR'} at 65 or older. Returns {@code null} when no birthDate is set. The age
     * is the number of complete years between the birthDate and the registrationDate (or the
     * current date when the registrationDate is not yet set).
     */
    public String getAgeBand() {
        if (this.birthDate == null) {
            return null;
        }
        LocalDate reference = this.registrationDate != null ? this.registrationDate : LocalDate.now();
        int years = Period.between(this.birthDate, reference).getYears();
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

    public Boolean getBulkSignupWarning() {
        return this.bulkSignupWarning;
    }

    public void setBulkSignupWarning(Boolean bulkSignupWarning) {
        this.bulkSignupWarning = bulkSignupWarning;
    }

    public Integer getHouseholdSize() {
        return this.householdSize;
    }

    public void setHouseholdSize(Integer householdSize) {
        this.householdSize = householdSize;
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
     * The owner's name formatted as {@code 'LastName, FirstName'} from the stored names.
     */
    public String getDisplayName() {
        return this.getLastName() + ", " + this.getFirstName();
    }

    /**
     * The owner's initials: the upper-cased first letters of firstName and lastName,
     * dot-separated with a trailing dot, e.g. {@code 'J.S.'}.
     */
    public String getInitials() {
        return Character.toUpperCase(this.getFirstName().charAt(0)) + "."
            + Character.toUpperCase(this.getLastName().charAt(0)) + ".";
    }

    /**
     * The owner's membership number, formatted {@code '<customerCode>-M<YY>'} where YY is the
     * last two digits of the registrationDate year, e.g. {@code 'NSW-3C1A9F2B-M26'}.
     */
    public String getMembershipNumber() {
        return this.customerCode + "-M" + String.format("%02d", this.registrationDate.getYear() % 100);
    }

    /**
     * The Luhn check digit (a single digit, {@code 0}-{@code 9}) computed over the decimal digits
     * contained in the owner's {@link #customerCode}.
     */
    public int getCheckDigit() {
        return luhnCheckDigit(this.customerCode);
    }

    /**
     * The Luhn check digit (a single digit, {@code 0}-{@code 9}) over the decimal digits
     * contained in {@code value}. Reading the digits right to left, every second one is
     * doubled (subtracting 9 whenever doubling yields a value above 9); the transformed
     * digits are summed, and the check digit is the amount that must be added to that sum to
     * reach the next multiple of ten. Any non-digit character in {@code value} is ignored, so
     * the digit is taken over exactly the digits the string carries.
     */
    private static int luhnCheckDigit(String value) {
        int sum = 0;
        boolean doubling = true;
        for (int i = value.length() - 1; i >= 0; i--) {
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

    /** Fixed city-to-region table used to derive an owner's locality. */
    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW",
        "Melbourne", "VIC",
        "Brisbane", "QLD");

    /** The region reported for a city that is not present in {@link #CITY_REGION}. */
    public static final String UNKNOWN_REGION = "UNKNOWN";

    /**
     * The canonical region for a city, derived from the fixed city-to-region table
     * ({@code Sydney -> NSW}, {@code Melbourne -> VIC}, {@code Brisbane -> QLD}), or
     * {@link #UNKNOWN_REGION} when the city is not in the table. This is the single
     * source of the city-to-region mapping, shared by every rule that is keyed by
     * region so the mapping is never re-derived elsewhere.
     */
    public static String regionOf(String city) {
        return CITY_REGION.getOrDefault(city, UNKNOWN_REGION);
    }

    /**
     * The owner's locality: the {@code <REGION>} component of the {@link #getCustomerCode()
     * customerCode} (its prefix up to the first {@code '-'}), which is the region derived from
     * the owner's postcode at creation ({@link #UNKNOWN_REGION} when the postcode maps to no
     * known region). Locality is read from the region-and-hash identity so it never re-derives
     * a region of its own; it is {@link #UNKNOWN_REGION} for an owner that has no customerCode.
     */
    public String getLocality() {
        if (this.customerCode == null) {
            return UNKNOWN_REGION;
        }
        int dash = this.customerCode.indexOf('-');
        return dash < 0 ? this.customerCode : this.customerCode.substring(0, dash);
    }

    /** Fixed region-to-timezone table, mapping a region to its IANA timezone name. */
    private static final Map<String, String> REGION_TIMEZONE = Map.of(
        "NSW", "Australia/Sydney",
        "VIC", "Australia/Melbourne",
        "QLD", "Australia/Brisbane");

    /**
     * The owner's timezone: the IANA name for this owner's {@linkplain #getLocality() locality},
     * looked up in the fixed region-to-timezone table ({@code NSW -> Australia/Sydney},
     * {@code VIC -> Australia/Melbourne}, {@code QLD -> Australia/Brisbane}), or {@code null} when
     * the locality maps to no known region (e.g. {@link #UNKNOWN_REGION}). It reads the locality
     * rather than re-deriving a region so the timezone shares the owner's single region identity.
     */
    public String getTimezone() {
        return REGION_TIMEZONE.get(getLocality());
    }

    /**
     * The region derived from this owner's postcode alone: the region whose fixed
     * {@linkplain #REGION_POSTCODE_RANGE postcode range} contains the postcode, or {@code null}
     * when the postcode is absent, not four digits, or in no known range. This is the {@code <REGION>}
     * component built into the owner's customer code at creation and, through it, the source of the
     * owner's {@linkplain #getLocality() locality}. It is the single place a region is read from an
     * owner's postcode, so every rule keyed by the postcode-derived region shares one derivation.
     */
    public String getRegion() {
        return regionOfPostcode(this.postcode);
    }

    /**
     * The region whose fixed {@linkplain #REGION_POSTCODE_RANGE postcode range} contains the
     * given postcode, or {@code null} when the postcode is {@code null}, not four digits, or in
     * no known range.
     */
    private static String regionOfPostcode(String postcode) {
        if (postcode == null || !postcode.matches("[0-9]{4}")) {
            return null;
        }
        int value = Integer.parseInt(postcode);
        for (Map.Entry<String, int[]> entry : REGION_POSTCODE_RANGE.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Fixed inclusive 4-digit postcode ranges keyed by region, as {@code {low, high}}:
     * {@code NSW 2000-2099}, {@code VIC 3000-3099}, {@code QLD 4000-4099}. A region not
     * listed here (i.e. {@link #UNKNOWN_REGION}) accepts any 4-digit postcode.
     */
    private static final Map<String, int[]> REGION_POSTCODE_RANGE = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /**
     * Whether this owner's postcode is acceptable for its city. Postcode is optional, so a
     * {@code null} postcode is accepted. When present it must be four digits and, for a city
     * whose {@linkplain #regionOf(String) region} is known, fall within that region's fixed
     * inclusive range ({@code NSW 2000-2099}, {@code VIC 3000-3099}, {@code QLD 4000-4099}); a
     * city with no known region accepts any 4-digit postcode.
     */
    public boolean isPostcodeValid() {
        if (this.postcode == null) {
            return true;
        }
        if (!this.postcode.matches("[0-9]{4}")) {
            return false;
        }
        int[] range = REGION_POSTCODE_RANGE.get(regionOf(this.city));
        if (range == null) {
            return true;
        }
        int value = Integer.parseInt(this.postcode);
        return value >= range[0] && value <= range[1];
    }

    /**
     * The national (subscriber) digit count required by each known country calling code, keyed by
     * the code's digits (without the leading {@code '+'}): {@code '+1'} carries 10 national digits
     * and {@code '+61'} carries 9. This is the single source of the country-calling-code table,
     * shared by every rule keyed by an E.164 number's country code so the mapping is never
     * re-derived elsewhere.
     */
    private static final Map<String, Integer> NATIONAL_DIGITS_BY_COUNTRY = Map.of(
        "1", 10,
        "61", 9);

    /**
     * The country calling code (its digits, without the leading {@code '+'}) that prefixes the given
     * E.164 digit string, choosing the longest matching code known to
     * {@link #NATIONAL_DIGITS_BY_COUNTRY}, or {@code null} when the number begins with no known
     * country code. This is the single place a country code is read from an E.164 number, so every
     * rule keyed by that code shares one derivation.
     */
    private static String countryCodeOf(String e164Digits) {
        return NATIONAL_DIGITS_BY_COUNTRY.keySet().stream()
            .filter(e164Digits::startsWith)
            .max(Comparator.comparingInt(String::length))
            .orElse(null);
    }

    /**
     * Whether the E.164 digit string (no leading {@code '+'}) carries the national-number length
     * required by its {@linkplain #countryCodeOf(String) country calling code}. The longest matching
     * known code wins; a number whose country code is not recognised passes this check (its length is
     * then governed only by the general E.164 bound), while a recognised code must be followed by
     * exactly the national-digit count it requires ({@code '+1'} 10, {@code '+61'} 9).
     */
    public static boolean hasValidNationalLength(String e164Digits) {
        String countryCode = countryCodeOf(e164Digits);
        if (countryCode == null) {
            return true;
        }
        return e164Digits.length() - countryCode.length() == NATIONAL_DIGITS_BY_COUNTRY.get(countryCode);
    }

    /**
     * The owner's membership points: they start at 0, gain 2 when an email is present, gain 1 when
     * namesakeCount is 0, gain 2 for a {@linkplain #hasLargeHousehold() household of 3 or more}, and
     * gain 3 when the owner's {@linkplain #getTenureDays() tenure} exceeds 365 days. The email,
     * namesake and household factors are available on creation; the tenure factor is reached only
     * once tenure grows beyond 365 days.
     */
    public Integer getMembershipPoints() {
        int points = 0;
        if (hasEmail()) {
            points += 2;
        }
        if (hasNoNamesakes()) {
            points += 1;
        }
        if (hasLargeHousehold()) {
            points += 2;
        }
        if (hasTenureBeyondOneYear()) {
            points += 3;
        }
        return points;
    }

    /**
     * The owner's membership level, a number from 1 to 4 derived from {@linkplain #getMembershipPoints()
     * membershipPoints}: level 1 for 0-1 points, level 2 for 2-3, level 3 for 4-5, and level 4 for 6 or
     * more.
     */
    public Integer getMembershipLevel() {
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
     * Whether this owner belongs to a household of three or more: its {@link #getHouseholdSize()
     * householdSize} is {@code 3} or greater. A {@code null} householdSize, meaning the size is not
     * known, is treated as not a large household.
     */
    private boolean hasLargeHousehold() {
        return this.householdSize != null && this.householdSize >= 3;
    }

    /**
     * Whether this owner has an email on record: a non-{@code null}, non-empty {@link #getEmail()
     * email}. This is the single source of the "has an email" test, shared by every rule keyed by
     * email presence so the check is never re-derived elsewhere.
     */
    private boolean hasEmail() {
        return this.email != null && !this.email.isEmpty();
    }

    /**
     * Whether this owner's name is unique in the clinic: its {@link #getNamesakeCount() namesakeCount}
     * is {@code 0} (no other owner shares the name). A {@code null} namesakeCount, meaning the count is
     * not known, is treated as not unique.
     */
    private boolean hasNoNamesakes() {
        return this.namesakeCount != null && this.namesakeCount == 0;
    }

    /**
     * Whether this owner's {@linkplain #getTenureDays() tenure} exceeds 365 days. A newly created owner
     * (zero tenure) does not qualify; the threshold is crossed only once tenure grows beyond one year.
     */
    private boolean hasTenureBeyondOneYear() {
        return this.getTenureDays() > 365;
    }

    /**
     * The owner's tenure in whole days: the number of days between the {@link #registrationDate} and
     * the current date, or {@code 0} when the registrationDate is not yet set (a newly created owner has
     * zero tenure). Never negative.
     */
    public long getTenureDays() {
        if (this.registrationDate == null) {
            return 0;
        }
        return Math.max(0, ChronoUnit.DAYS.between(this.registrationDate, LocalDate.now()));
    }

    /**
     * The owner's preferred contact channel: {@code 'EMAIL'} when an email is present,
     * otherwise {@code 'PHONE'}.
     */
    public String getContactPreference() {
        return hasEmail() ? "EMAIL" : "PHONE";
    }

    /**
     * The owner's stored E.164 {@linkplain #getTelephone() telephone} formatted for humans:
     * a {@code '+'}, the {@linkplain #countryCodeOf(String) country calling code}, a space, and
     * the national digits grouped into threes from the left separated by spaces,
     * e.g. {@code '+61412345678'} -> {@code '+61 412 345 678'}. The raw {@link #getTelephone()
     * telephone} stays in E.164. Returns the telephone unchanged when it is {@code null}, not in
     * E.164 form, or carries no known country code.
     */
    public String getTelephoneDisplay() {
        if (this.telephone == null || !this.telephone.startsWith("+")) {
            return this.telephone;
        }
        String digits = this.telephone.substring(1);
        String countryCode = countryCodeOf(digits);
        if (countryCode == null) {
            return this.telephone;
        }
        String national = digits.substring(countryCode.length());
        StringBuilder groups = new StringBuilder();
        for (int i = 0; i < national.length(); i += 3) {
            if (groups.length() > 0) {
                groups.append(' ');
            }
            groups.append(national, i, Math.min(i + 3, national.length()));
        }
        return "+" + countryCode + " " + groups;
    }

    /**
     * The owner's identity key: the single derived value used for duplicate detection.
     * It joins the normalized (E.164) telephone, the email (already lower-cased, or the
     * empty string when absent) and the householdId (or the empty string when the owner
     * has no household) with {@code '|'}, e.g. {@code '+61412345678|jane@example.test|3C1A9F2B7D4E'}.
     * Two owners are duplicates only when their whole identity keys are equal.
     */
    public String getIdentityKey() {
        String telephonePart = this.telephone == null ? "" : this.telephone;
        String emailPart = this.email == null ? "" : this.email;
        String householdPart = this.householdId == null ? "" : this.householdId;
        return telephonePart + "|" + emailPart + "|" + householdPart;
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
