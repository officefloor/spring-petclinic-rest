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

    /** Fixed city-to-region table used to derive {@link #getLocality()}. */
    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** Region -> inclusive 4-digit postcode range {low, high} used to derive
     *  {@link #getLocality()} in preference to the city. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
        "NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099}, "QLD", new int[] {4000, 4099});

    @Column(name = "address")
    @NotEmpty
    private String address;

    @Column(name = "city")
    @NotEmpty
    private String city;

    @Column(name = "telephone")
    @NotEmpty
    @Pattern(regexp = "^\\+[0-9]{8,15}$", message = "Telephone must be in E.164 form")
    private String telephone;

    @Column(name = "email")
    @Email
    private String email;

    @Column(name = "postcode")
    private String postcode;

    @Column(name = "registration_date")
    private LocalDate registrationDate;

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

    /**
     * The owner's locality, i.e. the canonical region it belongs to. This is now read
     * from the region-and-hash {@link #customerCode} identity: the region is the
     * {@code <REGION>} component that precedes the first {@code '-'} of the assigned
     * customer code (e.g. {@code NSW} for {@code NSW-9F86D081}). Until an owner has
     * been assigned a customer code (for example while its create request is still
     * being validated) this falls back to the region derived directly from its own
     * fields (see {@link #regionForOwner()}). Exposed as a derived (not persisted)
     * property and recomputed each call.
     *
     * @return the canonical region string, or {@code "UNKNOWN"}
     */
    @Transient
    public String getLocality() {
        if (this.customerCode != null) {
            int dash = this.customerCode.indexOf('-');
            if (dash >= 0) {
                return this.customerCode.substring(0, dash);
            }
        }
        return regionForOwner();
    }

    /**
     * The canonical region derived for this owner from its own fields. The
     * {@link #postcode} is consulted first: when it is present and falls within a known
     * region's inclusive 4-digit range ({@code NSW 2000-2099}, {@code VIC 3000-3099},
     * {@code QLD 4000-4099}) that region is returned. Only when the postcode is absent
     * or in no known range does this fall back to the fixed city-to-region table
     * ({@code Sydney -> NSW}, {@code Melbourne -> VIC}, {@code Brisbane -> QLD}),
     * returning {@code "UNKNOWN"} when the city is not in the table either. This yields
     * the same region for the known cities while disambiguating cities that share a name
     * via their postcode. Recomputed from the current fields each call.
     *
     * @return the canonical region string, or {@code "UNKNOWN"}
     */
    public String regionForOwner() {
        String byPostcode = regionForPostcode(this.postcode);
        if (byPostcode != null) {
            return byPostcode;
        }
        return CITY_REGION.getOrDefault(this.city, "UNKNOWN");
    }

    /**
     * The region whose inclusive postcode range contains the given postcode, or
     * {@code null} when the postcode is absent, not a four-digit number, or in no
     * known range.
     */
    private static String regionForPostcode(String postcode) {
        if (postcode == null || !postcode.matches("\\d{4}")) {
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
     * The owner's membership level, a number from 1 to 3 assigned at creation:
     * starting at 1, add 1 when an {@link #email} is present and add 1 when
     * {@link #namesakeCount} is 0, capped at 3 (level 4 is reserved for tenure).
     * Derived (not persisted); recomputed from the current fields each call.
     *
     * @return the membership level, from 1 to 3
     */
    @Transient
    public Integer getMembershipLevel() {
        int level = 1;
        if (this.email != null && !this.email.isBlank()) {
            level++;
        }
        if (this.namesakeCount != null && this.namesakeCount == 0) {
            level++;
        }
        return Math.min(level, 3);
    }

    /**
     * The owner's preferred contact channel: {@code "EMAIL"} when an
     * {@link #email} is present, otherwise {@code "PHONE"}. Derived (not
     * persisted); recomputed from the current fields each call.
     *
     * @return {@code "EMAIL"} or {@code "PHONE"}
     */
    @Transient
    public String getContactPreference() {
        if (this.email != null && !this.email.isBlank()) {
            return "EMAIL";
        }
        return "PHONE";
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

    public String getCustomerCode() {
        return this.customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    /**
     * The Luhn check digit (0-9) computed over the decimal digits contained in this
     * owner's {@link #customerCode}. Derived (not persisted); recomputed from the
     * current customer code each call. An absent or digit-free customer code yields
     * {@code 0}.
     *
     * @return the Luhn check digit, from 0 to 9
     */
    @Transient
    public Integer getCheckDigit() {
        return luhnCheckDigit(this.customerCode);
    }

    /**
     * The Luhn check digit (0-9) computed over the decimal digits contained in the
     * given value, processed right-to-left with every second digit doubled (and
     * reduced by 9 when the double exceeds 9). Non-digit characters are ignored, and
     * an absent or digit-free value yields {@code 0}.
     */
    private static int luhnCheckDigit(String value) {
        int sum = 0;
        boolean doubleDigit = true;
        for (int i = (value == null ? 0 : value.length()) - 1; i >= 0; i--) {
            char c = value.charAt(i);
            if (c < '0' || c > '9') {
                continue;
            }
            int d = c - '0';
            if (doubleDigit) {
                d *= 2;
                if (d > 9) {
                    d -= 9;
                }
            }
            sum += d;
            doubleDigit = !doubleDigit;
        }
        return (10 - (sum % 10)) % 10;
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

    /**
     * The single derived key used for duplicate detection, formed as
     * {@code normalizedTelephone + '|' + (email or empty) + '|' + householdId}.
     * The telephone is the owner's stored (E.164-normalised) telephone, the email
     * is the stored (lower-cased) email or the empty string when absent, and the
     * household component is the stored {@link #householdId} or the empty string
     * when the owner belongs to no household. Two owners are duplicates of each
     * other exactly when their whole identity keys are equal; because the telephone
     * is part of the key, two members of the same household with different
     * telephones have different identity keys. Derived (not persisted); recomputed
     * from the current fields each call.
     *
     * @return the owner's identity key
     */
    @Transient
    public String getIdentityKey() {
        String tel = this.telephone == null ? "" : this.telephone;
        String mail = this.email == null ? "" : this.email;
        String household = this.householdId == null ? "" : this.householdId;
        return tel + "|" + mail + "|" + household;
    }

    /**
     * A stable, normalised key identifying this owner's household (same last name
     * and address). The last name is folded case- and whitespace-insensitively and
     * the address is reduced to its canonical form exactly as in
     * {@link #sameHouseholdAs(Owner)}, so two owners share a key iff they share a
     * household.
     *
     * @return the household key
     */
    public String householdKey() {
        return normalize(this.getLastName()) + " " + normalizeAddress(this.address);
    }

    /**
     * Whether this owner belongs to the same household as {@code other}: that is,
     * they share the same last name and address. The last name is compared
     * case-insensitively with runs of whitespace collapsed to a single space (and
     * surrounding whitespace trimmed); the address is compared in its canonical
     * normalised form (see {@link #normalizeAddress(String)}).
     *
     * @param other the owner to compare against
     * @return {@code true} if both owners share a household
     */
    public boolean sameHouseholdAs(Owner other) {
        return normalize(this.getLastName()).equals(normalize(other.getLastName()))
            && normalizeAddress(this.address).equals(normalizeAddress(other.address));
    }

    /**
     * Normalise a value for case-insensitive, whitespace-insensitive comparison:
     * trim, collapse internal runs of whitespace to a single space and lower-case.
     */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    /**
     * Reduce an address to its canonical stored form: trim and collapse runs of
     * whitespace to a single space, upper-case, and expand common abbreviations
     * ({@code ST -> STREET}, {@code RD -> ROAD}, {@code AVE -> AVENUE}) whenever
     * they appear as whole words. The result is what is stored, returned and used
     * for every household comparison; it is idempotent, so re-normalising an
     * already-normalised address leaves it unchanged.
     *
     * @param value the raw address (may be {@code null})
     * @return the normalised address, or the empty string if {@code value} is
     *         {@code null} or blank
     */
    public static String normalizeAddress(String value) {
        if (value == null) {
            return "";
        }
        String collapsed = value.trim().replaceAll("\\s+", " ").toUpperCase();
        if (collapsed.isEmpty()) {
            return "";
        }
        String[] tokens = collapsed.split(" ");
        for (int i = 0; i < tokens.length; i++) {
            tokens[i] = switch (tokens[i]) {
                case "ST" -> "STREET";
                case "RD" -> "ROAD";
                case "AVE" -> "AVENUE";
                default -> tokens[i];
            };
        }
        return String.join(" ", tokens);
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
