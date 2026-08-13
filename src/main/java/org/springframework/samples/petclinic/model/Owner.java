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

    @Column(name = "household_member_count")
    private Integer householdMemberCount;

    @Column(name = "possible_duplicate")
    private boolean possibleDuplicate;

    @Column(name = "possible_duplicate_of")
    private Integer possibleDuplicateOf;

    @Column(name = "deleted")
    private boolean deleted;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner", fetch = FetchType.EAGER)
    private Set<Pet> pets;

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * The owner's salutation: the supplied {@code title} (MR/MRS/MS/DR) followed by a
     * single space and the {@code lastName}, or just the {@code lastName} when no title
     * was given.
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
     * The owner's age band, derived from {@code birthDate} measured against
     * {@code registrationDate}: {@code MINOR} when under 18, {@code ADULT} from 18 to
     * 64, and {@code SENIOR} at 65 or over. Returns {@code null} when no birth date was
     * supplied, so the field is absent from the response.
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

    public boolean isPossibleDuplicate() {
        return this.possibleDuplicate;
    }

    public void setPossibleDuplicate(boolean possibleDuplicate) {
        this.possibleDuplicate = possibleDuplicate;
    }

    public Integer getPossibleDuplicateOf() {
        return this.possibleDuplicateOf;
    }

    public void setPossibleDuplicateOf(Integer possibleDuplicateOf) {
        this.possibleDuplicateOf = possibleDuplicateOf;
    }

    /**
     * Whether this owner has been soft-deleted. A newly created owner is not deleted;
     * {@code DELETE /api/owners/{id}} sets this true while retaining the row, and the
     * create-owner duplicate/identity checks ignore owners flagged deleted.
     */
    public boolean isDeleted() {
        return this.deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    /**
     * The owner's membership points, accumulated from four factors: start at 0, add 2
     * when an email is present, add 1 when {@code namesakeCount} is 0, add 2 for a
     * household of 3 or more members, and add 3 when the owner's tenure spans at least
     * one elapsed fiscal year. The tenure points are the only route to 6 or more:
     * because a newly created owner has zero elapsed fiscal years of tenure, a new owner
     * scores at most 5 points (2 email + 1 namesake + 2 household).
     */
    @Transient
    public int getMembershipPoints() {
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
        if (getTenureFiscalYears() >= 1) {
            points += 3;
        }
        return points;
    }

    /**
     * The owner's numeric membership level, derived from {@link #getMembershipPoints()}:
     * level 1 for 0-1 points, 2 for 2-3, 3 for 4-5, and 4 for 6 or more. Because a newly
     * created owner scores at most 5 points (tenure being the only route to 6), a new
     * owner never exceeds level 3.
     */
    @Transient
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
     * The owner's tenure counted in elapsed fiscal years: the number of fiscal-year
     * boundaries (each 1 July) crossed between {@code registrationDate} and today, i.e.
     * {@code fiscalYearOf(today) - fiscalYearOf(registrationDate)}. Zero when no
     * registration date is set, and zero for an owner registered in the current fiscal
     * year, so the tenure point of {@link #getMembershipLevel()} is never earned at
     * creation.
     */
    @Transient
    public long getTenureFiscalYears() {
        if (this.registrationDate == null) {
            return 0;
        }
        return (long) fiscalYearOf(LocalDate.now()) - fiscalYearOf(this.registrationDate);
    }

    /**
     * The owner's fiscal year, formatted {@code 'FY<YY>'}, derived from the
     * business-day-adjusted {@code registrationDate}. The fiscal year starts on 1 July,
     * so a date in July or later belongs to the fiscal year named by its own calendar
     * year, and a date in January to June belongs to the fiscal year that started the
     * previous 1 July. {@code YY} is the last two digits of that fiscal year. Returns
     * {@code null} when no registration date is set.
     */
    @Transient
    public String getFiscalYear() {
        if (this.registrationDate == null) {
            return null;
        }
        return String.format("FY%02d", fiscalYearOf(this.registrationDate) % 100);
    }

    /**
     * The fiscal year (named by the calendar year in which its 1 July start falls) that
     * {@code date} belongs to. July onward is the current calendar year; January to June
     * belongs to the fiscal year that began the previous 1 July.
     */
    private static int fiscalYearOf(LocalDate date) {
        return date.getMonthValue() >= java.time.Month.JULY.getValue()
                ? date.getYear() : date.getYear() - 1;
    }

    /**
     * The owner's canonical region, preferring the postcode over the city.
     *
     * <p>The 4-digit postcode range is consulted first (NSW 2000-2099, VIC 3000-3099,
     * QLD 4000-4099); only when the postcode is absent or in no known range does this
     * fall back to the fixed city-to-region table (Sydney-&gt;NSW, Melbourne-&gt;VIC,
     * Brisbane-&gt;QLD). Anything unresolved is {@code "UNKNOWN"}. This single derivation
     * backs both the {@code locality} field and the {@code REGION} segment of the
     * {@code customerCode}, so the two always agree.
     */
    @Transient
    public String getRegion() {
        if (this.postcode != null && this.postcode.matches("[0-9]{4}")) {
            int value = Integer.parseInt(this.postcode);
            if (value >= 2000 && value <= 2099) {
                return "NSW";
            }
            if (value >= 3000 && value <= 3099) {
                return "VIC";
            }
            if (value >= 4000 && value <= 4099) {
                return "QLD";
            }
        }
        if ("Sydney".equals(this.city)) {
            return "NSW";
        }
        if ("Melbourne".equals(this.city)) {
            return "VIC";
        }
        if ("Brisbane".equals(this.city)) {
            return "QLD";
        }
        return "UNKNOWN";
    }

    /**
     * The owner's IANA timezone, derived from {@link #getRegion()} via the fixed
     * region-to-timezone table (NSW-&gt;Australia/Sydney, VIC-&gt;Australia/Melbourne,
     * QLD-&gt;Australia/Brisbane). {@code null} when the region is not in the table.
     */
    @Transient
    public String getTimezone() {
        switch (getRegion()) {
            case "NSW":
                return "Australia/Sydney";
            case "VIC":
                return "Australia/Melbourne";
            case "QLD":
                return "Australia/Brisbane";
            default:
                return null;
        }
    }

    /**
     * The owner's preferred contact channel: {@code EMAIL} when an email address is
     * present, otherwise {@code PHONE}.
     */
    @Transient
    public String getContactPreference() {
        return (this.email != null && !this.email.isBlank()) ? "EMAIL" : "PHONE";
    }

    /**
     * The owner's derived duplicate-detection key: the normalized (E.164) telephone,
     * the email (or empty when absent) and the {@code householdId} (or empty when
     * absent), joined by {@code '|'}. Two owners are duplicates only when their whole
     * identity keys are equal.
     */
    @Transient
    public String getIdentityKey() {
        String tel = this.telephone == null ? "" : this.telephone;
        String mail = this.email == null ? "" : this.email;
        String household = this.householdId == null ? "" : this.householdId;
        return tel + "|" + mail + "|" + household;
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
