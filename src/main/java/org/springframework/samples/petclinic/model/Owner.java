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
    @Pattern(regexp = "^\\+[0-9]{8,15}$", message = "Phone number must be in E.164 format")
    private String telephone;

    @Column(name = "email")
    private String email;

    @Column(name = "registration_date", columnDefinition = "DATE")
    private LocalDate registrationDate;

    @Column(name = "customer_code")
    private String customerCode;

    @Column(name = "household_id")
    private String householdId;

    @Column(name = "namesake_count")
    private Integer namesakeCount;

    @Column(name = "bulk_signup_warning")
    private Boolean bulkSignupWarning;

    @Column(name = "postcode")
    private String postcode;

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

    public String getPostcode() {
        return this.postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
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
     * The owner's membership number, formatted {@code "<customerCode>-M<YY>"} where
     * YY is the last two digits of the registration date's year
     * (e.g. {@code "TOW-SMI-0007-M26"}). Derived from the owner's own fields; null when
     * either the customer code or registration date is absent.
     */
    @Transient
    public String getMembershipNumber() {
        if (this.customerCode == null || this.registrationDate == null) {
            return null;
        }
        return String.format("%s-M%02d", this.customerCode, this.registrationDate.getYear() % 100);
    }

    /**
     * The owner's membership level: a number from 1 to 3 assigned at creation. Starts
     * at 1, gains 1 when an email address is present, gains a further 1 when the owner
     * has no namesakes ({@code namesakeCount} is 0), and is capped at 3 (level 4 is
     * reserved for tenure). Derived from the owner's own fields.
     */
    @Transient
    public Integer getMembershipLevel() {
        int level = 1;
        boolean hasEmail = this.email != null && !this.email.isBlank();
        if (hasEmail) {
            level++;
        }
        boolean unique = this.namesakeCount != null && this.namesakeCount == 0;
        if (unique) {
            level++;
        }
        return Math.min(level, 3);
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
     * The owner's locality: the canonical region derived by preferring the postcode
     * range ({@code 2000-2099->NSW}, {@code 3000-3099->VIC}, {@code 4000-4099->QLD}),
     * falling back to the fixed city-to-region table ({@code Sydney->NSW},
     * {@code Melbourne->VIC}, {@code Brisbane->QLD}) when the postcode is absent or in
     * no known range, or {@code "UNKNOWN"} when neither resolves. Derived from the
     * owner's own fields.
     */
    @Transient
    public String getLocality() {
        return region(this.city, this.postcode);
    }

    /**
     * The canonical region derived by preferring the postcode range over the city.
     * Looks the postcode up against the known ranges first (NSW 2000-2099,
     * VIC 3000-3099, QLD 4000-4099); only when the postcode is absent or in no known
     * range does it fall back to the fixed city-to-region table. Returns
     * {@code "UNKNOWN"} when neither resolves. The single source of truth for region
     * derivation, so a known city still yields its region while cities that share a
     * name are disambiguated by postcode.
     */
    public static String region(String city, String postcode) {
        String byPostcode = regionForPostcode(postcode);
        return byPostcode != null ? byPostcode : regionForCity(city);
    }

    /**
     * The canonical region for a city per the fixed city-to-region table
     * ({@code Sydney->NSW}, {@code Melbourne->VIC}, {@code Brisbane->QLD}), or
     * {@code "UNKNOWN"} when the city is not in the table. The single source of truth
     * for the city-to-region mapping, used as the fallback in
     * {@link #region(String, String)} and as the city's region in postcode validation.
     */
    public static String regionForCity(String city) {
        return CITY_REGION.getOrDefault(city, "UNKNOWN");
    }

    /**
     * The canonical region for a 4-digit postcode per the fixed range table
     * (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099), or {@code null} when the postcode
     * is absent, not a well-formed integer, or in no known range. Reads the shared
     * {@link #REGION_POSTCODE_RANGES} table; used by {@link #region(String, String)}
     * for locality derivation and by postcode validation to range-check a submitted
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
     * The owner's preferred contact channel: {@code "EMAIL"} when an email address is
     * present, otherwise {@code "PHONE"}. Derived from the owner's own fields.
     */
    @Transient
    public String getContactPreference() {
        boolean hasEmail = this.email != null && !this.email.isBlank();
        return hasEmail ? "EMAIL" : "PHONE";
    }

    /**
     * The owner's identity key: the single value that duplicate detection is expressed
     * through, formed as {@code normalizedTelephone + "|" + (email or empty) + "|" +
     * (householdId or empty)}. The telephone and email are already stored in their
     * normalized forms (E.164 and lower-cased), and the household id is empty for a solo
     * owner. Two owners are duplicates only when their whole identity keys are equal, so
     * two members of the same household with different telephones have distinct keys.
     * Derived from the owner's own fields.
     */
    @Transient
    public String getIdentityKey() {
        return identityKey(this.telephone, this.email, this.householdId);
    }

    /**
     * Composes an identity key from its already-normalized parts. The single place the
     * key's shape is defined, so the create-time duplicate check (which normalizes a
     * request's parts) and this entity's {@link #getIdentityKey()} cannot drift apart. A
     * null or blank part contributes an empty segment.
     */
    public static String identityKey(String normalizedTelephone, String normalizedEmail, String householdId) {
        return keyPart(normalizedTelephone) + "|" + keyPart(normalizedEmail) + "|" + keyPart(householdId);
    }

    private static String keyPart(String value) {
        return (value == null || value.isBlank()) ? "" : value;
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
