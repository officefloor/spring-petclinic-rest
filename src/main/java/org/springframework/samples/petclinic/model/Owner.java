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
    @Pattern(regexp = "^\\+[0-9]{8,15}$", message = "Phone number must be in E.164 form: a '+' followed by 8 to 15 digits")
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

    @Column(name = "membership_number")
    private String membershipNumber;

    @Column(name = "bulk_signup_warning")
    private Boolean bulkSignupWarning;

    @Column(name = "household_size")
    private Integer householdSize;

    @Column(name = "postcode")
    private String postcode;

    @Column(name = "possible_duplicate_of")
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

    /**
     * Return this owner's stored E.164 {@link #getTelephone() telephone} formatted for human display
     * as the calling code, a space, then the national digits grouped in threes, e.g.
     * {@code "+61 412 345 678"}. See {@link TelephoneFormatter}.
     */
    @Transient
    public String getTelephoneDisplay() {
        return TelephoneFormatter.toDisplay(this.telephone);
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

    public Integer getNamesakeCount() {
        return this.namesakeCount;
    }

    public void setNamesakeCount(Integer namesakeCount) {
        this.namesakeCount = namesakeCount;
    }

    public String getMembershipNumber() {
        return this.membershipNumber;
    }

    public void setMembershipNumber(String membershipNumber) {
        this.membershipNumber = membershipNumber;
    }

    /**
     * Whether this owner was created after more than 80 owners had already been registered on the
     * same day, flagging an unusually high-volume ("bulk") signup. Captured once at creation.
     */
    public Boolean getBulkSignupWarning() {
        return this.bulkSignupWarning;
    }

    public void setBulkSignupWarning(Boolean bulkSignupWarning) {
        this.bulkSignupWarning = bulkSignupWarning;
    }

    /**
     * The number of owners in this owner's household (owners sharing the same {@code householdId},
     * counting this owner) captured once at creation.
     */
    public Integer getHouseholdSize() {
        return this.householdSize;
    }

    public void setHouseholdSize(Integer householdSize) {
        this.householdSize = householdSize;
    }

    /**
     * This owner's 4-digit postcode, or {@code null} when none was supplied. When present it is
     * validated against the owner's city region at creation (see {@code PostcodeValidator}).
     */
    public String getPostcode() {
        return this.postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    /**
     * The id of an existing owner this owner was flagged as a possible (soft) duplicate of at
     * creation, or {@code null} when none. Because owners sharing a last name and postcode now form
     * the same household — rejected as a household duplicate unless declared via {@code sharesHousehold},
     * and a declared member is not a suspected duplicate — this is not populated at creation.
     * Captured once at creation.
     */
    public Integer getPossibleDuplicateOf() {
        return this.possibleDuplicateOf;
    }

    public void setPossibleDuplicateOf(Integer possibleDuplicateOf) {
        this.possibleDuplicateOf = possibleDuplicateOf;
    }

    /**
     * Whether this owner was flagged as a possible (soft) duplicate of an existing owner at creation,
     * i.e. whether {@link #getPossibleDuplicateOf()} is set.
     */
    @Transient
    public Boolean getPossibleDuplicate() {
        return this.possibleDuplicateOf != null;
    }

    /**
     * Return this owner's name formatted for display as {@code "LastName, FirstName"}.
     */
    @Transient
    public String getDisplayName() {
        return this.getLastName() + ", " + this.getFirstName();
    }

    /**
     * Return this owner's initials as the upper-cased first letters of the first and last
     * name, dot-separated with a trailing dot, e.g. {@code "J.S."}.
     */
    @Transient
    public String getInitials() {
        return initial(this.getFirstName()) + initial(this.getLastName());
    }

    private static String initial(String name) {
        return Character.toUpperCase(name.charAt(0)) + ".";
    }

    /** The membership level every owner starts at on creation. */
    private static final int BASE_MEMBERSHIP_LEVEL = 1;

    /** The highest membership level attainable; only reached with qualifying tenure. */
    private static final int MAX_MEMBERSHIP_LEVEL = 4;

    /** The tenure, in days, that must be exceeded to qualify for the top membership level. */
    private static final long MEMBERSHIP_TENURE_THRESHOLD_DAYS = 365;

    /**
     * Return this owner's membership level, a number from 1 to 4: every owner starts at
     * {@value #BASE_MEMBERSHIP_LEVEL}; add 1 when an email address is present; add 1 when the owner
     * has no namesakes ({@code namesakeCount} is 0); add 1 when the owner has qualifying tenure
     * (more than {@value #MEMBERSHIP_TENURE_THRESHOLD_DAYS} days since registration); capped at
     * {@value #MAX_MEMBERSHIP_LEVEL}. Because a newly created owner has zero tenure, a new owner
     * never exceeds level 3.
     */
    @Transient
    public Integer getMembershipLevel() {
        int level = BASE_MEMBERSHIP_LEVEL;
        if (hasEmail()) {
            level++;
        }
        if (Integer.valueOf(0).equals(this.namesakeCount)) {
            level++;
        }
        if (hasQualifyingTenure()) {
            level++;
        }
        return Math.min(level, MAX_MEMBERSHIP_LEVEL);
    }

    /**
     * Whether this owner's tenure exceeds {@value #MEMBERSHIP_TENURE_THRESHOLD_DAYS} days, i.e.
     * more than that many days have elapsed since the {@code registrationDate}. Owners without a
     * registration date have no measurable tenure and do not qualify.
     */
    private boolean hasQualifyingTenure() {
        if (this.registrationDate == null) {
            return false;
        }
        long tenureDays = ChronoUnit.DAYS.between(this.registrationDate, LocalDate.now());
        return tenureDays > MEMBERSHIP_TENURE_THRESHOLD_DAYS;
    }

    /** Whether this owner has a usable email address (present and not blank). */
    private boolean hasEmail() {
        return this.email != null && !this.email.isBlank();
    }

    /**
     * Return this owner's preferred contact channel: {@code "EMAIL"} when an email address is
     * present, otherwise {@code "PHONE"}.
     */
    @Transient
    public String getContactPreference() {
        return hasEmail() ? "EMAIL" : "PHONE";
    }

    /**
     * Return this owner's age band derived from the {@code birthDate} as of the
     * {@code registrationDate}, or {@code null} when no birth date is known.
     */
    @Transient
    public AgeBand getAgeBand() {
        if (this.birthDate == null || this.registrationDate == null) {
            return null;
        }
        return AgeBand.asOf(this.birthDate, this.registrationDate);
    }

    /**
     * Return this owner's locality: the region segment of the {@code customerCode} (the leading
     * {@code <REGION>} of {@code <REGION>-<HASH8>}), which is fixed at creation from the postcode
     * (preferred, matched against the known region ranges) falling back to the city-to-region table,
     * or {@code "UNKNOWN"} when neither yields a region. Before the customer code is assigned the
     * region is resolved directly from the postcode and city.
     */
    @Transient
    public String getLocality() {
        if (this.customerCode == null) {
            return LocalityResolver.regionFor(this.postcode, this.city);
        }
        int separator = this.customerCode.indexOf('-');
        return separator < 0 ? this.customerCode : this.customerCode.substring(0, separator);
    }

    /**
     * Return this owner's check digit: the single Luhn check digit computed over the digits of the
     * {@code customerCode}.
     */
    @Transient
    public Integer getCheckDigit() {
        return this.customerCode == null ? null : LuhnCheckDigit.of(this.customerCode);
    }

    /**
     * Return this owner's identity key, the single derived value all duplicate detection is based
     * on: the normalized telephone, email and household id joined with {@code '|'} as
     * {@code telephone + "|" + email + "|" + householdId}, with each absent component rendered as
     * the empty string. Two owners are duplicates only when their whole identity keys are equal;
     * because the telephone is part of the key, members of one household (same {@code householdId})
     * with different telephones have distinct keys and are not duplicates.
     */
    @Transient
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
            .append("registrationDate", this.registrationDate)
            .toString();
    }
}
