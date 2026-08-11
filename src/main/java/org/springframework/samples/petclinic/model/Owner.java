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
    @Pattern(regexp = "^\\+[0-9]{8,15}$", message = "Phone number must be E.164 form: a leading '+' followed by 8 to 15 digits")
    private String telephone;

    @Column(name = "email")
    @Email
    private String email;

    @Column(name = "registration_date", columnDefinition = "DATE")
    private LocalDate registrationDate;

    @Column(name = "birth_date", columnDefinition = "DATE")
    private LocalDate birthDate;

    @Column(name = "customer_code")
    private String customerCode;

    @Column(name = "household_id")
    private String householdId;

    @Column(name = "namesake_count")
    private Integer namesakeCount;

    @Column(name = "household_size")
    private Integer householdSize;

    @Column(name = "membership_level")
    private Integer membershipLevel;

    @Column(name = "postcode")
    private String postcode;

    @Column(name = "possible_duplicate")
    private Boolean possibleDuplicate;

    @Column(name = "possible_duplicate_of")
    private Integer possibleDuplicateOf;

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

    /**
     * The owner's salutation, derived from the {@link #title} and last name: {@code title + ' ' +
     * lastName} when a title is present, or just the last name when no title was supplied.
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

    /**
     * Stores the email lower-cased so it is persisted and returned in a canonical form. A
     * {@code null} value (email omitted) is preserved as {@code null}.
     */
    public void setEmail(String email) {
        this.email = email == null ? null : email.toLowerCase(Locale.ROOT);
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
     * The owner's age band, derived from {@link #birthDate} measured against the
     * {@link #registrationDate}: {@code MINOR} when under 18, {@code ADULT} from 18 to 64
     * inclusive, and {@code SENIOR} at 65 or over. Returns {@code null} when no birth date was
     * supplied, so the field is simply absent from the response.
     */
    @Transient
    public String getAgeBand() {
        if (this.birthDate == null) {
            return null;
        }
        LocalDate reference = this.registrationDate != null ? this.registrationDate : LocalDate.now();
        int age = java.time.Period.between(this.birthDate, reference).getYears();
        if (age < 18) {
            return "MINOR";
        }
        if (age < 65) {
            return "ADULT";
        }
        return "SENIOR";
    }

    /** National-number length by country code, mirroring the create-time E.164 normalization. */
    private static final Map<String, Integer> COUNTRY_NATIONAL_LENGTH = Map.of("61", 9, "1", 10);

    /**
     * The stored E.164 {@link #telephone} formatted for humans: the country code, a space, then the
     * national digits grouped in threes from the left (e.g. {@code +61412345678 -> +61 412 345 678}).
     * The country code is recognised from the known set ({@code +61} with 9 national digits, {@code +1}
     * with 10) that the create-time normalization produces. Values that are absent or not in E.164 form
     * are returned unchanged, so the raw {@link #telephone} still stands in for a display value.
     */
    @Transient
    public String getTelephoneDisplay() {
        if (this.telephone == null || !this.telephone.startsWith("+")) {
            return this.telephone;
        }
        String digits = this.telephone.substring(1);
        if (!digits.matches("[0-9]+")) {
            return this.telephone;
        }
        String countryCode = null;
        for (Map.Entry<String, Integer> entry : COUNTRY_NATIONAL_LENGTH.entrySet()) {
            if (digits.startsWith(entry.getKey())
                && digits.length() - entry.getKey().length() == entry.getValue()) {
                countryCode = entry.getKey();
                break;
            }
        }
        if (countryCode == null) {
            return this.telephone;
        }
        String national = digits.substring(countryCode.length());
        StringBuilder grouped = new StringBuilder();
        for (int i = 0; i < national.length(); i += 3) {
            if (grouped.length() > 0) {
                grouped.append(' ');
            }
            grouped.append(national, i, Math.min(i + 3, national.length()));
        }
        return "+" + countryCode + " " + grouped;
    }

    public String getCustomerCode() {
        return this.customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    /**
     * A single Luhn check digit (0-9) computed over the digits contained in the
     * {@link #customerCode}. Non-digit characters in the code are ignored; the standard Luhn
     * algorithm doubles every second digit from the right (subtracting 9 when the result exceeds
     * 9) and the check digit is {@code (10 - (sum % 10)) % 10}. {@code null} until a customer code
     * is assigned.
     */
    @Transient
    public Integer getCheckDigit() {
        if (this.customerCode == null) {
            return null;
        }
        int sum = 0;
        boolean dbl = true;
        for (int i = this.customerCode.length() - 1; i >= 0; i--) {
            char c = this.customerCode.charAt(i);
            if (c < '0' || c > '9') {
                continue;
            }
            int d = c - '0';
            if (dbl) {
                d *= 2;
                if (d > 9) {
                    d -= 9;
                }
            }
            sum += d;
            dbl = !dbl;
        }
        return (10 - (sum % 10)) % 10;
    }

    /** The month on which the fiscal year starts: 1 July. */
    private static final java.time.Month FISCAL_YEAR_START_MONTH = java.time.Month.JULY;

    /**
     * The calendar year in which the fiscal year containing {@code date} began. The fiscal year
     * starts on 1 July, so a date in July through December belongs to a fiscal year that began in
     * its own calendar year, while a date in January through June belongs to one that began in the
     * previous calendar year.
     */
    private static int fiscalYearStart(LocalDate date) {
        return date.getMonthValue() >= FISCAL_YEAR_START_MONTH.getValue() ? date.getYear() : date.getYear() - 1;
    }

    /**
     * The owner's fiscal year, formatted {@code FY<YY>} where {@code YY} is the last two digits of
     * the calendar year in which the fiscal year containing the {@code registrationDate} began (the
     * fiscal year starts on 1 July; e.g. a registration on 2026-08-11 yields {@code FY26}). Derived
     * from the (business-day-adjusted) {@code registrationDate}; {@code null} until it is assigned.
     */
    @Transient
    public String getFiscalYear() {
        if (this.registrationDate == null) {
            return null;
        }
        return String.format("FY%02d", fiscalYearStart(this.registrationDate) % 100);
    }

    /**
     * The owner's membership number, formatted {@code <customerCode>-M<YY>} where {@code YY} is
     * the last two digits of the fiscal year of the {@code registrationDate} (the fiscal year
     * starts on 1 July; e.g. {@code NSW-1A2B3C4D-M26}). Derived from the owner's own fields;
     * {@code null} until both are assigned.
     */
    @Transient
    public String getMembershipNumber() {
        if (this.customerCode == null || this.registrationDate == null) {
            return null;
        }
        return String.format("%s-M%02d", this.customerCode, fiscalYearStart(this.registrationDate) % 100);
    }

    /**
     * The owner's membership points, derived from the owner's own fields. Points start at 0 and
     * accumulate: 2 when an email is present, 1 when the owner has no namesakes ({@code namesakeCount}
     * is 0), 2 when the owner belongs to a household of 3 or more members ({@code householdSize} is at
     * least 3), and 3 when the owner's tenure — the number of elapsed fiscal years from
     * {@link #registrationDate} to today, the fiscal year starting on 1 July — exceeds one. Because
     * a newly created owner registers in the current fiscal year and so has zero tenure, and joins a
     * household of at most itself unless others already share it, a new owner's points reflect only the
     * factors captured at creation time.
     */
    @Transient
    public Integer getMembershipPoints() {
        int points = 0;
        boolean hasEmail = this.email != null && !this.email.isBlank();
        if (hasEmail) {
            points += 2;
        }
        boolean noNamesakes = this.namesakeCount != null && this.namesakeCount == 0;
        if (noNamesakes) {
            points += 1;
        }
        boolean largeHousehold = this.householdSize != null && this.householdSize >= 3;
        if (largeHousehold) {
            points += 2;
        }
        boolean longTenure = this.registrationDate != null
            && (fiscalYearStart(LocalDate.now()) - fiscalYearStart(this.registrationDate)) > 1;
        if (longTenure) {
            points += 3;
        }
        return points;
    }

    /**
     * The owner's membership level, a number from 1 to 4. Ordinarily it is the level derived from
     * {@link #getMembershipPoints()} (level 1 for 0-1 points, level 2 for 2-3 points, level 3 for 4-5
     * points, and level 4 for 6 or more points). When an explicit level has been assigned at creation
     * time — because it was capped to at most one above the maximum level among the owner's existing
     * household members — that stored value is returned instead.
     */
    public Integer getMembershipLevel() {
        if (this.membershipLevel != null) {
            return this.membershipLevel;
        }
        return getComputedMembershipLevel();
    }

    public void setMembershipLevel(Integer membershipLevel) {
        this.membershipLevel = membershipLevel;
    }

    /**
     * The membership level derived purely from {@link #getMembershipPoints()} (before any
     * household level ceiling is applied): level 1 for 0-1 points, level 2 for 2-3 points, level 3
     * for 4-5 points, and level 4 for 6 or more points.
     */
    @Transient
    public Integer getComputedMembershipLevel() {
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

    /** City -> canonical region for the {@code region} derivation. */
    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** Region -> inclusive 4-digit postcode range {low, high} for the {@code region} derivation. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
        "NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099}, "QLD", new int[] {4000, 4099});

    /** Region -> IANA timezone for the {@code timezone} derivation. */
    private static final Map<String, String> REGION_TIMEZONE = Map.of(
        "NSW", "Australia/Sydney", "VIC", "Australia/Melbourne", "QLD", "Australia/Brisbane");

    /**
     * The region code that forms the {@code REGION} segment of the {@link #customerCode}, derived
     * by looking up the region by {@link #postcode} range first (NSW 2000-2099, VIC 3000-3099,
     * QLD 4000-4099), and only falling back to the fixed city-to-region table (Sydney->NSW,
     * Melbourne->VIC, Brisbane->QLD) when the postcode is absent or in no known range. Returns
     * {@code UNKNOWN} when neither source resolves a region.
     */
    @Transient
    public String getRegion() {
        String region = regionForPostcode(this.postcode);
        if (region != null) {
            return region;
        }
        return CITY_REGION.getOrDefault(this.city, "UNKNOWN");
    }

    /**
     * The owner's locality: the {@code REGION} segment of its {@link #customerCode} (everything
     * before the first {@code '-'}). Because the customer code is {@code <REGION>-<HASH8>} with
     * {@code REGION} derived from the postcode, the locality now simply reads that region back off
     * the identity. Returns {@code UNKNOWN} until a customer code is assigned.
     */
    @Transient
    public String getLocality() {
        if (this.customerCode == null) {
            return "UNKNOWN";
        }
        int dash = this.customerCode.indexOf('-');
        return dash < 0 ? this.customerCode : this.customerCode.substring(0, dash);
    }

    /**
     * The owner's timezone as an IANA name, derived from its {@link #getLocality() locality} (the
     * region) via the fixed region-to-timezone table: {@code NSW->Australia/Sydney},
     * {@code VIC->Australia/Melbourne}, {@code QLD->Australia/Brisbane}. Returns {@code null} when
     * the locality is not one of those regions.
     */
    @Transient
    public String getTimezone() {
        return REGION_TIMEZONE.get(getLocality());
    }

    /**
     * The owner's segment, formatted {@code <TIER>_<AREA>}. {@code TIER} is {@code PREMIUM} when
     * {@link #getMembershipLevel() membershipLevel} is 3 or more, otherwise {@code STANDARD}.
     * {@code AREA} is {@code METRO} when the {@link #getLocality() locality} is a known region
     * (NSW, VIC or QLD), otherwise {@code REGIONAL}. Thus one of {@code PREMIUM_METRO},
     * {@code PREMIUM_REGIONAL}, {@code STANDARD_METRO} or {@code STANDARD_REGIONAL}.
     */
    @Transient
    public String getOwnerSegment() {
        Integer level = getMembershipLevel();
        String tier = (level != null && level >= 3) ? "PREMIUM" : "STANDARD";
        String area = REGION_TIMEZONE.containsKey(getLocality()) ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }

    /** Region whose postcode range contains the given postcode, or {@code null} when none does. */
    private static String regionForPostcode(String postcode) {
        if (postcode == null) {
            return null;
        }
        int value;
        try {
            value = Integer.parseInt(postcode.trim());
        }
        catch (NumberFormatException ex) {
            return null;
        }
        for (Map.Entry<String, int[]> entry : REGION_POSTCODES.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * The owner's identity key, the single derived value all duplicate detection is based on. It is
     * the full lower-case hex SHA-256 digest of {@code normalizedTelephone + '|' + lowerEmail + '|' +
     * soundex(lastName)}, where the telephone is the stored E.164 value, the email is the stored
     * lower-cased value (empty when absent) and the last-name segment is the American Soundex code of
     * the last name. Two owners are duplicates only when their whole identity keys are equal, so
     * owners sharing a last name (by soundex) and postcode but carrying different telephones no longer
     * collide here — they are surfaced as a soft (possible) duplicate instead.
     */
    @Transient
    public String getIdentityKey() {
        String telephonePart = this.telephone == null ? "" : this.telephone;
        String emailPart = (this.email == null || this.email.isBlank()) ? "" : this.email;
        String key = telephonePart + "|" + emailPart + "|" + soundex(getLastName());
        return sha256Hex(key);
    }

    /**
     * The American Soundex code of {@code name}: the first letter followed by three digits (padded
     * with {@code '0'} when there are too few coded consonants, truncated to four characters
     * otherwise). Non-letters are ignored; a {@code null}, blank, or letter-free value yields the
     * empty string. Consonants are grouped b/f/p/v=1, c/g/j/k/q/s/x/z=2, d/t=3, l=4, m/n=5, r=6;
     * adjacent same-coded letters (and same-coded letters separated only by {@code h}/{@code w}) are
     * coded once, while a vowel between them causes both to be coded.
     */
    public static String soundex(String name) {
        if (name == null) {
            return "";
        }
        StringBuilder letters = new StringBuilder();
        for (char c : name.toUpperCase(Locale.ROOT).toCharArray()) {
            if (c >= 'A' && c <= 'Z') {
                letters.append(c);
            }
        }
        if (letters.length() == 0) {
            return "";
        }
        StringBuilder code = new StringBuilder().append(letters.charAt(0));
        char prevCode = soundexCode(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            if (c == 'H' || c == 'W') {
                // Acts as a connector: same-coded consonants either side stay adjacent.
                continue;
            }
            if (c == 'A' || c == 'E' || c == 'I' || c == 'O' || c == 'U' || c == 'Y') {
                // A vowel separates same-coded consonants, so both are coded.
                prevCode = '0';
                continue;
            }
            char digit = soundexCode(c);
            if (digit != '0' && digit != prevCode) {
                code.append(digit);
            }
            prevCode = digit;
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    private static char soundexCode(char c) {
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

    /** The full lower-case hex SHA-256 digest of the UTF-8 bytes of {@code value}. */
    private static String sha256Hex(String value) {
        try {
            byte[] digest = java.security.MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
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

    /**
     * Whether this owner has been soft-deleted. A newly created owner is not deleted; a
     * {@code DELETE /api/owners/{id}} marks the owner deleted (setting this true) while retaining the
     * row, and duplicate/identity detection ignores owners flagged deleted.
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
