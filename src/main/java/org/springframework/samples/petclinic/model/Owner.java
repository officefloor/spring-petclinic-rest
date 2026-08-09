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
    @Pattern(regexp = "^\\+[0-9]{8,15}$", message = "Telephone must be in E.164 format")
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

    @Column(name = "membership_number")
    private String membershipNumber;

    @Column(name = "possible_duplicate")
    private Boolean possibleDuplicate;

    @Column(name = "possible_duplicate_of")
    private Integer possibleDuplicateOf;

    /**
     * The number of owners belonging to this owner's household (owners sharing the same
     * {@code householdId}), including this owner. It is a derived, non-persistent value populated
     * when an owner is mapped for a response.
     */
    @Transient
    private Integer householdMemberCount;

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
     * The known E.164 country calling codes this owner's telephone may carry, used only to split the
     * stored number into its country code and national digits for display. Australia ('+61') and the
     * North American Numbering Plan ('+1') mirror the codes the create endpoint recognises.
     */
    private static final String[] KNOWN_COUNTRY_CODES = {"61", "1"};

    /**
     * The stored E.164 {@code telephone} formatted for human reading: the country calling code (kept
     * with its leading {@code '+'}), a space, then the national digits grouped in threes from the left
     * and separated by spaces (e.g. {@code +61412345678} becomes {@code "+61 412 345 678"}). The
     * country code is taken as the longest {@link #KNOWN_COUNTRY_CODES} match, defaulting to a
     * two-digit code when none is recognised. It is a derived value; the raw {@code telephone} stays in
     * E.164 form.
     *
     * @return the human-formatted telephone, or the raw {@code telephone} when it is {@code null} or
     *         not in E.164 form
     */
    public String getTelephoneDisplay() {
        if (this.telephone == null || !this.telephone.matches("\\+[0-9]+")) {
            return this.telephone;
        }
        String digits = this.telephone.substring(1);
        String countryCode = null;
        for (String code : KNOWN_COUNTRY_CODES) {
            if (digits.startsWith(code) && digits.length() > code.length()
                && (countryCode == null || code.length() > countryCode.length())) {
                countryCode = code;
            }
        }
        if (countryCode == null) {
            countryCode = digits.substring(0, Math.min(2, digits.length()));
        }
        String national = digits.substring(countryCode.length());
        StringBuilder sb = new StringBuilder("+").append(countryCode);
        for (int i = 0; i < national.length(); i += 3) {
            sb.append(' ').append(national, i, Math.min(i + 3, national.length()));
        }
        return sb.toString();
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

    public String getMembershipNumber() {
        return this.membershipNumber;
    }

    public void setMembershipNumber(String membershipNumber) {
        this.membershipNumber = membershipNumber;
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

    public Integer getHouseholdMemberCount() {
        return this.householdMemberCount;
    }

    public void setHouseholdMemberCount(Integer householdMemberCount) {
        this.householdMemberCount = householdMemberCount;
    }

    /**
     * The owner's membership points, a non-negative score. It starts at 0 and accumulates: 2 points
     * when an email address is present, 1 point when the owner has no namesakes ({@code namesakeCount}
     * is zero), 2 points for a household of 3 or more members ({@code householdMemberCount} is at least
     * 3), and 3 points for tenure &mdash; the number of whole days from {@code registrationDate} to
     * today &mdash; of more than 365 days.
     *
     * @return the derived membership points (0 or more)
     */
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
            && java.time.temporal.ChronoUnit.DAYS.between(this.registrationDate, LocalDate.now()) > 365) {
            points += 3;
        }
        return points;
    }

    /**
     * The owner's membership level, a number from 1 to 4, derived from {@link #getMembershipPoints()}:
     * level 1 for 0-1 points, level 2 for 2-3 points, level 3 for 4-5 points, and level 4 for 6 or more
     * points.
     *
     * @return the derived membership level (1 to 4)
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
     * The Luhn check digit (0-9) computed over the digits contained in this owner's
     * {@code customerCode}. Non-digit characters are ignored; the rightmost digit is doubled.
     *
     * @return the single Luhn check digit, or {@code null} when there is no customer code
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
     * The owner's age band, derived from {@code birthDate} computed against {@code registrationDate}:
     * {@code MINOR} when the owner is under 18, {@code ADULT} from 18 to 64, and {@code SENIOR} at 65
     * or older. It is a derived, non-persistent value; when no {@code birthDate} is present it is
     * {@code null}.
     *
     * @return {@code "MINOR"}, {@code "ADULT"} or {@code "SENIOR"}, or {@code null} when no birth date
     *         is present
     */
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

    /**
     * The owner's preferred contact channel, derived from the stored fields: {@code EMAIL} when an
     * email address is present, otherwise {@code PHONE}.
     *
     * @return {@code "EMAIL"} when an email address is present, otherwise {@code "PHONE"}
     */
    public String getContactPreference() {
        return (this.email != null && !this.email.isBlank()) ? "EMAIL" : "PHONE";
    }

    /**
     * The owner's identity key: the single derived value used to detect duplicate owners. It is the
     * owner's normalized telephone, its email (empty when none) and its {@code householdId} (empty
     * when none) joined by {@code '|'}. Because the telephone is part of the key, two members of the
     * same household with different telephones have different identity keys; only owners whose whole
     * key is identical are duplicates.
     *
     * @return the {@code normalizedTelephone|email|householdId} identity key
     */
    public String getIdentityKey() {
        String telephonePart = this.telephone == null ? "" : this.telephone;
        String emailPart = this.email == null ? "" : this.email;
        String householdPart = this.householdId == null ? "" : this.householdId;
        return telephonePart + '|' + emailPart + '|' + householdPart;
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
