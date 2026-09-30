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
    @Pattern(regexp = "^\\+[0-9]{8,15}$",
            message = "Phone number must be E.164: '+' followed by 8 to 15 digits")
    private String telephone;

    @Column(name = "email")
    private String email;

    @Column(name = "registration_date")
    private LocalDate registrationDate;

    @Column(name = "customer_code")
    private String customerCode;

    @Column(name = "membership_number")
    private String membershipNumber;

    @Column(name = "household_id")
    private String householdId;

    @Column(name = "namesake_count")
    private Integer namesakeCount;

    @Transient
    private Boolean bulkSignupWarning;

    @Transient
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

    public String getMembershipNumber() {
        return this.membershipNumber;
    }

    public void setMembershipNumber(String membershipNumber) {
        this.membershipNumber = membershipNumber;
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

    /**
     * Whether more than 80 owners had already been created on this owner's registration day,
     * derived at response time and not persisted. {@code true} triggers a bulk-signup warning
     * in the response.
     */
    public Boolean getBulkSignupWarning() {
        return this.bulkSignupWarning;
    }

    public void setBulkSignupWarning(Boolean bulkSignupWarning) {
        this.bulkSignupWarning = bulkSignupWarning;
    }

    /**
     * The number of owners in this owner's household (owners sharing the same
     * {@link #householdId}), derived at response time and not persisted.
     */
    public Integer getHouseholdSize() {
        return this.householdSize;
    }

    public void setHouseholdSize(Integer householdSize) {
        this.householdSize = householdSize;
    }

    /**
     * The owner's numeric membership level, assigned at creation. Starts at {@code 1}, plus 1 when
     * an email is present, plus 1 when {@link #namesakeCount} is 0, capped at {@code 3} (level 4 is
     * reserved for tenure).
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

    /** Fixed city-to-region table used to derive {@link #getLocality()}. */
    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** Region -> inclusive 4-digit postcode range {low, high} used to derive {@link #getLocality()}. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
        "NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099}, "QLD", new int[] {4000, 4099});

    /**
     * The fixed city-to-region mapping ({@code Sydney -> NSW}, {@code Melbourne -> VIC},
     * {@code Brisbane -> QLD}): the region for {@code city}, or {@code null} when the city is not in
     * the table. Used only by postcode validation, which branches on "known region or not" by testing
     * for {@code null} before range-checking the postcode.
     */
    public static String regionOf(String city) {
        return CITY_REGION.get(city);
    }

    /**
     * The region whose fixed postcode range ({@code NSW 2000-2099}, {@code VIC 3000-3099},
     * {@code QLD 4000-4099}) contains {@code postcode}, or {@code null} when {@code postcode} is
     * absent, not a 4-digit number, or in no known range.
     */
    public static String regionOfPostcode(String postcode) {
        if (postcode == null || postcode.isEmpty()) {
            return null;
        }
        int value;
        try {
            value = Integer.parseInt(postcode);
        } catch (NumberFormatException ex) {
            return null;
        }
        for (Map.Entry<String, int[]> entry : REGION_POSTCODES.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * The owner's region, derived at response time from the {@code REGION} prefix of the
     * {@link #customerCode} (the segment before the first {@code '-'}). Because the customer code is
     * assigned as {@code <REGION>-<HASH8>} with REGION taken from the postcode, the locality follows
     * the same region-and-hash identity as the customer code. {@code UNKNOWN} when the owner has no
     * customer code.
     */
    public String getLocality() {
        if (this.customerCode == null) {
            return "UNKNOWN";
        }
        int dash = this.customerCode.indexOf('-');
        return dash < 0 ? this.customerCode : this.customerCode.substring(0, dash);
    }

    /**
     * The owner's preferred contact channel, derived at response time: {@code EMAIL} when an
     * email is present, otherwise {@code PHONE}. Not persisted.
     */
    public String getContactPreference() {
        return (this.email != null && !this.email.isEmpty()) ? "EMAIL" : "PHONE";
    }

    /**
     * A single Luhn check digit (0-9) derived at response time over the digits contained in
     * {@link #customerCode} (non-digit characters are ignored), or {@code null} when the owner
     * has no customer code. Computed with the standard Luhn algorithm — doubling every second
     * digit from the right, subtracting 9 from any result over 9, and taking
     * {@code (10 - sum % 10) % 10}. Not persisted.
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
     * The single source of truth for an owner's identity: the derived key
     * {@code normalizedTelephone|email|householdId}, joined with '|'. The telephone is stored in
     * canonical E.164 form and the email lower-cased, so this key composes their canonical values;
     * an absent email or householdId contributes an empty segment. Two owners are duplicates exactly
     * when their whole identity keys are equal — differing in any one segment (for example two
     * members of the same household with different telephones) yields different keys. Derived at
     * response time and used by the create pipeline's single duplicate check; not persisted.
     */
    public String getIdentityKey() {
        return keyPart(this.telephone) + "|" + keyPart(this.email) + "|" + keyPart(this.householdId);
    }

    private static String keyPart(String value) {
        return value == null ? "" : value;
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
