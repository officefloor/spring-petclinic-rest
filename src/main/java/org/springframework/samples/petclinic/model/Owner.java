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
import org.springframework.samples.petclinic.util.CityLocality;
import org.springframework.samples.petclinic.util.LuhnCheckDigit;

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
    @Pattern(regexp = "^\\+[0-9]{8,15}$", message = "Phone number must be in E.164 form: a '+' followed by 8 to 15 digits")
    private String telephone;

    @Column(name = "email")
    private String email;

    @Column(name = "postcode")
    private String postcode;

    @Column(name = "registration_date")
    private LocalDate registrationDate;

    @Column(name = "customer_code")
    private String customerCode;

    @Column(name = "household_id")
    private String householdId;

    @Column(name = "namesake_count")
    private Integer namesakeCount;

    @Column(name = "household_size")
    private Integer householdSize;

    @Column(name = "membership_number")
    private String membershipNumber;

    @Column(name = "bulk_signup_warning", nullable = false)
    private boolean bulkSignupWarning;

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

    /**
     * The owner's postcode: a 4-digit code that, when supplied at creation, is validated
     * against the city's region and then stored verbatim. Optional, so may be null.
     */
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

    public String getMembershipNumber() {
        return this.membershipNumber;
    }

    public void setMembershipNumber(String membershipNumber) {
        this.membershipNumber = membershipNumber;
    }

    /**
     * Whether this owner was created as part of a bulk signup: {@code true} when more
     * than 80 owners had already been registered on this owner's registration date at
     * the moment it was created, {@code false} otherwise. Captured at creation.
     */
    public boolean isBulkSignupWarning() {
        return this.bulkSignupWarning;
    }

    public void setBulkSignupWarning(boolean bulkSignupWarning) {
        this.bulkSignupWarning = bulkSignupWarning;
    }

    /**
     * The owner's locality: the canonical region derived by preferring the postcode
     * range (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099), falling back to the fixed
     * city-to-region table (Sydney->NSW, Melbourne->VIC, Brisbane->QLD) when the
     * postcode is absent or in no known range, or {@code "UNKNOWN"} otherwise. Derived
     * from stored state, never persisted.
     */
    public String getLocality() {
        return CityLocality.forPostcodeOrCity(this.postcode, this.city);
    }

    /**
     * The owner's membership level, from 1 to 3: starts at 1, gains 1 when this owner has
     * an email address, and gains 1 when this owner had no namesakes at creation
     * ({@code namesakeCount} is 0). Capped at 3 (level 4 is reserved for tenure). Derived
     * from stored state, never persisted.
     */
    public int getMembershipLevel() {
        int level = 1;
        if (hasEmail()) {
            level++;
        }
        if (Integer.valueOf(0).equals(this.namesakeCount)) {
            level++;
        }
        return Math.min(level, 3);
    }

    /**
     * The owner's check digit: a single Luhn check digit (0-9) computed over the digits of
     * the {@code customerCode}. Derived from stored state, never persisted.
     */
    public int getCheckDigit() {
        return LuhnCheckDigit.of(this.customerCode);
    }

    /**
     * The owner's preferred contact channel: {@code "EMAIL"} when an email address is
     * present, otherwise {@code "PHONE"}. Derived from stored state, never persisted.
     */
    public String getContactPreference() {
        return hasEmail() ? "EMAIL" : "PHONE";
    }

    /**
     * The owner's identity key: the single value all duplicate detection is expressed
     * through, formed as the normalized telephone, the email (or empty when absent) and
     * the household id (or empty when none), joined by {@code '|'}. Two owners are
     * duplicates only when their whole identity keys are equal, so members of the same
     * household with different telephones have different keys and are all allowed. Derived
     * from stored state, never persisted.
     */
    public String getIdentityKey() {
        String emailPart = hasEmail() ? this.email : "";
        String householdPart = this.householdId == null ? "" : this.householdId;
        return this.telephone + "|" + emailPart + "|" + householdPart;
    }

    private boolean hasEmail() {
        return this.email != null && !this.email.isEmpty();
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
