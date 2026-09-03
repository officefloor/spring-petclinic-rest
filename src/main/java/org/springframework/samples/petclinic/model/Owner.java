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
     * last two digits of the registrationDate year, e.g. {@code 'LON-SMI-0007-M26'}.
     */
    public String getMembershipNumber() {
        return this.customerCode + "-M" + String.format("%02d", this.registrationDate.getYear() % 100);
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
     * The owner's locality. The postcode is preferred: when it falls within a known
     * {@linkplain #REGION_POSTCODE_RANGE region postcode range} that region is returned,
     * which disambiguates cities that share a name. Only when the postcode is absent or in
     * no known range does this fall back to the {@linkplain #regionOf(String) city-to-region
     * table}, yielding {@link #UNKNOWN_REGION} when the city has no known region.
     */
    public String getLocality() {
        String byPostcode = regionOfPostcode(this.postcode);
        return byPostcode != null ? byPostcode : regionOf(this.city);
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
     * The owner's membership level, a number from 1 to 3 determined on creation: it starts at 1,
     * gains 1 when an email is present, gains 1 when namesakeCount is 0, and is capped at 3.
     * Level 4 is reserved for tenure.
     */
    public Integer getMembershipLevel() {
        int level = 1;
        if (this.email != null && !this.email.isEmpty()) {
            level++;
        }
        if (this.namesakeCount != null && this.namesakeCount == 0) {
            level++;
        }
        return Math.min(level, 3);
    }

    /**
     * The owner's preferred contact channel: {@code 'EMAIL'} when an email is present,
     * otherwise {@code 'PHONE'}.
     */
    public String getContactPreference() {
        return (this.email != null && !this.email.isEmpty()) ? "EMAIL" : "PHONE";
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
