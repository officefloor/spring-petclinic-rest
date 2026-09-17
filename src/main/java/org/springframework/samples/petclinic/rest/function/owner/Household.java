package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;

import org.springframework.samples.petclinic.model.Owner;

/**
 * The single definition of an owner "household": owners sharing the same last name and
 * address. The last name is compared case-insensitively with runs of whitespace collapsed
 * to a single space; the address is compared in its canonical {@link AddressNormalizer}
 * form, so both membership and the identifier use the same normalized address that is
 * stored and returned. Provides the membership test and the stable identifier shared by
 * the household.
 *
 * <p>The identifier is derived deterministically from the normalized last name and
 * address, so every owner in the same household resolves to the same value regardless of
 * creation order.
 *
 * @see AssignHousehold assigns the shared identifier to the joining owners.
 * @see IdentityKey folds the household identifier into the owner's duplicate-detection key.
 */
final class Household {

    private Household() {
    }

    /** Whether {@code owner} belongs to the household keyed by {@code lastName} and {@code address}. */
    static boolean matches(Owner owner, String lastName, String address) {
        return normalizeName(lastName).equals(normalizeName(owner.getLastName()))
                && AddressNormalizer.normalize(address).equals(AddressNormalizer.normalize(owner.getAddress()));
    }

    /** The stable identifier shared by every owner in the household keyed by {@code lastName} and {@code address}. */
    static String id(String lastName, String address) {
        String key = normalizeName(lastName) + "\n" + AddressNormalizer.normalize(address);
        return UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8)).toString();
    }

    /** A last name trimmed and lower-cased, with internal whitespace runs collapsed to a single space. */
    private static String normalizeName(String value) {
        return value == null ? "" : value.strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
