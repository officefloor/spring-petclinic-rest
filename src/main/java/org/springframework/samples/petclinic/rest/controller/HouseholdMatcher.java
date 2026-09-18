/*
 * Copyright 2016 the original author or authors.
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

package org.springframework.samples.petclinic.rest.controller;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.stereotype.Component;

/**
 * Groups owners into households and derives a household's stable shared identifier.
 * <p>
 * Two owners belong to the same household when they carry the same last name and the same
 * address. The last name is compared leniently: surrounding whitespace is trimmed, internal
 * runs of whitespace are collapsed to a single space and letters are compared without
 * regard to case, so values that differ only in spacing or capitalisation are treated as
 * equal. The address is compared in its canonical {@link AddressNormalizer normalized} form,
 * the same form under which it is stored and returned.
 */
@Component
public class HouseholdMatcher {

    /** Runs of whitespace collapsed to a single space before comparison. */
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    private final ClinicService clinicService;

    private final AddressNormalizer addressNormalizer;

    public HouseholdMatcher(ClinicService clinicService, AddressNormalizer addressNormalizer) {
        this.clinicService = clinicService;
        this.addressNormalizer = addressNormalizer;
    }

    /**
     * Returns the already-stored owners that share {@code candidate}'s household under the
     * lenient comparison described above. The candidate itself is not yet stored, so it is
     * never included.
     *
     * @param candidate the owner being created
     * @return existing household members, possibly empty
     */
    public List<Owner> findMembers(Owner candidate) {
        String key = householdKey(candidate);
        return clinicService.findAllOwners().stream()
            .filter(existing -> householdKey(existing).equals(key))
            .toList();
    }

    /**
     * The stable identifier for {@code candidate}'s household, derived deterministically
     * from its (normalised) last name and address so that every member resolves to the
     * same value.
     *
     * @param candidate an owner in the household
     * @return a non-blank household identifier
     */
    public String householdId(Owner candidate) {
        byte[] digest = sha256(householdKey(candidate));
        StringBuilder sb = new StringBuilder(12);
        for (int i = 0; i < 6; i++) {
            sb.append(String.format("%02X", digest[i]));
        }
        return sb.toString();
    }

    private String householdKey(Owner owner) {
        return normalizeName(owner.getLastName()) + "\n" + addressNormalizer.normalize(owner.getAddress());
    }

    private String normalizeName(String value) {
        if (value == null) {
            return "";
        }
        return WHITESPACE.matcher(value.trim()).replaceAll(" ").toLowerCase(Locale.ROOT);
    }

    private byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is required but unavailable", ex);
        }
    }
}
