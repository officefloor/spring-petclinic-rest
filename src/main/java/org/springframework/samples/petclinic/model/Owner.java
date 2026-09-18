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

    @Column(name = "address_line1")
    private String addressLine1;

    @Column(name = "address_line2")
    private String addressLine2;

    @Column(name = "address")
    @NotEmpty
    private String address;

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

    @Column(name = "membership_number")
    private String membershipNumber;

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

    @Column(name = "possible_duplicate_of")
    private Integer possibleDuplicateOf;

    @Column(name = "deleted")
    private boolean deleted;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner", fetch = FetchType.EAGER)
    private Set<Pet> pets;

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAddressLine1() {
        return this.addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return this.addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
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

    public Integer getHouseholdSize() {
        return this.householdSize;
    }

    public void setHouseholdSize(Integer householdSize) {
        this.householdSize = householdSize;
    }

    public Integer getMembershipPoints() {
        return this.membershipPoints;
    }

    public void setMembershipPoints(Integer membershipPoints) {
        this.membershipPoints = membershipPoints;
    }

    public Integer getMembershipLevel() {
        return this.membershipLevel;
    }

    public void setMembershipLevel(Integer membershipLevel) {
        this.membershipLevel = membershipLevel;
    }

    /**
     * The id of the existing owner this one is a soft duplicate of: an owner sharing this one's last
     * name and postcode but reachable on a different telephone, recorded at creation. {@code null}
     * when no such owner existed.
     */
    public Integer getPossibleDuplicateOf() {
        return this.possibleDuplicateOf;
    }

    public void setPossibleDuplicateOf(Integer possibleDuplicateOf) {
        this.possibleDuplicateOf = possibleDuplicateOf;
    }

    /**
     * Whether this owner has been soft-deleted: the record is retained but excluded from duplicate and
     * identity checks when creating new owners. A newly created owner is not deleted.
     */
    public boolean isDeleted() {
        return this.deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    /**
     * Whether this owner was flagged as a possible duplicate of an existing one at creation, i.e.
     * whether {@link #getPossibleDuplicateOf()} identifies a matching owner.
     */
    @Transient
    public boolean getPossibleDuplicate() {
        return this.possibleDuplicateOf != null;
    }

    /**
     * The owner's self link: the canonical REST path to this owner, {@code "/api/owners/<id>"}.
     * {@code null} until the owner has been assigned an id.
     */
    @Transient
    public String getSelfLink() {
        return (this.getId() != null) ? "/api/owners/" + this.getId() : null;
    }

    /**
     * The owner's locality: the REGION portion of its {@code customerCode} (see
     * {@link CustomerCode}), which is the region derived from the postcode or city at creation. This
     * disambiguates cities that share a name. Yields {@code "UNKNOWN"} when the owner has no customer
     * code or it resolves to no region.
     */
    @Transient
    public String getLocality() {
        return CustomerCode.regionOf(this.customerCode);
    }

    /**
     * The owner's IANA timezone, derived from its {@link #getLocality() locality} via the fixed
     * region-to-timezone table (see {@link RegionTimezone}). {@code null} when the locality has no
     * known timezone.
     */
    @Transient
    public String getTimezone() {
        return RegionTimezone.of(this.getLocality());
    }

    /**
     * The owner's check digit: the single Luhn check digit computed over the digits of the
     * {@code customerCode}, guarding against transcription errors of the code.
     */
    @Transient
    public int getCheckDigit() {
        return Luhn.checkDigit(this.customerCode);
    }

    /**
     * The owner's salutation: the {@link #getTitle() title} honorific followed by a space and the
     * last name (e.g. {@code "DR who"}), or just the last name when no title was supplied.
     */
    @Transient
    public String getSalutation() {
        return (this.title != null && !this.title.isBlank())
                ? this.title + " " + this.getLastName()
                : this.getLastName();
    }

    /**
     * The owner's preferred contact channel: {@code "EMAIL"} when an email address is
     * present, otherwise {@code "PHONE"}.
     */
    @Transient
    public String getContactPreference() {
        return (this.email != null && !this.email.isBlank()) ? "EMAIL" : "PHONE";
    }

    /**
     * The owner's telephone formatted for humans: the stored E.164 number rendered as its country
     * code, a space, then the national digits grouped in threes (e.g. {@code "+61 412 345 678"}). The
     * raw {@link #getTelephone() telephone} stays in compact E.164. See {@link E164}.
     */
    @Transient
    public String getTelephoneDisplay() {
        return E164.display(this.telephone);
    }

    /**
     * The owner's age band: {@code "MINOR"}, {@code "ADULT"} or {@code "SENIOR"} derived from the
     * birth date against the registration date (see {@link AgeBand}), or {@code null} when no birth
     * date was supplied.
     */
    @Transient
    public String getAgeBand() {
        return AgeBand.classify(this.birthDate, this.registrationDate);
    }

    /**
     * The owner's fiscal year: the {@code FY<YY>} label of the fiscal year (starting 1 July)
     * containing its business-day-adjusted registration date (see {@link FiscalYear}), or
     * {@code null} when no registration date is set.
     */
    @Transient
    public String getFiscalYear() {
        return (this.registrationDate != null) ? FiscalYear.label(this.registrationDate) : null;
    }

    /**
     * The owner's identity key: the single derived value all duplicate detection is expressed
     * through, formed as {@code <normalizedTelephone>|<email or empty>|<householdId or empty>}.
     * Two owners are duplicates only when their whole identity keys are equal, so members of one
     * household (same {@code householdId}) with different telephones have different keys.
     */
    @Transient
    public String getIdentityKey() {
        return blankToEmpty(this.telephone) + '|' + blankToEmpty(this.email) + '|' + blankToEmpty(this.householdId);
    }

    private static String blankToEmpty(String value) {
        return (value == null) ? "" : value;
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
