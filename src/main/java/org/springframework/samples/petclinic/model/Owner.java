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

    @Column(name = "customer_code")
    private String customerCode;

    @Column(name = "household_id")
    private String householdId;

    @Column(name = "namesake_count")
    private Integer namesakeCount;

    @Column(name = "household_member_count")
    private Integer householdMemberCount;

    @Column(name = "membership_number")
    private String membershipNumber;

    @Column(name = "bulk_signup_warning")
    private Boolean bulkSignupWarning;

    @Column(name = "postcode")
    private String postcode;

    @Column(name = "possible_duplicate")
    private Boolean possibleDuplicate;

    @Column(name = "possible_duplicate_of")
    private Integer possibleDuplicateOf;

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

    public Integer getHouseholdMemberCount() {
        return this.householdMemberCount;
    }

    public void setHouseholdMemberCount(Integer householdMemberCount) {
        this.householdMemberCount = householdMemberCount;
    }

    public String getMembershipNumber() {
        return this.membershipNumber;
    }

    public void setMembershipNumber(String membershipNumber) {
        this.membershipNumber = membershipNumber;
    }

    public Boolean getBulkSignupWarning() {
        return this.bulkSignupWarning;
    }

    public void setBulkSignupWarning(Boolean bulkSignupWarning) {
        this.bulkSignupWarning = bulkSignupWarning;
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
     * @return the membership level, from 1 to 4
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
     * The owner's check digit, derived on read: a single Luhn check digit computed over the
     * digits contained in the owner's {@code customerCode}.
     *
     * @return the Luhn check digit (0-9), or {@code null} when no customer code is assigned
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
     * detect duplicate owners on create. It is formed as
     * {@code normalizedTelephone + '|' + (email or empty) + '|' + householdId} from the
     * owner's already-normalized {@code telephone} (E.164 form), its lower-cased {@code email}
     * (empty when absent) and its assigned {@code householdId}. Two owners are duplicates only
     * when their whole {@code identityKey} values are equal.
     *
     * @return the consolidated identity key
     */
    @Transient
    public String getIdentityKey() {
        String telephonePart = this.telephone == null ? "" : this.telephone;
        String emailPart = this.email == null ? "" : this.email;
        String householdPart = this.householdId == null ? "" : this.householdId;
        return telephonePart + "|" + emailPart + "|" + householdPart;
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
