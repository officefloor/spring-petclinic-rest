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

    @Column(name = "postcode")
    private String postcode;

    @Column(name = "possible_duplicate")
    private Boolean possibleDuplicate;

    @Column(name = "possible_duplicate_of")
    private Integer possibleDuplicateOf;

    @Transient
    private boolean declaredHouseholdMember;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner", fetch = FetchType.EAGER)
    private Set<Pet> pets;

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * The owner's salutation: the {@link #getTitle() title} and last name separated by a
     * single space (e.g. {@code "DR Who"}), or just the last name when no title is on file.
     * Derived from the owner's own fields, so it stays consistent with them.
     */
    @Transient
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

    public Boolean getBulkSignupWarning() {
        return this.bulkSignupWarning;
    }

    public void setBulkSignupWarning(Boolean bulkSignupWarning) {
        this.bulkSignupWarning = bulkSignupWarning;
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

    /**
     * Whether this owner deliberately declared, when created, that it shares its
     * household with an existing owner (the request's {@code sharesHousehold} flag).
     * A declared member is created despite occupying an existing household and is not
     * treated as a suspected duplicate. This is a request-scoped flag, not persisted.
     */
    @Transient
    public boolean isDeclaredHouseholdMember() {
        return this.declaredHouseholdMember;
    }

    public void setDeclaredHouseholdMember(boolean declaredHouseholdMember) {
        this.declaredHouseholdMember = declaredHouseholdMember;
    }

    /**
     * Whether this owner, when created, was found to be a possible (soft) duplicate of an
     * existing owner: not a hard duplicate, but sharing that owner's last name and postcode
     * while carrying a different telephone. See {@link #getPossibleDuplicateOf()}.
     */
    public Boolean getPossibleDuplicate() {
        return this.possibleDuplicate;
    }

    public void setPossibleDuplicate(Boolean possibleDuplicate) {
        this.possibleDuplicate = possibleDuplicate;
    }

    /**
     * The id of the existing owner this owner was flagged as a possible duplicate of, or
     * null when it is not a possible duplicate. See {@link #getPossibleDuplicate()}.
     */
    public Integer getPossibleDuplicateOf() {
        return this.possibleDuplicateOf;
    }

    public void setPossibleDuplicateOf(Integer possibleDuplicateOf) {
        this.possibleDuplicateOf = possibleDuplicateOf;
    }

    /**
     * The owner's membership number, formatted {@code '<customerCode>-M<YY>'} where YY
     * is the last two digits of the registration date's year (e.g. {@code "NSW-1A2B3C4D-M26"}).
     * Derived from the owner's own fields, so it stays consistent with them.
     */
    @Transient
    public String getMembershipNumber() {
        if (this.customerCode == null || this.registrationDate == null) {
            return null;
        }
        return String.format("%s-M%02d", this.customerCode, this.registrationDate.getYear() % 100);
    }

    /**
     * The owner's customer-code check digit: the single Luhn check digit (0-9) computed
     * over the digits of the customer code. Derived from the owner's own customer code,
     * so it stays consistent with it.
     */
    @Transient
    public Integer getCheckDigit() {
        if (this.customerCode == null) {
            return null;
        }
        return LuhnCheckDigit.of(this.customerCode);
    }

    /**
     * The owner's membership points, starting at zero: two points are added when an email is
     * on file, one more when the owner is unique (namesake count of zero), two more for a
     * household of three or more, and three more when the owner has accrued more than a year of
     * tenure (see {@link Tenure}). Derived from the owner's own fields, so it stays consistent
     * with them.
     */
    @Transient
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
        boolean largeHousehold = this.householdSize != null && this.householdSize >= 3;
        if (largeHousehold) {
            points += 2;
        }
        boolean tenured = this.registrationDate != null
            && Tenure.qualifiesForTopLevel(this.registrationDate, LocalDate.now());
        if (tenured) {
            points += 3;
        }
        return points;
    }

    /**
     * The owner's membership level, a number from 1 to 4, derived from their
     * {@link #getMembershipPoints() membership points} (see {@link MembershipLevel}). Derived
     * from the owner's own fields, so it stays consistent with them.
     */
    @Transient
    public Integer getMembershipLevel() {
        return MembershipLevel.forPoints(getMembershipPoints());
    }

    /**
     * The owner's locality: the canonical region, taken as the region segment of the
     * customer code (formatted {@code '<REGION>-<HASH8>'}), so the locality stays
     * consistent with the owner's identity. When no customer code has been assigned yet
     * it falls back to resolving the region directly from the postcode and city (see
     * {@link RegionResolver}), the same rule the customer code itself is built from.
     */
    @Transient
    public String getLocality() {
        if (this.customerCode != null) {
            int separator = this.customerCode.indexOf('-');
            if (separator > 0) {
                return this.customerCode.substring(0, separator);
            }
        }
        return RegionResolver.regionFor(this.postcode, this.city);
    }

    /**
     * The owner's timezone: the IANA timezone name for their {@link #getLocality() locality},
     * taken from the fixed region-to-timezone table (see {@link RegionTimezoneTable}), so it
     * stays consistent with the owner's region. Null when the locality has no known timezone.
     */
    @Transient
    public String getTimezone() {
        return RegionTimezoneTable.timezoneFor(getLocality());
    }

    /**
     * The owner's preferred contact channel: {@code "EMAIL"} when an email address is on
     * file, otherwise {@code "PHONE"}. Derived from the owner's own contact details, so it
     * stays consistent with them.
     */
    @Transient
    public String getContactPreference() {
        boolean hasEmail = this.email != null && !this.email.isEmpty();
        return hasEmail ? "EMAIL" : "PHONE";
    }

    /**
     * The owner's telephone formatted for human display: the stored E.164 number
     * rendered as its country calling code, a space and the national digits grouped
     * in threes (e.g. {@code "+61 412 345 678"}). The raw {@link #getTelephone()}
     * stays in E.164 form. Derived from the owner's own telephone, so it stays
     * consistent with it. See {@link TelephoneFormatter}.
     */
    @Transient
    public String getTelephoneDisplay() {
        return TelephoneFormatter.toDisplay(this.telephone);
    }

    /**
     * The owner's identity key: the single derived value used to detect duplicate owners,
     * formed as {@code normalizedTelephone + '|' + (email or empty) + '|' + householdId}.
     * Two owners are duplicates only when their whole identity keys are equal, so members
     * of one household (sharing a household id) but with different telephones stay distinct.
     * Derived from the owner's own fields, so it stays consistent with them.
     */
    @Transient
    public String getIdentityKey() {
        String telephonePart = this.telephone == null ? "" : this.telephone;
        String emailPart = this.email == null ? "" : this.email;
        String householdPart = this.householdId == null ? "" : this.householdId;
        return telephonePart + "|" + emailPart + "|" + householdPart;
    }

    /**
     * The owner's age band relative to their registration date: {@code "MINOR"} when
     * under 18, {@code "ADULT"} from 18 to 64, and {@code "SENIOR"} at 65 or older.
     * Derived from the owner's own birth and registration dates, so it stays consistent
     * with them; null until a birth date is on file. See {@link AgeBand}.
     */
    @Transient
    public String getAgeBand() {
        if (this.birthDate == null || this.registrationDate == null) {
            return null;
        }
        return AgeBand.asOf(this.birthDate, this.registrationDate).name();
    }

    /**
     * Normalize the registration date before persisting: default it to the server's
     * current date when none was supplied, then roll it forward onto a business day so
     * every newly persisted owner is registered on a weekday.
     */
    @PrePersist
    void normalizeRegistrationDate() {
        if (this.registrationDate == null) {
            this.registrationDate = LocalDate.now();
        }
        this.registrationDate = BusinessDay.rollForward(this.registrationDate);
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
