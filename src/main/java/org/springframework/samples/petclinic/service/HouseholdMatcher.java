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
package org.springframework.samples.petclinic.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HexFormat;
import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Detects owners that belong to the same household. The household is keyed on the
 * normalised last name and the postcode, so any two owners sharing those values belong
 * to the same household without coordination.
 */
public final class HouseholdMatcher {

    private HouseholdMatcher() {
    }

    /**
     * A stable identifier shared by every owner in the same household: the first 12 hex
     * characters of SHA-256 over {@code normalizedLastName + '|' + postcode}.
     */
    public static String householdId(Owner owner) {
        String key = normalize(owner.getLastName()) + "|" + (owner.getPostcode() == null ? "" : owner.getPostcode());
        return hex12(key);
    }

    /**
     * True when an existing owner already belongs to this owner's household and the owner
     * has not declared shared-household membership. A declared member (sharesHousehold) is
     * allowed through rather than rejected as a household duplicate.
     */
    public static boolean isHouseholdDuplicate(Collection<Owner> owners, Owner owner, Boolean sharesHousehold) {
        return !Boolean.TRUE.equals(sharesHousehold)
            && owners.stream().anyMatch(o -> sameHousehold(o, owner));
    }

    /** The number of owners in this owner's household, counting the owner itself. */
    public static int householdSize(Collection<Owner> owners, Owner owner) {
        return (int) owners.stream().filter(o -> sameHousehold(o, owner)).count() + 1;
    }

    /**
     * The id of an owner this one may soft-duplicate, or {@code null}. A declared household
     * member is never a suspected duplicate.
     */
    public static Integer possibleDuplicateOf(Collection<Owner> owners, Owner owner, Boolean sharesHousehold) {
        return Boolean.TRUE.equals(sharesHousehold) ? null : PossibleDuplicateMatcher.matchId(owners, owner);
    }

    private static boolean sameHousehold(Owner a, Owner b) {
        return householdId(a).equals(householdId(b));
    }

    private static String hex12(String key) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest, 0, 6);
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
