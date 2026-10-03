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
import org.springframework.samples.petclinic.util.Sha256;
import org.springframework.samples.petclinic.util.Soundex;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.time.Month;
import java.time.Period;
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
    @Pattern(regexp = "^\\+[0-9]{8,15}$", message = "Phone number must be in E.164 form: a '+' followed by 8 to 15 digits")
    private String telephone;

    @Column(name = "email")
    @Email
    private String email;

    @Column(name = "registration_date", columnDefinition = "DATE")
    private LocalDate registrationDate;

    @Column(name = "birth_date", columnDefinition = "DATE")
    private LocalDate birthDate;

    @Column(name = "member_id")
    private String memberId;

    @Column(name = "household_id")
    private String householdId;

    @Column(name = "namesake_count")
    private Integer namesakeCount;

    @Column(name = "bulk_signup_warning")
    private boolean bulkSignupWarning;

    @Column(name = "capacity_warning")
    private boolean capacityWarning;

    @Column(name = "household_member_count")
    private Integer householdMemberCount;

    @Column(name = "membership_level_cap")
    private Integer membershipLevelCap;

    @Column(name = "possible_duplicate")
    private boolean possibleDuplicate;

    @Column(name = "possible_duplicate_of")
    private Integer possibleDuplicateOf;

    @Column(name = "deleted")
    private boolean deleted;

    @Column(name = "idempotency_key")
    private String idempotencyKey;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner", fetch = FetchType.EAGER)
    private Set<Pet> pets;

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Derive the owner's read-only {@code salutation}: the honorific {@code title} and
     * {@code lastName} joined by a single space ({@code '<title> <lastName>'}) when a title has
     * been supplied, otherwise just the {@code lastName}. A {@code null} or blank title yields the
     * bare last name.
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
     * Derive the owner's read-only {@code ageBand} from {@code birthDate}, computed against the
     * owner's {@code registrationDate} (falling back to the current date when no registration date
     * has been assigned yet): {@code MINOR} when under 18, {@code ADULT} from 18 to 64 inclusive,
     * {@code SENIOR} at 65 or older. Returns {@code null} when no {@code birthDate} has been supplied.
     */
    @Transient
    public String getAgeBand() {
        if (this.birthDate == null) {
            return null;
        }
        LocalDate reference = this.registrationDate != null ? this.registrationDate : LocalDate.now();
        int age = Period.between(this.birthDate, reference).getYears();
        if (age < 18) {
            return "MINOR";
        }
        if (age < 65) {
            return "ADULT";
        }
        return "SENIOR";
    }

    public String getMemberId() {
        return this.memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    /**
     * The leading alphabetic REGION segment of a {@code memberId} formatted
     * {@code '<REGION><FY><HASH8><CHK>'} (e.g. {@code 'NSW'} for {@code 'NSW271A2B3C4D5'}), or
     * {@code null} when {@code memberId} is {@code null} or does not begin with a letter. REGION is
     * always a run of letters immediately followed by the 2-digit fiscal year, so the leading letter
     * run is exactly the region code.
     */
    public static String regionSegment(String memberId) {
        if (memberId == null) {
            return null;
        }
        int i = 0;
        while (i < memberId.length() && Character.isLetter(memberId.charAt(i))) {
            i++;
        }
        return i == 0 ? null : memberId.substring(0, i);
    }

    /**
     * The 2-digit fiscal-year (FY) segment of a {@code memberId} formatted
     * {@code '<REGION><FY><HASH8><CHK>'} (e.g. {@code '27'} for {@code 'NSW271A2B3C4D5'}), the two
     * digits immediately following the {@link #regionSegment(String) REGION}. Returns {@code null}
     * when {@code memberId} has no region prefix or is too short to carry a 2-digit FY.
     */
    public static String fiscalYearSegment(String memberId) {
        String region = regionSegment(memberId);
        if (region == null || memberId.length() < region.length() + 2) {
            return null;
        }
        String fy = memberId.substring(region.length(), region.length() + 2);
        for (int i = 0; i < fy.length(); i++) {
            if (fy.charAt(i) < '0' || fy.charAt(i) > '9') {
                return null;
            }
        }
        return fy;
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

    /**
     * Derive the owner's {@code membershipPoints}: a score starting at 0 that adds 2 when an email
     * is present, adds 1 when the owner's name was unique at creation time ({@code namesakeCount}
     * is 0), adds 2 when the owner belongs to a household of three or more members, and adds 3 when
     * the owner's tenure is at least one elapsed fiscal year (measured from the
     * {@code registrationDate} to today, where the fiscal year starts on 1 July).
     */
    @Transient
    public Integer getMembershipPoints() {
        int points = 0;
        if (this.email != null && !this.email.isBlank()) {
            points += 2;
        }
        if (Integer.valueOf(0).equals(this.namesakeCount)) {
            points += 1;
        }
        if (this.householdMemberCount != null && this.householdMemberCount >= 3) {
            points += 2;
        }
        if (hasTenureOverOneFiscalYear()) {
            points += 3;
        }
        return points;
    }

    /**
     * Derive the owner's numeric {@code membershipLevel}, from 1 to 4, by mapping the owner's
     * {@code membershipPoints}: level 1 for 0-1 points, level 2 for 2-3 points, level 3 for 4-5
     * points, and level 4 for 6 or more points. The result is then capped at the owner's
     * {@code membershipLevelCap} when one has been assigned, so a new household member can never
     * exceed one above the highest level already present in their household (see
     * {@link #getMembershipLevelCap()}).
     */
    @Transient
    public Integer getMembershipLevel() {
        int points = getMembershipPoints();
        int level;
        if (points <= 1) {
            level = 1;
        }
        else if (points <= 3) {
            level = 2;
        }
        else if (points <= 5) {
            level = 3;
        }
        else {
            level = 4;
        }
        if (this.membershipLevelCap != null && level > this.membershipLevelCap) {
            return this.membershipLevelCap;
        }
        return level;
    }

    /**
     * The ceiling applied to this owner's derived {@code membershipLevel}: one above the highest
     * {@code membershipLevel} among the owner's household members (same last name and postcode) at
     * creation time. {@code null} when the owner had no existing household member, in which case no
     * cap applies.
     */
    public Integer getMembershipLevelCap() {
        return this.membershipLevelCap;
    }

    public void setMembershipLevelCap(Integer membershipLevelCap) {
        this.membershipLevelCap = membershipLevelCap;
    }

    /**
     * Whether the owner's tenure, measured as the number of elapsed fiscal years from
     * {@code registrationDate} to today (the fiscal year starting on 1 July), is at least one —
     * worth 3 membership points. A missing {@code registrationDate} (and therefore zero tenure)
     * never qualifies.
     */
    @Transient
    private boolean hasTenureOverOneFiscalYear() {
        if (this.registrationDate == null) {
            return false;
        }
        return fiscalYearOf(LocalDate.now()) - fiscalYearOf(this.registrationDate) >= 1;
    }

    /**
     * Derive the owner's read-only {@code fiscalYear}: {@code 'FY<YY>'} where YY is the two-digit
     * fiscal-year (FY) segment carried by the owner's {@code memberId}
     * ({@code '<REGION><FY><HASH8><CHK>'}), so the reported fiscal year and the FY embedded in the
     * memberId stay in lock-step. Falls back to computing YY from the last two digits of the fiscal
     * year of the owner's business-day-adjusted {@code registrationDate} (the fiscal year starting on
     * 1 July, so a registration on or after 1 July belongs to the fiscal year ending the following
     * 30 June) when the owner has no {@code memberId} yet. Returns {@code null} when the owner has
     * neither a {@code memberId} nor a {@code registrationDate}, so the field is omitted from the
     * response.
     */
    @Transient
    public String getFiscalYear() {
        String fy = fiscalYearSegment(this.memberId);
        if (fy != null) {
            return "FY" + fy;
        }
        if (this.registrationDate == null) {
            return null;
        }
        return String.format("FY%02d", fiscalYearOf(this.registrationDate) % 100);
    }

    /**
     * The fiscal year a {@code date} falls in, identified by its ending calendar year. The fiscal
     * year starts on 1 July, so dates on or after 1 July belong to the following year's fiscal year
     * (e.g. 1 July 2026 is fiscal year 2027), while dates from 1 January to 30 June belong to the
     * current year's fiscal year.
     */
    public static int fiscalYearOf(LocalDate date) {
        return date.getMonthValue() >= Month.JULY.getValue() ? date.getYear() + 1 : date.getYear();
    }

    /**
     * Derive the owner's {@code identityKey}, the single value used to detect duplicate owners: the
     * full lower-case hex SHA-256 of {@code normalizedTelephone + '|' + lowerEmail + '|' +
     * soundex(lastName)}. The telephone and email are already stored in their canonical (E.164 /
     * lower-cased) form, so a {@code null} or blank email contributes an empty middle segment, and
     * the last name is reduced to its {@link Soundex} code (empty when there is no last name). Two
     * owners are duplicates only when their whole {@code identityKey} is equal.
     */
    @Transient
    public String getIdentityKey() {
        String telephonePart = this.telephone == null ? "" : this.telephone;
        String emailPart = (this.email == null || this.email.isBlank()) ? "" : this.email;
        String soundexPart = Soundex.encode(getLastName());
        return Sha256.hex(telephonePart + "|" + emailPart + "|" + soundexPart);
    }

    public boolean isBulkSignupWarning() {
        return this.bulkSignupWarning;
    }

    public void setBulkSignupWarning(boolean bulkSignupWarning) {
        this.bulkSignupWarning = bulkSignupWarning;
    }

    /**
     * Whether this owner was flagged at creation as approaching the per-city capacity limit: its city
     * already held between 40 and 49 owners (inclusive) at the moment this owner was created, close to
     * the hard limit of 50. The owner is still created; this flag is advisory.
     */
    public boolean isCapacityWarning() {
        return this.capacityWarning;
    }

    public void setCapacityWarning(boolean capacityWarning) {
        this.capacityWarning = capacityWarning;
    }

    public Integer getHouseholdMemberCount() {
        return this.householdMemberCount;
    }

    public void setHouseholdMemberCount(Integer householdMemberCount) {
        this.householdMemberCount = householdMemberCount;
    }

    /**
     * Whether this owner was flagged at creation as a possible (soft) duplicate: its
     * {@code identityKey} differs from every existing owner's, but it shares an existing owner's
     * {@code soundex(lastName)} and postcode. The owner is still created; this flag is advisory.
     */
    public boolean isPossibleDuplicate() {
        return this.possibleDuplicate;
    }

    public void setPossibleDuplicate(boolean possibleDuplicate) {
        this.possibleDuplicate = possibleDuplicate;
    }

    /**
     * The id of the existing owner this one soft-matched against (same {@code soundex(lastName)} and
     * postcode, different {@code identityKey}), or {@code null} when {@link #isPossibleDuplicate()}
     * is {@code false}.
     */
    public Integer getPossibleDuplicateOf() {
        return this.possibleDuplicateOf;
    }

    public void setPossibleDuplicateOf(Integer possibleDuplicateOf) {
        this.possibleDuplicateOf = possibleDuplicateOf;
    }

    /**
     * Whether this owner has been soft-deleted: DELETE /api/owners/{id} sets this flag rather than
     * removing the row, so the owner is retained and still readable but is ignored by the create
     * endpoint's duplicate and identity checks. A newly created owner is not deleted.
     */
    public boolean isDeleted() {
        return this.deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    /**
     * The {@code Idempotency-Key} the owner was first created under, or {@code null} when the create
     * carried no such header. When a create repeats with an already-seen key, the originally created
     * owner is returned instead of a duplicate being created, so the key is retained to recognise the
     * repeat.
     */
    public String getIdempotencyKey() {
        return this.idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
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
