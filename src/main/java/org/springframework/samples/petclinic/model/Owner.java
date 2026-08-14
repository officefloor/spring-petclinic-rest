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
import java.time.Period;
import java.time.temporal.ChronoUnit;
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
    @Pattern(regexp = "^\\+[0-9]{8,15}$", message = "Phone number must be in E.164 form (a '+' followed by 8 to 15 digits)")
    private String telephone;

    @Column(name = "email")
    @Email
    private String email;

    @Column(name = "title")
    private String title;

    @Column(name = "registration_date")
    private LocalDate registrationDate;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "postcode")
    @Pattern(regexp = "^[0-9]{4}$", message = "Postcode must be 4 digits")
    private String postcode;

    @Column(name = "customer_code")
    private String customerCode;

    @Column(name = "household_id")
    private String householdId;

    @Column(name = "namesake_count")
    private Integer namesakeCount;

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

    /** Common street-type abbreviations expanded during address normalization. */
    private static final Map<String, String> ADDRESS_ABBREVIATIONS = Map.of(
        "ST", "STREET",
        "RD", "ROAD",
        "AVE", "AVENUE");

    /** Fixed city-to-region table used to derive an owner's locality. */
    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW",
        "Melbourne", "VIC",
        "Brisbane", "QLD");

    /** Fixed region-to-postcode-range table (inclusive) used to validate a postcode. */
    private static final Map<String, int[]> REGION_POSTCODE_RANGES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /** Fixed region-to-timezone table (IANA names) used to derive an owner's timezone. */
    private static final Map<String, String> REGION_TIMEZONE = Map.of(
        "NSW", "Australia/Sydney",
        "VIC", "Australia/Melbourne",
        "QLD", "Australia/Brisbane");

    /**
     * Return the owner's address. When a structured address is supplied (a non-blank
     * {@code addressLine1}) it takes precedence and the value is composed as the
     * normalized {@code addressLine1}, with a single space and the normalized
     * {@code addressLine2} appended when an {@code addressLine2} is present.
     * Otherwise the flat {@code address} is returned, preserving backward
     * compatibility for owners supplied in the flat form.
     */
    public String getAddress() {
        if (this.addressLine1 != null && !this.addressLine1.isBlank()) {
            if (this.addressLine2 != null && !this.addressLine2.isBlank()) {
                return this.addressLine1 + " " + this.addressLine2;
            }
            return this.addressLine1;
        }
        return this.address;
    }

    /**
     * Set the owner's flat address, normalizing it to a canonical form so it is both
     * stored and returned consistently. Leading/trailing whitespace is trimmed,
     * internal runs of whitespace are collapsed to a single space, the value is
     * upper-cased, and common street-type abbreviations are expanded ({@code ST ->
     * STREET}, {@code RD -> ROAD}, {@code AVE -> AVENUE}). A {@code null} value is
     * left as-is.
     */
    public void setAddress(String address) {
        this.address = normalizeAddress(address);
    }

    public String getAddressLine1() {
        return this.addressLine1;
    }

    /**
     * Set the first line of the owner's structured address, normalized the same way
     * as the flat {@link #setAddress(String)} (trimmed, whitespace collapsed,
     * upper-cased, street-type abbreviations expanded). A {@code null} value is left
     * as-is.
     */
    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = normalizeAddress(addressLine1);
    }

    public String getAddressLine2() {
        return this.addressLine2;
    }

    /**
     * Set the optional second line of the owner's structured address, normalized the
     * same way as {@link #setAddressLine1(String)}. A {@code null} value is left
     * as-is.
     */
    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = normalizeAddress(addressLine2);
    }

    /**
     * Normalize an address string to its canonical form: trimmed, whitespace
     * collapsed, upper-cased, with common street-type abbreviations expanded.
     * Returns {@code null} for a {@code null} input; a value that is blank once
     * trimmed normalizes to the empty string.
     */
    public static String normalizeAddress(String address) {
        if (address == null) {
            return null;
        }
        String collapsed = address.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        if (collapsed.isEmpty()) {
            return "";
        }
        String[] tokens = collapsed.split(" ");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(ADDRESS_ABBREVIATIONS.getOrDefault(tokens[i], tokens[i]));
        }
        return sb.toString();
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
     * Return the stored E.164 telephone formatted for humans: a {@code '+'} and the
     * country code, a space, then the national digits grouped in threes and separated
     * by single spaces (for example {@code '+61412345678' -> '+61 412 345 678'}). The
     * North American country code ({@code '+1'}) is one digit; other country codes are
     * taken as two. Returns {@code null} when the telephone is not set, and returns the
     * raw value unchanged when it is not a well-formed E.164 number.
     */
    public String getTelephoneDisplay() {
        if (this.telephone == null || !this.telephone.matches("^\\+[0-9]+$")) {
            return this.telephone;
        }
        String digits = this.telephone.substring(1);
        int countryCodeLength = digits.startsWith("1") ? 1 : 2;
        if (digits.length() <= countryCodeLength) {
            return this.telephone;
        }
        String countryCode = digits.substring(0, countryCodeLength);
        String national = digits.substring(countryCodeLength);
        StringBuilder sb = new StringBuilder("+").append(countryCode);
        for (int i = 0; i < national.length(); i += 3) {
            sb.append(' ').append(national, i, Math.min(i + 3, national.length()));
        }
        return sb.toString();
    }

    public String getEmail() {
        return this.email;
    }

    /**
     * Set the owner's email address, normalizing it to lower case so it is both
     * stored and returned in a canonical form. A {@code null} value is left as-is.
     */
    public void setEmail(String email) {
        this.email = (email == null) ? null : email.toLowerCase(Locale.ROOT);
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Return the owner's salutation: the title and last name separated by a single
     * space (for example {@code 'DR who'}), or just the last name when no title was
     * supplied (a {@code null} or blank title).
     */
    public String getSalutation() {
        if (this.title == null || this.title.isBlank()) {
            return getLastName();
        }
        return this.title + " " + getLastName();
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
     * Return the owner's age band derived from the birth date, computed against the
     * registration date: {@code 'MINOR'} when under 18, {@code 'ADULT'} when 18 to
     * 64 inclusive, and {@code 'SENIOR'} when 65 or older. Returns {@code null} when
     * either the birth date or the registration date is not set.
     */
    public String getAgeBand() {
        if (this.birthDate == null || this.registrationDate == null) {
            return null;
        }
        int age = Period.between(this.birthDate, this.registrationDate).getYears();
        if (age < 18) {
            return "MINOR";
        }
        if (age < 65) {
            return "ADULT";
        }
        return "SENIOR";
    }

    public String getPostcode() {
        return this.postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    /**
     * Whether this owner's postcode is valid for its city. A {@code null} postcode
     * is always valid (postcode is optional). When present, a postcode is valid if
     * the city has no known region, or if it is four digits falling within the
     * inclusive range for the city's region ({@code Sydney -> NSW 2000-2099},
     * {@code Melbourne -> VIC 3000-3099}, {@code Brisbane -> QLD 4000-4099}).
     */
    public boolean isPostcodeValidForCity() {
        if (this.postcode == null) {
            return true;
        }
        String region = CITY_REGION.get(this.city);
        int[] range = region == null ? null : REGION_POSTCODE_RANGES.get(region);
        if (range == null) {
            return true;
        }
        if (!this.postcode.matches("^[0-9]{4}$")) {
            return false;
        }
        int value = Integer.parseInt(this.postcode);
        return value >= range[0] && value <= range[1];
    }

    public String getCustomerCode() {
        return this.customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    /**
     * Return a single Luhn check digit (0-9) computed over the digits contained in
     * the owner's customer code, or {@code null} when the customer code is not set.
     * Non-digit characters in the code are ignored; the rightmost digit is doubled
     * first, following the standard Luhn algorithm.
     */
    public Integer getCheckDigit() {
        if (this.customerCode == null) {
            return null;
        }
        int sum = 0;
        boolean doubleDigit = true;
        for (int i = this.customerCode.length() - 1; i >= 0; i--) {
            char c = this.customerCode.charAt(i);
            if (c < '0' || c > '9') {
                continue;
            }
            int digit = c - '0';
            if (doubleDigit) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            doubleDigit = !doubleDigit;
        }
        return (10 - (sum % 10)) % 10;
    }

    /**
     * Return the owner's membership number, formatted {@code '<customerCode>-M<YY>'}
     * where {@code YY} is the last two digits of the registration date's year (for
     * example {@code 'SMI-0007-M26'}). Returns {@code null} when either the customer
     * code or the registration date is not set.
     */
    public String getMembershipNumber() {
        if (this.customerCode == null || this.registrationDate == null) {
            return null;
        }
        return String.format("%s-M%02d", this.customerCode, this.registrationDate.getYear() % 100);
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

    /**
     * Return the owner's membership points, the raw score from which the
     * membership level is derived. Starting at {@code 0}, add {@code 2} when an
     * email address is present, {@code 1} when the owner has no namesakes
     * (namesake count is {@code 0}), {@code 2} for a household of {@code 3} or
     * more, and {@code 3} when the owner's tenure (days since the registration
     * date) exceeds {@code 365}.
     */
    public Integer getMembershipPoints() {
        int points = 0;
        boolean hasEmail = this.email != null && !this.email.isBlank();
        if (hasEmail) {
            points += 2;
        }
        if (this.namesakeCount != null && this.namesakeCount == 0) {
            points += 1;
        }
        if (this.householdSize != null && this.householdSize >= 3) {
            points += 2;
        }
        if (this.registrationDate != null
                && ChronoUnit.DAYS.between(this.registrationDate, LocalDate.now()) > 365) {
            points += 3;
        }
        return points;
    }

    /**
     * Return the owner's membership level, a number from {@code 1} to {@code 4},
     * derived from {@link #getMembershipPoints()}: {@code 1} for {@code 0-1}
     * points, {@code 2} for {@code 2-3}, {@code 3} for {@code 4-5}, and
     * {@code 4} for {@code 6} or more.
     */
    public Integer getMembershipLevel() {
        int points = getMembershipPoints();
        if (points >= 6) {
            return 4;
        }
        if (points >= 4) {
            return 3;
        }
        if (points >= 2) {
            return 2;
        }
        return 1;
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
     * Whether this owner, when created, softly matched an existing owner by
     * sharing the same last name and postcode but with a different telephone.
     * Such an owner is still created; the flag surfaces the likely duplication.
     */
    public Boolean getPossibleDuplicate() {
        return this.possibleDuplicate;
    }

    public void setPossibleDuplicate(Boolean possibleDuplicate) {
        this.possibleDuplicate = possibleDuplicate;
    }

    /**
     * The id of the existing owner this owner softly matched on last name and
     * postcode (with a different telephone), or {@code null} when there is no
     * such match.
     */
    public Integer getPossibleDuplicateOf() {
        return this.possibleDuplicateOf;
    }

    public void setPossibleDuplicateOf(Integer possibleDuplicateOf) {
        this.possibleDuplicateOf = possibleDuplicateOf;
    }

    /**
     * Whether more than 80 owners had already been registered on this owner's
     * registration day at the time it was created, flagging a bulk-signup surge.
     */
    public Boolean getBulkSignupWarning() {
        return this.bulkSignupWarning;
    }

    public void setBulkSignupWarning(Boolean bulkSignupWarning) {
        this.bulkSignupWarning = bulkSignupWarning;
    }

    /**
     * Return the owner's locality (region), preferring the postcode: when the
     * postcode is present and falls within a known region's inclusive range
     * ({@code NSW 2000-2099}, {@code VIC 3000-3099}, {@code QLD 4000-4099}), that
     * region is returned. Otherwise the value falls back to the fixed
     * city-to-region table ({@code Sydney -> NSW}, {@code Melbourne -> VIC},
     * {@code Brisbane -> QLD}), or {@code 'UNKNOWN'} when the city is not in the
     * table. This yields the same region for known cities but disambiguates
     * cities that share a name.
     */
    public String getLocality() {
        String regionFromPostcode = regionForPostcode();
        if (regionFromPostcode != null) {
            return regionFromPostcode;
        }
        return CITY_REGION.getOrDefault(this.city, "UNKNOWN");
    }

    /**
     * Return the owner's IANA timezone, derived from the locality/region via the
     * fixed region-to-timezone table ({@code NSW -> Australia/Sydney},
     * {@code VIC -> Australia/Melbourne}, {@code QLD -> Australia/Brisbane}), or
     * {@code null} when the region is not in the table.
     */
    public String getTimezone() {
        return REGION_TIMEZONE.get(getLocality());
    }

    /**
     * Look up the region whose inclusive postcode range contains this owner's
     * postcode, or {@code null} when the postcode is absent, malformed, or in no
     * known range.
     */
    private String regionForPostcode() {
        if (this.postcode == null || !this.postcode.matches("^[0-9]{4}$")) {
            return null;
        }
        int value = Integer.parseInt(this.postcode);
        for (Map.Entry<String, int[]> entry : REGION_POSTCODE_RANGES.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Return the owner's preferred contact channel, derived from the stored
     * contact details: {@code 'EMAIL'} when an email address is present,
     * otherwise {@code 'PHONE'}.
     */
    public String getContactPreference() {
        boolean hasEmail = this.email != null && !this.email.isBlank();
        return hasEmail ? "EMAIL" : "PHONE";
    }

    /**
     * Return the owner's derived identity key, which consolidates all duplicate
     * detection into a single value formed as
     * {@code '<normalizedTelephone>|<email or empty>|<householdId or empty>'}.
     * The telephone and email are already stored in their normalized (E.164 /
     * lower-cased) forms; a missing email or household id contributes an empty
     * segment. A create is rejected only when a new owner's whole identity key
     * equals an existing owner's.
     */
    public String getIdentityKey() {
        String telephonePart = this.telephone == null ? "" : this.telephone;
        String emailPart = (this.email == null) ? "" : this.email;
        String householdPart = (this.householdId == null) ? "" : this.householdId;
        return telephonePart + "|" + emailPart + "|" + householdPart;
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
