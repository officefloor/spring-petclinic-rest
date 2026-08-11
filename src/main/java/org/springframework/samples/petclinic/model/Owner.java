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

    @Column(name = "telephone")
    @NotEmpty
    @Pattern(regexp = "^\\+[0-9]{8,15}$", message = "Phone number must be E.164 form: a leading '+' followed by 8 to 15 digits")
    private String telephone;

    @Column(name = "email")
    @Email
    private String email;

    @Column(name = "registration_date", columnDefinition = "DATE")
    private LocalDate registrationDate;

    @Column(name = "customer_code")
    private String customerCode;

    @Column(name = "household_id")
    private String householdId;

    @Column(name = "namesake_count")
    private Integer namesakeCount;

    @Column(name = "household_size")
    private Integer householdSize;

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

    /**
     * Stores the email lower-cased so it is persisted and returned in a canonical form. A
     * {@code null} value (email omitted) is preserved as {@code null}.
     */
    public void setEmail(String email) {
        this.email = email == null ? null : email.toLowerCase(Locale.ROOT);
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

    /**
     * The owner's membership number, formatted {@code <customerCode>-M<YY>} where {@code YY} is
     * the last two digits of the {@code registrationDate} year (e.g. {@code LON-SMI-0007-M26}).
     * Derived from the owner's own fields; {@code null} until both are assigned.
     */
    @Transient
    public String getMembershipNumber() {
        if (this.customerCode == null || this.registrationDate == null) {
            return null;
        }
        return String.format("%s-M%02d", this.customerCode, this.registrationDate.getYear() % 100);
    }

    /**
     * The owner's membership level, a number from 1 to 3 derived from the owner's own fields as
     * captured at creation time. It starts at 1, gains 1 when an email is present, gains a further
     * 1 when the owner has no namesakes ({@code namesakeCount} is 0), and is capped at 3. Level 4
     * is reserved for tenure and is never produced here.
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

    /** City -> canonical region for the {@code locality} derivation. */
    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /**
     * The owner's locality, derived from {@link #city} using the fixed city-to-region table
     * (Sydney->NSW, Melbourne->VIC, Brisbane->QLD). Returns {@code UNKNOWN} when the city is
     * not in the table (or is absent).
     */
    @Transient
    public String getLocality() {
        return CITY_REGION.getOrDefault(this.city, "UNKNOWN");
    }

    /**
     * The owner's identity key, the single derived value all duplicate detection is based on. It is
     * {@code normalizedTelephone + '|' + (email or empty) + '|' + householdId}, where the telephone
     * is the stored E.164 value, the email is the stored lower-cased value (empty when absent) and
     * the household segment is the {@code householdId} (empty when the owner does not share a
     * household). Two owners are duplicates only when their whole identity keys are equal.
     */
    @Transient
    public String getIdentityKey() {
        String telephonePart = this.telephone == null ? "" : this.telephone;
        String emailPart = (this.email == null || this.email.isBlank()) ? "" : this.email;
        String householdPart = this.householdId == null ? "" : this.householdId;
        return telephonePart + "|" + emailPart + "|" + householdPart;
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

    public String getPostcode() {
        return this.postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
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
