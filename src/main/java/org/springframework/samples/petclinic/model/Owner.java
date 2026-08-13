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
    @Column(name = "address")
    @NotEmpty
    private String address;

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
     * The owner's numeric membership level. Starts at 1, adds 1 when an email is
     * present, adds 1 when {@code namesakeCount} is 0, and adds 1 when the owner's
     * tenure exceeds 365 days. The tenure point is the only route to level 4: because a
     * newly created owner has zero tenure, a new owner never exceeds level 3 (a new
     * owner with an email, a {@code namesakeCount} of 0 and a 3-member household is
     * level 3, not 4).
     */
    @Transient
    public int getMembershipLevel() {
        int level = 1;
        if (this.email != null && !this.email.isBlank()) {
            level++;
        }
        if (this.namesakeCount != null && this.namesakeCount == 0) {
            level++;
        }
        if (getTenureDays() > 365) {
            level++;
        }
        return level;
    }

    /**
     * The owner's tenure in whole days: the span from {@code registrationDate} to today.
     * Zero when no registration date is set, and zero for an owner registered today, so
     * the tenure point of {@link #getMembershipLevel()} is never earned at creation.
     */
    @Transient
    public long getTenureDays() {
        if (this.registrationDate == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(this.registrationDate, LocalDate.now());
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
