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
    @Column(name = "address")
    @NotEmpty
    private String address;

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

    @Transient
    private Integer householdMemberCount;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner", fetch = FetchType.EAGER)
    private Set<Pet> pets;

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
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
     * Returns this owner's numeric membership level, assigned on creation. The level starts at
     * {@code 1}, gains {@code 1} when an email is present, gains {@code 1} when the owner has no
     * namesakes (namesakeCount is 0), and is capped at {@code 3} (level 4 is reserved for tenure).
     *
     * @return the owner's membership level
     */
    @Transient
    public Integer getMembershipLevel() {
        int level = 1;
        boolean hasEmail = this.email != null && !this.email.isBlank();
        if (hasEmail) {
            level++;
        }
        boolean noNamesakes = this.namesakeCount != null && this.namesakeCount == 0;
        if (noNamesakes) {
            level++;
        }
        return Math.min(level, 3);
    }

    /**
     * Returns this owner's membership number formatted as {@code '<customerCode>-M<YY>'}, where YY is
     * the last two digits of the registrationDate year, e.g. {@code 'NSW-A1B2C3D4-M26'}. Returns
     * {@code null} when the owner has no customer code or registration date.
     *
     * @return the owner's membership number, or {@code null} when it cannot be derived
     */
    @Transient
    public String getMembershipNumber() {
        if (this.customerCode == null || this.registrationDate == null) {
            return null;
        }
        return String.format("%s-M%02d", this.customerCode, this.registrationDate.getYear() % 100);
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
