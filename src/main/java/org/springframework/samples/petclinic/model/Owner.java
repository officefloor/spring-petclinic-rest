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
    @Pattern(regexp = "^\\+[0-9]{8,15}$", message = "Phone number must be in E.164 form: a '+' followed by 8 to 15 digits")
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

    @Column(name = "membership_number")
    private String membershipNumber;

    @Column(name = "bulk_signup_warning")
    private Boolean bulkSignupWarning;

    @Column(name = "household_size")
    private Integer householdSize;

    @Column(name = "postcode")
    private String postcode;

    @Column(name = "possible_duplicate_of")
    private Integer possibleDuplicateOf;

    @Column(name = "deleted", nullable = false)
    private boolean deleted;

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

    /**
     * The first line of this owner's structured postal address, or {@code null} when the owner was
     * created with only the flat {@link #getAddress() address}. Preferred over the flat address when
     * present; see {@link #getAddress()} for the composed form.
     */
    public String getAddressLine1() {
        return this.addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    /**
     * The optional second line of this owner's structured postal address, or {@code null} when none
     * was supplied. When present it is appended to {@link #getAddressLine1() addressLine1} in the
     * composed {@link #getAddress() address}.
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

    public String getTelephone() {
        return this.telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    /**
     * Return this owner's stored E.164 {@link #getTelephone() telephone} formatted for human display
     * as the calling code, a space, then the national digits grouped in threes, e.g.
     * {@code "+61 412 345 678"}. See {@link TelephoneFormatter}.
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

    /**
     * Whether this owner was created after more than 80 owners had already been registered on the
     * same day, flagging an unusually high-volume ("bulk") signup. Captured once at creation.
     */
    public Boolean getBulkSignupWarning() {
        return this.bulkSignupWarning;
    }

    public void setBulkSignupWarning(Boolean bulkSignupWarning) {
        this.bulkSignupWarning = bulkSignupWarning;
    }

    /**
     * The number of owners in this owner's household (owners sharing the same {@code householdId},
     * counting this owner) captured once at creation.
     */
    public Integer getHouseholdSize() {
        return this.householdSize;
    }

    public void setHouseholdSize(Integer householdSize) {
        this.householdSize = householdSize;
    }

    /**
     * This owner's 4-digit postcode, or {@code null} when none was supplied. When present it is
     * validated against the owner's city region at creation (see {@code PostcodeValidator}).
     */
    public String getPostcode() {
        return this.postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    /**
     * The id of an existing owner this owner was flagged as a possible (soft) duplicate of at
     * creation, or {@code null} when none. Because owners sharing a last name and postcode now form
     * the same household — rejected as a household duplicate unless declared via {@code sharesHousehold},
     * and a declared member is not a suspected duplicate — this is not populated at creation.
     * Captured once at creation.
     */
    public Integer getPossibleDuplicateOf() {
        return this.possibleDuplicateOf;
    }

    public void setPossibleDuplicateOf(Integer possibleDuplicateOf) {
        this.possibleDuplicateOf = possibleDuplicateOf;
    }

    /**
     * Whether this owner was flagged as a possible (soft) duplicate of an existing owner at creation,
     * i.e. whether {@link #getPossibleDuplicateOf()} is set.
     */
    @Transient
    public Boolean getPossibleDuplicate() {
        return this.possibleDuplicateOf != null;
    }

    /**
     * Whether this owner has been soft-deleted. A newly created owner is not deleted; deleting an owner
     * flags it {@code true} and retains the record rather than removing it. Duplicate and identity
     * checks at creation ignore owners flagged deleted.
     */
    public boolean isDeleted() {
        return this.deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    /**
     * Return this owner's name formatted for display as {@code "LastName, FirstName"}.
     */
    @Transient
    public String getDisplayName() {
        return this.getLastName() + ", " + this.getFirstName();
    }

    /**
     * Return this owner's salutation as the {@link #getTitle() title} and last name separated by a
     * space (e.g. {@code "DR Who"}), or just the last name when no title was supplied.
     */
    @Transient
    public String getSalutation() {
        return (this.title == null || this.title.isBlank())
            ? this.getLastName()
            : this.title + " " + this.getLastName();
    }

    /**
     * Return this owner's initials as the upper-cased first letters of the first and last
     * name, dot-separated with a trailing dot, e.g. {@code "J.S."}.
     */
    @Transient
    public String getInitials() {
        return initial(this.getFirstName()) + initial(this.getLastName());
    }

    private static String initial(String name) {
        return Character.toUpperCase(name.charAt(0)) + ".";
    }

    /** Points awarded when an email address is present. */
    private static final int EMAIL_POINTS = 2;

    /** Points awarded when the owner has no namesakes ({@code namesakeCount} is 0). */
    private static final int NO_NAMESAKE_POINTS = 1;

    /** Points awarded for belonging to a household of {@value #LARGE_HOUSEHOLD_SIZE} or more. */
    private static final int LARGE_HOUSEHOLD_POINTS = 2;

    /** The household size at or above which {@link #LARGE_HOUSEHOLD_POINTS} are awarded. */
    private static final int LARGE_HOUSEHOLD_SIZE = 3;

    /** Points awarded for qualifying tenure. */
    private static final int TENURE_POINTS = 3;

    /** The tenure, in days, that must be exceeded to earn {@link #TENURE_POINTS}. */
    private static final long MEMBERSHIP_TENURE_THRESHOLD_DAYS = 365;

    /**
     * Return this owner's membership points, starting at 0: add {@value #EMAIL_POINTS} when an email
     * address is present; add {@value #NO_NAMESAKE_POINTS} when the owner has no namesakes
     * ({@code namesakeCount} is 0); add {@value #LARGE_HOUSEHOLD_POINTS} for a household of
     * {@value #LARGE_HOUSEHOLD_SIZE} or more; add {@value #TENURE_POINTS} for qualifying tenure
     * (more than {@value #MEMBERSHIP_TENURE_THRESHOLD_DAYS} days since registration).
     */
    @Transient
    public Integer getMembershipPoints() {
        int points = 0;
        if (hasEmail()) {
            points += EMAIL_POINTS;
        }
        if (Integer.valueOf(0).equals(this.namesakeCount)) {
            points += NO_NAMESAKE_POINTS;
        }
        if (hasLargeHousehold()) {
            points += LARGE_HOUSEHOLD_POINTS;
        }
        if (hasQualifyingTenure()) {
            points += TENURE_POINTS;
        }
        return points;
    }

    /**
     * Return this owner's membership level, derived from {@link #getMembershipPoints() membership
     * points}: 1 for 0-1 points, 2 for 2-3, 3 for 4-5, and 4 for 6 or more.
     */
    @Transient
    public Integer getMembershipLevel() {
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
     * Whether this owner belongs to a household of {@value #LARGE_HOUSEHOLD_SIZE} or more, per the
     * {@code householdSize} captured at creation. Owners without a recorded household size do not
     * qualify.
     */
    private boolean hasLargeHousehold() {
        return this.householdSize != null && this.householdSize >= LARGE_HOUSEHOLD_SIZE;
    }

    /**
     * Whether this owner's tenure exceeds {@value #MEMBERSHIP_TENURE_THRESHOLD_DAYS} days, i.e.
     * more than that many days have elapsed since the {@code registrationDate}. Owners without a
     * registration date have no measurable tenure and do not qualify.
     */
    private boolean hasQualifyingTenure() {
        if (this.registrationDate == null) {
            return false;
        }
        long tenureDays = ChronoUnit.DAYS.between(this.registrationDate, LocalDate.now());
        return tenureDays > MEMBERSHIP_TENURE_THRESHOLD_DAYS;
    }

    /** Whether this owner has a usable email address (present and not blank). */
    private boolean hasEmail() {
        return this.email != null && !this.email.isBlank();
    }

    /**
     * Return this owner's preferred contact channel: {@code "EMAIL"} when an email address is
     * present, otherwise {@code "PHONE"}.
     */
    @Transient
    public String getContactPreference() {
        return hasEmail() ? "EMAIL" : "PHONE";
    }

    /**
     * Return this owner's age band derived from the {@code birthDate} as of the
     * {@code registrationDate}, or {@code null} when no birth date is known.
     */
    @Transient
    public AgeBand getAgeBand() {
        if (this.birthDate == null || this.registrationDate == null) {
            return null;
        }
        return AgeBand.asOf(this.birthDate, this.registrationDate);
    }

    /**
     * Return this owner's locality: the region segment of the {@code customerCode} (the leading
     * {@code <REGION>} of {@code <REGION>-<HASH8>}), which is fixed at creation from the postcode
     * (preferred, matched against the known region ranges) falling back to the city-to-region table,
     * or {@code "UNKNOWN"} when neither yields a region. Before the customer code is assigned the
     * region is resolved directly from the postcode and city.
     */
    @Transient
    public String getLocality() {
        if (this.customerCode == null) {
            return LocalityResolver.regionFor(this.postcode, this.city);
        }
        int separator = this.customerCode.indexOf('-');
        return separator < 0 ? this.customerCode : this.customerCode.substring(0, separator);
    }

    /**
     * Return this owner's timezone: the IANA timezone name for the owner's {@link #getLocality()
     * locality} via the fixed region-to-timezone table, or {@code null} when the locality has no
     * known timezone.
     */
    @Transient
    public String getTimezone() {
        return LocalityResolver.timezoneFor(getLocality());
    }

    /**
     * Return this owner's check digit: the single Luhn check digit computed over the digits of the
     * {@code customerCode}.
     */
    @Transient
    public Integer getCheckDigit() {
        return this.customerCode == null ? null : LuhnCheckDigit.of(this.customerCode);
    }

    /**
     * Return this owner's identity key, the single derived value all duplicate detection is based
     * on: the normalized telephone, email and household id joined with {@code '|'} as
     * {@code telephone + "|" + email + "|" + householdId}, with each absent component rendered as
     * the empty string. Two owners are duplicates only when their whole identity keys are equal;
     * because the telephone is part of the key, members of one household (same {@code householdId})
     * with different telephones have distinct keys and are not duplicates.
     */
    @Transient
    public String getIdentityKey() {
        return keyPart(this.telephone) + "|" + keyPart(this.email) + "|" + keyPart(this.householdId);
    }

    private static String keyPart(String value) {
        return value == null ? "" : value;
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
            .append("registrationDate", this.registrationDate)
            .toString();
    }
}
