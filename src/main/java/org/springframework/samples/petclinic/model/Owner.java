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

    @Column(name = "member_id")
    private String memberId;

    @Column(name = "household_id")
    private String householdId;

    @Column(name = "namesake_count")
    private Integer namesakeCount;

    @Column(name = "bulk_signup_warning")
    private Boolean bulkSignupWarning;

    @Column(name = "capacity_warning")
    private Boolean capacityWarning;

    @Column(name = "household_size")
    private Integer householdSize;

    @Column(name = "possible_duplicate")
    private Boolean possibleDuplicate;

    @Column(name = "possible_duplicate_of")
    private Integer possibleDuplicateOf;

    @Column(name = "membership_level_cap")
    private Integer membershipLevelCap;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner", fetch = FetchType.EAGER)
    private Set<Pet> pets;

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * The owner's salutation, composed from its {@link #getTitle() title} and
     * {@link #getLastName() last name}: {@code title + ' ' + lastName} when a title is
     * present, or just the last name when no title was supplied. Derived (not persisted);
     * recomputed each call. See {@link OwnerDerivations#salutation(String, String)}.
     *
     * @return the composed salutation
     */
    @Transient
    public String getSalutation() {
        return OwnerDerivations.salutation(this.title, this.getLastName());
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public String getCity() {
        return this.city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    /**
     * The owner's locality, i.e. the plain canonical region it belongs to, derived from the
     * owner's own fields (see {@link #regionForOwner()}). This is the user-facing region code
     * (for example {@code NSW}) and never carries the {@code V2} version tag mixed into the
     * owner's identifiers. Derived (not persisted); recomputed each call. See
     * {@link OwnerDerivations#locality(String, String)} for the full derivation.
     *
     * @return the canonical region string, or {@code "UNKNOWN"}
     */
    @Transient
    public String getLocality() {
        return OwnerDerivations.locality(this.postcode, this.city);
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
     * The owner's IANA timezone name, derived from its {@link #getLocality() locality}
     * (canonical region) via the fixed region-to-timezone table. Derived (not persisted);
     * recomputed each call and {@code null} when the locality is not a known region. See
     * {@link OwnerDerivations#timezone(String)} for the derivation.
     *
     * @return the IANA timezone name, or {@code null}
     */
    @Transient
    public String getTimezone() {
        return OwnerDerivations.timezone(this.getLocality());
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
     * The owner's membership points, a non-negative score derived from its factors.
     * Derived (not persisted); recomputed from the current fields each call, so it rises
     * once the owner's tenure since {@link #registrationDate} exceeds 365 days. See
     * {@link OwnerDerivations#membershipPoints(String, Integer, Integer, LocalDate, LocalDate)}
     * for how it is scored.
     *
     * @return the membership points, 0 or more
     */
    @Transient
    public Integer getMembershipPoints() {
        return OwnerDerivations.membershipPoints(this.email, this.namesakeCount, this.householdSize,
            this.registrationDate, LocalDate.now());
    }

    /**
     * The owner's membership level, a number from 1 to 4, mapped from its
     * {@link #getMembershipPoints() membership points}. Derived (not persisted);
     * recomputed from the current fields each call, so it rises to level 4 once the
     * owner's tenure since {@link #registrationDate} exceeds 365 days. The level is then
     * held to this owner's household level ceiling ({@link #getMembershipLevelCap()}): when
     * a ceiling was assigned at creation the reported level is at most that ceiling, one
     * above the highest level then held by an existing household member. See
     * {@link OwnerDerivations#membershipLevel(int, Integer)} for the mapping.
     *
     * @return the membership level, from 1 to 4
     */
    @Transient
    public Integer getMembershipLevel() {
        return OwnerDerivations.membershipLevel(getMembershipPoints(), this.membershipLevelCap);
    }

    /**
     * The owner's marketing segment, formatted {@code <TIER>_<AREA>}, one of
     * {@code PREMIUM_METRO}, {@code PREMIUM_REGIONAL}, {@code STANDARD_METRO} or
     * {@code STANDARD_REGIONAL}: TIER is {@code PREMIUM} when its
     * {@link #getMembershipLevel() membership level} is 3 or more (otherwise
     * {@code STANDARD}) and AREA is {@code METRO} when its {@link #getLocality() locality}
     * is a known region (otherwise {@code REGIONAL}). Derived (not persisted); recomputed
     * each call. See {@link OwnerDerivations#ownerSegment(int, String)}.
     *
     * @return the owner's segment as {@code <TIER>_<AREA>}
     */
    @Transient
    public String getOwnerSegment() {
        return OwnerDerivations.ownerSegment(getMembershipLevel(), this.getLocality());
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

    /**
     * The stored E.164 {@link #telephone} formatted for humans: a leading '+'
     * and country code, a space, then the national digits grouped in threes
     * (e.g. {@code "+61 412 345 678"}). Derived (not persisted); recomputed each
     * call and {@code null} when no telephone is present. See
     * {@link Telephones#toDisplay(String)} for the exact format.
     *
     * @return the human-formatted telephone, or {@code null} when absent
     */
    @Transient
    public String getTelephoneDisplay() {
        return Telephones.toDisplay(this.telephone);
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

    /**
     * The owner's fiscal year, formatted {@code FY<YY>}, derived from its
     * business-day-adjusted {@link #registrationDate} (the fiscal year starts on 1 July).
     * Derived (not persisted); recomputed each call and {@code null} when no registration
     * date is present. See {@link OwnerDerivations#fiscalYear(LocalDate)} for the derivation.
     *
     * @return the fiscal year as {@code FY<YY>}, or {@code null}
     */
    @Transient
    public String getFiscalYear() {
        return OwnerDerivations.fiscalYear(this.registrationDate);
    }

    public String getMemberId() {
        return this.memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
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

    public Boolean getBulkSignupWarning() {
        return this.bulkSignupWarning;
    }

    public void setBulkSignupWarning(Boolean bulkSignupWarning) {
        this.bulkSignupWarning = bulkSignupWarning;
    }

    public Boolean getCapacityWarning() {
        return this.capacityWarning;
    }

    public void setCapacityWarning(Boolean capacityWarning) {
        this.capacityWarning = capacityWarning;
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
     * Whether this owner is flagged as risky: {@code true} when any of its risk signals
     * holds — it is a {@link #getPossibleDuplicate() possible duplicate}, its email domain
     * is disposable-adjacent (on the disposable-domain blocklist), or its city was over its
     * soft capacity at creation ({@link #getCapacityWarning() capacity warning}) — otherwise
     * {@code false}. Derived (not persisted); recomputed each call. See
     * {@link OwnerDerivations#riskFlag(Boolean, String, Boolean)}.
     *
     * @return {@code true} when any risk signal holds, otherwise {@code false}
     */
    @Transient
    public Boolean getRiskFlag() {
        return OwnerDerivations.riskFlag(this.possibleDuplicate, this.email, this.capacityWarning);
    }

    /**
     * The household level ceiling assigned to this owner at creation: one above the highest
     * {@link #getMembershipLevel() membership level} then held by an existing member of its
     * household, or {@code null} when the owner joined no existing household (so no ceiling
     * applies). Persisted, and used by {@link #getMembershipLevel()} to cap the reported
     * level.
     *
     * @return the household level ceiling, or {@code null} when uncapped
     */
    public Integer getMembershipLevelCap() {
        return this.membershipLevelCap;
    }

    public void setMembershipLevelCap(Integer membershipLevelCap) {
        this.membershipLevelCap = membershipLevelCap;
    }

    public Boolean getDeleted() {
        return this.deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    /**
     * The derived key used for duplicate detection: the SHA-256 hex digest over this
     * owner's normalised telephone, lower-cased email and the {@link #soundex(String)
     * Soundex} of its last name. Two owners are duplicates exactly when their whole
     * identity keys are equal, so because the telephone is part of the key, members of
     * the same household with different telephones have different keys. Derived (not
     * persisted); recomputed each call. See
     * {@link OwnerDerivations#identityKey(String, String, String)} for the exact format.
     *
     * @return the owner's identity key
     */
    @Transient
    public String getIdentityKey() {
        return OwnerDerivations.identityKey(this.telephone, this.email, this.getLastName());
    }

    /**
     * The American Soundex code of the given name. This is the last-name comparison used
     * by both the identity key and the soft-match duplicate check; see
     * {@link OwnerDerivations#soundex(String)} for the exact algorithm.
     *
     * @param value the name to encode (may be {@code null})
     * @return the four-character Soundex code, or the empty string
     */
    public static String soundex(String value) {
        return OwnerDerivations.soundex(value);
    }

    /**
     * Compute the unified member id to assign to this owner at creation, formatted
     * {@code <REGION><FY><HASH8><CHK>} from the owner's (already normalised) fields and
     * registration date. This is the value stored in {@link #getMemberId()}; see
     * {@link OwnerDerivations#memberId(String, String, String, String, LocalDate)} for the
     * exact format.
     *
     * @return the member id to assign
     */
    public String computeMemberId() {
        return OwnerDerivations.memberId(this.postcode, this.city, this.telephone,
            this.getLastName(), this.registrationDate);
    }

    /**
     * Compute the stable household identifier for this owner, derived deterministically
     * from its last name and postcode so every owner sharing a last name and postcode
     * resolves to the same value. This is the value stored in {@link #getHouseholdId()};
     * see {@link OwnerDerivations#householdId(String, String)} for the exact derivation.
     *
     * @return the household identifier
     */
    public String computeHouseholdId() {
        return OwnerDerivations.householdId(normalize(this.getLastName()), this.postcode);
    }

    /**
     * Whether this owner belongs to the same household as {@code other}: that is, they
     * share the same last name and postcode. The last name is compared
     * case-insensitively with runs of whitespace collapsed to a single space (and
     * surrounding whitespace trimmed); the postcode is compared for exact equality (two
     * absent postcodes count as equal). Owners in the same household resolve to the same
     * {@link #computeHouseholdId() household identifier}.
     *
     * @param other the owner to compare against
     * @return {@code true} if both owners share a household
     */
    public boolean sameHouseholdAs(Owner other) {
        return normalize(this.getLastName()).equals(normalize(other.getLastName()))
            && Objects.equals(this.postcode, other.postcode);
    }

    /**
     * Normalise a value for case-insensitive, whitespace-insensitive comparison:
     * trim, collapse internal runs of whitespace to a single space and lower-case.
     */
    private static String normalize(String value) {
        return OwnerDerivations.collapseWhitespace(value).toLowerCase();
    }

    /**
     * Reduce an address to its canonical stored form. This is the value stored,
     * returned and used for every household comparison; see
     * {@link OwnerDerivations#normalizeAddress(String)} for the exact normalisation.
     *
     * @param value the raw address (may be {@code null})
     * @return the normalised address, or the empty string if {@code value} is
     *         {@code null} or blank
     */
    public static String normalizeAddress(String value) {
        return OwnerDerivations.normalizeAddress(value);
    }

    /**
     * Whether the given (already lower-cased) email address belongs to a disposable,
     * throw-away mailbox domain on the blocklist. This is the disposable-domain check
     * applied at creation to reject such addresses; see
     * {@link OwnerDerivations#emailDomainIsDisposable(String)} for the exact rule.
     *
     * @param email the owner's normalised (lower-cased) email address
     * @return {@code true} if the email's domain is on the disposable-domain blocklist
     */
    public static boolean emailDomainIsDisposable(String email) {
        return OwnerDerivations.emailDomainIsDisposable(email);
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
