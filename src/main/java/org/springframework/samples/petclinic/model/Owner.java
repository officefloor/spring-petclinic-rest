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
     * The owner's numeric membership level, derived on creation: it starts at 1, gains 1 when an
     * email is present, gains 1 when {@link #namesakeCount} is 0, and is capped at 3 (level 4 is
     * reserved for tenure).
     */
    @Transient
    public Integer getMembershipLevel() {
        int level = 1;
        if (this.email != null && !this.email.isBlank()) {
            level++;
        }
        if (this.namesakeCount != null && this.namesakeCount == 0) {
            level++;
        }
        return Math.min(level, 3);
    }

    /**
     * The owner's preferred contact channel, derived on read: {@code "EMAIL"} when an email is
     * present, otherwise {@code "PHONE"}.
     */
    @Transient
    public String getContactPreference() {
        return (this.email != null && !this.email.isBlank()) ? "EMAIL" : "PHONE";
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
