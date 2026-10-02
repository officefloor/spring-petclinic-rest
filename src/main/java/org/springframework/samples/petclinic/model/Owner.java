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
    @Pattern(regexp = "^\\+[0-9]{8,15}$", message = "Phone number must be in E.164 form (+ followed by 8 to 15 digits)")
    private String telephone;

    @Column(name = "email")
    private String email;

    @Column(name = "title")
    private String title;

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

    @Column(name = "postcode")
    private String postcode;

    @Column(name = "deleted", nullable = false)
    private boolean deleted;

    @Transient
    private Integer householdSize;

    @Transient
    private Integer membershipLevelCap;

    @Transient
    private boolean bulkSignupWarning;

    @Transient
    private boolean capacityWarning;

    @Transient
    private boolean possibleDuplicate;

    @Transient
    private Integer possibleDuplicateOf;

    @Transient
    private boolean riskFlag;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner", fetch = FetchType.EAGER)
    private Set<Pet> pets;

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

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    /**
     * The number of owners in this owner's household (owners sharing its householdId, counting
     * this owner). Not a persisted column — populated by the owner pipelines (create and read)
     * that have repository access, and consumed only by {@link #getMembershipPoints()}. {@code
     * null} when it has not been resolved, which the points rule treats as a single-member
     * household.
     */
    @Transient
    public Integer getHouseholdSize() {
        return this.householdSize;
    }

    public void setHouseholdSize(Integer householdSize) {
        this.householdSize = householdSize;
    }

    /**
     * The ceiling applied to this owner's {@link #getMembershipLevel() membershipLevel}: the new
     * owner's level cannot exceed one above the maximum level among the other members of its
     * household. Not a persisted column — populated by the create pipeline, which alone has the
     * repository access to read the household's members. {@code null} when no cap applies (the
     * owner has no existing household member), leaving the level uncapped.
     */
    @Transient
    public Integer getMembershipLevelCap() {
        return this.membershipLevelCap;
    }

    public void setMembershipLevelCap(Integer membershipLevelCap) {
        this.membershipLevelCap = membershipLevelCap;
    }

    public Integer getNamesakeCount() {
        return this.namesakeCount;
    }

    public void setNamesakeCount(Integer namesakeCount) {
        this.namesakeCount = namesakeCount;
    }

    public String getPostcode() {
        return this.postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    /**
     * Whether this owner has been soft-deleted. A soft-delete ({@code DELETE /api/owners/{id}})
     * flags the owner deleted and retains the row rather than removing it, so the owner is still
     * returned by a fetch; the create pipeline's duplicate and identity checks ignore owners
     * flagged deleted. A newly created owner is not deleted.
     */
    public boolean isDeleted() {
        return this.deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    /**
     * The owner's name formatted as {@code 'LastName, FirstName'} from the stored names.
     * Not persisted — derived from {@link #getLastName()} and {@link #getFirstName()}.
     */
    @Transient
    public String getDisplayName() {
        return this.getLastName() + ", " + this.getFirstName();
    }

    /**
     * The owner's initials: the upper-cased first letters of firstName and lastName,
     * dot-separated with a trailing dot (e.g. {@code 'J.S.'}).
     * Not persisted — derived from {@link #getFirstName()} and {@link #getLastName()}.
     */
    @Transient
    public String getInitials() {
        return Character.toUpperCase(this.getFirstName().charAt(0)) + "."
            + Character.toUpperCase(this.getLastName().charAt(0)) + ".";
    }

    /**
     * The owner's salutation: the {@link #getTitle() title} and {@link #getLastName() lastName}
     * separated by a single space (e.g. {@code 'DR Who'}), or just the last name when no title is
     * supplied. Not persisted — derived from {@link #getTitle()} and {@link #getLastName()}.
     */
    @Transient
    public String getSalutation() {
        if (this.title == null || this.title.isBlank()) {
            return this.getLastName();
        }
        return this.title + " " + this.getLastName();
    }

    /**
     * The owner's {@link #getTelephone() telephone} formatted for humans: the stored E.164
     * value rewritten as the country code, a space, then the national digits grouped in threes
     * separated by spaces (e.g. {@code '+61 412 345 678'}). The known country codes are
     * {@code '+61'} (Australia) and {@code '+1'} (NANP); any other E.164 value is split with a
     * two-digit country code. {@code null} when no telephone is set or the value is not in E.164
     * form. The raw {@link #getTelephone() telephone} is left in E.164 form. Not persisted —
     * derived from {@link #getTelephone()}.
     */
    @Transient
    public String getTelephoneDisplay() {
        if (this.telephone == null || !this.telephone.matches("\\+[0-9]+")) {
            return null;
        }
        String digits = this.telephone.substring(1);
        int countryCodeLength = digits.startsWith("1") && !digits.startsWith("61") ? 1 : 2;
        if (digits.length() <= countryCodeLength) {
            return this.telephone;
        }
        String countryCode = digits.substring(0, countryCodeLength);
        String national = digits.substring(countryCodeLength);
        StringBuilder grouped = new StringBuilder();
        for (int i = 0; i < national.length(); i++) {
            if (i > 0 && i % 3 == 0) {
                grouped.append(' ');
            }
            grouped.append(national.charAt(i));
        }
        return "+" + countryCode + " " + grouped;
    }

    /**
     * The calendar year in which {@code date}'s fiscal year starts. The fiscal year starts on
     * 1 July, so a date in July–December belongs to the fiscal year starting that calendar year,
     * while a date in January–June belongs to the fiscal year that started the previous calendar
     * year.
     */
    private static int fiscalYearStart(LocalDate date) {
        return date.getMonthValue() >= 7 ? date.getYear() : date.getYear() - 1;
    }

    /**
     * The 2-digit fiscal-year segment ({@code 'YY'}) for {@code date}: the last two digits of the
     * calendar year in which the fiscal year starts (the fiscal year starts on 1 July), zero-padded
     * (e.g. a date in October 2026 yields {@code '26'}). The single authority for the FY segment of
     * the owner's {@link #getMemberId() memberId}, reused by the create pipeline so the memberId and
     * the derived {@link #getFiscalYear() fiscalYear} agree on it.
     */
    public static String fiscalYearSegment(LocalDate date) {
        return String.format("%02d", fiscalYearStart(date) % 100);
    }

    /** The length of the fixed {@code <FY><HASH8><CHK>} tail of a {@link #getMemberId() memberId}
     * core: 2 (FY) + 8 (HASH8) + 1 (CHK). The REGION prefix is whatever precedes this tail. */
    private static final int MEMBER_ID_TAIL = 11;

    /**
     * The owner's memberId with any {@code '-<n>'} collision suffix removed, or {@code null} when no
     * memberId is set or the value is too short to carry the fixed {@code <FY><HASH8><CHK>} tail.
     * The core is {@code <REGION><FY><HASH8><CHK>}; collision handling appends {@code '-<n>'} and
     * REGION/FY/HASH8/CHK contain no {@code '-'}, so the segments can be read off the core by length.
     */
    private String memberIdCore() {
        if (this.memberId == null) {
            return null;
        }
        int dash = this.memberId.indexOf('-');
        String core = dash < 0 ? this.memberId : this.memberId.substring(0, dash);
        return core.length() < MEMBER_ID_TAIL ? null : core;
    }

    /**
     * The owner's fiscal year, formatted {@code 'FY<YY>'} where {@code YY} is the 2-digit fiscal-year
     * segment of the owner's {@link #getMemberId() memberId} (e.g. a memberId {@code 'NSW261A2B3C4D7'}
     * yields {@code 'FY26'}). The memberId's FY segment is derived from the business-day-adjusted
     * {@link #getRegistrationDate() registrationDate}. {@code null} when no memberId is set. Not
     * persisted — derived from {@link #getMemberId()}.
     */
    @Transient
    public String getFiscalYear() {
        String core = memberIdCore();
        if (core == null) {
            return null;
        }
        return "FY" + core.substring(core.length() - MEMBER_ID_TAIL, core.length() - MEMBER_ID_TAIL + 2);
    }

    /**
     * The owner's membership points. Starts at {@code 0}; adds {@code 2} when an
     * {@link #getEmail() email} is present; adds {@code 1} when {@link #getNamesakeCount()
     * namesakeCount} is {@code 0}; adds {@code 2} for a household of {@code 3} or more members
     * ({@link #getHouseholdSize() householdSize}); adds {@code 3} for a tenure of one or more
     * elapsed fiscal years since the {@link #getRegistrationDate() registration date}'s fiscal
     * year (the fiscal year starts on 1 July). Not persisted — derived from {@link #getEmail()},
     * {@link #getNamesakeCount()}, {@link #getHouseholdSize()} and {@link #getRegistrationDate()}.
     */
    @Transient
    public int getMembershipPoints() {
        int points = 0;
        boolean hasEmail = this.email != null && !this.email.isBlank();
        if (hasEmail) {
            points += 2;
        }
        boolean uniqueName = this.namesakeCount != null && this.namesakeCount == 0;
        if (uniqueName) {
            points += 1;
        }
        if (this.householdSize != null && this.householdSize >= 3) {
            points += 2;
        }
        if (this.registrationDate != null
                && fiscalYearStart(LocalDate.now()) - fiscalYearStart(this.registrationDate) >= 1) {
            points += 3;
        }
        return points;
    }

    /**
     * The owner's numeric membership level, mapped from {@link #getMembershipPoints()
     * membershipPoints}: {@code 1} for {@code 0-1} points, {@code 2} for {@code 2-3},
     * {@code 3} for {@code 4-5} and {@code 4} for {@code 6} or more, then capped at
     * {@link #getMembershipLevelCap() membershipLevelCap} when one applies (a new owner's level
     * cannot exceed one above the maximum level among its existing household members). Not
     * persisted — derived from {@link #getMembershipPoints()} and {@link #getMembershipLevelCap()}.
     */
    @Transient
    public int getMembershipLevel() {
        int points = getMembershipPoints();
        int level;
        if (points >= 6) {
            level = 4;
        }
        else if (points >= 4) {
            level = 3;
        }
        else if (points >= 2) {
            level = 2;
        }
        else {
            level = 1;
        }
        if (this.membershipLevelCap != null && level > this.membershipLevelCap) {
            return this.membershipLevelCap;
        }
        return level;
    }

    /**
     * The owner's segment, formatted {@code '<TIER>_<AREA>'}, one of {@code 'PREMIUM_METRO'},
     * {@code 'PREMIUM_REGIONAL'}, {@code 'STANDARD_METRO'} or {@code 'STANDARD_REGIONAL'}. TIER is
     * {@code 'PREMIUM'} when {@link #getMembershipLevel() membershipLevel} is 3 or more, otherwise
     * {@code 'STANDARD'}. AREA is {@code 'METRO'} when the {@link #getLocality() locality} is a known
     * region (NSW, VIC or QLD), otherwise {@code 'REGIONAL'}. Not persisted — derived from
     * {@link #getMembershipLevel()} and {@link #getLocality()}.
     */
    @Transient
    public String getOwnerSegment() {
        String tier = getMembershipLevel() >= 3 ? "PREMIUM" : "STANDARD";
        String area = isKnownRegion(getLocality()) ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }

    /**
     * Fixed city-to-region table used as the fall-back when deriving an owner's region.
     */
    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /**
     * Region -> inclusive 4-digit postcode range {low, high}, the primary source for
     * deriving an owner's region (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099).
     */
    private static final Map<String, int[]> REGION_RANGE = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /**
     * Fixed region -> IANA timezone table (NSW->Australia/Sydney, VIC->Australia/Melbourne,
     * QLD->Australia/Brisbane), the single authority for {@link #getTimezone()}.
     */
    private static final Map<String, String> REGION_TIMEZONE = Map.of(
        "NSW", "Australia/Sydney",
        "VIC", "Australia/Melbourne",
        "QLD", "Australia/Brisbane");

    /**
     * The canonical region for a postcode/city pair, derived first from {@code postcode} by range
     * ({@link #regionForPostcode}), falling back to the fixed city table ({@link #regionForCity})
     * when the postcode is absent or in no known range, or {@code 'UNKNOWN'} when neither matches.
     * Preferring the postcode disambiguates cities that share a name. This is the REGION prefix of
     * the owner's {@link #getMemberId() memberId}, so the create pipeline and
     * {@link #getLocality() locality} agree on it. The single authority on the region rule, reused
     * by the owner pipelines (e.g. postcode validation) rather than copied.
     */
    public static String regionFor(String postcode, String city) {
        String region = regionForPostcode(postcode);
        if (region != null) {
            return region;
        }
        String cityRegion = regionForCity(city);
        return cityRegion != null ? cityRegion : "UNKNOWN";
    }

    /** The canonical region for a city by exact name match (Sydney->NSW, Melbourne->VIC,
     * Brisbane->QLD), or {@code null} when the city has no known region. */
    public static String regionForCity(String city) {
        return city == null ? null : CITY_REGION.get(city);
    }

    /** Whether {@code region} is one of the known regions (NSW, VIC or QLD); {@code 'UNKNOWN'}
     * and any other value are not. Backed by {@link #REGION_RANGE}, the primary source for the
     * region set. */
    public static boolean isKnownRegion(String region) {
        return REGION_RANGE.containsKey(region);
    }

    /** Region whose inclusive range contains the given postcode (NSW 2000-2099, VIC 3000-3099,
     * QLD 4000-4099), or {@code null} when the postcode is absent, not 4 digits, or in no known
     * range. */
    public static String regionForPostcode(String postcode) {
        if (postcode == null || !postcode.matches("[0-9]{4}")) {
            return null;
        }
        int value = Integer.parseInt(postcode);
        for (Map.Entry<String, int[]> entry : REGION_RANGE.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * The owner's locality: the REGION prefix of the owner's {@link #getMemberId() memberId}
     * (everything before the fixed {@code <FY><HASH8><CHK>} tail), which the create pipeline derives
     * from the postcode-and-city region. {@code 'UNKNOWN'} when no memberId is set. Not persisted —
     * derived from {@link #getMemberId()}.
     */
    @Transient
    public String getLocality() {
        String core = memberIdCore();
        if (core == null) {
            return "UNKNOWN";
        }
        String region = core.substring(0, core.length() - MEMBER_ID_TAIL);
        return region.isEmpty() ? "UNKNOWN" : region;
    }

    /**
     * The owner's timezone: the IANA name for the owner's {@link #getLocality() locality}/region
     * from the fixed region-to-timezone table (NSW->Australia/Sydney, VIC->Australia/Melbourne,
     * QLD->Australia/Brisbane), or {@code null} when the region has no mapped timezone. Not
     * persisted — derived from {@link #getLocality()}.
     */
    @Transient
    public String getTimezone() {
        return REGION_TIMEZONE.get(getLocality());
    }

    /**
     * Whether more than 80 owners had already been created today when this response was
     * built. Not persisted — populated for the response by the create and fetch pipelines
     * and defaults to {@code false} so every owner serialization carries the flag.
     */
    @Transient
    public boolean isBulkSignupWarning() {
        return this.bulkSignupWarning;
    }

    public void setBulkSignupWarning(boolean bulkSignupWarning) {
        this.bulkSignupWarning = bulkSignupWarning;
    }

    /**
     * Whether this owner's city already held between 40 and 49 owners when this response was
     * built, approaching the hard capacity limit of 50 (compared case-insensitively). Not
     * persisted — populated for the response by the create and fetch pipelines and defaults to
     * {@code false} so every owner serialization carries the flag.
     */
    @Transient
    public boolean isCapacityWarning() {
        return this.capacityWarning;
    }

    public void setCapacityWarning(boolean capacityWarning) {
        this.capacityWarning = capacityWarning;
    }

    /**
     * Whether this owner is a possible (soft) duplicate: not a hard duplicate, but shares an
     * existing owner's last name and postcode with a different telephone. Not persisted —
     * populated for the response by the create and fetch pipelines and defaults to
     * {@code false} so every owner serialization carries the flag.
     */
    @Transient
    public boolean isPossibleDuplicate() {
        return this.possibleDuplicate;
    }

    public void setPossibleDuplicate(boolean possibleDuplicate) {
        this.possibleDuplicate = possibleDuplicate;
    }

    /**
     * The id of the existing owner this owner possibly duplicates (shared last name and
     * postcode, different telephone), or {@code null} when it is not a possible duplicate.
     * Not persisted — populated for the response alongside {@link #isPossibleDuplicate()}.
     */
    @Transient
    public Integer getPossibleDuplicateOf() {
        return this.possibleDuplicateOf;
    }

    public void setPossibleDuplicateOf(Integer possibleDuplicateOf) {
        this.possibleDuplicateOf = possibleDuplicateOf;
    }

    /**
     * Whether this owner is flagged for review: true when any of these hold — the owner is a
     * {@link #isPossibleDuplicate() possible duplicate}, the email domain is disposable-adjacent,
     * or the owner's city is at or over its soft capacity (the {@link #isCapacityWarning()
     * capacityWarning} band) — otherwise false. Not persisted — populated for the response by the
     * create and fetch pipelines and defaults to {@code false} so every owner serialization
     * carries the flag.
     */
    @Transient
    public boolean isRiskFlag() {
        return this.riskFlag;
    }

    public void setRiskFlag(boolean riskFlag) {
        this.riskFlag = riskFlag;
    }

    /**
     * The owner's preferred contact channel: {@code 'EMAIL'} when an {@link #getEmail() email}
     * is present, otherwise {@code 'PHONE'}.
     * Not persisted — derived from {@link #getEmail()}.
     */
    @Transient
    public String getContactPreference() {
        boolean hasEmail = this.email != null && !this.email.isBlank();
        return hasEmail ? "EMAIL" : "PHONE";
    }

    /**
     * The owner's age band as of the {@link #getRegistrationDate() registrationDate}, derived
     * from {@link #getBirthDate() birthDate}: {@code 'MINOR'} when under 18, {@code 'ADULT'} for
     * 18 to 64 inclusive, {@code 'SENIOR'} for 65 and over. {@code null} when no birthDate is
     * supplied. Not persisted — derived from {@link #getBirthDate()} and
     * {@link #getRegistrationDate()}.
     */
    @Transient
    public String getAgeBand() {
        if (this.birthDate == null || this.registrationDate == null) {
            return null;
        }
        int age = Period.between(this.birthDate, this.registrationDate).getYears();
        if (age < 18) {
            return "MINOR";
        }
        return age < 65 ? "ADULT" : "SENIOR";
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
