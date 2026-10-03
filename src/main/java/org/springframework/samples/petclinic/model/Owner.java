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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

    @Column(name = "city")
    @NotEmpty
    private String city;

    @Column(name = "telephone")
    @NotEmpty
    @Pattern(regexp = "^\\+[0-9]{8,15}$", message = "Phone number must be in E.164 form (a '+' followed by 8 to 15 digits)")
    private String telephone;

    @Column(name = "email")
    private String email;

    @Column(name = "registration_date", columnDefinition = "DATE")
    private LocalDate registrationDate;

    @Column(name = "customer_code")
    private String customerCode;

    @Column(name = "household_id")
    private String householdId;

    @Column(name = "namesake_count")
    private Integer namesakeCount;

    @Column(name = "household_size")
    private Integer householdSize;

    @Column(name = "postcode")
    private String postcode;

    @Column(name = "bulk_signup_warning")
    private Boolean bulkSignupWarning;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner", fetch = FetchType.EAGER)
    private Set<Pet> pets;

    /**
     * Fixed city-to-region table used to derive {@link #getLocality() locality} when the postcode
     * is absent or in no known range. Any city not listed derives the locality {@code 'UNKNOWN'}.
     */
    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW",
        "Melbourne", "VIC",
        "Brisbane", "QLD");

    /**
     * Inclusive 4-digit postcode range per region, used to derive {@link #getLocality() locality}
     * from the postcode in preference to the city: NSW 2000-2099, VIC 3000-3099, QLD 4000-4099.
     */
    private static final Map<String, int[]> REGION_POSTCODE = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

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

    /**
     * The owner's membership number, formatted {@code '<customerCode>-M<YY>'} where {@code YY} is
     * the last two digits of the registration date's year (e.g. {@code 'NSW-1A2B3C4D-M26'}). Derived
     * from the owner's own customer code and registration date; {@code null} when either is absent.
     *
     * @return the membership number, or {@code null} when it cannot be derived
     */
    public String getMembershipNumber() {
        if (this.customerCode == null || this.registrationDate == null) {
            return null;
        }
        return String.format("%s-M%02d", this.customerCode, this.registrationDate.getYear() % 100);
    }

    /**
     * The owner's Luhn check digit, a single digit (0-9) computed over the digits contained in the
     * {@link #getCustomerCode() customer code}. Non-digit characters (such as the separators and the
     * letter prefixes) are ignored. Returns {@code null} when the customer code is absent.
     *
     * @return the Luhn check digit, or {@code null} when the customer code is absent
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
     * The owner's numeric membership level, derived from the values frozen at create time. It starts
     * at 1, gains 1 when an email address is present, gains 1 when the owner has no namesakes
     * ({@code namesakeCount} is 0), and is capped at 3. Level 4 is reserved for tenure.
     *
     * @return the membership level, an integer from 1 to 3
     */
    public Integer getMembershipLevel() {
        int level = 1;
        boolean hasEmail = this.email != null && !this.email.isBlank();
        if (hasEmail) {
            level++;
        }
        boolean hasNoNamesakes = this.namesakeCount != null && this.namesakeCount == 0;
        if (hasNoNamesakes) {
            level++;
        }
        return Math.min(level, 3);
    }

    /**
     * The owner's locality, the canonical region string. Once the owner has a
     * {@link #getCustomerCode() customer code} the locality is the {@code <REGION>} component of that
     * code (the text before the first {@code '-'}), so the locality and the identity share a single
     * source of truth. Before the customer code is assigned (for example while a create is still
     * validating the postcode) it falls back to the region the code will be built from: the
     * {@link #getPostcode() postcode} looked up by range first (NSW 2000-2099, VIC 3000-3099,
     * QLD 4000-4099), then the fixed city-to-region table (Sydney->NSW, Melbourne->VIC,
     * Brisbane->QLD), and finally {@code 'UNKNOWN'} when neither resolves to a region.
     *
     * @return the canonical region string, or {@code 'UNKNOWN'}
     */
    public String getLocality() {
        if (this.customerCode != null) {
            int dash = this.customerCode.indexOf('-');
            return dash >= 0 ? this.customerCode.substring(0, dash) : this.customerCode;
        }
        return deriveRegion();
    }

    /**
     * The region the customer code's {@code <REGION>} component is built from: the postcode looked up
     * by range first (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099), falling back to the fixed
     * city-to-region table (Sydney->NSW, Melbourne->VIC, Brisbane->QLD) when the postcode is absent or
     * in no known range, and {@code 'UNKNOWN'} when neither resolves. Preferring the postcode returns
     * the same region for known cities but disambiguates cities that share a name.
     *
     * @return the canonical region string, or {@code 'UNKNOWN'}
     */
    private String deriveRegion() {
        String region = regionFromPostcode();
        if (region != null) {
            return region;
        }
        return CITY_REGION.getOrDefault(this.city, "UNKNOWN");
    }

    /**
     * Builds this owner's customer code, formatted {@code '<REGION>-<HASH8>'}. {@code REGION} is the
     * region derived from the postcode (falling back to the city, then {@code 'UNKNOWN'}; see
     * {@link #deriveRegion()}) and {@code HASH8} is the first 8 upper-case hexadecimal characters of
     * the SHA-256 digest of the owner's normalized telephone concatenated with its last name (e.g.
     * {@code 'NSW-1A2B3C4D'}). The code carries no sequence number, so it is a pure function of the
     * owner's region, telephone and last name. Call this once at create time and store the result via
     * {@link #setCustomerCode(String)}, which thereafter backs {@link #getLocality()},
     * {@link #getMembershipNumber()} and {@link #getCheckDigit()}.
     *
     * @return the assembled customer code
     */
    public String buildCustomerCode() {
        return deriveRegion() + "-" + sha256Hex8(this.telephone + this.getLastName());
    }

    /**
     * Computes the first 8 upper-case hexadecimal characters (the first 4 bytes) of the SHA-256 digest
     * of the given value's UTF-8 bytes, the {@code HASH8} component of the customer code.
     *
     * @param value the value to hash
     * @return the 8-character upper-case hex prefix of the SHA-256 digest
     */
    private static String sha256Hex8(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(8);
            for (int i = 0; i < 4; i++) {
                hex.append(String.format("%02X", digest[i]));
            }
            return hex.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * The region whose range contains this owner's postcode, or {@code null} when the postcode is
     * absent, not a 4-digit number, or in no known range.
     *
     * @return the matching region, or {@code null}
     */
    private String regionFromPostcode() {
        if (this.postcode == null) {
            return null;
        }
        int value;
        try {
            value = Integer.parseInt(this.postcode.trim());
        }
        catch (NumberFormatException ex) {
            return null;
        }
        for (Map.Entry<String, int[]> entry : REGION_POSTCODE.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * The owner's preferred contact channel, derived from the owner's details. Returns
     * {@code 'EMAIL'} when an email address is present, otherwise {@code 'PHONE'}.
     *
     * @return {@code 'EMAIL'} when an email address is present, otherwise {@code 'PHONE'}
     */
    public String getContactPreference() {
        boolean hasEmail = this.email != null && !this.email.isBlank();
        return hasEmail ? "EMAIL" : "PHONE";
    }

    /**
     * The owner's identity key, the single derived value used for duplicate detection. It is the
     * owner's normalized telephone, email (empty when absent) and household id joined with
     * {@code '|'} ({@code '<telephone>|<email>|<householdId>'}); any absent ({@code null}) component
     * contributes an empty string. Two owners are duplicates exactly when their whole identity keys
     * are equal, so two members of the same household with different telephones differ here and are
     * both allowed.
     *
     * @return the derived identity key
     */
    public String getIdentityKey() {
        return (this.telephone == null ? "" : this.telephone) + "|"
            + (this.email == null ? "" : this.email) + "|"
            + (this.householdId == null ? "" : this.householdId);
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

    public String getPostcode() {
        return this.postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public Boolean getBulkSignupWarning() {
        return this.bulkSignupWarning;
    }

    public void setBulkSignupWarning(Boolean bulkSignupWarning) {
        this.bulkSignupWarning = bulkSignupWarning;
    }

    /**
     * Stamps the effective registration date exactly once on insert, so every newly persisted owner
     * carries a resolved, business-day registration date (see {@link #resolveRegistrationDate}).
     */
    @PrePersist
    void onCreate() {
        this.registrationDate = resolveRegistrationDate(this.registrationDate);
    }

    /**
     * Resolves the effective registration date and rolls it onto a business day. An absent
     * ({@code null}) value defaults to the server's current date; a supplied value is used as-is.
     * The resulting date is then rolled forward so it never falls on a weekend: a Saturday or Sunday
     * becomes the following Monday, while any weekday is returned unchanged.
     *
     * <p>This is the single definition of both the defaulting and the business-day roll, used
     * wherever the effective registration date is needed - by the {@code @PrePersist} hook that
     * stamps the owner and by the create endpoint that counts owners per business day.
     *
     * @param supplied the registration date from the request, or {@code null} when none was supplied
     * @return the effective registration date, guaranteed to fall on a business day
     */
    public static LocalDate resolveRegistrationDate(LocalDate supplied) {
        LocalDate effective = supplied != null ? supplied : LocalDate.now();
        return switch (effective.getDayOfWeek()) {
            case SATURDAY -> effective.plusDays(2);
            case SUNDAY -> effective.plusDays(1);
            default -> effective;
        };
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
