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
import org.springframework.samples.petclinic.rest.validation.CustomerCodeGenerator;
import org.springframework.samples.petclinic.rest.validation.LuhnCheckDigit;
import org.springframework.samples.petclinic.rest.validation.TelephoneFormatter;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
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

    @Column(name = "household_size")
    private Integer householdSize;

    @Column(name = "membership_number")
    private String membershipNumber;

    @Column(name = "possible_duplicate_of")
    private Integer possibleDuplicateOf;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner", fetch = FetchType.EAGER)
    private Set<Pet> pets;

    /**
     * Default the registration date to the server's current date when none was
     * supplied, then roll the effective date forward onto a business day so a
     * weekend registration date always lands on the following Monday. Runs only
     * on insert.
     */
    @PrePersist
    private void defaultRegistrationDate() {
        LocalDate effectiveDate = this.registrationDate == null ? LocalDate.now() : this.registrationDate;
        this.registrationDate = BusinessDayAdjuster.toBusinessDay(effectiveDate);
    }

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

    public Integer getPossibleDuplicateOf() {
        return this.possibleDuplicateOf;
    }

    public void setPossibleDuplicateOf(Integer possibleDuplicateOf) {
        this.possibleDuplicateOf = possibleDuplicateOf;
    }

    /**
     * Return whether this owner is a possible duplicate: true when it was created while an existing
     * owner already shared its last name and postcode under a different telephone, i.e. when a
     * {@link #getPossibleDuplicateOf() possible-duplicate-of} owner id is present.
     */
    @Transient
    public boolean getPossibleDuplicate() {
        return this.possibleDuplicateOf != null;
    }

    /**
     * Return this owner's name formatted as {@code LastName, FirstName}.
     */
    @Transient
    public String getDisplayName() {
        return this.getLastName() + ", " + this.getFirstName();
    }

    /**
     * Return this owner's telephone formatted for human display: the country code, a space, then
     * the national digits grouped in threes (e.g. {@code '+61 412 345 678'}). The raw
     * {@link #getTelephone() telephone} remains in canonical E.164 form.
     */
    @Transient
    public String getTelephoneDisplay() {
        return TelephoneFormatter.format(this.telephone);
    }

    /**
     * Return this owner's identity key: the derived value formed as
     * {@code normalizedTelephone + '|' + email + '|' + householdId} (an absent email or household id
     * contributes an empty segment). Members of the same household share a household id but, having
     * different telephones, still have distinct identity keys. The telephone and email are expected
     * to already be in their normalized storage form.
     */
    @Transient
    public String getIdentityKey() {
        return orEmpty(this.telephone) + "|" + orEmpty(this.email) + "|" + orEmpty(this.householdId);
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }

    /**
     * Return this owner's locality: the REGION segment of the owner's
     * {@link #getCustomerCode() customer code}, which is the region the identity was minted for.
     * When no customer code has been assigned yet the region is resolved directly from the
     * {@link #getPostcode() postcode} and {@link #getCity() city} via {@link RegionResolver}.
     */
    @Transient
    public String getLocality() {
        if (this.customerCode != null && !this.customerCode.isBlank()) {
            return CustomerCodeGenerator.regionOf(this.customerCode);
        }
        return RegionResolver.regionFor(this.postcode, this.city);
    }

    /**
     * Return this owner's membership points: starting at 0, plus 2 when the owner has an email,
     * plus 1 when the owner is uniquely named (a {@link #getNamesakeCount() namesakeCount} of 0),
     * plus 2 for a household of three or more ({@link #getHouseholdSize() household size}), plus 3
     * for long tenure ({@link #getTenureDays() tenure} of more than 365 days).
     */
    @Transient
    public int getMembershipPoints() {
        boolean hasEmail = this.email != null && !this.email.isEmpty();
        boolean uniquelyNamed = Integer.valueOf(0).equals(this.namesakeCount);
        boolean largeHousehold = this.householdSize != null && this.householdSize >= 3;
        boolean longTenure = getTenureDays() > 365;
        return MembershipPoints.of(hasEmail, uniquelyNamed, largeHousehold, longTenure);
    }

    /**
     * Return this owner's membership level: the band their {@link #getMembershipPoints() membership
     * points} fall into — 1 for 0-1 points, 2 for 2-3, 3 for 4-5, 4 for 6 or more.
     */
    @Transient
    public int getMembershipLevel() {
        return MembershipPoints.levelFor(getMembershipPoints());
    }

    /**
     * Return this owner's tenure in whole days: the number of days elapsed from the
     * {@link #getRegistrationDate() registration date} to today. A newly created owner
     * whose registration date is today has zero tenure. Zero when no registration date
     * is known.
     */
    @Transient
    public long getTenureDays() {
        if (this.registrationDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(this.registrationDate, LocalDate.now());
    }

    /**
     * Return this owner's check digit: the single Luhn check digit computed over the
     * digits of the {@link #getCustomerCode() customer code}.
     */
    @Transient
    public int getCheckDigit() {
        return LuhnCheckDigit.of(this.customerCode);
    }

    /**
     * Return this owner's preferred contact channel: {@code EMAIL} when an
     * {@link #getEmail() email} is present, otherwise {@code PHONE}.
     */
    @Transient
    public String getContactPreference() {
        boolean hasEmail = this.email != null && !this.email.isEmpty();
        return hasEmail ? "EMAIL" : "PHONE";
    }

    /**
     * Return this owner's age band as of the {@link #getRegistrationDate() registration date},
     * derived from the {@link #getBirthDate() birth date}: {@code MINOR} under 18, {@code ADULT}
     * from 18 to 64, {@code SENIOR} at 65 or older. Null when no birth date is known.
     */
    @Transient
    public String getAgeBand() {
        AgeBand ageBand = AgeBand.asOf(this.birthDate, this.registrationDate);
        return ageBand == null ? null : ageBand.name();
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
