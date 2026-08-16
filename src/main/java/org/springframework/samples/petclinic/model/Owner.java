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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
    @Pattern(regexp = "^\\+[0-9]{8,15}$", message = "Telephone must be in E.164 form (a '+' followed by 8 to 15 digits)")
    private String telephone;

    @Column(name = "email")
    private String email;

    @Column(name = "registration_date")
    private LocalDate registrationDate;

    @Column(name = "member_id")
    private String memberId;

    @Column(name = "household_id")
    private String householdId;

    @Column(name = "namesake_count")
    private Integer namesakeCount;

    @Column(name = "household_member_count")
    private Integer householdMemberCount;

    @Column(name = "bulk_signup_warning")
    private Boolean bulkSignupWarning;

    @Column(name = "capacity_warning")
    private Boolean capacityWarning;

    @Column(name = "postcode")
    private String postcode;

    @Column(name = "possible_duplicate")
    private Boolean possibleDuplicate;

    @Column(name = "possible_duplicate_of")
    private Integer possibleDuplicateOf;

    @Column(name = "membership_level_cap")
    private Integer membershipLevelCap;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner", fetch = FetchType.EAGER)
    private Set<Pet> pets;

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
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
        this.email = email;
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

    public Integer getHouseholdMemberCount() {
        return this.householdMemberCount;
    }

    public void setHouseholdMemberCount(Integer householdMemberCount) {
        this.householdMemberCount = householdMemberCount;
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

    public String getPostcode() {
        return this.postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
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

    public Integer getMembershipLevelCap() {
        return this.membershipLevelCap;
    }

    public void setMembershipLevelCap(Integer membershipLevelCap) {
        this.membershipLevelCap = membershipLevelCap;
    }

    public LocalDate getBirthDate() {
        return this.birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public boolean isDeleted() {
        return this.deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    /**
     * The owner's salutation, derived on read: the {@code title} and {@code lastName} separated
     * by a single space (e.g. {@code 'DR Franklin'}) when a title is present, or just the
     * {@code lastName} when no title is set.
     *
     * @return the salutation
     */
    @Transient
    public String getSalutation() {
        if (this.title != null && !this.title.isBlank()) {
            return this.title + " " + getLastName();
        }
        return getLastName();
    }

    /**
     * The fiscal year that a date falls in, on a fiscal year that starts on 1 July: a date on or
     * after 1 July belongs to the fiscal year named for the following calendar year, and a date
     * before 1 July belongs to the fiscal year named for its own calendar year (e.g. both
     * 2026-07-01 and 2027-06-30 fall in fiscal year 2027).
     *
     * @param date the date to classify
     * @return the fiscal year number
     */
    public static int fiscalYearOf(LocalDate date) {
        return date.getMonthValue() >= 7 ? date.getYear() + 1 : date.getYear();
    }

    /**
     * The owner's fiscal year, derived on read from the (business-day-adjusted)
     * {@code registrationDate} on a fiscal year that starts on 1 July, formatted {@code 'FY<YY>'}
     * where YY is the last two digits of the fiscal year, zero-padded (e.g. a registration date of
     * 2026-08-16 yields {@code 'FY27'}).
     *
     * @return the fiscal year, or {@code null} when no {@code registrationDate} is set
     */
    @Transient
    public String getFiscalYear() {
        if (this.registrationDate == null) {
            return null;
        }
        return String.format("FY%02d", fiscalYearOf(this.registrationDate) % 100);
    }

    /**
     * The owner's membership points, derived on read from the fields assigned at create. Points
     * start at 0 and accumulate: 2 when an email is present, 1 when {@code namesakeCount} is 0,
     * 2 when the household has 3 or more members ({@code householdMemberCount} of 3 or more), and
     * 3 when the owner's tenure spans at least one elapsed fiscal year. Tenure is the number of
     * fiscal years (which start on 1 July) elapsed between the {@code registrationDate} and the
     * current date. Because a newly created owner has zero tenure, the tenure points are only
     * earned once the owner has entered a later fiscal year than the one they registered in.
     *
     * @return the membership points, 0 or more
     */
    @Transient
    public Integer getMembershipPoints() {
        int points = 0;
        if (this.email != null && !this.email.isBlank()) {
            points += 2;
        }
        if (this.namesakeCount != null && this.namesakeCount == 0) {
            points += 1;
        }
        if (this.householdMemberCount != null && this.householdMemberCount >= 3) {
            points += 2;
        }
        if (this.registrationDate != null
                && fiscalYearOf(LocalDate.now()) - fiscalYearOf(this.registrationDate) >= 1) {
            points += 3;
        }
        return points;
    }

    /**
     * The owner's numeric membership level, derived on read by mapping {@link #getMembershipPoints}
     * to a level: 1 for 0-1 points, 2 for 2-3 points, 3 for 4-5 points, and 4 for 6 or more points.
     * Because a newly created owner has zero tenure, level 4 (which requires the tenure points) is
     * reached only once the owner has entered a later fiscal year than the one they registered in.
     *
     * <p>The points-derived level is finally capped by {@link #membershipLevelCap} when one was
     * assigned at create: a new owner joining an existing household without declaring it (see
     * {@code membershipLevelCap}) may not exceed one above the highest membership level already held
     * by an existing household member, so the returned level is the lesser of the derived level and
     * the cap. When no cap was assigned (a household's first member, or a declared member) the
     * derived level is returned unchanged.
     *
     * @return the membership level, from 1 to 4
     */
    @Transient
    public Integer getMembershipLevel() {
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
     * The owner's preferred contact channel, derived on read: {@code 'EMAIL'} when an
     * email address is present, otherwise {@code 'PHONE'}.
     *
     * @return {@code "EMAIL"} when an email is present, otherwise {@code "PHONE"}
     */
    @Transient
    public String getContactPreference() {
        return (this.email != null && !this.email.isBlank()) ? "EMAIL" : "PHONE";
    }

    /**
     * The owner's age band, derived on read from {@code birthDate} against the
     * {@code registrationDate}: {@code 'MINOR'} when the owner is under 18, {@code 'ADULT'}
     * from 18 to 64, and {@code 'SENIOR'} at 65 or older. The age is the number of whole years
     * between the birth date and the registration date (falling back to the current date when no
     * registration date is set).
     *
     * @return the age band, or {@code null} when no {@code birthDate} is set
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

    /**
     * The owner's stored E.164 {@code telephone} formatted for humans, derived on read: the
     * leading {@code '+'} and country code, a space, then the national digits grouped in threes
     * (e.g. {@code '+61 412 345 678'} for a stored {@code '+61412345678'}). The recognised
     * country codes are {@code '+61'} (Australia) and {@code '+1'} (NANP); any other prefix falls
     * back to a two-digit country code. The raw {@code telephone} is left in E.164 form.
     *
     * @return the human-formatted telephone, or the raw value when it is not in E.164 form
     */
    @Transient
    public String getTelephoneDisplay() {
        if (this.telephone == null || !this.telephone.startsWith("+")
                || !this.telephone.substring(1).matches("[0-9]+")) {
            return this.telephone;
        }
        String digits = this.telephone.substring(1);
        String countryCode;
        if (digits.startsWith("61")) {
            countryCode = "61";
        } else if (digits.startsWith("1")) {
            countryCode = "1";
        } else {
            countryCode = digits.length() > 2 ? digits.substring(0, 2) : digits;
        }
        String national = digits.substring(countryCode.length());
        StringBuilder display = new StringBuilder("+").append(countryCode);
        for (int i = 0; i < national.length(); i += 3) {
            display.append(' ').append(national, i, Math.min(i + 3, national.length()));
        }
        return display.toString();
    }

    /**
     * The owner's derived duplicate-detection key: the single consolidated identity used to
     * detect duplicate owners on create. It is the version-2 lower-case hex SHA-256 digest of
     * {@code IDENTITY_VERSION + '|' + normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)}
     * from the fixed {@code 'V2'} version tag (see {@link #IDENTITY_VERSION}), the owner's
     * already-normalized {@code telephone} (E.164 form), its lower-cased {@code email} (empty
     * when absent) and the Soundex code of its {@code lastName} (see {@link #soundex}). Two owners
     * are duplicates only when their whole {@code identityKey} values are equal.
     *
     * @return the consolidated identity key
     */
    @Transient
    public String getIdentityKey() {
        return identityKey(this.telephone, this.email, this.lastName);
    }

    /**
     * The fixed version tag mixed into every derived owner identifier so that the version-2
     * identity ({@code identityKey}, {@code memberId} and {@code householdId}) never reproduces a
     * value produced under version 1. It is an internal component of the identifiers only and never
     * leaks into the user-facing {@code locality}, {@code timezone} or owner-segment region.
     */
    public static final String IDENTITY_VERSION = "V2";

    /**
     * Computes the consolidated version-2 {@code identityKey} for the given identity-bearing fields:
     * the lower-case hex SHA-256 digest of
     * {@code IDENTITY_VERSION + '|' + normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)}.
     * The fixed {@code 'V2'} version tag (see {@link #IDENTITY_VERSION}) is mixed in so the key differs
     * from the version-1 key; the {@code email} is lower-cased (empty when absent) and the
     * {@code lastName} is reduced to its Soundex code (see {@link #soundex}). The {@code telephone} is
     * expected already normalized to E.164 form.
     *
     * @param telephone the owner's normalized (E.164) telephone, possibly {@code null}
     * @param email     the owner's email, possibly {@code null}
     * @param lastName  the owner's last name, possibly {@code null}
     * @return the lower-case hex SHA-256 identity key
     */
    public static String identityKey(String telephone, String email, String lastName) {
        String telephonePart = telephone == null ? "" : telephone;
        String emailPart = email == null ? "" : email.toLowerCase(Locale.ROOT);
        return sha256Hex(IDENTITY_VERSION + "|" + telephonePart + "|" + emailPart + "|" + soundex(lastName));
    }

    /**
     * Returns the American Soundex code of {@code value}: the first letter followed by three
     * digits encoding the remaining consonants (b,f,p,v→1; c,g,j,k,q,s,x,z→2; d,t→3; l→4; m,n→5;
     * r→6), where vowels and {@code h}/{@code w} are not coded, adjacent letters with the same
     * code collapse to one (and a code repeated across an {@code h}/{@code w} is treated as
     * adjacent), and the result is right-padded with zeros to length four. A {@code null} or
     * letter-free value yields the empty string.
     *
     * @param value the value to encode, possibly {@code null}
     * @return the four-character Soundex code, or the empty string when there is no letter
     */
    public static String soundex(String value) {
        if (value == null) {
            return "";
        }
        String letters = value.toUpperCase(Locale.ROOT).replaceAll("[^A-Z]", "");
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
            previous = (c == 'A' || c == 'E' || c == 'I' || c == 'O' || c == 'U' || c == 'Y')
                ? '0' : digit;
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    private static char soundexDigit(char c) {
        switch (c) {
            case 'B': case 'F': case 'P': case 'V':
                return '1';
            case 'C': case 'G': case 'J': case 'K': case 'Q': case 'S': case 'X': case 'Z':
                return '2';
            case 'D': case 'T':
                return '3';
            case 'L':
                return '4';
            case 'M': case 'N':
                return '5';
            case 'R':
                return '6';
            default:
                return '0';
        }
    }

    private static String sha256Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * A link to this owner's own resource, derived on read as {@code '/api/owners/'} followed by
     * the owner's id.
     *
     * @return the self link, or {@code null} when no id is assigned
     */
    @Transient
    public String getSelfLink() {
        return getId() == null ? null : "/api/owners/" + getId();
    }

    /**
     * The known disposable-email domains (kept in sync with the create-time blocklist). An owner
     * can never carry one of these verbatim — a create with such an email is rejected — but the
     * derived {@link #isEmailDisposableAdjacent} risk check uses their second-level labels to spot
     * domains that are merely adjacent to them.
     */
    private static final Set<String> DISPOSABLE_EMAIL_DOMAINS = Set.of(
        "mailinator.com", "tempmail.com", "guerrillamail.com");

    /**
     * Whether the owner's {@code email} domain is disposable-adjacent, derived on read: the domain
     * (the part after the {@code '@'}, compared case-insensitively) shares its second-level label —
     * the label immediately before the top-level domain — with a known disposable-email domain (see
     * {@link #DISPOSABLE_EMAIL_DOMAINS}). This catches domains that are near a disposable provider
     * without being one verbatim (a subdomain such as {@code 'x.mailinator.com'} or the same brand
     * on another TLD such as {@code 'mailinator.net'}), since the exact disposable domains are
     * rejected outright at create and so are never stored. An absent or malformed email is not
     * disposable-adjacent.
     *
     * @return {@code true} when the email domain is disposable-adjacent, otherwise {@code false}
     */
    @Transient
    public boolean isEmailDisposableAdjacent() {
        if (this.email == null) {
            return false;
        }
        int at = this.email.indexOf('@');
        if (at < 0) {
            return false;
        }
        String domain = this.email.substring(at + 1).toLowerCase(Locale.ROOT);
        if (domain.isEmpty()) {
            return false;
        }
        String label = secondLevelLabel(domain);
        for (String disposable : DISPOSABLE_EMAIL_DOMAINS) {
            if (label.equals(secondLevelLabel(disposable))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the second-level label of a dotted domain — the label immediately before the
     * top-level domain (e.g. {@code 'mailinator'} for both {@code 'mailinator.com'} and
     * {@code 'x.mailinator.com'}). A domain with no dot yields the whole value.
     *
     * @param domain the lower-cased domain
     * @return the second-level label
     */
    private static String secondLevelLabel(String domain) {
        String[] labels = domain.split("\\.");
        return labels.length >= 2 ? labels[labels.length - 2] : labels[labels.length - 1];
    }

    /**
     * The owner's risk flag, derived on read: {@code true} when any of these hold — the owner is a
     * possible (soft-match) duplicate ({@link #possibleDuplicate}), the owner's city was over its
     * soft capacity when the owner was created ({@link #capacityWarning}), or the owner's email
     * domain is disposable-adjacent (see {@link #isEmailDisposableAdjacent}); otherwise
     * {@code false}.
     *
     * @return {@code true} when the owner is flagged for review, otherwise {@code false}
     */
    @Transient
    public Boolean getRiskFlag() {
        return Boolean.TRUE.equals(this.possibleDuplicate)
            || Boolean.TRUE.equals(this.capacityWarning)
            || isEmailDisposableAdjacent();
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
