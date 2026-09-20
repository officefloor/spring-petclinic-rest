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
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.stereotype.Component;

/**
 * Household identity for owners: two owners belong to the same household when their last name
 * and address match once compared case-insensitively and with runs of whitespace collapsed to
 * a single space. Provides the shared lookup and stable-identifier logic reused by the
 * duplicate guard and the shared-household assignment.
 */
@Component
public class Households {

    private final ClinicService clinicService;

    public Households(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    /**
     * Existing owners that share {@code candidate}'s household (same normalized last name and
     * address). Intended for a not-yet-persisted candidate, so the candidate never appears in
     * the result.
     */
    public List<Owner> findMembers(Owner candidate) {
        String key = key(candidate);
        return clinicService.findOwnerByLastNameIgnoreCase(candidate.getLastName()).stream()
            .filter(existing -> key.equals(key(existing)))
            .toList();
    }

    /**
     * The stable identifier every owner in {@code owner}'s household shares. Derived
     * deterministically from the household key, so the same household always yields the same id.
     */
    public String stableId(Owner owner) {
        return UUID.nameUUIDFromBytes(key(owner).getBytes(StandardCharsets.UTF_8)).toString();
    }

    /**
     * Canonical household key: the normalized last name and address joined so that distinct
     * fields cannot collide.
     */
    private String key(Owner owner) {
        return normalize(owner.getLastName()) + "\n" + normalize(owner.getAddress());
    }

    /**
     * Canonicalize a value for household comparison: {@code null} becomes empty, surrounding
     * whitespace is trimmed, internal whitespace runs collapse to a single space, and the result
     * is lower-cased.
     */
    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
