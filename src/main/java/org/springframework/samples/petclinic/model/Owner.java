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
import org.springframework.samples.petclinic.util.HashUtils;

import jakarta.persistence.*;
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
    /**
     * The fixed list of public holidays that a registration date must roll forward past, in addition
     * to weekends.
     */
    private static final Set<LocalDate> PUBLIC_HOLIDAYS = Set.of(
        LocalDate.of(2026, 1, 1),
        LocalDate.of(2026, 1, 26),
        LocalDate.of(2026, 4, 25),
        LocalDate.of(2026, 12, 25),
        LocalDate.of(2026, 12, 28));

    /**
     * The fixed version tag mixed into the version-2 identity derivations. It is folded into the
     * region code used inside every owner identifier (the {@link #getMemberId() memberId}, the
     * {@link #getHouseholdId() householdId} and the {@link #getIdentityKey() identityKey}) so each
     * value differs from its version-1 form. The tag is confined to the identifiers: it never leaks
     * into the user-facing region ({@link #deriveRegion()}) reported as locality, timezone or the
     * owner segment's derived region.
     */
    public static final String IDENTITY_VERSION = "V2";

    /** Fixed city-to-region table used to derive an owner's plain region code. */
    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** Region to inclusive 4-digit postcode range {low, high}, preferred over the city table
     *  when deriving an owner's region. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

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
    @Pattern(regexp = "^\\+[0-9]{8,15}$", message = "Phone number must be in E.164 form (+ followed by 8 to 15 digits)")
    private String telephone;

    @Column(name = "email")
    private String email;

    @Column(name = "postcode")
    private String postcode;

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

    @Column(name = "possible_duplicate")
    private Boolean possibleDuplicate;

    @Column(name = "possible_duplicate_of")
    private Integer possibleDuplicateOf;

    @Column(name = "membership_level_cap")
    private Integer membershipLevelCap;

    @Column(name = "deleted")
    private boolean deleted;

    @Transient
    private Integer householdSize;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner", fetch = FetchType.EAGER)
    private Set<Pet> pets;

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Derives the owner's salutation for the read-only {@code salutation} field: the title, a single
     * space and the lastName when a title is present (e.g. {@code 'DR Who'}), or just the lastName
     * when no title was supplied.
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
        this.email = email == null ? null : email.toLowerCase();
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
     * Derives the owner's age band from the birth date, measured against the registration date:
     * {@code 'MINOR'} when under 18, {@code 'ADULT'} for 18 to 64 inclusive, and {@code 'SENIOR'}
     * from 65 on. Returns {@code null} when either the birth date or the registration date is absent,
     * so an owner created without a birth date has no age band.
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

    public String getMemberId() {
        return this.memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    /**
     * This owner's primary identifier: its unified {@code memberId}. Callers that need "whatever
     * identifies the owner right now" (e.g. the structured audit event) read it here.
     */
    public String primaryIdentifier() {
        return this.memberId;
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
     * The number of owners belonging to this owner's household (owners sharing this owner's
     * household id, including this owner). Not persisted: it is a live count populated at read
     * time and feeds the {@code membershipPoints} calculation. Null when it has not been resolved.
     */
    public Integer getHouseholdSize() {
        return this.householdSize;
    }

    public void setHouseholdSize(Integer householdSize) {
        this.householdSize = householdSize;
    }

    /**
     * Whether this owner was flagged a possible (soft-match) duplicate at creation: it was not a
     * hard duplicate, yet it shared an existing owner's lastName and postcode while carrying a
     * different telephone. False when no such match existed.
     */
    public Boolean getPossibleDuplicate() {
        return this.possibleDuplicate;
    }

    public void setPossibleDuplicate(Boolean possibleDuplicate) {
        this.possibleDuplicate = possibleDuplicate;
    }

    /**
     * The id of the existing owner this owner soft-matches (same lastName and postcode, different
     * telephone), or {@code null} when it is not a possible duplicate.
     */
    public Integer getPossibleDuplicateOf() {
        return this.possibleDuplicateOf;
    }

    public void setPossibleDuplicateOf(Integer possibleDuplicateOf) {
        this.possibleDuplicateOf = possibleDuplicateOf;
    }

    /**
     * The ceiling applied to this owner's derived {@code membershipLevel}, fixed when the owner was
     * created: one above the maximum membershipLevel among the household members that already existed
     * at that time. The reported membership level is never allowed to exceed this cap. {@code null}
     * when the owner had no existing household member at creation, in which case no cap applies.
     */
    public Integer getMembershipLevelCap() {
        return this.membershipLevelCap;
    }

    public void setMembershipLevelCap(Integer membershipLevelCap) {
        this.membershipLevelCap = membershipLevelCap;
    }

    /**
     * Computes this owner's membership points for the given household size. Points start at 0 and
     * accumulate: 2 when an email address is present, 1 when the owner had no namesakes at creation
     * (namesakeCount is 0), 2 for a household of 3 or more members, and 3 for tenure of one or more
     * elapsed fiscal years (the number of 1-July fiscal-year boundaries between the registration date
     * and the current date). The household size is supplied by the caller (it is a live, non-persisted
     * count), so a {@code null} size contributes no household points.
     */
    public int membershipPoints(Integer householdSize) {
        int points = 0;
        if (this.email != null && !this.email.isBlank()) {
            points += 2;
        }
        if (this.namesakeCount != null && this.namesakeCount == 0) {
            points += 1;
        }
        if (householdSize != null && householdSize >= 3) {
            points += 2;
        }
        if (this.registrationDate != null
            && fiscalYear(LocalDate.now()) - fiscalYear(this.registrationDate) >= 1) {
            points += 3;
        }
        return points;
    }

    /**
     * Derives this owner's numeric membership level for the given household size from its membership
     * points: 1 for 0-1 points, 2 for 2-3 points, 3 for 4-5 points, and 4 for 6 or more points. The
     * result is then capped at {@link #getMembershipLevelCap()} (the ceiling fixed at creation from
     * the household this owner joined): when a cap is present the level can never exceed it. No cap is
     * applied when the owner had no existing household member at creation.
     */
    public int membershipLevel(Integer householdSize) {
        int points = membershipPoints(householdSize);
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
            level = this.membershipLevelCap;
        }
        return level;
    }

    /**
     * Whether this owner has been soft-deleted. A newly created owner is not deleted; a
     * {@code DELETE /api/owners/{id}} flags it deleted while retaining the row. Deleted owners are
     * ignored by the create endpoint's duplicate and identity checks.
     */
    public boolean isDeleted() {
        return this.deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    /**
     * Derives the owner's plain region code, preferring the postcode: the region whose inclusive
     * 4-digit range (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099) contains the postcode is used
     * first. When the postcode is absent or in no known range, it falls back to the fixed
     * city-to-region table (Sydney-&gt;NSW, Melbourne-&gt;VIC, Brisbane-&gt;QLD), returning
     * {@code 'UNKNOWN'} when the city is not in the table. This is the user-facing region reported
     * as {@code locality}/{@code timezone} and used by the owner segment; the fixed
     * {@code 'V2'} version tag is never mixed in here.
     */
    public String deriveRegion() {
        if (this.postcode != null && this.postcode.matches("^[0-9]{4}$")) {
            int value = Integer.parseInt(this.postcode);
            for (Map.Entry<String, int[]> entry : REGION_POSTCODES.entrySet()) {
                int[] range = entry.getValue();
                if (value >= range[0] && value <= range[1]) {
                    return entry.getKey();
                }
            }
        }
        return CITY_REGION.getOrDefault(this.city, "UNKNOWN");
    }

    /**
     * Derives the version-2 region code used INSIDE the owner's identifiers: the plain
     * {@link #deriveRegion() region} with the fixed {@link #IDENTITY_VERSION 'V2'} version tag mixed
     * in (e.g. {@code 'NSWV2'}). This is the shared source of the version bump across the memberId,
     * householdId and identityKey, so every identifier differs from its version-1 form while the
     * plain region reported to users stays untagged.
     */
    public String deriveIdentityRegion() {
        return deriveRegion() + IDENTITY_VERSION;
    }

    /**
     * The owner's derived identity key, used as the single basis for duplicate detection: the
     * lower-case hex SHA-256 digest over {@code identityRegion + '|' + normalizedTelephone + '|' +
     * lowerEmail + '|' + soundex(lastName)}, where {@code identityRegion} is the version-2 region
     * ({@link #deriveIdentityRegion()}, which mixes in the fixed {@code 'V2'} tag) and the telephone
     * and email components are empty when absent. Two owners are duplicates only when their whole
     * identity keys are equal, so any single differing component yields distinct keys and is allowed.
     * Derived from persisted fields, so it is not itself a persistent column.
     */
    @Transient
    public String getIdentityKey() {
        String normalizedTelephone = this.telephone == null ? "" : this.telephone;
        String lowerEmail = this.email == null ? "" : this.email.toLowerCase();
        String seed = deriveIdentityRegion() + "|" + normalizedTelephone + "|" + lowerEmail
            + "|" + soundex(getLastName());
        return HashUtils.sha256Hex(seed);
    }

    /**
     * Computes the American Soundex code for a name: the retained first letter followed by three
     * digits derived from the remaining consonants (b,f,p,v-&gt;1; c,g,j,k,q,s,x,z-&gt;2; d,t-&gt;3;
     * l-&gt;4; m,n-&gt;5; r-&gt;6), with adjacent equal codes collapsed, h and w ignored (so they do
     * not separate equal codes) and vowels acting as separators, right-padded with zeros to length
     * four. Returns an empty string when the value is null or holds no letters, so surnames that
     * sound alike share a code.
     */
    public static String soundex(String value) {
        if (value == null) {
            return "";
        }
        String letters = value.toUpperCase().replaceAll("[^A-Z]", "");
        if (letters.isEmpty()) {
            return "";
        }
        StringBuilder code = new StringBuilder();
        code.append(letters.charAt(0));
        char previous = soundexDigit(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            if (c == 'H' || c == 'W') {
                continue;
            }
            char digit = soundexDigit(c);
            if (digit != '0' && digit != previous) {
                code.append(digit);
            }
            previous = digit;
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    /** Maps a letter to its Soundex digit, or {@code '0'} for vowels, y, h and w. */
    private static char soundexDigit(char c) {
        return switch (c) {
            case 'B', 'F', 'P', 'V' -> '1';
            case 'C', 'G', 'J', 'K', 'Q', 'S', 'X', 'Z' -> '2';
            case 'D', 'T' -> '3';
            case 'L' -> '4';
            case 'M', 'N' -> '5';
            case 'R' -> '6';
            default -> '0';
        };
    }

    /**
     * Establishes the effective registration date at persist time, so the create-time default
     * applies to any persistence path.
     */
    @PrePersist
    protected void defaultRegistrationDate() {
        resolveRegistrationDate();
    }

    /**
     * Resolves this owner's effective registration date and stores it: the supplied value is used, or
     * the server's current date when none was supplied, and the result is rolled forward onto a
     * business day (a Saturday, Sunday or public holiday rolls forward to the next business day).
     * Returns the resolved date.
     * Idempotent, so it is safe to call from both the create flow and the persist hook.
     */
    public LocalDate resolveRegistrationDate() {
        LocalDate effective = this.registrationDate == null ? LocalDate.now() : this.registrationDate;
        this.registrationDate = toBusinessDay(effective);
        return this.registrationDate;
    }

    /**
     * Derives the fiscal year that a date falls in, where each fiscal year starts on 1 July and is
     * identified by the calendar year in which it ends: a date from July onwards belongs to the
     * fiscal year ending in the following calendar year (e.g. 2 July 2026 gives 2027), while a date
     * from January to June belongs to the fiscal year ending in its own calendar year. Returns
     * {@code null} when the date is absent.
     */
    public static Integer fiscalYear(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.getMonthValue() >= Month.JULY.getValue() ? date.getYear() + 1 : date.getYear();
    }

    /**
     * Rolls a registration date forward onto a business day: a Saturday, Sunday or listed public
     * holiday is advanced to the next non-holiday weekday, while any other weekday (and {@code null})
     * is returned unchanged. Applying this to a date that is already a business day is a no-op, so it
     * is safe to call more than once.
     */
    public static LocalDate toBusinessDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        LocalDate result = date;
        while (isWeekend(result) || PUBLIC_HOLIDAYS.contains(result)) {
            result = result.plusDays(1);
        }
        return result;
    }

    private static boolean isWeekend(LocalDate date) {
        return switch (date.getDayOfWeek()) {
            case SATURDAY, SUNDAY -> true;
            default -> false;
        };
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
