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
import org.springframework.samples.petclinic.util.E164Telephone;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
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
    @Pattern(regexp = "^\\+[0-9]{8,15}$", message = "Telephone must be in E.164 form (a leading '+' and 8 to 15 digits)")
    private String telephone;

    @Column(name = "email")
    @Email
    private String email;

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

    @Column(name = "postcode")
    private String postcode;

    /**
     * Whether this owner was flagged as a suspected soft-duplicate of an existing owner. Retained
     * as part of the API/persistence contract but always {@code false} under the current
     * household-keyed rule: an owner sharing an existing owner's {@code lastName} and
     * {@code postcode} is now the same household, so it is either rejected as a household duplicate
     * (409) or admitted as a declared {@code sharesHousehold} member — and a declared member is not
     * a suspected duplicate. No production code path sets this field.
     */
    @Column(name = "possible_duplicate")
    private boolean possibleDuplicate;

    /**
     * The id of the existing owner this owner soft-matches, set together with
     * {@link #isPossibleDuplicate()}. Retained for contract compatibility but always {@code null}
     * under the current household-keyed rule (see {@link #isPossibleDuplicate()}); no production
     * code path sets it.
     */
    @Column(name = "possible_duplicate_of")
    private Integer possibleDuplicateOf;

    /**
     * Whether this owner has been soft-deleted. A newly created owner is {@code false}; deleting an
     * owner (DELETE /api/owners/{id}) flags this {@code true} and retains the row instead of removing
     * it. Deleted owners are ignored by the create endpoint's duplicate and identity checks.
     */
    @Column(name = "deleted")
    private boolean deleted;

    /**
     * Write-only, non-persistent acknowledgement that this owner deliberately shares a
     * household with an existing owner. When {@code true}, creation is allowed even if
     * another owner already has the same lastName and address.
     */
    @Transient
    private boolean sharesHousehold;

    /**
     * Read-only, non-persistent count of the owners that share this owner's {@code householdId}
     * (including this owner), populated by the service when an owner is loaded for presentation.
     * Zero when the owner belongs to no household. Used to derive the membership tier.
     */
    @Transient
    private int householdSize;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner", fetch = FetchType.EAGER)
    private Set<Pet> pets;

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * The owner's salutation: the {@link #getTitle() title} and {@code lastName} separated by a
     * single space (e.g. {@code "DR Franklin"}), or just the {@code lastName} when the owner has no
     * title (title null or empty).
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
     * The stored E.164 {@link #getTelephone() telephone} formatted for human display: the
     * country code, a space, then the national digits grouped in threes (e.g.
     * {@code "+61 412 345 678"}). Null when no telephone is set.
     */
    public String getTelephoneDisplay() {
        return E164Telephone.toDisplay(this.telephone);
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

    public LocalDate getBirthDate() {
        return this.birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    /**
     * The owner's age band derived from {@code birthDate} measured against {@code registrationDate}:
     * {@code "MINOR"} when under 18, {@code "ADULT"} from 18 to 64, and {@code "SENIOR"} at 65 or
     * over. Returns null when either the birth date or the registration date is not set.
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

    public String getPostcode() {
        return this.postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
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

    public boolean isDeleted() {
        return this.deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    /** The household size at or above which an owner earns the household membership points. */
    public static final int MEMBERSHIP_HOUSEHOLD_SIZE = 3;

    /** The month (1 July) on which the fiscal year begins. */
    public static final int FISCAL_YEAR_START_MONTH = 7;

    /**
     * The number of elapsed fiscal years at or above which an owner earns the tenure membership
     * points.
     */
    public static final long MEMBERSHIP_TENURE_FISCAL_YEARS = 1;

    /**
     * The fiscal year (as a full four-digit number) that {@code date} falls in, where the fiscal
     * year begins on 1 July and is labelled by the calendar year in which it ends: a date on or
     * after 1 July belongs to the next calendar year's fiscal year.
     */
    public static int fiscalYearOf(LocalDate date) {
        return date.getMonthValue() >= FISCAL_YEAR_START_MONTH ? date.getYear() + 1 : date.getYear();
    }

    /**
     * The fiscal year of the owner's (business-day-adjusted) {@code registrationDate}, formatted
     * {@code 'FY<YY>'} where YY is the last two digits of the fiscal year (see
     * {@link #fiscalYearOf(LocalDate)}). Null when no registration date is set.
     */
    public String getFiscalYear() {
        if (this.registrationDate == null) {
            return null;
        }
        return String.format("FY%02d", fiscalYearOf(this.registrationDate) % 100);
    }

    /**
     * The owner's membership points, computed from stored fields: they start at 0, gain 2 when an
     * email is present, gain 1 when {@code namesakeCount} is 0, gain 2 for a household of
     * {@value #MEMBERSHIP_HOUSEHOLD_SIZE} or more (see {@link #getHouseholdSize()}), and gain 3 when
     * the owner's tenure (fiscal years elapsed since its {@code registrationDate}) is at least
     * {@value #MEMBERSHIP_TENURE_FISCAL_YEARS}.
     */
    public int getMembershipPoints() {
        int points = 0;
        if (this.email != null && !this.email.isEmpty()) {
            points += 2;
        }
        if (this.namesakeCount != null && this.namesakeCount == 0) {
            points += 1;
        }
        if (this.householdSize >= MEMBERSHIP_HOUSEHOLD_SIZE) {
            points += 2;
        }
        if (this.registrationDate != null
                && (fiscalYearOf(LocalDate.now()) - fiscalYearOf(this.registrationDate))
                        >= MEMBERSHIP_TENURE_FISCAL_YEARS) {
            points += 3;
        }
        return points;
    }

    /**
     * The owner's numeric membership level, mapped from {@link #getMembershipPoints()}: 1 for 0-1
     * points, 2 for 2-3 points, 3 for 4-5 points, and 4 for 6 or more points.
     */
    public int getMembershipLevel() {
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
     * The derived identity key used to detect duplicate owners: the (already normalized) telephone,
     * the email or empty, and the {@code householdId} or empty, joined with {@code '|'}. This single
     * key consolidates the former separate telephone, email and household duplicate rules — two
     * owners are duplicates only when their whole identity keys are equal. Because the telephone is
     * part of the key, two members of the same household (same {@code householdId}) with different
     * telephones have different keys and are both allowed.
     */
    public String getIdentityKey() {
        String tel = this.telephone == null ? "" : this.telephone;
        String mail = this.email == null ? "" : this.email;
        String household = this.householdId == null ? "" : this.householdId;
        return tel + "|" + mail + "|" + household;
    }

    /**
     * A single Luhn check digit (0-9) computed over the digits contained in {@code customerCode},
     * or null when the {@code customerCode} has not been assigned.
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
     * The owner's membership number formatted {@code '<customerCode>-M<YY>'} where YY is the last
     * two digits of the fiscal year of the {@code registrationDate} (see
     * {@link #fiscalYearOf(LocalDate)}), or null when either the {@code customerCode} or the
     * {@code registrationDate} is not set.
     */
    public String getMembershipNumber() {
        if (this.customerCode == null || this.registrationDate == null) {
            return null;
        }
        String yy = String.format("%02d", fiscalYearOf(this.registrationDate) % 100);
        return this.customerCode + "-M" + yy;
    }

    public boolean isSharesHousehold() {
        return this.sharesHousehold;
    }

    public void setSharesHousehold(boolean sharesHousehold) {
        this.sharesHousehold = sharesHousehold;
    }

    public int getHouseholdSize() {
        return this.householdSize;
    }

    public void setHouseholdSize(int householdSize) {
        this.householdSize = householdSize;
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
