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
    @Pattern(regexp = "^(MR|MRS|MS|DR)$", message = "Title must be one of MR, MRS, MS or DR")
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
    @Email
    private String email;

    @Column(name = "registration_date")
    private LocalDate registrationDate;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "customer_code")
    private String customerCode;

    @Column(name = "namesake_count")
    private Integer namesakeCount;

    @Column(name = "household_count")
    private Integer householdCount;

    @Column(name = "membership_level")
    private Integer membershipLevel;

    @Column(name = "bulk_signup_warning")
    private Boolean bulkSignupWarning;

    @Column(name = "capacity_warning")
    private Boolean capacityWarning;

    @Column(name = "postcode")
    private String postcode;

    @Column(name = "possible_duplicate")
    private Boolean possibleDuplicate;

    @Column(name = "possible_duplicate_of")
    private Integer possibleDuplicateOf;

    @Column(name = "deleted")
    private Boolean deleted = false;

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
        this.address = normalizeAddress(address);
    }

    public String getAddressLine1() {
        return this.addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = normalizeAddress(addressLine1);
    }

    public String getAddressLine2() {
        return this.addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = normalizeAddress(addressLine2);
    }

    /**
     * Populate the persisted composed {@link #address} from the structured and flat address fields
     * just before the owner is written. Invoked by JPA as a lifecycle callback on insert and update
     * so the stored {@code address} is always the canonical composition (see
     * {@link #composeAddress(String, String, String)}): the structured {@code addressLine1} /
     * {@code addressLine2} when present, otherwise the flat value already held in {@code address}.
     * The address setters stay trivial — each just stores its own normalized field — and the
     * derivation lives in one place that runs regardless of the order the fields were set.
     */
    @PrePersist
    @PreUpdate
    void composeStoredAddress() {
        this.address = composeAddress(this.addressLine1, this.addressLine2, this.address);
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

    /**
     * Resolve this owner's current <em>primary identifier</em>: the single external-facing identity an
     * owner is known by. Today that is the {@link #getCustomerCode() customerCode}; when the customer
     * code is later unified into a {@code memberId}, this becomes the sole place that choice is made, so
     * every consumer (such as the {@code OWNER_CREATED} audit event) automatically carries the new
     * identifier without further change.
     *
     * @return the owner's current primary identifier, or {@code null} when none has been assigned
     */
    public String primaryIdentifier() {
        return this.customerCode;
    }

    public Integer getNamesakeCount() {
        return this.namesakeCount;
    }

    public Integer getHouseholdCount() {
        return this.householdCount;
    }

    public void setHouseholdCount(Integer householdCount) {
        this.householdCount = householdCount;
    }

    public void setNamesakeCount(Integer namesakeCount) {
        this.namesakeCount = namesakeCount;
    }

    /**
     * The owner's stored {@code membershipLevel}, the create-time membership level after the
     * household level ceiling has been applied (see
     * {@link org.springframework.samples.petclinic.service.ClinicService#saveOwner(Owner, boolean)}).
     * A {@code null} stored value means no capped level was recorded, in which case the level is
     * recomputed from {@code membershipPoints}.
     */
    public Integer getMembershipLevel() {
        return this.membershipLevel;
    }

    public void setMembershipLevel(Integer membershipLevel) {
        this.membershipLevel = membershipLevel;
    }

    public Boolean getBulkSignupWarning() {
        return this.bulkSignupWarning;
    }

    public void setBulkSignupWarning(Boolean bulkSignupWarning) {
        this.bulkSignupWarning = bulkSignupWarning;
    }

    public Boolean getCapacityWarning() {
        return this.capacityWarning;
    }

    public void setCapacityWarning(Boolean capacityWarning) {
        this.capacityWarning = capacityWarning;
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

    public Boolean getDeleted() {
        return this.deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    /**
     * Whether this owner has been soft-deleted. A soft-deleted owner keeps its row (so it is still
     * readable via GET) but is treated as absent by the create-time duplicate and identity checks. A
     * {@code null} stored value is read as not deleted.
     *
     * @return {@code true} if the owner is flagged deleted, {@code false} otherwise
     */
    public boolean isDeleted() {
        return Boolean.TRUE.equals(this.deleted);
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
     * Disposable email domains that are not accepted for an owner. Compared case-insensitively
     * against the domain part of the owner's email (see {@link #isDisposableEmailDomain(String)}).
     */
    private static final Set<String> DISPOSABLE_EMAIL_DOMAINS = Set.of(
        "mailinator.com",
        "tempmail.com",
        "guerrillamail.com");

    /**
     * Check whether an email's domain is on the disposable-domain blocklist. The domain is the text
     * after the last {@code '@'}, compared case-insensitively against {@link #DISPOSABLE_EMAIL_DOMAINS}.
     * This is the single source of truth for which email domains are rejected, shared by any
     * persistence path. A {@code null} or empty email, or one with no {@code '@'}, is not disposable.
     *
     * @param email the owner's email, possibly {@code null}
     * @return {@code true} if the email's domain is on the blocklist, {@code false} otherwise
     */
    public static boolean isDisposableEmailDomain(String email) {
        if (email == null) {
            return false;
        }
        int at = email.lastIndexOf('@');
        if (at < 0) {
            return false;
        }
        String domain = email.substring(at + 1).toLowerCase();
        return DISPOSABLE_EMAIL_DOMAINS.contains(domain);
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
     * Format a stored E.164 telephone for human display: the country code, a space, then the
     * national digits grouped left-to-right in blocks of three separated by spaces (e.g.
     * {@code "+61412345678"} becomes {@code "+61 412 345 678"}). The country code is identified
     * from the recognized {@link #NATIONAL_NUMBER_LENGTHS} table (longest prefix first); when no
     * recognized country code matches, the digits after the {@code '+'} are grouped in threes with
     * no separate country-code segment. This is a display-only transform; the raw E.164 value is
     * kept unchanged elsewhere.
     *
     * @param telephone the stored E.164 telephone, possibly {@code null}
     * @return the human-formatted telephone, or {@code null} when the input is {@code null}; an
     *     input that is not a well-formed E.164 number ({@code '+'} followed by digits) is returned
     *     unchanged
     */
    public static String telephoneDisplay(String telephone) {
        if (telephone == null) {
            return null;
        }
        if (!telephone.matches("\\+\\d+")) {
            return telephone;
        }
        String digits = telephone.substring(1);
        for (String countryCode : NATIONAL_NUMBER_LENGTHS.keySet()) {
            if (digits.startsWith(countryCode)) {
                return "+" + countryCode + " " + groupDigits(digits.substring(countryCode.length()));
            }
        }
        return "+" + groupDigits(digits);
    }

    /**
     * Group a run of digits left-to-right into blocks of three separated by single spaces, with a
     * trailing block of one or two digits when the length is not a multiple of three (e.g.
     * {@code "412345678"} becomes {@code "412 345 678"}).
     *
     * @param digits the national digits
     * @return the space-grouped digits
     */
    private static String groupDigits(String digits) {
        StringBuilder grouped = new StringBuilder(digits.length() + digits.length() / 3);
        for (int i = 0; i < digits.length(); i++) {
            if (i > 0 && i % 3 == 0) {
                grouped.append(' ');
            }
            grouped.append(digits.charAt(i));
        }
        return grouped.toString();
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
     * Compose an owner's canonical {@code address} from the two accepted input forms, preferring the
     * structured fields over the flat value. When a non-blank {@code addressLine1} is present the
     * result is that line, with a single space and {@code addressLine2} appended when the second line
     * is present; otherwise the flat {@code address} value is used. All three arguments are expected
     * to already be normalized (see {@link #normalizeAddress(String)}). This is the single source of
     * truth for how an owner's address is read, so every consumer (the stored value and the response)
     * sees the structured fields when present and the flat value as the backward-compatible fallback.
     *
     * @param addressLine1 the normalized first structured line, possibly {@code null} or blank
     * @param addressLine2 the normalized optional second structured line, possibly {@code null} or blank
     * @param flatAddress the normalized flat address, possibly {@code null}
     * @return the composed address, or the flat value / {@code null} when no structured line is present
     */
    public static String composeAddress(String addressLine1, String addressLine2, String flatAddress) {
        if (isPresent(addressLine1)) {
            if (isPresent(addressLine2)) {
                return addressLine1 + " " + addressLine2;
            }
            return addressLine1;
        }
        return flatAddress;
    }

    /**
     * Whether an address field supplies a usable value, i.e. is non-{@code null} and not blank. This
     * is the shared "presence" rule behind both {@link #composeAddress(String, String, String)} and
     * {@link #hasAddress(String, String)}, so they agree on what counts as a supplied address field.
     *
     * @param value the address field, possibly {@code null}
     * @return {@code true} if the value is present and non-blank
     */
    private static boolean isPresent(String value) {
        return value != null && !value.isBlank();
    }

    /**
     * Check whether an owner supplies a postal address in at least one accepted form: a non-blank
     * structured {@code addressLine1}, or a non-blank flat {@code address}. This is the single source
     * of truth for the address presence rule, shared by the DTO validation and any persistence path.
     *
     * @param addressLine1 the structured first line, possibly {@code null}
     * @param address the flat address, possibly {@code null}
     * @return {@code true} if either form supplies a non-blank value, {@code false} otherwise
     */
    public static boolean hasAddress(String addressLine1, String address) {
        return isPresent(addressLine1) || isPresent(address);
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
     * {@link #normalizeHousehold(String)}) and the same postcode. The value is deterministic in
     * those two fields, so two owners who share a last name and postcode are assigned the same id
     * automatically. It is formatted {@code 'HH-<HEX12>'} where {@code HEX12} is the first twelve
     * upper-cased hex characters of the SHA-256 digest of
     * {@code normalizedLastName + '|' + postcode}.
     *
     * @param lastName the owner's last name, possibly {@code null}
     * @param postcode the owner's postcode, possibly {@code null}
     * @return the household id, or {@code null} if either field is {@code null}
     */
    public static String householdId(String lastName, String postcode) {
        String normalizedLastName = normalizeHousehold(lastName);
        if (normalizedLastName == null || postcode == null) {
            return null;
        }
        String key = normalizedLastName + "|" + postcode;
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
     * duplicate detection is now expressed. It is the lower-case hex SHA-256 digest (see
     * {@link #sha256Hex(String)}) of the three identity components joined with {@code '|'}:
     * {@code normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)}, where
     * <ul>
     *   <li>the normalized telephone (see {@link #normalizeTelephone(String)}),</li>
     *   <li>the lower-cased email (see {@link #normalizeEmail(String)}), or the empty string
     *       when no email is present, and</li>
     *   <li>the Soundex code of the last name (see {@link #soundex(String)}).</li>
     * </ul>
     * A {@code null} component is rendered as the empty string before hashing. Two owners are the
     * same identity only when their <em>whole</em> keys are equal; because the telephone is part of
     * the key, two members of the same household (same last name and postcode) with different
     * telephones have different keys and are not a hard duplicate.
     *
     * @param telephone the owner's raw telephone, possibly {@code null}
     * @param email the owner's email, possibly {@code null}
     * @param lastName the owner's last name, possibly {@code null}
     * @return the 64-character lower-case hex identity key, never {@code null}
     */
    public static String identityKey(String telephone, String email, String lastName) {
        String normalizedTelephone = normalizeTelephone(telephone);
        String normalizedEmail = normalizeEmail(email);
        String lastNameSoundex = soundex(lastName);
        String raw = (normalizedTelephone == null ? "" : normalizedTelephone)
            + "|" + (normalizedEmail == null ? "" : normalizedEmail)
            + "|" + (lastNameSoundex == null ? "" : lastNameSoundex);
        return sha256Hex(raw);
    }

    /** Soundex digit for each consonant; vowels, {@code H}, {@code W} and {@code Y} map to '0'. */
    private static final Map<Character, Character> SOUNDEX_CODES = new HashMap<>();
    static {
        for (char c : "BFPV".toCharArray()) {
            SOUNDEX_CODES.put(c, '1');
        }
        for (char c : "CGJKQSXZ".toCharArray()) {
            SOUNDEX_CODES.put(c, '2');
        }
        for (char c : "DT".toCharArray()) {
            SOUNDEX_CODES.put(c, '3');
        }
        SOUNDEX_CODES.put('L', '4');
        for (char c : "MN".toCharArray()) {
            SOUNDEX_CODES.put(c, '5');
        }
        SOUNDEX_CODES.put('R', '6');
    }

    /**
     * Compute the American Soundex code of a name: the upper-cased first letter followed by three
     * digits encoding the following consonants ({@code B,F,P,V}->1; {@code C,G,J,K,Q,S,X,Z}->2;
     * {@code D,T}->3; {@code L}->4; {@code M,N}->5; {@code R}->6), padded with zeros to length four.
     * Vowels ({@code A,E,I,O,U,Y}) separate codes; {@code H} and {@code W} are ignored so two
     * like-coded consonants on either side of them collapse to a single digit; adjacent letters with
     * the same code also collapse to one. Non-letters are stripped before coding. This is the single
     * source of truth for the surname component of the {@code identityKey} and the soft-match rule.
     *
     * @param name the raw name, possibly {@code null}
     * @return the four-character Soundex code, {@code null} if the input is {@code null}, or the empty
     *     string if the input contains no letters
     */
    public static String soundex(String name) {
        if (name == null) {
            return null;
        }
        String letters = name.toUpperCase().replaceAll("[^A-Z]", "");
        if (letters.isEmpty()) {
            return "";
        }
        StringBuilder code = new StringBuilder();
        code.append(letters.charAt(0));
        char previous = SOUNDEX_CODES.getOrDefault(letters.charAt(0), '0');
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            if (c == 'H' || c == 'W') {
                continue;
            }
            char digit = SOUNDEX_CODES.getOrDefault(c, '0');
            if (digit != '0' && digit != previous) {
                code.append(digit);
            }
            previous = digit;
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    /**
     * The month in which the fiscal year starts (1 July). Dates on or after this month belong to
     * the fiscal year named for the following calendar year.
     */
    private static final int FISCAL_YEAR_START_MONTH = 7;

    /**
     * Resolve the fiscal-year number for a date, where the fiscal year starts on 1 July and is
     * named for the calendar year in which it ends. A date in July&ndash;December belongs to the
     * fiscal year of the next calendar year; a date in January&ndash;June belongs to the fiscal
     * year of its own calendar year.
     *
     * @param date the date to resolve, never {@code null}
     * @return the fiscal-year number (a four-digit calendar year)
     */
    private static int fiscalYearNumber(LocalDate date) {
        return date.getMonthValue() >= FISCAL_YEAR_START_MONTH ? date.getYear() + 1 : date.getYear();
    }

    /**
     * Compute an owner's {@code fiscalYear}, the fiscal year of a date formatted {@code 'FY<YY>'}
     * where {@code YY} is the last two digits of the fiscal-year number (see
     * {@link #fiscalYearNumber(LocalDate)}). The fiscal year starts on 1 July, so a date of
     * {@code 2026-08-01} yields {@code 'FY27'} and {@code 2026-03-01} yields {@code 'FY26'}.
     *
     * @param date the date to derive the fiscal year from, possibly {@code null}
     * @return the fiscal year, or {@code null} if {@code date} is {@code null}
     */
    public static String fiscalYear(LocalDate date) {
        if (date == null) {
            return null;
        }
        return String.format("FY%02d", fiscalYearNumber(date) % 100);
    }

    /**
     * Build an owner's {@code membershipNumber}, the stable membership identifier derived from the
     * owner's own persisted fields. It is formatted {@code '<customerCode>-M<YY>'} where {@code YY}
     * is the last two digits of the registration date's fiscal year (see
     * {@link #fiscalYear(LocalDate)}), e.g. {@code 'NSW-1A2B3C4D-M27'}. The value is deterministic
     * in those two fields.
     *
     * @param customerCode the owner's customer code, possibly {@code null}
     * @param registrationDate the owner's registration date, possibly {@code null}
     * @return the membership number, or {@code null} if either field is {@code null}
     */
    public static String membershipNumber(String customerCode, LocalDate registrationDate) {
        if (customerCode == null || registrationDate == null) {
            return null;
        }
        return String.format("%s-M%02d", customerCode, fiscalYearNumber(registrationDate) % 100);
    }

    /**
     * Compute an owner's {@code membershipPoints}, the derived points score assigned on create. The
     * score starts at 0, gains 2 when an email is present, gains 1 when {@code namesakeCount} is 0,
     * gains 2 when the owner belongs to a household of 3 or more members (see {@code householdCount}),
     * and gains 3 when the owner's tenure is more than one fiscal year (the tenure being the number
     * of elapsed fiscal years from {@code registrationDate} to today, see
     * {@link #fiscalYear(LocalDate)}). Because a newly created owner has zero tenure, the tenure
     * points accrue only once the owner has been a member for more than a full fiscal year.
     *
     * @param namesakeCount the owner's namesake count, possibly {@code null}
     * @param email the owner's email, possibly {@code null}
     * @param householdCount the number of members in the owner's household, possibly {@code null}
     * @param registrationDate the owner's registration date, possibly {@code null}
     * @return the membership points, a non-negative integer
     */
    public static int membershipPoints(Integer namesakeCount, String email, Integer householdCount,
            LocalDate registrationDate) {
        int points = 0;
        if (email != null && !email.isEmpty()) {
            points += 2;
        }
        if (namesakeCount != null && namesakeCount == 0) {
            points += 1;
        }
        if (householdCount != null && householdCount >= 3) {
            points += 2;
        }
        if (registrationDate != null
            && fiscalYearNumber(LocalDate.now()) - fiscalYearNumber(registrationDate) > 1) {
            points += 3;
        }
        return points;
    }

    /**
     * Map an owner's {@code membershipPoints} to its {@code membershipLevel}: level 1 for 0-1 points,
     * level 2 for 2-3, level 3 for 4-5 and level 4 for 6 or more.
     *
     * @param membershipPoints the owner's membership points
     * @return the membership level, an integer from 1 to 4
     */
    public static int membershipLevel(int membershipPoints) {
        if (membershipPoints >= 6) {
            return 4;
        }
        if (membershipPoints >= 4) {
            return 3;
        }
        if (membershipPoints >= 2) {
            return 2;
        }
        return 1;
    }

    /**
     * Resolve this owner's effective {@code membershipLevel}: the capped value stored on the owner
     * when present (the create-time level after the household ceiling has been applied, see
     * {@link #getMembershipLevel()}), otherwise the level recomputed from the owner's membership
     * points (see {@link #membershipLevel(int)} and
     * {@link #membershipPoints(Integer, String, Integer, LocalDate)}). This is the single source of
     * truth for how a stored level is read back, deferred to by both the create-time household-ceiling
     * computation and the read-side DTO mapping so they always agree.
     *
     * @return the stored capped level when present, otherwise the level derived from membership points
     */
    public int effectiveMembershipLevel() {
        if (this.membershipLevel != null) {
            return this.membershipLevel;
        }
        return membershipLevel(membershipPoints(this.namesakeCount, this.email,
            this.householdCount, this.registrationDate));
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
     * Fixed region-to-timezone table used by {@link #timezone(String)} to resolve an owner's IANA
     * timezone name from its canonical region ({@code NSW}->{@code Australia/Sydney},
     * {@code VIC}->{@code Australia/Melbourne}, {@code QLD}->{@code Australia/Brisbane}). A region
     * not in this table (including {@code 'UNKNOWN'}) has no timezone.
     */
    private static final Map<String, String> REGION_TIMEZONE = Map.of(
        "NSW", "Australia/Sydney",
        "VIC", "Australia/Melbourne",
        "QLD", "Australia/Brisbane");

    /**
     * Derive an owner's {@code timezone}, the IANA timezone name for the owner's locality/region,
     * from the fixed {@link #REGION_TIMEZONE} table ({@code NSW}->{@code Australia/Sydney},
     * {@code VIC}->{@code Australia/Melbourne}, {@code QLD}->{@code Australia/Brisbane}). The region
     * is the owner's {@code locality} (see {@link #locality(String)}), itself the {@code REGION}
     * segment of the customer code. A region not in the table (including {@code 'UNKNOWN'}) has no
     * timezone and yields {@code null}.
     *
     * @param customerCode the owner's customer code, possibly {@code null}
     * @return the IANA timezone name, or {@code null} when the owner's region has no mapped timezone
     */
    public static String timezone(String customerCode) {
        return REGION_TIMEZONE.get(locality(customerCode));
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

    /**
     * Compose an owner's {@code salutation} from the optional honorific title and the last name.
     * When a title is present the result is the title and the last name separated by a single space
     * (e.g. {@code 'DR Franklin'}); when no title is given the result is just the last name. The
     * value is deterministic in those two fields.
     *
     * @param title the owner's honorific title, possibly {@code null} or blank
     * @param lastName the owner's last name, possibly {@code null}
     * @return the composed salutation
     */
    public static String salutation(String title, String lastName) {
        if (title == null || title.isBlank()) {
            return lastName;
        }
        return title + " " + lastName;
    }

    /**
     * Derive an owner's {@code ageBand}, the band the owner's age falls into on the registration
     * date. The age in whole years is the number of completed years between {@code birthDate} and
     * {@code registrationDate}, and the band is {@code 'MINOR'} when under 18, {@code 'ADULT'} when
     * 18 to 64 inclusive, and {@code 'SENIOR'} when 65 or older. The value is deterministic in those
     * two fields and is only defined when both are present.
     *
     * @param birthDate the owner's date of birth, possibly {@code null}
     * @param registrationDate the owner's registration date, possibly {@code null}
     * @return {@code 'MINOR'}, {@code 'ADULT'} or {@code 'SENIOR'}, or {@code null} when either date
     *     is {@code null}
     */
    public static String ageBand(LocalDate birthDate, LocalDate registrationDate) {
        if (birthDate == null || registrationDate == null) {
            return null;
        }
        int age = Period.between(birthDate, registrationDate).getYears();
        if (age < 18) {
            return "MINOR";
        }
        if (age < 65) {
            return "ADULT";
        }
        return "SENIOR";
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
