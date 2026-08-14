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
    @Pattern(regexp = "^\\+[0-9]{8,15}$", message = "Phone number must be E.164: a '+' followed by 8 to 15 digits")
    private String telephone;

    @Column(name = "email")
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

    @Column(name = "membership_level_cap")
    private Integer membershipLevelCap;

    @Column(name = "possible_duplicate")
    private Boolean possibleDuplicate;

    @Column(name = "possible_duplicate_of")
    private Integer possibleDuplicateOf;

    @Column(name = "deleted")
    private Boolean deleted = Boolean.FALSE;

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
     * The owner's member id, the single external handle that unifies the former customerCode and
     * membershipNumber. Formatted {@code '<REGION><FY><HASH8><CHK>'}: the region code, the 2-digit
     * fiscal year, the 8 upper-case hex HASH8 (the SHA-256 over normalized telephone + last name) and
     * a single Luhn check digit over the digits of {@code <REGION><FY><HASH8>} (e.g. 'NSW271A2B3C4D5').
     * Assigned at creation.
     */
    public String getMemberId() {
        return this.memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    /**
     * The owner's current primary identifier — the single external handle that identifies this owner
     * to other systems and to the audit trail. This is the {@link #getMemberId() memberId}, so
     * anything keyed on the primary identifier (such as the {@code OWNER_CREATED} audit event) tracks
     * it automatically.
     */
    @Transient
    public String getPrimaryIdentifier() {
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

    public Integer getMembershipLevelCap() {
        return this.membershipLevelCap;
    }

    public void setMembershipLevelCap(Integer membershipLevelCap) {
        this.membershipLevelCap = membershipLevelCap;
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

    /** Whether this owner has been soft-deleted. A newly created owner is not deleted. */
    public Boolean getDeleted() {
        return this.deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    /**
     * The owner's risk flag, derived at read time: {@code true} when any risk signal holds, otherwise
     * {@code false}. A signal holds when the owner is a {@link #getPossibleDuplicate() possible
     * duplicate}, when its {@link #getEmail() email} domain is <em>disposable-adjacent</em> (its
     * second-level label matches a disposable domain — e.g. {@code 'mailinator'} — while the domain
     * itself is not on the disposable blocklist, so it was allowed at creation), or when the city is
     * over its soft capacity ({@link #getCapacityWarning() capacityWarning}).
     */
    @Transient
    public Boolean getRiskFlag() {
        if (Boolean.TRUE.equals(this.possibleDuplicate) || Boolean.TRUE.equals(this.capacityWarning)) {
            return true;
        }
        return isDisposableAdjacentEmail(this.email);
    }

    /**
     * Second-level labels of the disposable email domains (mirrors the blocklist applied at creation
     * in {@code OwnerEmails}). An email whose domain shares one of these labels — a different TLD or a
     * subdomain of a disposable domain — is <em>disposable-adjacent</em>.
     */
    private static final Set<String> DISPOSABLE_DOMAIN_LABELS =
        Set.of("mailinator", "tempmail", "guerrillamail");

    /**
     * Whether {@code email}'s domain is disposable-adjacent: its second-level label (the label
     * immediately before the final TLD) matches a known disposable domain. Absent/blank or malformed
     * addresses are not adjacent.
     */
    private static boolean isDisposableAdjacentEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        int at = email.lastIndexOf('@');
        if (at < 0 || at == email.length() - 1) {
            return false;
        }
        String[] labels = email.substring(at + 1).toLowerCase().split("\\.");
        if (labels.length < 2) {
            return false;
        }
        return DISPOSABLE_DOMAIN_LABELS.contains(labels[labels.length - 2]);
    }

    /**
     * The owner's salutation, derived at read time as the {@link #getTitle() title}, a single space
     * and the {@link #getLastName() lastName} (e.g. {@code 'DR Franklin'}); just the lastName when no
     * title is present.
     */
    @Transient
    public String getSalutation() {
        if (this.title == null || this.title.isBlank()) {
            return this.getLastName();
        }
        return this.title + " " + this.getLastName();
    }

    /**
     * The canonical relative URL of this owner, derived at read time as {@code '/api/owners/'}
     * followed by the owner's {@link #getId() id}.
     */
    @Transient
    public String getSelfLink() {
        return "/api/owners/" + this.getId();
    }

    /**
     * The fiscal year (starting 1 July) containing {@code date}, named by the calendar year it ends
     * in: a date on or after 1 July belongs to the next calendar year's fiscal year (e.g. 14 Aug
     * 2026 is fiscal year 2027), a date before 1 July to the current calendar year's.
     */
    public static int fiscalYearOf(LocalDate date) {
        return date.getMonthValue() >= 7 ? date.getYear() + 1 : date.getYear();
    }

    /**
     * The owner's fiscal year, derived at read time from the business-day-adjusted
     * {@link #getRegistrationDate() registrationDate} and formatted {@code 'FY<YY>'} where YY is the
     * last two digits of the fiscal year (which starts on 1 July; e.g. a registration date of
     * 14 Aug 2026 yields {@code 'FY27'}). {@code null} when the owner has no registrationDate.
     */
    @Transient
    public String getFiscalYear() {
        if (this.registrationDate == null) {
            return null;
        }
        return String.format("FY%02d", fiscalYearOf(this.registrationDate) % 100);
    }

    /**
     * The owner's membership points, derived at read time from stored fields. Starts at 0; add 2 when
     * an email is present; add 1 when {@link #getNamesakeCount() namesakeCount} is 0; add 2 for a
     * household of 3 or more ({@link #getHouseholdSize() householdSize}); add 3 when tenure — the
     * number of elapsed fiscal years since {@link #getRegistrationDate() registrationDate}
     * ({@link #getFiscalYear() fiscal year} basis, 1 July start) — is one or more. A newly created
     * owner has zero elapsed fiscal years of tenure, so a new owner can score at most 5 points.
     */
    @Transient
    public Integer getMembershipPoints() {
        return computeMembershipPoints(this.householdSize);
    }

    private int computeMembershipPoints(Integer householdSize) {
        int points = 0;
        boolean hasEmail = this.email != null && !this.email.isBlank();
        if (hasEmail) {
            points += 2;
        }
        if (this.namesakeCount != null && this.namesakeCount == 0) {
            points += 1;
        }
        if (householdSize != null && householdSize >= 3) {
            points += 2;
        }
        if (this.registrationDate != null
                && fiscalYearOf(LocalDate.now()) - fiscalYearOf(this.registrationDate) >= 1) {
            points += 3;
        }
        return points;
    }

    /**
     * The owner's membership level, a number from 1 to 4 derived at read time by banding
     * {@link #getMembershipPoints() membershipPoints}: 1 (0-1 points), 2 (2-3), 3 (4-5), 4 (6 or more).
     * Because a new owner scores at most 5 points, only tenure (+3) can reach level 4.
     *
     * <p>A new owner also cannot outrank their household: when a {@link #getMembershipLevelCap()
     * membershipLevelCap} is set (one above the highest level in the household at the moment the owner
     * joined) the banded level is capped to it.
     */
    @Transient
    public Integer getMembershipLevel() {
        return capLevel(levelForPoints(getMembershipPoints()));
    }

    /**
     * The membership level this owner would band to if their household held {@code householdSize}
     * members, still honouring this owner's own {@link #getMembershipLevelCap() cap}. Used when a
     * new member sizes their household ceiling against the household as it stands once they join, so
     * every existing member is scored under the same (grown) household size.
     */
    public int membershipLevelForHouseholdSize(int householdSize) {
        return capLevel(levelForPoints(computeMembershipPoints(householdSize)));
    }

    private int capLevel(int level) {
        if (this.membershipLevelCap != null && level > this.membershipLevelCap) {
            return this.membershipLevelCap;
        }
        return level;
    }

    private static int levelForPoints(int points) {
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
     * The owner's segment, derived at read time and formatted {@code '<TIER>_<AREA>'} — one of
     * {@code 'PREMIUM_METRO'}, {@code 'PREMIUM_REGIONAL'}, {@code 'STANDARD_METRO'} or
     * {@code 'STANDARD_REGIONAL'}. TIER is {@code 'PREMIUM'} when {@link #getMembershipLevel()
     * membershipLevel} is 3 or more, otherwise {@code 'STANDARD'}. AREA is {@code 'METRO'} when the
     * {@link #getLocality() locality} is a known region (NSW, VIC or QLD), otherwise
     * {@code 'REGIONAL'}.
     */
    @Transient
    public String getOwnerSegment() {
        String tier = getMembershipLevel() >= 3 ? "PREMIUM" : "STANDARD";
        String area = REGION_TIMEZONE.containsKey(getLocality()) ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }

    /**
     * The owner's region, derived at read time. The {@link #getPostcode() postcode} is preferred:
     * a 4-digit postcode falling in a known region's range (NSW 2000-2099, VIC 3000-3099,
     * QLD 4000-4099) yields that region, which disambiguates cities that share a name. Only when the
     * postcode is absent (or in no known range) does it fall back to the fixed city-to-region table
     * ({@code Sydney->NSW}, {@code Melbourne->VIC}, {@code Brisbane->QLD}). Any unresolved value
     * yields {@code 'UNKNOWN'}.
     */
    @Transient
    public String getLocality() {
        String byPostcode = regionForPostcode(this.postcode);
        if (byPostcode != null) {
            return byPostcode;
        }
        return CITY_REGION.getOrDefault(this.city, "UNKNOWN");
    }

    /**
     * The owner's IANA timezone, derived at read time from the owner's region
     * ({@link #getLocality()}) via the pinned region-to-timezone table (NSW->Australia/Sydney,
     * VIC->Australia/Melbourne, QLD->Australia/Brisbane). {@code null} when the region is unknown.
     */
    @Transient
    public String getTimezone() {
        return REGION_TIMEZONE.get(getLocality());
    }

    /** The region whose range contains {@code postcode}, or {@code null} when absent/out of range. */
    private static String regionForPostcode(String postcode) {
        if (postcode == null || !postcode.matches("[0-9]{4}")) {
            return null;
        }
        int value = Integer.parseInt(postcode);
        for (Map.Entry<String, int[]> entry : REGION_POSTCODES.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * The stored E.164 {@link #getTelephone() telephone} formatted for humans, derived at read
     * time: the country code, a space, then the national digits grouped in threes (e.g.
     * {@code '+61412345678'} yields {@code '+61 412 345 678'}). Known country codes ({@code '+61'},
     * {@code '+1'}) are split off explicitly; otherwise a two-digit country code is assumed. The
     * raw {@link #getTelephone() telephone} itself stays in E.164 form. {@code null} when the owner
     * has no telephone or it is not a well-formed E.164 number.
     */
    @Transient
    public String getTelephoneDisplay() {
        if (this.telephone == null || !this.telephone.matches("\\+[0-9]+")) {
            return null;
        }
        String digits = this.telephone.substring(1); // drop the leading '+'
        String countryCode = countryCodeOf(digits);
        String national = digits.substring(countryCode.length());
        StringBuilder display = new StringBuilder("+").append(countryCode);
        display.append(' ');
        for (int i = 0; i < national.length(); i++) {
            if (i > 0 && i % 3 == 0) {
                display.append(' ');
            }
            display.append(national.charAt(i));
        }
        return display.toString();
    }

    /**
     * The E.164 country code at the start of {@code digits} (the '+'-stripped number). Known codes
     * are matched longest-first; any other number falls back to a two-digit country code.
     */
    private static String countryCodeOf(String digits) {
        for (String code : KNOWN_COUNTRY_CODES) {
            if (digits.startsWith(code) && digits.length() > code.length()) {
                return code;
            }
        }
        return digits.length() > 2 ? digits.substring(0, 2) : digits;
    }

    /** Recognised E.164 country codes, longest-first, for {@link #getTelephoneDisplay()}. */
    private static final List<String> KNOWN_COUNTRY_CODES = List.of("61", "1");

    /**
     * The owner's preferred contact channel, derived at read time: {@code 'EMAIL'} when an
     * {@link #getEmail() email} is present, otherwise {@code 'PHONE'}.
     */
    @Transient
    public String getContactPreference() {
        boolean hasEmail = this.email != null && !this.email.isBlank();
        return hasEmail ? "EMAIL" : "PHONE";
    }

    /**
     * The owner's age band, derived at read time from {@link #getBirthDate() birthDate} measured
     * against the {@link #getRegistrationDate() registrationDate}: {@code 'MINOR'} when the owner is
     * under 18 at registration, {@code 'ADULT'} from 18 to 64, {@code 'SENIOR'} at 65 or over.
     * {@code null} when the owner has no birthDate (or no registrationDate to measure against).
     */
    @Transient
    public String getAgeBand() {
        if (this.birthDate == null || this.registrationDate == null) {
            return null;
        }
        int years = java.time.Period.between(this.birthDate, this.registrationDate).getYears();
        if (years < 18) {
            return "MINOR";
        }
        if (years < 65) {
            return "ADULT";
        }
        return "SENIOR";
    }

    /**
     * The single derived duplicate-detection key: the 64-character lower-case hex SHA-256 of
     * {@code normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)} (the normalized
     * telephone, the lower-cased email or empty when absent, and the {@link Soundex} of the
     * lastName). All duplicate detection is expressed through this one key: a create whose WHOLE
     * identityKey equals an existing owner's is a duplicate. Because the telephone is part of the
     * key, two members of the same household with different telephones have different identityKeys
     * and are both allowed.
     */
    @Transient
    public String getIdentityKey() {
        return identityKey(this.telephone, this.email, this.lastName);
    }

    /**
     * Derive the {@link #getIdentityKey() identityKey} from the raw parts, so a create pipeline can
     * compute the key for an incoming request exactly as a stored owner reports it.
     */
    public static String identityKey(String telephone, String email, String lastName) {
        String tel = telephone == null ? "" : telephone;
        String mail = (email == null || email.isBlank()) ? "" : email.toLowerCase();
        return sha256Hex(tel + "|" + mail + "|" + Soundex.encode(lastName));
    }

    /** The 64-character lower-case hex SHA-256 of {@code seed}. */
    private static String sha256Hex(String seed) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(seed.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(64);
            for (byte b : hash) {
                hex.append(String.format("%02x", b & 0xff));
            }
            return hex.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e); // every JRE ships SHA-256
        }
    }

    /** City -> canonical region for {@link #getLocality()}. Any other city is 'UNKNOWN'. */
    private static final Map<String, String> CITY_REGION =
        Map.of("Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** Region -> IANA timezone for {@link #getTimezone()}. */
    private static final Map<String, String> REGION_TIMEZONE =
        Map.of("NSW", "Australia/Sydney", "VIC", "Australia/Melbourne", "QLD", "Australia/Brisbane");

    /** Region -> inclusive 4-digit postcode range {low, high}, preferred by {@link #getLocality()}. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

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
