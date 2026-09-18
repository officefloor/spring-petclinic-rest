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
import org.springframework.samples.petclinic.util.FiscalYear;
import org.springframework.samples.petclinic.util.RegistrationDatePolicy;
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

    @Column(name = "postcode")
    private String postcode;

    @Column(name = "telephone")
    @NotEmpty
    @Pattern(regexp = "^\\+[0-9]{8,15}$", message = "Phone number must be in E.164 form")
    private String telephone;

    @Column(name = "email")
    private String email;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "registration_date")
    private LocalDate registrationDate;

    @Column(name = "member_id")
    private String memberId;

    @Column(name = "household_id")
    private String householdId;

    @Column(name = "namesake_count")
    private Integer namesakeCount;

    @Column(name = "household_size")
    private Integer householdSize;

    @Column(name = "membership_level_cap")
    private Integer membershipLevelCap;

    @Column(name = "possible_duplicate")
    private Boolean possibleDuplicate;

    @Column(name = "possible_duplicate_of")
    private Integer possibleDuplicateOf;

    @Column(name = "deleted", nullable = false)
    private boolean deleted;

    /**
     * Whether this owner was knowingly created into a shared household (the request declared shared
     * membership). A declared member is not treated as a suspected duplicate. Set per request and
     * never persisted.
     */
    @Transient
    private boolean declaredHouseholdMember;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner", fetch = FetchType.EAGER)
    private Set<Pet> pets;

    /**
     * Resolve the registration date at creation time: default it to the server's
     * current date when none was supplied, and roll a weekend date forward to the
     * next business day so every value derived from it (such as the member id's
     * fiscal-year segment) uses the adjusted date.
     */
    @PrePersist
    private void resolveRegistrationDate() {
        this.registrationDate = RegistrationDatePolicy.effectiveDate(this.registrationDate);
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * The owner's salutation: the {@link #title} followed by a space and the
     * {@linkplain #getLastName() last name} (e.g. {@code "DR Who"}), or just the
     * last name when no title is on file.
     */
    public String getSalutation() {
        if (this.title == null || this.title.isEmpty()) {
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

    public String getPostcode() {
        return this.postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    /**
     * The owner's locality: the region component of its {@link #memberId} identity
     * (see {@link MemberId}), or {@code null} when no member id is assigned. The
     * region was derived from the owner's postcode and city when the identity was
     * minted, so the locality now follows the identity rather than being recomputed
     * from the current city and postcode.
     */
    public String getLocality() {
        return MemberId.regionOf(this.memberId);
    }

    /**
     * The owner's timezone: the IANA name for its {@linkplain #getLocality()
     * locality}, from the fixed region-to-timezone table (see
     * {@link RegionTimezone}), or {@code null} when the locality is absent or has
     * no known timezone.
     */
    public String getTimezone() {
        return RegionTimezone.zoneFor(getLocality());
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

    public LocalDate getBirthDate() {
        return this.birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    /**
     * The owner's {@link AgeBand age band} on the {@link #registrationDate}, derived
     * from the {@link #birthDate}, or {@code null} when no birth date is on file.
     */
    public AgeBand getAgeBand() {
        return AgeBand.on(this.birthDate, this.registrationDate);
    }

    public LocalDate getRegistrationDate() {
        return this.registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getMemberId() {
        return this.memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    /**
     * The owner's current primary identifier: the {@link #getMemberId() member id}.
     *
     * <p>Consumers such as audit events depend on "the primary identifier" through this single
     * accessor rather than on whichever field currently holds it, so the identifier the events
     * carry follows the member id automatically.
     */
    public String getPrimaryIdentifier() {
        return getMemberId();
    }

    /**
     * The owner's fiscal year: the {@code 'FY<YY>'} label (e.g. {@code "FY27"}) read from the
     * two-digit fiscal-year segment of its {@link #memberId} (see {@link MemberId} and
     * {@link FiscalYear}), which was set to the fiscal year the {@link #registrationDate} falls
     * in when the identity was minted. {@code null} when no member id is assigned.
     */
    public String getFiscalYear() {
        Integer yearSegment = MemberId.fiscalYearSegmentOf(this.memberId);
        return yearSegment == null ? null : FiscalYear.label(yearSegment);
    }

    public String getHouseholdId() {
        return this.householdId;
    }

    public void setHouseholdId(String householdId) {
        this.householdId = householdId;
    }

    /**
     * The owner's identity key: the single derived value used to detect duplicate owners. It is the
     * 64-character lower-case SHA-256 hex digest (see {@link org.springframework.samples.petclinic.util.Sha256})
     * of the normalized telephone, the lower-cased email (or an empty string when absent) and the
     * {@linkplain org.springframework.samples.petclinic.util.Soundex Soundex} code of the last name,
     * joined with {@code '|'}. Two owners are the same person only when their identity keys are
     * equal; because the telephone is part of the key, household members with different telephones
     * have different keys.
     */
    public String getIdentityKey() {
        String key = String.join("|",
            this.telephone == null ? "" : this.telephone,
            this.email == null ? "" : this.email.toLowerCase(Locale.ROOT),
            Soundex.encode(getLastName()));
        return Sha256.hex(key);
    }

    /** Tenure, in elapsed fiscal years, at or beyond which an owner earns the tenure membership points. */
    private static final long TENURE_FISCAL_YEARS_FOR_POINTS = 1;

    /** Household size at or above which an owner earns the household membership points. */
    private static final int HOUSEHOLD_SIZE_FOR_POINTS = 3;

    /**
     * The owner's membership points, a score derived from the owner's own
     * fields. Starts at {@code 0}, gains {@code 2} when a contact email is on
     * file, gains {@code 1} when the owner has no namesakes, gains {@code 2}
     * when the owner belongs to a household of {@value #HOUSEHOLD_SIZE_FOR_POINTS}
     * or more, and gains {@code 3} when the owner's {@link #getTenureFiscalYears()
     * tenure} reaches {@value #TENURE_FISCAL_YEARS_FOR_POINTS} or more elapsed fiscal years.
     */
    public Integer getMembershipPoints() {
        int points = 0;
        boolean hasEmail = this.email != null && !this.email.isEmpty();
        if (hasEmail) {
            points += 2;
        }
        boolean noNamesakes = this.namesakeCount != null && this.namesakeCount == 0;
        if (noNamesakes) {
            points += 1;
        }
        boolean largeHousehold = this.householdSize != null && this.householdSize >= HOUSEHOLD_SIZE_FOR_POINTS;
        if (largeHousehold) {
            points += 2;
        }
        Long tenureFiscalYears = getTenureFiscalYears();
        if (tenureFiscalYears != null && tenureFiscalYears >= TENURE_FISCAL_YEARS_FOR_POINTS) {
            points += 3;
        }
        return points;
    }

    /**
     * The owner's membership level, a number from 1 to 4 mapped from the owner's
     * {@link #getMembershipPoints() membership points}: {@code 1} for 0-1 points,
     * {@code 2} for 2-3, {@code 3} for 4-5 and {@code 4} for 6 or more.
     *
     * <p>The level is held down to the {@linkplain #getMembershipLevelCap()
     * household ceiling} when one applies, so a new owner never outranks its
     * household by more than one level.
     */
    public Integer getMembershipLevel() {
        int level = pointsToLevel(getMembershipPoints());
        if (this.membershipLevelCap != null && level > this.membershipLevelCap) {
            return this.membershipLevelCap;
        }
        return level;
    }

    private static int pointsToLevel(int points) {
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
     * The ceiling applied to this owner's {@link #getMembershipLevel() membership
     * level}: one above the highest level among its household's existing members
     * when it was created, or {@code null} when no household member capped it.
     */
    public Integer getMembershipLevelCap() {
        return this.membershipLevelCap;
    }

    public void setMembershipLevelCap(Integer membershipLevelCap) {
        this.membershipLevelCap = membershipLevelCap;
    }

    /**
     * The owner's tenure: the number of whole fiscal years elapsed from the
     * {@link #registrationDate} to the server's current date — the count of 1 July
     * boundaries crossed between them (see {@link FiscalYear}), or {@code null} when no
     * registration date is on file. Zero while both dates fall in the same fiscal year,
     * and never negative for a registration date that is today or earlier.
     */
    public Long getTenureFiscalYears() {
        if (this.registrationDate == null) {
            return null;
        }
        return FiscalYear.elapsedYears(this.registrationDate, LocalDate.now());
    }

    /**
     * The owner's marketing segment, formatted {@code '<TIER>_<AREA>'} (see
     * {@link OwnerSegment}). The {@code TIER} follows the {@linkplain
     * #getMembershipLevel() membership level} and the {@code AREA} follows the
     * {@linkplain #getLocality() locality}, so the segment is one of
     * {@code "PREMIUM_METRO"}, {@code "PREMIUM_REGIONAL"}, {@code "STANDARD_METRO"}
     * or {@code "STANDARD_REGIONAL"}.
     */
    public String getOwnerSegment() {
        return OwnerSegment.of(getMembershipLevel(), getLocality());
    }

    /**
     * The owner's preferred contact channel, derived from the fields on file:
     * {@code "EMAIL"} when a contact email is present, otherwise {@code "PHONE"}.
     */
    public String getContactPreference() {
        boolean hasEmail = this.email != null && !this.email.isEmpty();
        return hasEmail ? "EMAIL" : "PHONE";
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
     * Whether this owner, while not a declared household member, shares an existing owner's
     * last name and postcode but carries a different telephone. Determined once,
     * when the owner is created. A declared household member is never a suspected duplicate.
     */
    public Boolean getPossibleDuplicate() {
        return this.possibleDuplicate;
    }

    public void setPossibleDuplicate(Boolean possibleDuplicate) {
        this.possibleDuplicate = possibleDuplicate;
    }

    /**
     * Whether this owner was knowingly created into a shared household. Such a declared member
     * bypasses the household-duplicate block and is not flagged as a suspected duplicate.
     */
    public boolean isDeclaredHouseholdMember() {
        return this.declaredHouseholdMember;
    }

    public void setDeclaredHouseholdMember(boolean declaredHouseholdMember) {
        this.declaredHouseholdMember = declaredHouseholdMember;
    }

    /**
     * The id of the existing owner this owner possibly duplicates, or {@code null}
     * when it has no such match.
     */
    public Integer getPossibleDuplicateOf() {
        return this.possibleDuplicateOf;
    }

    public void setPossibleDuplicateOf(Integer possibleDuplicateOf) {
        this.possibleDuplicateOf = possibleDuplicateOf;
    }

    /**
     * Whether this owner has been soft-deleted: {@code DELETE /api/owners/{id}} flags the owner
     * deleted and retains the row rather than removing it. A soft-deleted owner is still returned by
     * a read, but is ignored by the create endpoint's duplicate and identity checks. A newly created
     * owner is not deleted.
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
            .toString();
    }
}
