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
    @Column(name = "title")
    private String title;

    @Column(name = "address")
    @NotEmpty
    private String address;

    @Column(name = "address_line1")
    private String addressLine1;

    @Column(name = "address_line2")
    private String addressLine2;

    @Column(name = "city")
    @NotEmpty
    private String city;

    @Column(name = "telephone")
    @NotEmpty
    @Pattern(regexp = "^\\+[0-9]{8,15}$", message = "Phone number must be in E.164 form")
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

    @Column(name = "membership_points")
    private Integer membershipPoints;

    @Column(name = "membership_level")
    private Integer membershipLevel;

    @Column(name = "bulk_signup_warning")
    private Boolean bulkSignupWarning;

    @Column(name = "possible_duplicate")
    private Boolean possibleDuplicate;

    @Column(name = "possible_duplicate_of")
    private Integer possibleDuplicateOf;

    @Column(name = "deleted", nullable = false)
    private boolean deleted;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner", fetch = FetchType.EAGER)
    private Set<Pet> pets;

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * The owner's salutation, derived on read from the owner's {@link #title} and last
     * name: {@code "<title> <lastName>"} when a title has been supplied, or just the last
     * name when it has not.
     */
    @Transient
    public String getSalutation() {
        if (this.title == null || this.title.isBlank()) {
            return getLastName();
        }
        return this.title + " " + getLastName();
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * The first line of the owner's structured address, when supplied. Preferred over the
     * flat {@link #getAddress() address} and used to compose it; {@code null} for owners
     * created from the flat address form.
     */
    public String getAddressLine1() {
        return this.addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    /**
     * The optional second line of the owner's structured address; {@code null} when absent.
     * When present it is appended to {@link #getAddressLine1() addressLine1} to compose the
     * {@link #getAddress() address}.
     */
    public String getAddressLine2() {
        return this.addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
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
     * The owner's stored E.164 {@link #telephone} formatted for humans: the country code, a
     * space, then the national digits grouped in threes, e.g. {@code "+61 412 345 678"}.
     * Derived on read; {@code null} until a telephone has been set.
     */
    @Transient
    public String getTelephoneDisplay() {
        return TelephoneDisplay.format(this.telephone);
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

    /**
     * The owner's {@link AgeBand age band} as of their {@link #registrationDate}, derived on
     * read from the owner's {@link #birthDate}. {@code null} until both the birth date and the
     * registration date have been supplied.
     */
    @Transient
    public String getAgeBand() {
        if (this.birthDate == null || this.registrationDate == null) {
            return null;
        }
        return AgeBand.asOf(this.birthDate, this.registrationDate).name();
    }

    public String getCustomerCode() {
        return this.customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    /**
     * The owner's membership number, formatted {@code <customerCode>-M<YY>} where
     * {@code YY} is the last two digits of the {@code registrationDate} year, e.g.
     * {@code "NSW-1A2B3C4D-M26"}. Derived from the owner's own fields; {@code null} until
     * both the customer code and registration date have been assigned.
     */
    @Transient
    public String getMembershipNumber() {
        if (this.customerCode == null || this.registrationDate == null) {
            return null;
        }
        return String.format("%s-M%02d", this.customerCode, this.registrationDate.getYear() % 100);
    }

    /**
     * The owner's check digit: a single {@link Luhn} check digit computed over the digits
     * of the owner's {@link #customerCode}. {@code null} until the customer code has been
     * assigned.
     */
    @Transient
    public Integer getCheckDigit() {
        if (this.customerCode == null) {
            return null;
        }
        return Luhn.checkDigit(this.customerCode);
    }

    /**
     * The owner's region: the canonical region derived from the owner's {@link #postcode}
     * via the fixed {@link RegionPostcodeTable}, falling back to the owner's {@link #city}
     * via the fixed {@link CityRegionTable} when the postcode is absent or in no known
     * range, or {@code "UNKNOWN"} when neither yields a region. This is the {@code REGION}
     * segment of the owner's {@link #getCustomerCode() customer code}.
     */
    @Transient
    public String getRegion() {
        return RegionPostcodeTable.regionOf(this.postcode)
            .orElseGet(() -> CityRegionTable.regionOf(this.city));
    }

    /**
     * The owner's locality, read from its region-and-hash identity: the {@code REGION}
     * segment (everything before the first {@code '-'}) of the owner's
     * {@link #getCustomerCode() customer code}. Falls back to the owner's live
     * {@link #getRegion() region} until a customer code has been assigned.
     */
    @Transient
    public String getLocality() {
        if (this.customerCode == null) {
            return getRegion();
        }
        int dash = this.customerCode.indexOf('-');
        return dash < 0 ? this.customerCode : this.customerCode.substring(0, dash);
    }

    /**
     * The owner's IANA timezone name, derived on read from the owner's
     * {@link #getLocality() locality} via the fixed {@link RegionTimezoneTable}, e.g.
     * {@code "Australia/Sydney"} for {@code NSW}. {@code null} when the locality has no
     * timezone in the table.
     */
    @Transient
    public String getTimezone() {
        return RegionTimezoneTable.timezoneOf(getLocality());
    }

    /**
     * The owner's preferred contact channel, derived on read from the owner's own
     * fields: {@code "EMAIL"} when the owner has an email address, otherwise
     * {@code "PHONE"}.
     */
    @Transient
    public String getContactPreference() {
        return (this.email == null || this.email.isBlank()) ? "PHONE" : "EMAIL";
    }

    /**
     * The owner's identity key: the single value used to detect duplicate owners on
     * create. It joins the owner's (already normalized) telephone, email and household id
     * with {@code '|'} — {@code <telephone>|<email>|<householdId>} — a missing email or
     * household id contributing an empty segment. Two owners are duplicates only when
     * their whole identity keys are equal.
     */
    @Transient
    public String getIdentityKey() {
        return orEmpty(this.telephone) + "|" + orEmpty(this.email) + "|" + orEmpty(this.householdId);
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
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

    /**
     * The owner's membership points, assigned on create by tallying the owner's standing:
     * points for having an email address, for having no namesakes, for a large household and
     * for tenure (see {@code MembershipLevelAssigner}). The points determine the owner's
     * {@link #getMembershipLevel() membership level}.
     */
    public Integer getMembershipPoints() {
        return this.membershipPoints;
    }

    public void setMembershipPoints(Integer membershipPoints) {
        this.membershipPoints = membershipPoints;
    }

    /**
     * The owner's membership level, a number from 1 to 4 assigned on create by banding the
     * owner's {@link #getMembershipPoints() membership points} (see
     * {@code MembershipLevelAssigner}).
     */
    public Integer getMembershipLevel() {
        return this.membershipLevel;
    }

    public void setMembershipLevel(Integer membershipLevel) {
        this.membershipLevel = membershipLevel;
    }

    public Boolean getBulkSignupWarning() {
        return this.bulkSignupWarning;
    }

    public void setBulkSignupWarning(Boolean bulkSignupWarning) {
        this.bulkSignupWarning = bulkSignupWarning;
    }

    /**
     * Whether this owner, when created, softly matched an already-stored owner: one that
     * shares its last name and postcode but carries a different telephone. Assigned on
     * create; when {@code true}, {@link #getPossibleDuplicateOf()} identifies the matched
     * owner.
     */
    public Boolean getPossibleDuplicate() {
        return this.possibleDuplicate;
    }

    public void setPossibleDuplicate(Boolean possibleDuplicate) {
        this.possibleDuplicate = possibleDuplicate;
    }

    /**
     * The id of the already-stored owner this owner softly matched on create (same last
     * name and postcode, different telephone), or {@code null} when it matched none.
     */
    public Integer getPossibleDuplicateOf() {
        return this.possibleDuplicateOf;
    }

    public void setPossibleDuplicateOf(Integer possibleDuplicateOf) {
        this.possibleDuplicateOf = possibleDuplicateOf;
    }

    /**
     * Whether this owner has been soft-deleted. A newly created owner is {@code false};
     * {@code DELETE /api/owners/{id}} flags it {@code true} while retaining the record. Owners
     * flagged deleted are ignored by the create endpoint's duplicate and identity checks.
     */
    public boolean isDeleted() {
        return this.deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
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
            .append("customerCode", this.customerCode)
            .toString();
    }
}
