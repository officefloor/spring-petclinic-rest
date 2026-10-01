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
     *   <li>the result must have 8 to 15 digits after the {@code '+'}.</li>
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
        return "+" + digits;
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
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return "HH-" + hex.substring(0, 12).toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is a standard algorithm required to be present on every JVM.
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * Build an owner's {@code membershipNumber}, the stable membership identifier derived from the
     * owner's own persisted fields. It is formatted {@code '<customerCode>-M<YY>'} where {@code YY}
     * is the last two digits of the registration date's year (e.g. {@code 'SMI-0007-M26'}). The
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
