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
import jakarta.validation.constraints.Email;
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

    @Column(name = "postcode")
    private String postcode;

    @Column(name = "telephone")
    @NotEmpty
    @Pattern(regexp = "^\\+[0-9]{8,15}$", message = "Telephone must be in E.164 format (a '+' followed by 8 to 15 digits)")
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

    @Column(name = "bulk_signup_warning")
    private Boolean bulkSignupWarning;

    @Column(name = "household_size")
    private Integer householdSize;

    @Column(name = "membership_level")
    private Integer membershipLevel;

    @Column(name = "possible_duplicate")
    private Boolean possibleDuplicate;

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
     * The owner's salutation: the optional {@link #title} (MR/MRS/MS/DR) followed by a single space
     * and the last name, or just the last name when no title was supplied. A blank title is treated
     * as absent.
     *
     * @return the composed salutation
     */
    @Transient
    public String getSalutation() {
        if (this.title == null || this.title.isBlank()) {
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
     * The known E.164 country calling codes used to split the stored telephone into its country
     * code and national digits when formatting {@link #getTelephoneDisplay()}. Ordered
     * longest-first so the most specific code wins; this mirrors the codes the application
     * recognises elsewhere (Australia {@code '+61'} and the NANP {@code '+1'}).
     */
    private static final List<String> KNOWN_COUNTRY_CODES = List.of("61", "1");

    /**
     * The stored E.164 {@link #telephone} formatted for humans: the country code, a space, then the
     * national digits grouped in threes from the left and separated by spaces. So the stored
     * {@code '+61412345678'} is displayed as {@code '+61 412 345 678'}. The raw {@link #telephone}
     * itself is left in E.164 form. A {@code null} telephone yields {@code null}, and a value not in
     * E.164 form (no leading {@code '+'}) is returned unchanged.
     *
     * @return the human-formatted telephone, or {@code null} when no telephone is stored
     */
    @Transient
    public String getTelephoneDisplay() {
        if (this.telephone == null || !this.telephone.startsWith("+")) {
            return this.telephone;
        }
        String allDigits = this.telephone.substring(1);
        if (allDigits.isEmpty()) {
            return this.telephone;
        }
        String countryCode = null;
        for (String code : KNOWN_COUNTRY_CODES) {
            if (allDigits.startsWith(code)) {
                countryCode = code;
                break;
            }
        }
        if (countryCode == null) {
            countryCode = allDigits.substring(0, 1);
        }
        String national = allDigits.substring(countryCode.length());
        StringBuilder display = new StringBuilder("+").append(countryCode);
        for (int i = 0; i < national.length(); i += 3) {
            display.append(' ').append(national, i, Math.min(i + 3, national.length()));
        }
        return display.toString();
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
     * Derives the owner's age band from a birth date, computed against the reference date
     * (the registration date): {@code 'MINOR'} when under 18, {@code 'ADULT'} from 18 to 64,
     * and {@code 'SENIOR'} at 65 or older. The completed-years age is the whole number of years
     * between the birth date and the reference date. Returns {@code null} when no birth date is
     * supplied. When the reference date is absent the current server date is used.
     *
     * @param birthDate the owner's date of birth, or {@code null} when absent
     * @param reference the date to compute the age against (the registration date), may be {@code null}
     * @return {@code 'MINOR'}, {@code 'ADULT'} or {@code 'SENIOR'}, or {@code null} when no birth date
     */
    public static String ageBandOf(LocalDate birthDate, LocalDate reference) {
        if (birthDate == null) {
            return null;
        }
        LocalDate asOf = reference != null ? reference : LocalDate.now();
        int years = java.time.Period.between(birthDate, asOf).getYears();
        if (years < 18) {
            return "MINOR";
        }
        if (years < 65) {
            return "ADULT";
        }
        return "SENIOR";
    }

    /**
     * The starting calendar year of the fiscal year that contains the given date. The fiscal year
     * starts on 1 July, so a date in July or later belongs to the fiscal year starting that same
     * calendar year, while a date from January to June belongs to the fiscal year that started in
     * the previous calendar year.
     *
     * @param date the date to classify
     * @return the calendar year in which the containing fiscal year starts
     */
    public static int fiscalYearNumber(LocalDate date) {
        return date.getMonthValue() >= java.time.Month.JULY.getValue()
            ? date.getYear() : date.getYear() - 1;
    }

    /**
     * The fiscal year that contains the given date, formatted {@code 'FY<YY>'} where YY is the last
     * two digits of the fiscal year's starting calendar year. For example a date on or after 1 July
     * 2026 yields {@code 'FY26'}. The fiscal year starts on 1 July.
     *
     * @param date the date to classify, which for an owner is the business-day-adjusted registration date
     * @return the fiscal year label, e.g. {@code 'FY26'}
     */
    public static String fiscalYearLabel(LocalDate date) {
        return String.format("FY%02d", fiscalYearNumber(date) % 100);
    }

    /**
     * Whether the owner's tenure — the number of elapsed fiscal years from the registration date to
     * the current server date — exceeds one year (strictly more than one elapsed fiscal year). The
     * elapsed count is the difference between the fiscal year of the current server date and the
     * fiscal year of the registration date. This gates the top membership level: only an owner past
     * a full year of tenure can reach level 4. A {@code null} registration date counts as zero
     * tenure and so never exceeds the threshold, which is why a newly created owner (zero tenure)
     * can never exceed level 3.
     *
     * @param registrationDate the date the owner registered, or {@code null} when absent
     * @return {@code true} when more than one fiscal year has elapsed since registration
     */
    public static boolean tenureExceedsOneYear(LocalDate registrationDate) {
        if (registrationDate == null) {
            return false;
        }
        return fiscalYearNumber(LocalDate.now()) - fiscalYearNumber(registrationDate) > 1;
    }

    /**
     * The owner's membership points, accumulated from the membership factors. Points start at 0 and
     * gain 2 when an email address is present, 1 when the owner has no namesakes (namesakeCount is
     * 0), 2 for a household of 3 or more members, and 3 when the owner's tenure exceeds one year.
     *
     * @param owner the owner to score
     * @return the total membership points (never negative)
     */
    public static int membershipPoints(Owner owner) {
        int points = 0;
        if (owner.getEmail() != null && !owner.getEmail().isEmpty()) {
            points += 2;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            points += 1;
        }
        if (owner.getHouseholdSize() != null && owner.getHouseholdSize() >= 3) {
            points += 2;
        }
        if (tenureExceedsOneYear(owner.getRegistrationDate())) {
            points += 3;
        }
        return points;
    }

    /**
     * Maps membership points to the numeric membership level: 1 for 0-1 points, 2 for 2-3, 3 for
     * 4-5, and 4 for 6 or more.
     *
     * @param points the owner's membership points
     * @return the membership level, from 1 to 4
     */
    public static int membershipLevel(int points) {
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
     * The owner's effective membership level: the explicitly stored {@link #membershipLevel} when one
     * was fixed at creation (for example, an owner whose level was capped to one above their
     * household), otherwise the level derived live from {@link #membershipPoints(Owner)}.
     *
     * @param owner the owner to score
     * @return the effective membership level
     */
    public static int effectiveMembershipLevel(Owner owner) {
        if (owner.getMembershipLevel() != null) {
            return owner.getMembershipLevel();
        }
        return membershipLevel(membershipPoints(owner));
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

    public Integer getMembershipLevel() {
        return this.membershipLevel;
    }

    public void setMembershipLevel(Integer membershipLevel) {
        this.membershipLevel = membershipLevel;
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

    public boolean isDeleted() {
        return this.deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    /**
     * The single derived key used for all duplicate detection, formatted
     * {@code '<normalizedTelephone>|<email or empty>|<householdId or empty>'}. Two owners are
     * duplicates only when their whole identity keys are equal; because the telephone is part
     * of the key, two members of the same household with different telephones have different
     * keys and are both allowed.
     *
     * @return the derived identity key
     */
    @Transient
    public String getIdentityKey() {
        return (this.telephone == null ? "" : this.telephone) + "|"
            + (this.email == null ? "" : this.email) + "|"
            + (this.householdId == null ? "" : this.householdId);
    }

    /**
     * A single Luhn check digit (0-9) computed over the digits contained in the
     * {@code customerCode}. Non-digit characters (such as the letter prefixes and the
     * separators) are ignored; a {@code null} customer code is treated as having no digits.
     *
     * @return the Luhn check digit for the customer code
     */
    @Transient
    public int getCheckDigit() {
        String source = this.customerCode == null ? "" : this.customerCode;
        int sum = 0;
        boolean dbl = true;
        for (int i = source.length() - 1; i >= 0; i--) {
            char c = source.charAt(i);
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
