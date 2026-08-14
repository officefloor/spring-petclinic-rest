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
    @Pattern(regexp = "^\\+[0-9]{8,15}$", message = "Phone number must be in E.164 form")
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

    /**
     * The stored E.164 {@link #telephone} formatted for humans as the country code, a single
     * space, then the national digits (the digits after the country code) grouped in threes from
     * the left, e.g. {@code "+61412345678"} becomes {@code "+61 412 345 678"}. The country code is
     * a single digit for the {@code +1} and {@code +7} zones and two digits otherwise.
     *
     * @return the human-formatted telephone, or the raw {@link #telephone} unchanged when it is
     *         {@code null} or not a {@code +} followed by digits
     */
    public String getTelephoneDisplay() {
        if (this.telephone == null || !this.telephone.matches("\\+[0-9]+")) {
            return this.telephone;
        }
        String digits = this.telephone.substring(1);
        int countryCodeLength = ONE_DIGIT_CALLING_CODES.contains(digits.substring(0, 1)) ? 1 : 2;
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
     * The E.164 calling codes that are a single digit ({@code +1} for the North American Numbering
     * Plan and {@code +7} for the Russia/Kazakhstan zone); every other calling code is two digits.
     */
    private static final Set<String> ONE_DIGIT_CALLING_CODES = Set.of("1", "7");

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
     * The owner's age band, derived from the {@link #birthDate} measured in completed years
     * against the {@link #registrationDate}: {@code "MINOR"} when under 18, {@code "ADULT"}
     * from 18 to 64, and {@code "SENIOR"} at 65 and over.
     *
     * @return the age band, or {@code null} when either the birth date or the registration
     *         date has not been assigned
     */
    public String getAgeBand() {
        if (this.birthDate == null || this.registrationDate == null) {
            return null;
        }
        int age = java.time.Period.between(this.birthDate, this.registrationDate).getYears();
        if (age < 18) {
            return "MINOR";
        }
        if (age < 65) {
            return "ADULT";
        }
        return "SENIOR";
    }

    public String getCustomerCode() {
        return this.customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
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
     * Whether more than 80 owners had already been created today at the time this owner
     * was created, indicating an unusually high signup volume.
     *
     * @return {@code true} when the bulk-signup threshold was exceeded, otherwise
     *         {@code false} (never {@code null})
     */
    public Boolean getBulkSignupWarning() {
        return this.bulkSignupWarning != null && this.bulkSignupWarning;
    }

    public void setBulkSignupWarning(Boolean bulkSignupWarning) {
        this.bulkSignupWarning = bulkSignupWarning;
    }

    /**
     * The number of owners in this owner's household (owners sharing the same
     * {@code householdId}) as of when this owner was created, used to derive the GOLD
     * membership tier.
     *
     * @return the household member count, or {@code null} if it has not been assigned
     */
    public Integer getHouseholdSize() {
        return this.householdSize;
    }

    public void setHouseholdSize(Integer householdSize) {
        this.householdSize = householdSize;
    }

    /**
     * Whether this owner, though not a hard duplicate, shares an existing owner's last name and
     * postcode while carrying a different telephone, flagging a likely duplicate registration.
     *
     * @return {@code true} when a soft-duplicate match was found on creation, otherwise
     *         {@code false} (never {@code null})
     */
    public Boolean getPossibleDuplicate() {
        return this.possibleDuplicate != null && this.possibleDuplicate;
    }

    public void setPossibleDuplicate(Boolean possibleDuplicate) {
        this.possibleDuplicate = possibleDuplicate;
    }

    /**
     * The id of the existing owner this owner may duplicate (same last name and postcode,
     * different telephone), or {@code null} when this owner is not a possible duplicate.
     *
     * @return the matching owner id, or {@code null}
     */
    public Integer getPossibleDuplicateOf() {
        return this.possibleDuplicateOf;
    }

    public void setPossibleDuplicateOf(Integer possibleDuplicateOf) {
        this.possibleDuplicateOf = possibleDuplicateOf;
    }

    /**
     * The owner's name formatted as {@code "LastName, FirstName"} from the stored names.
     *
     * @return the formatted display name
     */
    public String getDisplayName() {
        return this.getLastName() + ", " + this.getFirstName();
    }

    /**
     * The owner's initials: the upper-cased first letters of the first and last
     * names, dot-separated with a trailing dot (e.g. {@code "J.S."}).
     *
     * @return the formatted initials
     */
    public String getInitials() {
        return Character.toUpperCase(this.getFirstName().charAt(0)) + "."
            + Character.toUpperCase(this.getLastName().charAt(0)) + ".";
    }

    /**
     * The owner's membership number formatted as {@code "<customerCode>-M<YY>"}, where
     * {@code YY} is the last two digits of the registration date's year (e.g.
     * {@code "NSW-1A2B3C4D-M26"}).
     *
     * @return the formatted membership number, or {@code null} if the customer code or
     *         registration date has not been assigned
     */
    public String getMembershipNumber() {
        if (this.customerCode == null || this.registrationDate == null) {
            return null;
        }
        return String.format("%s-M%02d", this.customerCode, this.registrationDate.getYear() % 100);
    }

    /**
     * The owner's Luhn check digit: a single digit ({@code 0}-{@code 9}) computed with the
     * standard Luhn algorithm over the decimal digits contained in the {@code customerCode}
     * (non-digit characters are ignored).
     *
     * @return the Luhn check digit, or {@code null} if the customer code has not been assigned
     */
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

    /**
     * The owner's membership points, starting at {@code 0}. The owner gains {@code 2} when
     * they carry an email address, {@code 1} when they have no namesakes
     * ({@code namesakeCount} is {@code 0}), {@code 2} for a household of {@code 3} or more
     * members ({@code householdSize}), and {@code 3} for tenure of more than {@code 365}
     * days (measured from the {@code registrationDate}). Because a newly created owner has
     * zero tenure, a new owner can score at most {@code 5} points.
     *
     * @return the membership points
     */
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
        if (this.householdSize != null && this.householdSize >= 3) {
            points += 2;
        }
        if (this.registrationDate != null
            && java.time.temporal.ChronoUnit.DAYS.between(this.registrationDate, LocalDate.now()) > 365) {
            points += 3;
        }
        return points;
    }

    /**
     * The owner's numeric membership level, from {@code 1} to {@code 4}, derived from the
     * {@link #getMembershipPoints() membership points}: {@code 1} for {@code 0-1} points,
     * {@code 2} for {@code 2-3} points, {@code 3} for {@code 4-5} points, and {@code 4} for
     * {@code 6} or more points. Because a newly created owner has zero tenure, a new owner
     * scores at most {@code 5} points and so never exceeds level {@code 3}; level {@code 4}
     * is reserved for owners whose tenure exceeds {@code 365} days.
     *
     * @return the membership level
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

    /**
     * The owner's preferred contact channel: {@code "EMAIL"} when the owner carries an
     * email address, otherwise {@code "PHONE"}.
     *
     * @return {@code "EMAIL"} when an email is present, otherwise {@code "PHONE"}
     */
    public String getContactPreference() {
        boolean hasEmail = this.email != null && !this.email.isBlank();
        return hasEmail ? "EMAIL" : "PHONE";
    }

    /**
     * The owner's identity key: the single derived value used for duplicate detection, formed as
     * {@code normalizedTelephone + "|" + (email or empty) + "|" + householdId}. The telephone and
     * email are the already-normalized stored values and a {@code null} email or household id is
     * rendered as the empty string. Two owners are duplicates only when their whole identity keys
     * are equal; because the telephone is part of the key, household members with different
     * telephones have different identity keys.
     *
     * @return the derived identity key
     */
    public String getIdentityKey() {
        String telephonePart = this.telephone == null ? "" : this.telephone;
        String emailPart = this.email == null ? "" : this.email;
        String householdPart = this.householdId == null ? "" : this.householdId;
        return telephonePart + "|" + emailPart + "|" + householdPart;
    }

    /**
     * City -> canonical region, the fixed ground truth for deriving locality.
     */
    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /**
     * Region -> inclusive 4-digit postcode range {@code {low, high}}, the fixed ground truth for
     * deriving locality from the postcode ahead of the city.
     */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
        "NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099}, "QLD", new int[] {4000, 4099});

    /**
     * The owner's locality: the canonical region resolved by the postcode range first
     * ({@code NSW 2000-2099}, {@code VIC 3000-3099}, {@code QLD 4000-4099}), falling back to the
     * fixed city-to-region table ({@code Sydney->NSW}, {@code Melbourne->VIC},
     * {@code Brisbane->QLD}) when the postcode is absent or in no known range, or {@code "UNKNOWN"}
     * when the city is not in the table either. The postcode takes precedence so cities that share a
     * name are disambiguated by their postcode's region.
     *
     * @return the canonical region string, or {@code "UNKNOWN"}
     */
    public String getLocality() {
        String byPostcode = regionForPostcode(this.postcode);
        if (byPostcode != null) {
            return byPostcode;
        }
        return CITY_REGION.getOrDefault(this.city, "UNKNOWN");
    }

    /**
     * Region -> IANA timezone name, the fixed ground truth for deriving the owner's timezone from
     * its locality.
     */
    private static final Map<String, String> REGION_TIMEZONE = Map.of(
        "NSW", "Australia/Sydney", "VIC", "Australia/Melbourne", "QLD", "Australia/Brisbane");

    /**
     * The owner's timezone: the IANA name resolved from the {@link #getLocality() locality} via the
     * fixed region-to-timezone table ({@code NSW->Australia/Sydney}, {@code VIC->Australia/Melbourne},
     * {@code QLD->Australia/Brisbane}), or {@code null} when the locality is not one of these regions
     * (e.g. {@code "UNKNOWN"}).
     *
     * @return the IANA timezone name, or {@code null}
     */
    public String getTimezone() {
        return REGION_TIMEZONE.get(getLocality());
    }

    /**
     * Region whose postcode range contains the given postcode, or {@code null} when the postcode is
     * absent, not a 4-digit number, or in no known range.
     *
     * @param postcode the owner's postcode (may be {@code null})
     * @return the matching region, or {@code null}
     */
    private static String regionForPostcode(String postcode) {
        if (postcode == null || !postcode.matches("\\d{4}")) {
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
