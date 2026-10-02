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

    @Column(name = "city")
    @NotEmpty
    private String city;

    @Column(name = "telephone")
    @NotEmpty
    @Pattern(regexp = "^\\+[0-9]{8,15}$", message = "Phone number must be in E.164 form (+ followed by 8 to 15 digits)")
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

    @Column(name = "namesake_count")
    private Integer namesakeCount;

    @Column(name = "postcode")
    private String postcode;

    @Transient
    private Integer householdSize;

    @Transient
    private boolean bulkSignupWarning;

    @Transient
    private boolean possibleDuplicate;

    @Transient
    private Integer possibleDuplicateOf;

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

    /**
     * The number of owners in this owner's household (owners sharing its householdId, counting
     * this owner). Not a persisted column — populated by the owner pipelines (create and read)
     * that have repository access, and consumed only by {@link #getMembershipPoints()}. {@code
     * null} when it has not been resolved, which the points rule treats as a single-member
     * household.
     */
    @Transient
    public Integer getHouseholdSize() {
        return this.householdSize;
    }

    public void setHouseholdSize(Integer householdSize) {
        this.householdSize = householdSize;
    }

    public Integer getNamesakeCount() {
        return this.namesakeCount;
    }

    public void setNamesakeCount(Integer namesakeCount) {
        this.namesakeCount = namesakeCount;
    }

    public String getPostcode() {
        return this.postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    /**
     * The owner's name formatted as {@code 'LastName, FirstName'} from the stored names.
     * Not persisted — derived from {@link #getLastName()} and {@link #getFirstName()}.
     */
    @Transient
    public String getDisplayName() {
        return this.getLastName() + ", " + this.getFirstName();
    }

    /**
     * The owner's initials: the upper-cased first letters of firstName and lastName,
     * dot-separated with a trailing dot (e.g. {@code 'J.S.'}).
     * Not persisted — derived from {@link #getFirstName()} and {@link #getLastName()}.
     */
    @Transient
    public String getInitials() {
        return Character.toUpperCase(this.getFirstName().charAt(0)) + "."
            + Character.toUpperCase(this.getLastName().charAt(0)) + ".";
    }

    /**
     * The owner's {@link #getTelephone() telephone} formatted for humans: the stored E.164
     * value rewritten as the country code, a space, then the national digits grouped in threes
     * separated by spaces (e.g. {@code '+61 412 345 678'}). The known country codes are
     * {@code '+61'} (Australia) and {@code '+1'} (NANP); any other E.164 value is split with a
     * two-digit country code. {@code null} when no telephone is set or the value is not in E.164
     * form. The raw {@link #getTelephone() telephone} is left in E.164 form. Not persisted —
     * derived from {@link #getTelephone()}.
     */
    @Transient
    public String getTelephoneDisplay() {
        if (this.telephone == null || !this.telephone.matches("\\+[0-9]+")) {
            return null;
        }
        String digits = this.telephone.substring(1);
        int countryCodeLength = digits.startsWith("1") && !digits.startsWith("61") ? 1 : 2;
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
     * The owner's membership number, formatted {@code '<customerCode>-M<YY>'} where YY is
     * the last two digits of the {@link #getRegistrationDate() registrationDate} year
     * (e.g. {@code 'NSW-1A2B3C4D-M26'}).
     * Not persisted — derived from {@link #getCustomerCode()} and {@link #getRegistrationDate()}.
     */
    @Transient
    public String getMembershipNumber() {
        return this.customerCode + "-M" + String.format("%02d", this.registrationDate.getYear() % 100);
    }

    /**
     * The owner's check digit: the Luhn check digit (0-9) computed over the digits contained
     * in the {@link #getCustomerCode() customerCode}. Non-digit characters are ignored.
     * Not persisted — derived from {@link #getCustomerCode()}.
     */
    @Transient
    public int getCheckDigit() {
        int sum = 0;
        boolean dbl = true;
        String code = this.customerCode == null ? "" : this.customerCode;
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
     * The owner's membership points. Starts at {@code 0}; adds {@code 2} when an
     * {@link #getEmail() email} is present; adds {@code 1} when {@link #getNamesakeCount()
     * namesakeCount} is {@code 0}; adds {@code 2} for a household of {@code 3} or more members
     * ({@link #getHouseholdSize() householdSize}); adds {@code 3} for a tenure of more than
     * {@code 365} days since the {@link #getRegistrationDate() registration date}. Not persisted
     * — derived from {@link #getEmail()}, {@link #getNamesakeCount()}, {@link #getHouseholdSize()}
     * and {@link #getRegistrationDate()}.
     */
    @Transient
    public int getMembershipPoints() {
        int points = 0;
        boolean hasEmail = this.email != null && !this.email.isBlank();
        if (hasEmail) {
            points += 2;
        }
        boolean uniqueName = this.namesakeCount != null && this.namesakeCount == 0;
        if (uniqueName) {
            points += 1;
        }
        if (this.householdSize != null && this.householdSize >= 3) {
            points += 2;
        }
        if (this.registrationDate != null
                && ChronoUnit.DAYS.between(this.registrationDate, LocalDate.now()) > 365) {
            points += 3;
        }
        return points;
    }

    /**
     * The owner's numeric membership level, mapped from {@link #getMembershipPoints()
     * membershipPoints}: {@code 1} for {@code 0-1} points, {@code 2} for {@code 2-3},
     * {@code 3} for {@code 4-5} and {@code 4} for {@code 6} or more. Not persisted — derived
     * from {@link #getMembershipPoints()}.
     */
    @Transient
    public int getMembershipLevel() {
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
     * Fixed city-to-region table used as the fall-back when deriving an owner's region.
     */
    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /**
     * Region -> inclusive 4-digit postcode range {low, high}, the primary source for
     * deriving an owner's region (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099).
     */
    private static final Map<String, int[]> REGION_RANGE = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /**
     * Fixed region -> IANA timezone table (NSW->Australia/Sydney, VIC->Australia/Melbourne,
     * QLD->Australia/Brisbane), the single authority for {@link #getTimezone()}.
     */
    private static final Map<String, String> REGION_TIMEZONE = Map.of(
        "NSW", "Australia/Sydney",
        "VIC", "Australia/Melbourne",
        "QLD", "Australia/Brisbane");

    /**
     * The canonical region for a postcode/city pair, derived first from {@code postcode} by range
     * ({@link #regionForPostcode}), falling back to the fixed city table ({@link #regionForCity})
     * when the postcode is absent or in no known range, or {@code 'UNKNOWN'} when neither matches.
     * Preferring the postcode disambiguates cities that share a name. This is the REGION prefix of
     * the owner's {@link #getCustomerCode() customerCode}, so the create pipeline and
     * {@link #getLocality() locality} agree on it. The single authority on the region rule, reused
     * by the owner pipelines (e.g. postcode validation) rather than copied.
     */
    public static String regionFor(String postcode, String city) {
        String region = regionForPostcode(postcode);
        if (region != null) {
            return region;
        }
        String cityRegion = regionForCity(city);
        return cityRegion != null ? cityRegion : "UNKNOWN";
    }

    /** The canonical region for a city by exact name match (Sydney->NSW, Melbourne->VIC,
     * Brisbane->QLD), or {@code null} when the city has no known region. */
    public static String regionForCity(String city) {
        return city == null ? null : CITY_REGION.get(city);
    }

    /** Region whose inclusive range contains the given postcode (NSW 2000-2099, VIC 3000-3099,
     * QLD 4000-4099), or {@code null} when the postcode is absent, not 4 digits, or in no known
     * range. */
    public static String regionForPostcode(String postcode) {
        if (postcode == null || !postcode.matches("[0-9]{4}")) {
            return null;
        }
        int value = Integer.parseInt(postcode);
        for (Map.Entry<String, int[]> entry : REGION_RANGE.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * The owner's locality: the REGION prefix of the {@link #getCustomerCode() customerCode}
     * (the portion before the first {@code '-'}), which the create pipeline derives from the
     * postcode-and-city region. {@code 'UNKNOWN'} when no customerCode is set. Not persisted —
     * derived from {@link #getCustomerCode()}.
     */
    @Transient
    public String getLocality() {
        if (this.customerCode == null) {
            return "UNKNOWN";
        }
        int dash = this.customerCode.indexOf('-');
        return dash <= 0 ? "UNKNOWN" : this.customerCode.substring(0, dash);
    }

    /**
     * The owner's timezone: the IANA name for the owner's {@link #getLocality() locality}/region
     * from the fixed region-to-timezone table (NSW->Australia/Sydney, VIC->Australia/Melbourne,
     * QLD->Australia/Brisbane), or {@code null} when the region has no mapped timezone. Not
     * persisted — derived from {@link #getLocality()}.
     */
    @Transient
    public String getTimezone() {
        return REGION_TIMEZONE.get(getLocality());
    }

    /**
     * Whether more than 80 owners had already been created today when this response was
     * built. Not persisted — populated for the response by the create and fetch pipelines
     * and defaults to {@code false} so every owner serialization carries the flag.
     */
    @Transient
    public boolean isBulkSignupWarning() {
        return this.bulkSignupWarning;
    }

    public void setBulkSignupWarning(boolean bulkSignupWarning) {
        this.bulkSignupWarning = bulkSignupWarning;
    }

    /**
     * Whether this owner is a possible (soft) duplicate: not a hard duplicate, but shares an
     * existing owner's last name and postcode with a different telephone. Not persisted —
     * populated for the response by the create and fetch pipelines and defaults to
     * {@code false} so every owner serialization carries the flag.
     */
    @Transient
    public boolean isPossibleDuplicate() {
        return this.possibleDuplicate;
    }

    public void setPossibleDuplicate(boolean possibleDuplicate) {
        this.possibleDuplicate = possibleDuplicate;
    }

    /**
     * The id of the existing owner this owner possibly duplicates (shared last name and
     * postcode, different telephone), or {@code null} when it is not a possible duplicate.
     * Not persisted — populated for the response alongside {@link #isPossibleDuplicate()}.
     */
    @Transient
    public Integer getPossibleDuplicateOf() {
        return this.possibleDuplicateOf;
    }

    public void setPossibleDuplicateOf(Integer possibleDuplicateOf) {
        this.possibleDuplicateOf = possibleDuplicateOf;
    }

    /**
     * The owner's preferred contact channel: {@code 'EMAIL'} when an {@link #getEmail() email}
     * is present, otherwise {@code 'PHONE'}.
     * Not persisted — derived from {@link #getEmail()}.
     */
    @Transient
    public String getContactPreference() {
        boolean hasEmail = this.email != null && !this.email.isBlank();
        return hasEmail ? "EMAIL" : "PHONE";
    }

    /**
     * The owner's age band as of the {@link #getRegistrationDate() registrationDate}, derived
     * from {@link #getBirthDate() birthDate}: {@code 'MINOR'} when under 18, {@code 'ADULT'} for
     * 18 to 64 inclusive, {@code 'SENIOR'} for 65 and over. {@code null} when no birthDate is
     * supplied. Not persisted — derived from {@link #getBirthDate()} and
     * {@link #getRegistrationDate()}.
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
        return age < 65 ? "ADULT" : "SENIOR";
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
