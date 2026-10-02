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
import java.time.Month;
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

    @Column(name = "postcode")
    private String postcode;

    @Column(name = "telephone")
    @NotEmpty
    @Pattern(regexp = "^\\+[0-9]{8,15}$", message = "Phone number must be in E.164 form (+ then 8 to 15 digits)")
    private String telephone;

    @Column(name = "email")
    private String email;

    @Column(name = "title")
    private String title;

    @Column(name = "registration_date")
    private LocalDate registrationDate;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "customer_code")
    private String customerCode;

    @Column(name = "membership_number")
    private String membershipNumber;

    @Column(name = "household_id")
    private String householdId;

    @Column(name = "namesake_count")
    private Integer namesakeCount;

    @Column(name = "deleted")
    private boolean deleted;

    @Transient
    private Integer householdSize;

    @Transient
    private Boolean bulkSignupWarning;

    @Transient
    private Boolean possibleDuplicate;

    @Transient
    private Integer possibleDuplicateOf;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner", fetch = FetchType.EAGER)
    private Set<Pet> pets;

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * The first line of the structured postal address (street number and name), or {@code null} when
     * the owner was supplied with only the flat {@link #getAddress() address}. The preferred street
     * address when present; {@link #getAddress() address} holds the composed form.
     */
    public String getAddressLine1() {
        return this.addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    /**
     * The optional second line of the structured postal address (unit, suite, etc.), or {@code null}
     * when none was supplied. When present it is appended to the composed {@link #getAddress() address}.
     */
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

    /**
     * The stored {@link #getTelephone() telephone} formatted for humans by {@link TelephoneFormatter}:
     * the country code, a space, then the national digits grouped in threes (e.g. {@code +61412345678}
     * -> {@code +61 412 345 678}). The raw {@link #getTelephone() telephone} stays in E.164 form.
     */
    public String getTelephoneDisplay() {
        return TelephoneFormatter.toDisplay(this.telephone);
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

    /**
     * The owner's salutation: the {@link #getTitle() title} followed by a single space and the
     * {@link #getLastName() last name} (e.g. {@code "DR Franklin"}), or just the last name when no
     * title was supplied. Derived per response, not persisted.
     */
    public String getSalutation() {
        if (this.title == null || this.title.isBlank()) {
            return this.getLastName();
        }
        return this.title + " " + this.getLastName();
    }

    public LocalDate getRegistrationDate() {
        return this.registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }

    /**
     * The fiscal year a {@code date} belongs to, as a four-digit year. The fiscal year starts on 1 July
     * and is identified by the calendar year in which it ends: a date on or after 1 July of year
     * {@code Y} belongs to the fiscal year ending in {@code Y + 1}, while a date in January to June of
     * year {@code Y} belongs to the fiscal year ending in {@code Y}.
     */
    public static int fiscalYear(LocalDate date) {
        return date.getMonthValue() >= Month.JULY.getValue() ? date.getYear() + 1 : date.getYear();
    }

    /**
     * The owner's fiscal year, formatted {@code "FY<YY>"} where YY is the last two digits of the
     * {@link #fiscalYear(LocalDate) fiscal year} of the business-day-adjusted
     * {@link #getRegistrationDate() registrationDate} (e.g. a registration on 2026-10-02 falls in the
     * fiscal year ending June 2027, so {@code "FY27"}). {@code null} when no registration date is
     * present. Derived per response.
     */
    public String getFiscalYear() {
        if (this.registrationDate == null) {
            return null;
        }
        return String.format("FY%02d", fiscalYear(this.registrationDate) % 100);
    }

    public LocalDate getBirthDate() {
        return this.birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    /**
     * The owner's age band, derived from {@link #getBirthDate() birthDate} measured against the
     * {@link #getRegistrationDate() registrationDate}: {@code "MINOR"} when under 18, {@code "ADULT"}
     * when 18 to 64, or {@code "SENIOR"} when 65 or older. {@code null} when no birth date is present
     * (so the field is absent from the response) or the registration date is unknown.
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

    public String getMembershipNumber() {
        return this.membershipNumber;
    }

    public void setMembershipNumber(String membershipNumber) {
        this.membershipNumber = membershipNumber;
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
     * Whether this owner has been soft-deleted. A {@code DELETE /api/owners/{id}} sets this true and
     * retains the record rather than removing it; a newly created owner is false. Soft-deleted owners
     * are still returned by {@code GET /api/owners/{id}} but are ignored by the create endpoint's
     * duplicate/identity checks (see {@code CheckHouseholdUnique}).
     */
    public boolean isDeleted() {
        return this.deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    /**
     * Sets the number of owners that belong to this owner's household (same last name and postcode; see
     * {@link org.springframework.samples.petclinic.rest.function.owner.Household}), including this owner.
     * Computed per request and used only by {@link #getMembershipPoints()} — a household of three or more
     * contributes points. Not persisted and not serialized into the response (no getter is exposed); the
     * count is recomputed on each read because household membership changes as owners come and go.
     */
    public void setHouseholdSize(Integer householdSize) {
        this.householdSize = householdSize;
    }

    /**
     * Whether this response should carry a bulk-signup warning: {@code true} when more than 80 owners
     * have already been created for this owner's registration day. Computed per request, not persisted.
     */
    public Boolean getBulkSignupWarning() {
        return this.bulkSignupWarning;
    }

    public void setBulkSignupWarning(Boolean bulkSignupWarning) {
        this.bulkSignupWarning = bulkSignupWarning;
    }

    /**
     * Whether this owner is a <em>possible</em> (soft) duplicate of another owner: {@code true} when some
     * other owner shares this owner's last name (case-insensitively) and postcode but has a different
     * telephone — a near match. Always false for an owner admitted as a declared household member (via
     * {@code sharesHousehold}), which is not a suspected duplicate.
     * Computed per request, not persisted. See {@link #getPossibleDuplicateOf()} for the matched owner.
     */
    public Boolean getPossibleDuplicate() {
        return this.possibleDuplicate;
    }

    public void setPossibleDuplicate(Boolean possibleDuplicate) {
        this.possibleDuplicate = possibleDuplicate;
    }

    /**
     * The id of the existing owner this owner possibly duplicates (same last name and postcode, different
     * telephone), or {@code null} when {@link #getPossibleDuplicate() possibleDuplicate} is false. When
     * several owners match, this is the earliest (lowest id). Computed per request, not persisted.
     */
    public Integer getPossibleDuplicateOf() {
        return this.possibleDuplicateOf;
    }

    public void setPossibleDuplicateOf(Integer possibleDuplicateOf) {
        this.possibleDuplicateOf = possibleDuplicateOf;
    }

    /**
     * The owner's membership points: start at 0; add 2 when an email is present; add 1 when
     * {@code namesakeCount} is 0; add 2 for a household of 3 or more members (see
     * {@link #setHouseholdSize(Integer)}); add 3 for a tenure of at least one elapsed fiscal year, i.e.
     * the {@code registrationDate} falls in an earlier {@link #fiscalYear(LocalDate) fiscal year} than
     * today (fiscal years start 1 July). A newly created owner has zero tenure, so a fresh owner scores
     * at most 5 points (2 + 1 + 2).
     */
    public int getMembershipPoints() {
        int points = 0;
        boolean hasEmail = this.email != null && !this.email.isBlank();
        if (hasEmail) {
            points += 2;
        }
        boolean unique = this.namesakeCount != null && this.namesakeCount == 0;
        if (unique) {
            points += 1;
        }
        boolean largeHousehold = this.householdSize != null && this.householdSize >= 3;
        if (largeHousehold) {
            points += 2;
        }
        boolean tenured = this.registrationDate != null
            && fiscalYear(LocalDate.now()) - fiscalYear(this.registrationDate) >= 1;
        if (tenured) {
            points += 3;
        }
        return points;
    }

    /**
     * The owner's membership level, derived from {@link #getMembershipPoints() membershipPoints}:
     * 1 for 0-1 points, 2 for 2-3, 3 for 4-5, and 4 for 6 or more.
     */
    public int getMembershipLevel() {
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

    /**
     * Fixed city-to-region table mapping each known city to its canonical region string.
     */
    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW",
        "Melbourne", "VIC",
        "Brisbane", "QLD");

    /**
     * Region -> inclusive {low, high} four-digit postcode range: the single canonical home of the
     * postcode bands (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099), read via
     * {@link #postcodeRangeOf(String)} by both locality derivation and the create-time range check.
     */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /**
     * Fixed region-to-timezone table mapping each known region to its IANA timezone name
     * (NSW -> Australia/Sydney, VIC -> Australia/Melbourne, QLD -> Australia/Brisbane). The single
     * source of truth for the timezone derivation read by {@link #getTimezone()}.
     */
    private static final Map<String, String> REGION_TIMEZONE = Map.of(
        "NSW", "Australia/Sydney",
        "VIC", "Australia/Melbourne",
        "QLD", "Australia/Brisbane");

    /**
     * The canonical region for {@code city} derived from the fixed city-to-region table
     * ({@code Sydney->NSW}, {@code Melbourne->VIC}, {@code Brisbane->QLD}), or {@code "UNKNOWN"} when the
     * city is not in the table. The single source of truth for the create-time postcode range check in
     * {@link org.springframework.samples.petclinic.rest.function.owner.ValidateOwnerPostcode}.
     */
    public static String regionOf(String city) {
        return CITY_REGION.getOrDefault(city, "UNKNOWN");
    }

    /**
     * The inclusive {low, high} four-digit postcode band for {@code region} (NSW 2000-2099,
     * VIC 3000-3099, QLD 4000-4099), or {@code null} when the region has no band. This is the single
     * source of truth for the bands, shared by {@link #regionOfPostcode(String) the postcode-to-region
     * lookup} (which feeds the customerCode's region, and hence the locality) and the create-time
     * postcode range check.
     */
    public static int[] postcodeRangeOf(String region) {
        return REGION_POSTCODES.get(region);
    }

    /**
     * The canonical region whose postcode band contains {@code postcode} (NSW 2000-2099, VIC 3000-3099,
     * QLD 4000-4099), or {@code null} when {@code postcode} is absent, not four digits, or in no known
     * range.
     */
    public static String regionOfPostcode(String postcode) {
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
     * The owner's locality: the REGION segment of the {@link #getCustomerCode() customerCode}, which is
     * the region code derived from the postcode at creation (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099,
     * else {@code "UNKNOWN"}). The customer code is the single region-and-hash owner identity, so the
     * locality reads straight off it rather than re-deriving the region. {@code "UNKNOWN"} when no
     * customer code is assigned.
     */
    public String getLocality() {
        if (this.customerCode == null) {
            return "UNKNOWN";
        }
        int dash = this.customerCode.indexOf('-');
        return dash > 0 ? this.customerCode.substring(0, dash) : "UNKNOWN";
    }

    /**
     * The owner's IANA timezone, derived from the {@link #getLocality() locality}/region via the fixed
     * region-to-timezone table (NSW -> Australia/Sydney, VIC -> Australia/Melbourne,
     * QLD -> Australia/Brisbane), or {@code null} (so the field is absent from the response) when the
     * locality maps to no known region.
     */
    public String getTimezone() {
        return REGION_TIMEZONE.get(getLocality());
    }

    /**
     * The single Luhn check digit (0-9) computed over the digits contained in the
     * {@link #getCustomerCode() customerCode}: non-digit characters are ignored, each second digit from
     * the right is doubled (subtracting 9 when the double exceeds 9), the results are summed, and the
     * check digit is {@code (10 - sum % 10) % 10}. {@code 0} when no customer code is assigned.
     */
    public int getCheckDigit() {
        String code = this.customerCode;
        if (code == null) {
            return 0;
        }
        int sum = 0;
        boolean dbl = true;
        for (int i = code.length() - 1; i >= 0; i--) {
            char c = code.charAt(i);
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
     * The owner's preferred contact channel: {@code "EMAIL"} when an email is present, otherwise
     * {@code "PHONE"}.
     */
    public String getContactPreference() {
        boolean hasEmail = this.email != null && !this.email.isBlank();
        return hasEmail ? "EMAIL" : "PHONE";
    }

    /**
     * The single canonical format of an owner's derived identity key:
     * {@code telephone + "|" + (email or empty) + "|" + (householdId or empty)}. The telephone and
     * household id are used as given (they are expected to already be canonical: E.164 and an assigned
     * household id); the email is lower-cased here and a null/blank email, telephone or household id
     * contributes an empty component. This is the only place the {@code "|"}-join lives; the stored-owner
     * {@link #getIdentityKey()} routes through here so the format cannot drift. The key is a descriptive,
     * returned field; create-time duplicate detection itself is keyed on the household alone (see
     * {@code CheckHouseholdUnique}).
     */
    public static String identityKey(String telephone, String email, String householdId) {
        String tel = (telephone == null) ? "" : telephone;
        String mail = (email == null || email.isBlank()) ? "" : email.toLowerCase(Locale.ROOT);
        String household = (householdId == null || householdId.isBlank()) ? "" : householdId;
        return tel + "|" + mail + "|" + household;
    }

    /**
     * The owner's derived identity key, built from its stored canonical telephone (E.164), email and
     * household id via {@link #identityKey(String, String, String)}. A descriptive, returned value;
     * create-time duplicate detection is keyed on the household (see {@code CheckHouseholdUnique}).
     */
    public String getIdentityKey() {
        return identityKey(this.telephone, this.email, this.householdId);
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
