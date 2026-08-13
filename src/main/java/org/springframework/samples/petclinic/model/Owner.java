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
    @Pattern(regexp = "^\\+[0-9]{8,15}$", message = "Phone number must be in E.164 form (a '+' followed by 8 to 15 digits)")
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

    /** Common street-type abbreviations expanded during address normalization. */
    private static final Map<String, String> ADDRESS_ABBREVIATIONS = Map.of(
        "ST", "STREET",
        "RD", "ROAD",
        "AVE", "AVENUE");

    /** Fixed city-to-region table used to derive an owner's locality. */
    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW",
        "Melbourne", "VIC",
        "Brisbane", "QLD");

    public String getAddress() {
        return this.address;
    }

    /**
     * Set the owner's address, normalizing it to a canonical form so it is both
     * stored and returned consistently. Leading/trailing whitespace is trimmed,
     * internal runs of whitespace are collapsed to a single space, the value is
     * upper-cased, and common street-type abbreviations are expanded ({@code ST ->
     * STREET}, {@code RD -> ROAD}, {@code AVE -> AVENUE}). A {@code null} value is
     * left as-is.
     */
    public void setAddress(String address) {
        this.address = normalizeAddress(address);
    }

    /**
     * Normalize an address string to its canonical form: trimmed, whitespace
     * collapsed, upper-cased, with common street-type abbreviations expanded.
     * Returns {@code null} for a {@code null} input; a value that is blank once
     * trimmed normalizes to the empty string.
     */
    public static String normalizeAddress(String address) {
        if (address == null) {
            return null;
        }
        String collapsed = address.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        if (collapsed.isEmpty()) {
            return "";
        }
        String[] tokens = collapsed.split(" ");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(ADDRESS_ABBREVIATIONS.getOrDefault(tokens[i], tokens[i]));
        }
        return sb.toString();
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
     * Set the owner's email address, normalizing it to lower case so it is both
     * stored and returned in a canonical form. A {@code null} value is left as-is.
     */
    public void setEmail(String email) {
        this.email = (email == null) ? null : email.toLowerCase(Locale.ROOT);
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
     * Return the owner's membership number, formatted {@code '<customerCode>-M<YY>'}
     * where {@code YY} is the last two digits of the registration date's year (for
     * example {@code 'SMI-0007-M26'}). Returns {@code null} when either the customer
     * code or the registration date is not set.
     */
    public String getMembershipNumber() {
        if (this.customerCode == null || this.registrationDate == null) {
            return null;
        }
        return String.format("%s-M%02d", this.customerCode, this.registrationDate.getYear() % 100);
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

    /**
     * Return the owner's membership level, a number from {@code 1} to {@code 3}
     * assigned at creation: starting at {@code 1}, plus {@code 1} when an email
     * address is present, plus {@code 1} when the owner has no namesakes (namesake
     * count is {@code 0}), capped at {@code 3}. (Level {@code 4} is reserved for
     * tenure.)
     */
    public Integer getMembershipLevel() {
        int level = 1;
        boolean hasEmail = this.email != null && !this.email.isBlank();
        if (hasEmail) {
            level += 1;
        }
        if (this.namesakeCount != null && this.namesakeCount == 0) {
            level += 1;
        }
        return Math.min(level, 3);
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

    /**
     * Whether more than 80 owners had already been registered on this owner's
     * registration day at the time it was created, flagging a bulk-signup surge.
     */
    public Boolean getBulkSignupWarning() {
        return this.bulkSignupWarning;
    }

    public void setBulkSignupWarning(Boolean bulkSignupWarning) {
        this.bulkSignupWarning = bulkSignupWarning;
    }

    /**
     * Return the owner's locality, derived from the city using the fixed
     * city-to-region table ({@code Sydney -> NSW}, {@code Melbourne -> VIC},
     * {@code Brisbane -> QLD}). Returns the canonical region string, or
     * {@code 'UNKNOWN'} when the city is not in the table.
     */
    public String getLocality() {
        return CITY_REGION.getOrDefault(this.city, "UNKNOWN");
    }

    /**
     * Return the owner's preferred contact channel, derived from the stored
     * contact details: {@code 'EMAIL'} when an email address is present,
     * otherwise {@code 'PHONE'}.
     */
    public String getContactPreference() {
        boolean hasEmail = this.email != null && !this.email.isBlank();
        return hasEmail ? "EMAIL" : "PHONE";
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
