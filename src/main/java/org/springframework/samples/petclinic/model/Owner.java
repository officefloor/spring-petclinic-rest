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
    @Pattern(regexp = "^\\+[0-9]{8,15}$", message = "Phone number must be in E.164 format")
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

    @Column(name = "household_size")
    private Integer householdSize;

    @Column(name = "membership_level_cap")
    private Integer membershipLevelCap;

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
     * The owner's salutation: the {@link #getTitle() title} followed by a space and the last name
     * (e.g. {@code "DR Franklin"}) when a title is present, or just the last name when no title has
     * been supplied.
     */
    public String getSalutation() {
        if (this.title == null || this.title.isEmpty()) {
            return this.getLastName();
        }
        return this.title + " " + this.getLastName();
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

    /**
     * The owner's stored E.164 {@link #getTelephone() telephone} formatted for humans: the country
     * code, a space, then the national digits grouped in threes (e.g. {@code "+61 412 345 678"}).
     * The raw {@link #getTelephone() telephone} stays in E.164 form. Absent until a telephone has
     * been set.
     */
    public String getTelephoneDisplay() {
        return Telephone.forDisplay(this.telephone);
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

    public Integer getHouseholdSize() {
        return this.householdSize;
    }

    public void setHouseholdSize(Integer householdSize) {
        this.householdSize = householdSize;
    }

    /**
     * The ceiling applied to this owner's {@link #getMembershipLevel() membership level}: one above
     * the highest membership level held by the owner's household members when this owner was created.
     * {@code null} when the owner had no existing household member and so is uncapped.
     */
    public Integer getMembershipLevelCap() {
        return this.membershipLevelCap;
    }

    public void setMembershipLevelCap(Integer membershipLevelCap) {
        this.membershipLevelCap = membershipLevelCap;
    }

    /**
     * Whether this owner, though not a hard duplicate, shares an existing owner's last name and
     * postcode while holding a different telephone. Resolved at creation and stored so it is
     * reported consistently on every later read.
     */
    public Boolean getPossibleDuplicate() {
        return this.possibleDuplicate;
    }

    public void setPossibleDuplicate(Boolean possibleDuplicate) {
        this.possibleDuplicate = possibleDuplicate;
    }

    /**
     * The id of the existing owner this owner is a possible duplicate of, or {@code null} when it
     * is not a possible duplicate.
     */
    public Integer getPossibleDuplicateOf() {
        return this.possibleDuplicateOf;
    }

    public void setPossibleDuplicateOf(Integer possibleDuplicateOf) {
        this.possibleDuplicateOf = possibleDuplicateOf;
    }

    /**
     * Whether this owner has been soft-deleted. A soft-deleted owner keeps its row and remains
     * retrievable, but is ignored by the create endpoint's duplicate and identity checks. A newly
     * created owner is never deleted.
     */
    public boolean isDeleted() {
        return this.deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    /**
     * The owner's identity key: the single derived value used to detect duplicate owners, formed
     * as {@code normalizedTelephone + '|' + email + '|' + householdId} where an absent telephone,
     * email or household contributes an empty string. Two owners are duplicates only when their
     * whole identity keys are equal, so housemates that share a {@code householdId} but hold
     * different telephones have distinct identity keys and are both allowed.
     */
    public String getIdentityKey() {
        return orEmpty(this.telephone) + "|" + orEmpty(this.email) + "|" + orEmpty(this.householdId);
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }

    /**
     * The owner's membership number, formatted {@code '<customerCode>-M<YY>'} where YY is the
     * last two digits of the {@link #getFiscalYear() fiscal year} of the business-day-adjusted
     * registration date (e.g. {@code 'SMI-0007-M27'}). Derived from the owner's own fields;
     * absent until both the customer code and registration date have been assigned.
     */
    public String getMembershipNumber() {
        if (this.customerCode == null || this.registrationDate == null) {
            return null;
        }
        return String.format("%s-M%02d", this.customerCode, FiscalYear.endingYear(this.registrationDate) % 100);
    }

    /**
     * The owner's fiscal year, formatted {@code 'FY<YY>'} and derived from the business-day-adjusted
     * {@link #getRegistrationDate() registration date}. The fiscal year starts on 1 July; see
     * {@link FiscalYear}. Absent until a registration date has been assigned.
     */
    public String getFiscalYear() {
        if (this.registrationDate == null) {
            return null;
        }
        return FiscalYear.label(this.registrationDate);
    }

    /**
     * The owner's check digit: a single Luhn check digit (0-9) computed over the digits of the
     * {@link #getCustomerCode() customer code}. Absent until the customer code has been assigned.
     */
    public Integer getCheckDigit() {
        if (this.customerCode == null) {
            return null;
        }
        return Luhn.checkDigit(this.customerCode);
    }

    /**
     * The owner's tenure in whole elapsed fiscal years: the number of 1 July boundaries crossed
     * from the {@link #getRegistrationDate() registration date} up to today (see {@link FiscalYear}).
     * An owner still in their registration fiscal year has a tenure of zero. Absent until a
     * registration date is present.
     */
    public Long getTenureFiscalYears() {
        if (this.registrationDate == null) {
            return null;
        }
        return FiscalYear.elapsed(this.registrationDate, LocalDate.now());
    }

    /**
     * The owner's membership points, a score derived from the owner's own fields: starting from 0,
     * the owner earns 2 points when an email address is present, 1 point when this owner has no
     * namesakes ({@code namesakeCount} is 0), 2 points for a household of 3 or more members, and 3
     * points once the owner's {@link #getTenureFiscalYears() tenure} spans at least one full fiscal
     * year (the registration and current dates fall in different fiscal years).
     */
    public Integer getMembershipPoints() {
        int points = 0;
        boolean hasEmail = this.email != null && !this.email.isEmpty();
        if (hasEmail) {
            points += 2;
        }
        boolean unique = this.namesakeCount != null && this.namesakeCount == 0;
        if (unique) {
            points += 1;
        }
        boolean sharedHousehold = this.householdSize != null && this.householdSize >= 3;
        if (sharedHousehold) {
            points += 2;
        }
        Long tenureFiscalYears = getTenureFiscalYears();
        boolean tenured = tenureFiscalYears != null && tenureFiscalYears >= 1;
        if (tenured) {
            points += 3;
        }
        return points;
    }

    /**
     * The owner's membership level, a number from 1 to 4 derived from the owner's
     * {@link #getMembershipPoints() membership points}: 1 for 0-1 points, 2 for 2-3, 3 for 4-5, and
     * 4 for 6 or more. The level is then held to its {@link #getMembershipLevelCap() cap} (one above
     * the highest level among the owner's household members at creation), so a new owner cannot rank
     * more than one level above their household. An uncapped owner keeps the derived level.
     */
    public Integer getMembershipLevel() {
        int level = derivedMembershipLevel();
        if (this.membershipLevelCap != null && level > this.membershipLevelCap) {
            return this.membershipLevelCap;
        }
        return level;
    }

    private int derivedMembershipLevel() {
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
     * The owner's locality: the REGION segment of the {@link #getCustomerCode() customer code} (the
     * region derived from the postcode when the identity was assigned), or {@link Regions#UNKNOWN}
     * before a customer code has been assigned.
     */
    public String getLocality() {
        if (this.customerCode == null) {
            return Regions.UNKNOWN;
        }
        int separator = this.customerCode.indexOf('-');
        return separator < 0 ? this.customerCode : this.customerCode.substring(0, separator);
    }

    /**
     * The owner's timezone: the IANA timezone name for the owner's {@link #getLocality() locality}
     * from the fixed region-to-timezone table (NSW&rarr;Australia/Sydney, VIC&rarr;Australia/Melbourne,
     * QLD&rarr;Australia/Brisbane), or absent when the locality has no known timezone.
     */
    public String getTimezone() {
        return Regions.timezoneOf(getLocality());
    }

    /**
     * The owner's preferred contact channel, derived from the owner's own fields:
     * {@code "EMAIL"} when an {@link #getEmail() email} is present, otherwise {@code "PHONE"}.
     */
    public String getContactPreference() {
        boolean hasEmail = this.email != null && !this.email.isEmpty();
        return hasEmail ? "EMAIL" : "PHONE";
    }

    /**
     * The owner's age band, derived from the owner's own fields: the whole years between
     * {@link #getBirthDate() birth date} and {@link #getRegistrationDate() registration date}
     * classified as {@code "MINOR"} (under 18), {@code "ADULT"} (18-64) or {@code "SENIOR"}
     * (65 or older). Absent until a birth date and a registration date are both present.
     */
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

    /**
     * Resolve the effective registration date before persisting: default it to the server's
     * current date when none was supplied, then roll it forward off weekends so a newly
     * registered owner always has a business-day registration date. Every value derived from it
     * (such as the {@link #getMembershipNumber() membership number}) therefore uses the adjusted
     * date.
     */
    @PrePersist
    private void resolveRegistrationDate() {
        if (this.registrationDate == null) {
            this.registrationDate = LocalDate.now();
        }
        this.registrationDate = BusinessDay.onOrAfter(this.registrationDate);
    }

    /**
     * The owner's self link: the canonical API path of this owner, formatted
     * {@code "/api/owners/<id>"}. Absent until the owner has been assigned an id.
     */
    public String getSelfLink() {
        if (this.getId() == null) {
            return null;
        }
        return "/api/owners/" + this.getId();
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
