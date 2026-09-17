package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Locale;
import java.util.UUID;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

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
 * @see EnsureUniqueHousehold rejects a duplicate household unless the request opts in.
 * @see AssignHousehold assigns the shared identifier to the joining owners.
 */
final class Household {

    private Household() {
    }

    /** Whether {@code owner} belongs to the household keyed by {@code lastName} and {@code address}. */
    static boolean matches(Owner owner, String lastName, String address) {
        return normalizeName(lastName).equals(normalizeName(owner.getLastName()))
                && AddressNormalizer.normalize(address).equals(AddressNormalizer.normalize(owner.getAddress()));
    }

    /** The number of owners in {@code owner}'s household — those sharing its {@code householdId} — or
     *  1 when it belongs to no shared household (its {@code householdId} is unset). */
    static int size(Owner owner, OwnerRepository ownerRepository) {
        return size(owner, ownerRepository.findAll());
    }

    /** As {@link #size(Owner, OwnerRepository)} but counting within an already-loaded set of owners,
     *  so a whole page can be sized from a single query. */
    static int size(Owner owner, Collection<Owner> owners) {
        String householdId = owner.getHouseholdId();
        if (householdId == null) {
            return 1;
        }
        int count = 0;
        for (Owner other : owners) {
            if (householdId.equals(other.getHouseholdId())) {
                count++;
            }
        }
        return count;
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
