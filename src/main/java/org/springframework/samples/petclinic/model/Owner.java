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
import org.springframework.samples.petclinic.util.TelephoneFormatter;

import jakarta.persistence.*;
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

    @Column(name = "city")
    @NotEmpty
    private String city;

    @Column(name = "telephone")
    @NotEmpty
    @Pattern(regexp = "^\\+[0-9]{8,15}$", message = "Phone number must be in E.164 form: a '+' followed by 8 to 15 digits")
    private String telephone;

    @Column(name = "email")
    private String email;

    @Column(name = "postcode")
    private String postcode;

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
     * The stored E.164 {@code telephone} formatted for humans (e.g. {@code '+61 412 345 678'} for the
     * stored {@code '+61412345678'}); the raw {@code telephone} stays in E.164 form. Derived from the
     * owner's own {@code telephone}; see {@link TelephoneFormatter#toDisplay(String)}.
     */
    @Transient
    public String getTelephoneDisplay() {
        return TelephoneFormatter.toDisplay(this.telephone);
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
     * Whether this owner was flagged a possible (soft) duplicate at creation time: it was not a
     * hard duplicate, but shared an existing owner's last name and postcode while carrying a
     * different telephone. {@code false} (or {@code null} for owners created before the check) when
     * no such match was found.
     */
    public Boolean getPossibleDuplicate() {
        return this.possibleDuplicate;
    }

    public void setPossibleDuplicate(Boolean possibleDuplicate) {
        this.possibleDuplicate = possibleDuplicate;
    }

    /**
     * The id of the existing owner this one was flagged a possible duplicate of, or {@code null}
     * when {@code possibleDuplicate} is not set.
     */
    public Integer getPossibleDuplicateOf() {
        return this.possibleDuplicateOf;
    }

    public void setPossibleDuplicateOf(Integer possibleDuplicateOf) {
        this.possibleDuplicateOf = possibleDuplicateOf;
    }

    /**
     * The owner's name formatted as {@code 'LastName, FirstName'} from the stored names.
     */
    @Transient
    public String getDisplayName() {
        return this.getLastName() + ", " + this.getFirstName();
    }

    /**
     * The owner's initials: the upper-cased first letters of the first and last name,
     * dot-separated with a trailing dot (e.g. {@code 'J.S.'}).
     */
    @Transient
    public String getInitials() {
        return Character.toUpperCase(this.getFirstName().charAt(0)) + "."
            + Character.toUpperCase(this.getLastName().charAt(0)) + ".";
    }

    /**
     * The owner's membership number, formatted {@code '<customerCode>-M<YY>'} where {@code YY} is
     * the last two digits of the registration date year (e.g. {@code 'NSW-1A2B3C4D-M26'}). Derived
     * from the owner's own fields; {@code null} until both the customer code and registration date
     * are set.
     */
    @Transient
    public String getMembershipNumber() {
        if (this.customerCode == null || this.registrationDate == null) {
            return null;
        }
        return String.format("%s-M%02d", this.customerCode, this.registrationDate.getYear() % 100);
    }

    /**
     * A single Luhn check digit (0-9) computed over the digits contained in the owner's
     * {@code customerCode}. Derived from the owner's own fields; {@code null} until the customer
     * code is set. The standard Luhn algorithm is applied to the digits of the customer code, from
     * right to left, doubling every second digit (subtracting 9 when the doubled value exceeds 9).
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

    /**
     * The owner's membership points, a score accumulated from the owner's own fields. It starts at
     * 0 and gains: 2 when the owner has an email address; 1 when the owner is unique
     * ({@code namesakeCount} is 0); 2 when the owner belongs to a household of 3 or more members
     * (the owner together with their pets); and 3 when the owner's tenure exceeds 365 days, measured
     * from {@code registrationDate} to today. Because a newly created owner has zero tenure, a new
     * owner never earns the tenure points. The score feeds {@link #getMembershipLevel()}.
     */
    @Transient
    public Integer getMembershipPoints() {
        int points = 0;
        boolean hasEmail = this.email != null && !this.email.isBlank();
        if (hasEmail) {
            points += 2;
        }
        boolean unique = this.namesakeCount != null && this.namesakeCount == 0;
        if (unique) {
            points += 1;
        }
        int householdSize = 1 + getPetsInternal().size();
        if (householdSize >= 3) {
            points += 2;
        }
        boolean tenured = this.registrationDate != null
            && ChronoUnit.DAYS.between(this.registrationDate, LocalDate.now()) > 365;
        if (tenured) {
            points += 3;
        }
        return points;
    }

    /**
     * The owner's numeric membership level, from 1 to 4, derived from {@link #getMembershipPoints()}:
     * level 1 for 0-1 points, 2 for 2-3 points, 3 for 4-5 points, and 4 for 6 or more points.
     */
    @Transient
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
     * The owner's locality: the canonical region carried by the {@code customerCode}, which is the
     * {@code <REGION>} segment preceding its first {@code '-'} (e.g. {@code 'NSW'} for customer code
     * {@code 'NSW-1A2B3C4D'}). The region is the one derived from the postcode at creation time and
     * baked into the identity. {@code 'UNKNOWN'} when the owner has no customer code yet.
     */
    @Transient
    public String getLocality() {
        if (this.customerCode == null) {
            return "UNKNOWN";
        }
        int dash = this.customerCode.indexOf('-');
        return dash < 0 ? "UNKNOWN" : this.customerCode.substring(0, dash);
    }

    /**
     * Fixed region-to-timezone lookup mapping each canonical region to its IANA timezone name:
     * {@code NSW -> 'Australia/Sydney'}, {@code VIC -> 'Australia/Melbourne'},
     * {@code QLD -> 'Australia/Brisbane'}.
     */
    private static final Map<String, String> REGION_TIMEZONE = Map.of(
        "NSW", "Australia/Sydney",
        "VIC", "Australia/Melbourne",
        "QLD", "Australia/Brisbane");

    /**
     * The owner's timezone: the IANA name derived from the owner's {@link #getLocality() locality}
     * via the fixed region-to-timezone table ({@code NSW -> 'Australia/Sydney'},
     * {@code VIC -> 'Australia/Melbourne'}, {@code QLD -> 'Australia/Brisbane'}). {@code null} when
     * the locality is not one of these regions.
     */
    @Transient
    public String getTimezone() {
        return REGION_TIMEZONE.get(getLocality());
    }

    /**
     * The owner's identity key: a single derived value that consolidates all duplicate detection,
     * formatted {@code '<normalizedTelephone>|<email or empty>|<householdId or empty>'}. The
     * telephone is the stored E.164 value, the email its stored lower-cased form (empty when the
     * owner has none), and the household id the stored shared identifier (empty when the owner is
     * not part of a shared household). Two owners are duplicates only when their whole identity keys
     * are equal; because the telephone is part of the key, two members of the same household with
     * different telephones have different identity keys.
     */
    @Transient
    public String getIdentityKey() {
        String telephonePart = this.telephone == null ? "" : this.telephone;
        String emailPart = this.email == null ? "" : this.email;
        String householdPart = this.householdId == null ? "" : this.householdId;
        return telephonePart + "|" + emailPart + "|" + householdPart;
    }

    /**
     * The owner's preferred contact channel, derived from the owner's own fields:
     * {@code 'EMAIL'} when an email address is present, otherwise {@code 'PHONE'}.
     */
    @Transient
    public String getContactPreference() {
        boolean hasEmail = this.email != null && !this.email.isBlank();
        return hasEmail ? "EMAIL" : "PHONE";
    }

    /**
     * The owner's age band, derived from the owner's own fields: the whole number of years from
     * {@code birthDate} to {@code registrationDate} placed into a band — {@code 'MINOR'} when under
     * 18, {@code 'ADULT'} when 18 to 64, and {@code 'SENIOR'} when 65 or older. The age is measured
     * as of the registration date rather than today, so the band is stable over time. {@code null}
     * when the owner has no birth date (or no registration date to measure against).
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
        if (age < 65) {
            return "ADULT";
        }
        return "SENIOR";
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
