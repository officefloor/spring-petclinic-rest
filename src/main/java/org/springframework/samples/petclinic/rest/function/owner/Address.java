package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Map;

import org.springframework.samples.petclinic.rest.escalation.AddressRequiredException;

/**
 * Normalises a postal address: trim and collapse runs of whitespace to single spaces,
 * upper-case, and expand common abbreviations token by token (ST->STREET, RD->ROAD,
 * AVE->AVENUE). A value that is blank once whitespace is collapsed is rejected, so the
 * required-field check applies to the normalised form.
 */
final class Address {

    private static final Map<String, String> ABBREVIATIONS = Map.of(
            "ST", "STREET", "RD", "ROAD", "AVE", "AVENUE");

    private Address() {
    }

    static String normalize(String address) throws AddressRequiredException {
        String collapsed = address == null ? "" : address.trim().replaceAll("\\s+", " ");
        if (collapsed.isEmpty()) {
            throw new AddressRequiredException("Address is required");
        }
        String[] tokens = collapsed.toUpperCase(Locale.ROOT).split(" ");
        StringBuilder sb = new StringBuilder(collapsed.length());
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(ABBREVIATIONS.getOrDefault(tokens[i], tokens[i]));
        }
        return sb.toString();
    }
}
