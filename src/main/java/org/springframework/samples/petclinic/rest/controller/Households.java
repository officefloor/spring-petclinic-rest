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
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.stereotype.Component;

/**
 * Household identity for owners: two owners belong to the same household when their last name
 * (compared case-insensitively, with runs of whitespace collapsed) and postcode match. The
 * {@code householdId} is derived deterministically from those two fields, so owners with the
 * same last name and postcode share it automatically without any explicit linking. Provides the
 * shared lookup and identifier logic reused by the duplicate guard and by the household-size
 * queries that key off {@code householdId}.
 */
@Component
public class Households {

    /** Number of leading SHA-256 hex characters that make up a household identifier. */
    private static final int ID_LENGTH = 12;

    private final ClinicService clinicService;

    public Households(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    /**
     * Existing owners that share {@code candidate}'s household (same {@link #householdId(Owner)
     * household identifier}). Intended for a not-yet-persisted candidate, so the candidate never
     * appears in the result.
     */
    public List<Owner> findMembers(Owner candidate) {
        String householdId = householdId(candidate);
        return clinicService.findOwnerByLastNameIgnoreCase(candidate.getLastName()).stream()
            .filter(existing -> householdId.equals(householdId(existing)))
            .toList();
    }

    /**
     * The stable identifier every owner in {@code owner}'s household shares: the first
     * {@value #ID_LENGTH} hex characters of {@code SHA-256(normalizedLastName + '|' + postcode)}.
     * The same last name and postcode always yield the same id.
     */
    public String householdId(Owner owner) {
        String key = normalize(owner.getLastName()) + "|" + segment(owner.getPostcode());
        return sha256Hex(key).substring(0, ID_LENGTH);
    }

    /**
     * Canonicalize the last name for household comparison: {@code null} becomes empty, surrounding
     * whitespace is trimmed, internal whitespace runs collapse to a single space, and the result
     * is lower-cased.
     */
    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    private String segment(String value) {
        return value == null ? "" : value;
    }

    private String sha256Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required but unavailable", e);
        }
    }
}
