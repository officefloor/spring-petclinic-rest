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
    @Pattern(regexp = "^\\+[0-9]{8,15}$", message = "Telephone must be in E.164 form (a '+' followed by 8 to 15 digits)")
    private String telephone;

    @Column(name = "email")
    @Email
    private String email;

    @Column(name = "registration_date")
    private LocalDate registrationDate;

    @Column(name = "customer_code")
    private String customerCode;

    @Column(name = "namesake_count")
    private Integer namesakeCount;

    @Column(name = "bulk_signup_warning")
    private Boolean bulkSignupWarning;

    @Column(name = "postcode")
    private String postcode;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner", fetch = FetchType.EAGER)
    private Set<Pet> pets;

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = normalizeAddress(address);
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
        this.email = normalizeEmail(email);
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

    public String getPostcode() {
        return this.postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    /**
     * Normalize an email to its canonical stored form, which is lower-cased. This is the single
     * source of truth for how an email is persisted, so inputs that differ only in letter case
     * are stored identically. Applied by {@link #setEmail(String)} so every persistence path
     * (create and update, mapper or hand-copied) stores the same canonical value.
     *
     * @param email the raw email, possibly {@code null}
     * @return the lower-cased form, or {@code null} if the input was {@code null}
     */
    public static String normalizeEmail(String email) {
        return email == null ? null : email.toLowerCase();
    }

    /**
     * Normalize a telephone to its canonical E.164 stored form. This is the single source of
     * truth for how a telephone is compared and persisted, so inputs that differ only in
     * formatting (spaces, dashes, brackets) are treated as the same number.
     *
     * <p>The rules are:
     * <ul>
     *   <li>spaces, dashes and brackets are stripped;</li>
     *   <li>a leading {@code '+'} and its country code are kept as given;</li>
     *   <li>otherwise country code {@code '+61'} is assumed and a single leading {@code '0'}
     *       is dropped from the national digits;</li>
     *   <li>the result must have 8 to 15 digits after the {@code '+'};</li>
     *   <li>for a recognized country code the national number (the digits after the country
     *       code) must have the length that code requires: {@code '+61'} requires 9 national
     *       digits and {@code '+1'} requires 10.</li>
     * </ul>
     * For example {@code "0412 345 678"} becomes {@code "+61412345678"}. Already-normalized
     * values are returned unchanged.
     *
     * @param telephone the raw telephone, possibly {@code null}
     * @return the E.164 form, or {@code null} if the input is {@code null} or cannot form a
     *     valid E.164 number
     */
    public static String normalizeTelephone(String telephone) {
        if (telephone == null) {
            return null;
        }
        String trimmed = telephone.trim();
        boolean hasCountryCode = trimmed.startsWith("+");
        // Strip spaces, dashes and brackets; a leading '+' (if any) is left in place.
        String stripped = trimmed.replaceAll("[\\s\\-()]", "");
        if (hasCountryCode) {
            stripped = stripped.substring(1);
        }
        if (!stripped.matches("\\d+")) {
            return null;
        }
        String digits;
        if (hasCountryCode) {
            digits = stripped;
        } else {
            if (stripped.startsWith("0")) {
                stripped = stripped.substring(1);
            }
            digits = "61" + stripped;
        }
        if (digits.length() < 8 || digits.length() > 15) {
            return null;
        }
        if (!hasValidNationalNumberLength(digits)) {
            return null;
        }
        return "+" + digits;
    }

    /**
     * Required national-number length (the count of digits after the country code) for each
     * recognized country code, longest country code first so the prefix match is unambiguous.
     */
    private static final Map<String, Integer> NATIONAL_NUMBER_LENGTHS = new LinkedHashMap<>();
    static {
        NATIONAL_NUMBER_LENGTHS.put("61", 9);
        NATIONAL_NUMBER_LENGTHS.put("1", 10);
    }

    /**
     * Check that the national number of an E.164 digit string (no leading {@code '+'}) has the
     * length its country code requires. A country code with no registered length places no
     * additional constraint and is accepted.
     *
     * @param digits the E.164 digits without the leading {@code '+'}
     * @return {@code true} if the national-number length is valid for the country code
     */
    private static boolean hasValidNationalNumberLength(String digits) {
        for (Map.Entry<String, Integer> entry : NATIONAL_NUMBER_LENGTHS.entrySet()) {
            String countryCode = entry.getKey();
            if (digits.startsWith(countryCode)) {
                return digits.length() - countryCode.length() == entry.getValue();
            }
        }
        return true;
    }

    /**
     * Common street-type abbreviations expanded by {@link #normalizeAddress(String)}, keyed by the
     * upper-cased abbreviation.
     */
    private static final Map<String, String> ADDRESS_ABBREVIATIONS = Map.of(
        "ST", "STREET",
        "RD", "ROAD",
        "AVE", "AVENUE");

    /**
     * Normalize an address to its canonical stored form. Leading and trailing whitespace is
     * trimmed, internal runs of whitespace are collapsed to a single space, the text is upper-cased,
     * and common street-type abbreviations are expanded as whole words ({@code ST}->{@code STREET},
     * {@code RD}->{@code ROAD}, {@code AVE}->{@code AVENUE}). This is the single source of truth for
     * how an address is persisted and compared, so inputs that differ only in letter case, spacing
     * or abbreviation are treated as the same address. Applied by {@link #setAddress(String)} so
     * every persistence path (create and update, mapper or hand-copied) stores the same canonical
     * value. The transform is idempotent: a normalized address normalizes to itself.
     *
     * @param address the raw address, possibly {@code null}
     * @return the normalized form, or {@code null} if the input was {@code null}
     */
    public static String normalizeAddress(String address) {
        if (address == null) {
            return null;
        }
        String collapsed = address.trim().replaceAll("\\s+", " ").toUpperCase();
        if (collapsed.isEmpty()) {
            return collapsed;
        }
        String[] tokens = collapsed.split(" ");
        StringBuilder normalized = new StringBuilder(collapsed.length());
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) {
                normalized.append(' ');
            }
            normalized.append(ADDRESS_ABBREVIATIONS.getOrDefault(tokens[i], tokens[i]));
        }
        return normalized.toString();
    }

    /**
     * Normalize a household field (such as last name or address) to its canonical form for
     * duplicate-household comparison. Leading and trailing whitespace is trimmed, internal runs
     * of whitespace are collapsed to a single space, and the result is lower-cased, so inputs
     * that differ only in letter case or spacing are treated as the same value.
     *
     * @param value the raw field value, possibly {@code null}
     * @return the normalized form, or {@code null} if the input was {@code null}
     */
    public static String normalizeHousehold(String value) {
        if (value == null) {
            return null;
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    /**
     * Compute the stable identifier shared by all owners in the same household, i.e. with the same
     * last name (compared case-insensitively with collapsed whitespace, see
     * {@link #normalizeHousehold(String)}) and the same normalized address (see
     * {@link #normalizeAddress(String)}). The value is deterministic in those two fields, so two
     * owners who share a household are assigned the same id. It is formatted {@code 'HH-<HEX12>'}
     * where {@code HEX12} is the first twelve upper-cased hex characters of the SHA-256 digest of
     * the normalized last name and address.
     *
     * @param lastName the owner's last name, possibly {@code null}
     * @param address the owner's address, possibly {@code null}
     * @return the household id, or {@code null} if either field is {@code null}
     */
    public static String householdId(String lastName, String address) {
        String normalizedLastName = normalizeHousehold(lastName);
        String normalizedAddress = normalizeAddress(address);
        if (normalizedLastName == null || normalizedAddress == null) {
            return null;
        }
        String key = normalizedLastName + "\n" + normalizedAddress;
        return "HH-" + sha256Hex(key).substring(0, 12).toUpperCase();
    }

    /**
     * Compute the full lower-case hex SHA-256 digest of the UTF-8 bytes of {@code value}. This is the
     * shared primitive behind every hash-derived owner value (the household id and the customer code),
     * so they all agree on algorithm and encoding.
     *
     * @param value the string to digest, never {@code null}
     * @return the 64-character lower-case hex digest
     */
    private static String sha256Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is a standard algorithm required to be present on every JVM.
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * Build an owner's {@code customerCode}, the stable owner identity assigned once when the owner is
     * created. It is formatted {@code '<REGION>-<HASH8>'} where:
     * <ul>
     *   <li>{@code REGION} is the region code derived from the postcode (see
     *       {@link #regionForPostcode(String)}), or {@code 'UNKNOWN'} when the postcode is absent or
     *       in no known range, and</li>
     *   <li>{@code HASH8} is the first eight upper-cased hex characters of the SHA-256 digest of the
     *       normalized telephone (see {@link #normalizeTelephone(String)}) concatenated with the last
     *       name.</li>
     * </ul>
     * The value is deterministic in the postcode, telephone and last name (e.g.
     * {@code 'NSW-1A2B3C4D'}); it carries no sequence number.
     *
     * @param postcode the owner's postcode, possibly {@code null}
     * @param telephone the owner's raw telephone, possibly {@code null}
     * @param lastName the owner's last name, possibly {@code null}
     * @return the customer code, or {@code null} if the telephone or last name is {@code null}
     */
    public static String customerCode(String postcode, String telephone, String lastName) {
        if (telephone == null || lastName == null) {
            return null;
        }
        String region = regionForPostcode(postcode);
        if (region == null) {
            region = "UNKNOWN";
        }
        String normalizedTelephone = normalizeTelephone(telephone);
        String key = (normalizedTelephone == null ? "" : normalizedTelephone) + lastName;
        return region + "-" + sha256Hex(key).substring(0, 8).toUpperCase();
    }

    /**
     * Compute an owner's {@code identityKey}, the single derived value through which all
     * duplicate detection is now expressed. It consolidates the three formerly separate
     * telephone, email and household duplicate checks into one key, formatted as
     * {@code normalizedTelephone + '|' + (email or empty) + '|' + householdId}:
     * <ul>
     *   <li>the normalized telephone (see {@link #normalizeTelephone(String)}),</li>
     *   <li>the normalized email (see {@link #normalizeEmail(String)}), or the empty string
     *       when no email is present, and</li>
     *   <li>the household id (see {@link #householdId(String, String)}).</li>
     * </ul>
     * A {@code null} component is rendered as the empty string. Two owners are the same identity
     * only when their <em>whole</em> keys are equal; because the telephone is part of the key, two
     * members of the same household with different telephones have different keys.
     *
     * @param telephone the owner's raw telephone, possibly {@code null}
     * @param email the owner's email, possibly {@code null}
     * @param lastName the owner's last name, possibly {@code null}
     * @param address the owner's address, possibly {@code null}
     * @return the identity key, never {@code null}
     */
    public static String identityKey(String telephone, String email, String lastName, String address) {
        String normalizedTelephone = normalizeTelephone(telephone);
        String normalizedEmail = normalizeEmail(email);
        String household = householdId(lastName, address);
        return (normalizedTelephone == null ? "" : normalizedTelephone)
            + "|" + (normalizedEmail == null ? "" : normalizedEmail)
            + "|" + (household == null ? "" : household);
    }

    /**
     * Build an owner's {@code membershipNumber}, the stable membership identifier derived from the
     * owner's own persisted fields. It is formatted {@code '<customerCode>-M<YY>'} where {@code YY}
     * is the last two digits of the registration date's year (e.g. {@code 'NSW-1A2B3C4D-M26'}). The
     * value is deterministic in those two fields.
     *
     * @param customerCode the owner's customer code, possibly {@code null}
     * @param registrationDate the owner's registration date, possibly {@code null}
     * @return the membership number, or {@code null} if either field is {@code null}
     */
    public static String membershipNumber(String customerCode, LocalDate registrationDate) {
        if (customerCode == null || registrationDate == null) {
            return null;
        }
        return String.format("%s-M%02d", customerCode, registrationDate.getYear() % 100);
    }

    /**
     * Build an owner's {@code membershipLevel}, the derived numeric level assigned on create. The
     * value starts at 1, gains 1 when an email is present and gains 1 when {@code namesakeCount} is 0,
     * and is capped at 3 (level 4 is reserved for tenure). The value is deterministic in those two
     * fields.
     *
     * @param namesakeCount the owner's namesake count, possibly {@code null}
     * @param email the owner's email, possibly {@code null}
     * @return the membership level, an integer from 1 to 3
     */
    public static int membershipLevel(Integer namesakeCount, String email) {
        int level = 1;
        if (email != null && !email.isEmpty()) {
            level++;
        }
        if (namesakeCount != null && namesakeCount == 0) {
            level++;
        }
        return Math.min(level, 3);
    }

    /**
     * Fixed city-to-region table used by {@link #regionForCity(String)} to resolve a city's region
     * for postcode validation, keyed by the exact city name.
     */
    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW",
        "Melbourne", "VIC",
        "Brisbane", "QLD");

    /**
     * Derive an owner's {@code locality}, the canonical region for the owner, from the {@code REGION}
     * segment of the owner's {@code customerCode} (see {@link #customerCode(String, String, String)}).
     * Because the customer code's region is itself derived from the postcode, the locality now shares
     * the owner's region-and-hash identity: it is the text before the first {@code '-'} of the customer
     * code. When the owner has no customer code the locality is {@code 'UNKNOWN'}.
     *
     * @param customerCode the owner's customer code, possibly {@code null}
     * @return the canonical region string, or {@code 'UNKNOWN'} when no customer code is present
     */
    public static String locality(String customerCode) {
        if (customerCode == null) {
            return "UNKNOWN";
        }
        int dash = customerCode.indexOf('-');
        return dash < 0 ? customerCode : customerCode.substring(0, dash);
    }

    /**
     * Resolve the canonical region for a city from the fixed {@link #CITY_REGION} table. A city not
     * in the table (including {@code null}) resolves to {@code 'UNKNOWN'}.
     *
     * @param city the owner's city, possibly {@code null}
     * @return the matching region, or {@code 'UNKNOWN'} when the city is not in the table
     */
    private static String regionForCity(String city) {
        return CITY_REGION.getOrDefault(city, "UNKNOWN");
    }

    /**
     * Fixed region-to-postcode-range table used by {@link #isPostcodeValidForCity(String, String)}
     * and {@link #regionForPostcode(String)}, keyed by the canonical region. Each entry is the
     * inclusive low and high bound of the region's valid 4-digit postcodes
     * ({@code NSW} 2000-2099, {@code VIC} 3000-3099, {@code QLD} 4000-4099). A region not in this
     * table (including {@code 'UNKNOWN'}) places no range constraint.
     */
    private static final Map<String, int[]> REGION_POSTCODE_RANGES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /**
     * Resolve the canonical region whose postcode range contains the given postcode, using the fixed
     * {@link #REGION_POSTCODE_RANGES} table. A {@code null} postcode, one that is not exactly four
     * digits, or one that falls in no known range resolves to {@code null}.
     *
     * @param postcode the owner's postcode, possibly {@code null}
     * @return the matching region, or {@code null} when no range contains the postcode
     */
    private static String regionForPostcode(String postcode) {
        if (postcode == null || !postcode.matches("\\d{4}")) {
            return null;
        }
        int value = Integer.parseInt(postcode);
        for (Map.Entry<String, int[]> entry : REGION_POSTCODE_RANGES.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Check whether a postcode is valid for an owner's city. Postcode is optional, so a
     * {@code null} or empty postcode is always valid. When present it must be exactly four
     * digits, and when the city's region (from the fixed city-to-region table) has a known postcode
     * range the value must fall within that inclusive range. A city with no known region accepts
     * any four-digit postcode. This is the single source of
     * truth for postcode validity, shared by the DTO validation and any persistence path.
     *
     * @param postcode the owner's postcode, possibly {@code null}
     * @param city the owner's city, possibly {@code null}
     * @return {@code true} if the postcode is absent or valid for the city, {@code false} otherwise
     */
    public static boolean isPostcodeValidForCity(String postcode, String city) {
        if (postcode == null || postcode.isEmpty()) {
            return true;
        }
        if (!postcode.matches("\\d{4}")) {
            return false;
        }
        int[] range = REGION_POSTCODE_RANGES.get(regionForCity(city));
        if (range == null) {
            return true;
        }
        int value = Integer.parseInt(postcode);
        return value >= range[0] && value <= range[1];
    }

    /**
     * Compute an owner's {@code checkDigit}, a single Luhn check digit (0-9) over the digits
     * contained in the {@code customerCode}. Non-digit characters in the code are ignored. The
     * value is deterministic in the customer code and is the standard Luhn check digit: each
     * digit is weighted right-to-left, every second digit (starting with the rightmost) is
     * doubled with digit sums above nine reduced by nine, and the check digit is the amount that
     * brings the weighted sum up to the next multiple of ten.
     *
     * @param customerCode the owner's customer code, possibly {@code null}
     * @return the Luhn check digit from 0 to 9, or {@code null} if the customer code is {@code null}
     */
    public static Integer checkDigit(String customerCode) {
        if (customerCode == null) {
            return null;
        }
        int sum = 0;
        boolean dbl = true;
        for (int i = customerCode.length() - 1; i >= 0; i--) {
            char c = customerCode.charAt(i);
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
     * Derive an owner's {@code contactPreference}, the preferred channel for contacting the owner.
     * The value is {@code 'EMAIL'} when an email is present and {@code 'PHONE'} otherwise. The value
     * is deterministic in the email.
     *
     * @param email the owner's email, possibly {@code null}
     * @return {@code 'EMAIL'} when an email is present, otherwise {@code 'PHONE'}
     */
    public static String contactPreference(String email) {
        return (email != null && !email.isEmpty()) ? "EMAIL" : "PHONE";
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
