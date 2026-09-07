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
    @Pattern(regexp = "^\\+[0-9]{8,15}$", message = "Telephone must be in E.164 form")
    private String telephone;

    @Column(name = "email")
    @Email
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

    @Column(name = "membership_number")
    private String membershipNumber;

    @Column(name = "bulk_signup_warning")
    private Boolean bulkSignupWarning;

    @Column(name = "household_size")
    private Integer householdSize;

    @Column(name = "possible_duplicate")
    private Boolean possibleDuplicate;

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

    /**
     * The owner's locality, i.e. the canonical region it belongs to (read from the
     * assigned {@link #customerCode} when present, otherwise derived from the owner's
     * own fields via {@link #regionForOwner()}). Derived (not persisted); recomputed
     * each call. See {@link OwnerDerivations#locality(String, String, String)} for the
     * full derivation.
     *
     * @return the canonical region string, or {@code "UNKNOWN"}
     */
    @Transient
    public String getLocality() {
        return OwnerDerivations.locality(this.customerCode, this.postcode, this.city);
    }

    /**
     * The canonical region derived for this owner from its own fields. Recomputed each
     * call. See {@link OwnerDerivations#region(String, String)} for the derivation.
     *
     * @return the canonical region string, or {@code "UNKNOWN"}
     */
    public String regionForOwner() {
        return OwnerDerivations.region(this.postcode, this.city);
    }

    /**
     * Whether this owner's postcode is consistent with its {@link #getLocality()
     * locality}: an absent postcode is accepted, a present one must be exactly four
     * digits and, when the locality has a known postcode range, fall within it. See
     * {@link OwnerDerivations#postcodeMatchesRegion(String, String)}.
     *
     * @return {@code true} if the postcode is absent or valid for the owner's locality
     */
    public boolean postcodeValidForLocality() {
        return OwnerDerivations.postcodeMatchesRegion(this.postcode, this.getLocality());
    }

    /**
     * The owner's membership level, a number from 1 to 3. Derived (not persisted);
     * recomputed from the current fields each call. See
     * {@link OwnerDerivations#membershipLevel(String, Integer)} for how it is assigned.
     *
     * @return the membership level, from 1 to 3
     */
    @Transient
    public Integer getMembershipLevel() {
        return OwnerDerivations.membershipLevel(this.email, this.namesakeCount);
    }

    /**
     * The owner's preferred contact channel, {@code "EMAIL"} or {@code "PHONE"}.
     * Derived (not persisted); recomputed from the current fields each call. See
     * {@link OwnerDerivations#contactPreference(String)}.
     *
     * @return {@code "EMAIL"} or {@code "PHONE"}
     */
    @Transient
    public String getContactPreference() {
        return OwnerDerivations.contactPreference(this.email);
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

    /**
     * The owner's age band, {@code "MINOR"}, {@code "ADULT"} or {@code "SENIOR"},
     * derived from {@link #birthDate} measured against {@link #registrationDate}.
     * Derived (not persisted); recomputed each call and {@code null} when no birth
     * date has been supplied. See
     * {@link OwnerDerivations#ageBand(LocalDate, LocalDate)} for the derivation.
     *
     * @return the age band, or {@code null} when no birth date is present
     */
    @Transient
    public String getAgeBand() {
        return OwnerDerivations.ageBand(this.birthDate, this.registrationDate);
    }

    public String getCustomerCode() {
        return this.customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    /**
     * The Luhn check digit (0-9) computed over the digits in this owner's
     * {@link #customerCode}. Derived (not persisted); recomputed each call. See
     * {@link OwnerDerivations#checkDigit(String)}.
     *
     * @return the Luhn check digit, from 0 to 9
     */
    @Transient
    public Integer getCheckDigit() {
        return OwnerDerivations.checkDigit(this.customerCode);
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

    public Boolean getBulkSignupWarning() {
        return this.bulkSignupWarning;
    }

    public void setBulkSignupWarning(Boolean bulkSignupWarning) {
        this.bulkSignupWarning = bulkSignupWarning;
    }

    public Integer getHouseholdSize() {
        return this.householdSize;
    }

    public void setHouseholdSize(Integer householdSize) {
        this.householdSize = householdSize;
    }

    public Boolean getPossibleDuplicate() {
        return this.possibleDuplicate;
    }

    public void setPossibleDuplicate(Boolean possibleDuplicate) {
        this.possibleDuplicate = possibleDuplicate;
    }

    public Integer getPossibleDuplicateOf() {
        return this.possibleDuplicateOf;
    }

    public void setPossibleDuplicateOf(Integer possibleDuplicateOf) {
        this.possibleDuplicateOf = possibleDuplicateOf;
    }

    /**
     * The derived key used for duplicate detection, combining this owner's normalised
     * telephone, email and {@link #householdId}. Two owners are duplicates exactly when
     * their whole identity keys are equal, so members of the same household with
     * different telephones have different keys. Derived (not persisted); recomputed each
     * call. See {@link OwnerDerivations#identityKey(String, String, String)} for the
     * exact format.
     *
     * @return the owner's identity key
     */
    @Transient
    public String getIdentityKey() {
        return OwnerDerivations.identityKey(this.telephone, this.email, this.householdId);
    }

    /**
     * A stable, normalised key identifying this owner's household (same last name
     * and address). The last name is folded case- and whitespace-insensitively and
     * the address is reduced to its canonical form exactly as in
     * {@link #sameHouseholdAs(Owner)}, so two owners share a key iff they share a
     * household.
     *
     * @return the household key
     */
    public String householdKey() {
        return normalize(this.getLastName()) + " " + normalizeAddress(this.address);
    }

    /**
     * Whether this owner belongs to the same household as {@code other}: that is,
     * they share the same last name and address. The last name is compared
     * case-insensitively with runs of whitespace collapsed to a single space (and
     * surrounding whitespace trimmed); the address is compared in its canonical
     * normalised form (see {@link #normalizeAddress(String)}).
     *
     * @param other the owner to compare against
     * @return {@code true} if both owners share a household
     */
    public boolean sameHouseholdAs(Owner other) {
        return normalize(this.getLastName()).equals(normalize(other.getLastName()))
            && normalizeAddress(this.address).equals(normalizeAddress(other.address));
    }

    /**
     * Normalise a value for case-insensitive, whitespace-insensitive comparison:
     * trim, collapse internal runs of whitespace to a single space and lower-case.
     */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    /**
     * Reduce an address to its canonical stored form: trim and collapse runs of
     * whitespace to a single space, upper-case, and expand common abbreviations
     * ({@code ST -> STREET}, {@code RD -> ROAD}, {@code AVE -> AVENUE}) whenever
     * they appear as whole words. The result is what is stored, returned and used
     * for every household comparison; it is idempotent, so re-normalising an
     * already-normalised address leaves it unchanged.
     *
     * @param value the raw address (may be {@code null})
     * @return the normalised address, or the empty string if {@code value} is
     *         {@code null} or blank
     */
    public static String normalizeAddress(String value) {
        if (value == null) {
            return "";
        }
        String collapsed = value.trim().replaceAll("\\s+", " ").toUpperCase();
        if (collapsed.isEmpty()) {
            return "";
        }
        String[] tokens = collapsed.split(" ");
        for (int i = 0; i < tokens.length; i++) {
            tokens[i] = switch (tokens[i]) {
                case "ST" -> "STREET";
                case "RD" -> "ROAD";
                case "AVE" -> "AVENUE";
                default -> tokens[i];
            };
        }
        return String.join(" ", tokens);
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
