package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Single source of truth for the owner "household" notion used by the {@code POST /owners}
 * pipeline. A household is keyed on {@code (lastName, postcode)}: two owners belong to the same
 * household when they share a last name (compared case-insensitively with collapsed whitespace)
 * and a postcode. The household identifier is <em>deterministic</em> — derived from those two
 * fields alone — so every owner with the same last name and postcode derives the same id
 * automatically, without any explicit linking step.
 *
 * <p>Centralised here (rather than inlined in {@link AssignHousehold} or
 * {@link CheckOwnerIdentityUnique}) so the id derivation cannot silently disagree about who
 * belongs together.
 */
final class OwnerHousehold {

    private OwnerHousehold() {
    }

    /**
     * A stable identifier for the household the request names, or {@code null} when the request
     * carries no postcode (an owner without a postcode belongs to no household).
     */
    static String deriveId(OwnerFieldsDto request) {
        return idFor(request.getLastName(), request.getPostcode());
    }

    /**
     * A stable identifier for the household the existing owner belongs to, or {@code null} when it
     * has no postcode. Recomputed from {@code (lastName, postcode)} so it stays correct even for
     * records stored before household ids were assigned.
     */
    static String deriveId(Owner owner) {
        return idFor(owner.getLastName(), owner.getPostcode());
    }

    /**
     * The household id for the given last name and postcode: the first 12 hex characters
     * (upper-cased) of SHA-256 over {@code normalizedLastName + '|' + postcode}. Deterministic, so
     * every member of the same household derives the same id. Returns {@code null} when the
     * postcode is absent or blank.
     */
    static String idFor(String lastName, String postcode) {
        if (postcode == null || postcode.isBlank()) {
            return null;
        }
        String key = normalize(lastName) + "|" + postcode;
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02X", b));
            }
            return sb.substring(0, 12);
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    /** The last-name comparison form: trimmed, internal whitespace collapsed, lower-cased. */
    static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
