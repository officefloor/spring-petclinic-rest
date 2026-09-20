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
import org.springframework.samples.petclinic.util.AgeBand;
import org.springframework.samples.petclinic.util.CityLocality;
import org.springframework.samples.petclinic.util.CustomerCode;
import org.springframework.samples.petclinic.util.E164PhoneNumber;
import org.springframework.samples.petclinic.util.FiscalYear;
import org.springframework.samples.petclinic.util.LuhnCheckDigit;
import org.springframework.samples.petclinic.util.RegionTimezone;
import org.springframework.samples.petclinic.util.Sha256;
import org.springframework.samples.petclinic.util.Soundex;

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
    @Pattern(regexp = "^\\+[0-9]{8,15}$", message = "Phone number must be in E.164 form: a '+' followed by 8 to 15 digits")
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

    @Column(name = "membership_level_cap")
    private Integer membershipLevelCap;

    @Column(name = "bulk_signup_warning", nullable = false)
    private boolean bulkSignupWarning;

    @Column(name = "capacity_warning", nullable = false)
    private boolean capacityWarning;

    @Column(name = "possible_duplicate", nullable = false)
    private boolean possibleDuplicate;

    @Column(name = "possible_duplicate_of")
    private Integer possibleDuplicateOf;

    @Column(name = "deleted", nullable = false)
    private boolean deleted;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner", fetch = FetchType.EAGER)
    private Set<Pet> pets;

    /**
     * The owner's personal title (one of MR, MRS, MS or DR), or null when none was
     * supplied. Stored verbatim and used to compose the {@link #getSalutation() salutation}.
     */
    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * The owner's salutation: the {@link #getTitle() title} and last name separated by a
     * space (e.g. {@code "DR Who"}), or just the last name when no title is recorded.
     * Derived from stored state, never persisted.
     */
    public String getSalutation() {
        return (this.title == null || this.title.isEmpty())
            ? getLastName()
            : this.title + " " + getLastName();
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * The first line of the owner's structured address, in normalized form, or null when the
     * owner was created from the flat {@link #getAddress() address} only. The composed
     * {@code address} is derived from this and {@link #getAddressLine2()}.
     */
    public String getAddressLine1() {
        return this.addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    /**
     * The optional second line of the owner's structured address, in normalized form, or null
     * when absent or when the owner was created from the flat {@link #getAddress() address}.
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
     * The stored E.164 telephone formatted for humans: the country code, a space, then the
     * national digits grouped in threes (e.g. {@code "+61 412 345 678"}), while
     * {@link #getTelephone()} keeps the raw E.164 form. Derived from stored state, never
     * persisted.
     */
    public String getTelephoneDisplay() {
        return E164PhoneNumber.toDisplay(this.telephone);
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

    /**
     * The owner's date of birth. Optional, so may be null.
     */
    public LocalDate getBirthDate() {
        return this.birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    /**
     * The owner's age band as at registration: {@code "MINOR"} for under 18,
     * {@code "ADULT"} for 18 to 64, or {@code "SENIOR"} for 65 and over, measured from the
     * birth date against the registration date. Null when no birth date is recorded.
     * Derived from stored state, never persisted.
     */
    public String getAgeBand() {
        if (this.birthDate == null || this.registrationDate == null) {
            return null;
        }
        return AgeBand.of(this.birthDate, this.registrationDate);
    }

    public String getCustomerCode() {
        return this.customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    /**
     * The owner's current primary identifier: the {@link #getCustomerCode() customer code} today.
     * The single source of truth for "which field identifies this owner", so a consumer such as the
     * {@code OWNER_CREATED} audit event always carries the right value; when the customer code is
     * later unified into a member id, this method returns that instead and every consumer follows
     * automatically. Derived from stored state, never persisted.
     */
    public String getPrimaryIdentifier() {
        return this.customerCode;
    }

    /**
     * The stable identifier of the household this owner belongs to: the first twelve hex
     * characters of the SHA-256 of the normalized last name and postcode, so every owner
     * sharing a last name and postcode resolves to the same value. Null when the owner has
     * no postcode. Assigned at creation.
     */
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
     * The ceiling applied to this owner's {@link #getMembershipLevel() membership level}: at
     * creation a new owner's level cannot exceed one above the highest level then held by an
     * existing member of its household (same last name and postcode). Null when no cap
     * applies, i.e. the household had no existing members. Captured at creation.
     */
    public Integer getMembershipLevelCap() {
        return this.membershipLevelCap;
    }

    public void setMembershipLevelCap(Integer membershipLevelCap) {
        this.membershipLevelCap = membershipLevelCap;
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
     * Whether this owner's city was approaching its capacity limit when this owner was
     * created: {@code true} when the city already held between 40 and 49 owners (the hard
     * limit being 50), {@code false} otherwise. Captured at creation.
     */
    public boolean isCapacityWarning() {
        return this.capacityWarning;
    }

    public void setCapacityWarning(boolean capacityWarning) {
        this.capacityWarning = capacityWarning;
    }

    /**
     * Whether this owner resembles an existing one: {@code true} when, at creation, it shared
     * an existing owner's postcode and a phonetically-equal last name ({@link Soundex Soundex})
     * yet differed in {@link #getIdentityKey() identity key} and was not a declared household
     * member, {@code false} otherwise. A declared member (created with {@code sharesHousehold})
     * is a genuine member, not a suspected duplicate. When true, {@link #getPossibleDuplicateOf()}
     * holds that owner's id. Captured at creation.
     */
    public boolean isPossibleDuplicate() {
        return this.possibleDuplicate;
    }

    public void setPossibleDuplicate(boolean possibleDuplicate) {
        this.possibleDuplicate = possibleDuplicate;
    }

    /**
     * The id of the existing owner this owner possibly duplicates (same postcode and a
     * phonetically-equal last name but a different {@link #getIdentityKey() identity key}), or
     * null when it is not a possible duplicate. Captured at creation.
     */
    public Integer getPossibleDuplicateOf() {
        return this.possibleDuplicateOf;
    }

    public void setPossibleDuplicateOf(Integer possibleDuplicateOf) {
        this.possibleDuplicateOf = possibleDuplicateOf;
    }

    /**
     * Whether this owner has been soft-deleted: {@code true} once a {@code DELETE} has
     * flagged the record (which is retained rather than removed), {@code false} for a live
     * owner. A newly created owner is not deleted. A deleted owner is still returned by
     * {@code GET}, but is ignored by the create endpoint's duplicate and identity checks.
     */
    public boolean isDeleted() {
        return this.deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    /**
     * The owner's locality: the canonical region carried in the {@code <REGION>-<HASH8>}
     * customer code, i.e. the same region-and-hash identity used everywhere else. That
     * region was derived at creation by preferring the postcode range (NSW 2000-2099,
     * VIC 3000-3099, QLD 4000-4099), falling back to the fixed city-to-region table
     * (Sydney->NSW, Melbourne->VIC, Brisbane->QLD) when the postcode is absent or in no
     * known range, or {@code "UNKNOWN"} otherwise. For an owner with no customer code the
     * region is derived directly from the stored postcode and city. Derived from stored
     * state, never persisted.
     */
    public String getLocality() {
        String region = CustomerCode.regionOf(this.customerCode);
        return region != null ? region : CityLocality.forPostcodeOrCity(this.postcode, this.city);
    }

    /**
     * The owner's timezone: the IANA name for this owner's {@link #getLocality() locality}
     * via the fixed region-to-timezone table (NSW->Australia/Sydney,
     * VIC->Australia/Melbourne, QLD->Australia/Brisbane), or {@code null} when the region
     * has no known timezone. Derived from stored state, never persisted.
     */
    public String getTimezone() {
        return RegionTimezone.forRegion(getLocality());
    }

    /**
     * The owner's membership points, from 0 upwards: starts at 0, gains 2 when this owner
     * has an email address, gains 1 when this owner had no namesakes at creation
     * ({@code namesakeCount} is 0), gains 2 when this owner's household has 3 or more
     * members, and gains 3 once this owner's tenure spans at least one elapsed fiscal year.
     * A newly created owner has zero tenure, so it never earns those points at creation.
     * Derived from stored state, never persisted.
     */
    public int getMembershipPoints() {
        int points = 0;
        if (hasEmail()) {
            points += 2;
        }
        if (Integer.valueOf(0).equals(this.namesakeCount)) {
            points += 1;
        }
        if (this.householdSize != null && this.householdSize >= 3) {
            points += 2;
        }
        if (getTenureFiscalYears() >= 1) {
            points += 3;
        }
        return points;
    }

    /**
     * The owner's fiscal year: the {@code FY<YY>} label (fiscal year starting 1 July,
     * identified by its ending calendar year) of the {@code registrationDate}, or
     * {@code null} when no registration date is recorded. Derived from stored state, never
     * persisted.
     */
    public String getFiscalYear() {
        return this.registrationDate == null ? null : FiscalYear.labelOf(this.registrationDate);
    }

    /**
     * The owner's membership level, from 1 to 4: derived from {@link #getMembershipPoints()}
     * as level 1 for 0-1 points, 2 for 2-3, 3 for 4-5, and 4 for 6 or more, then capped at
     * the {@link #getMembershipLevelCap() membership level cap} when one applies. Derived from
     * stored state, never persisted.
     */
    public int getMembershipLevel() {
        int level = uncappedMembershipLevel();
        return this.membershipLevelCap == null ? level : Math.min(level, this.membershipLevelCap);
    }

    private int uncappedMembershipLevel() {
        int points = getMembershipPoints();
        if (points <= 1) {
            return 1;
        }
        if (points <= 3) {
            return 2;
        }
        if (points <= 5) {
            return 3;
        }
        return 4;
    }

    /**
     * The owner's tenure in elapsed fiscal years: the number of fiscal years (each
     * starting 1 July) between the {@code registrationDate} and the current server date, or
     * 0 when no registration date is recorded. Never negative in practice, since a future
     * registration date is rejected at creation.
     */
    private int getTenureFiscalYears() {
        if (this.registrationDate == null) {
            return 0;
        }
        return FiscalYear.elapsed(this.registrationDate, LocalDate.now());
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
     * The owner's identity key: the 64-character lower-case SHA-256 hex of the normalized
     * telephone, the lower-cased email (or empty when absent) and the {@link Soundex Soundex}
     * code of the last name, joined by {@code '|'}. Two owners collide on this key only when
     * they share a telephone, an email and a phonetically-equal surname, so it is the single
     * fingerprint the create endpoint's duplicate check keys off. Derived from stored state,
     * never persisted.
     */
    public String getIdentityKey() {
        String telephonePart = this.telephone == null ? "" : this.telephone;
        String emailPart = hasEmail() ? this.email.toLowerCase(Locale.ROOT) : "";
        String namePart = Soundex.of(getLastName());
        return Sha256.lowerHex(telephonePart + "|" + emailPart + "|" + namePart);
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
