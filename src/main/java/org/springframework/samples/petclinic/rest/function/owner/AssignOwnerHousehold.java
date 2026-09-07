package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Settles the household id for a new owner. When the request opted in with
 * {@code sharesHousehold} true and an existing owner shares its normalized last name and
 * address (see {@link AddressNormalizer}), the two share a household: a stable
 * {@code householdId} is stamped on the new owner and on every existing member, and the
 * household's size is recorded on all of them. Otherwise the owner is a household of one.
 *
 * <p>This step no longer rejects duplicates — a shared last name and address is not itself a
 * conflict. All duplicate detection is now the single {@link CheckOwnerIdentityUnique} step,
 * which compares the whole {@link OwnerIdentity#key(Owner) identityKey}. Because the household
 * id set here is part of that key, two members of the same household with different telephones
 * have different keys and are both allowed. Runs after {@link BuildOwner}.
 */
public class AssignOwnerHousehold {

    public void service(@Val Owner owner, @Val Boolean sharesHousehold,
            OwnerRepository ownerRepository) {
        owner.setHouseholdSize(1); // a household of one until an existing member is found
        if (!Boolean.TRUE.equals(sharesHousehold)) {
            return; // did not opt in: never joins an existing household
        }
        String lastName = key(owner.getLastName());
        String address = AddressNormalizer.normalize(owner.getAddress());
        List<Owner> household = new ArrayList<>();
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getId() != null && existing.getId().equals(owner.getId())) {
                continue; // same record (e.g. re-save)
            }
            if (lastName.equals(key(existing.getLastName()))
                    && address.equals(AddressNormalizer.normalize(existing.getAddress()))) {
                household.add(existing);
            }
        }
        if (household.isEmpty()) {
            return; // no existing owner at this last name + address
        }
        // Assign the same stable household identifier to all members, and record the household's
        // size (existing members plus this new owner) on every member so it reflects the
        // household as it stands after this create.
        String householdId = householdId(lastName, address, household);
        int householdSize = household.size() + 1;
        owner.setHouseholdId(householdId);
        owner.setHouseholdSize(householdSize);
        for (Owner member : household) {
            member.setHouseholdId(householdId);
            member.setHouseholdSize(householdSize);
            ownerRepository.save(member);
        }
    }

    /**
     * The household's stable shared identifier: reuse the id an existing member already carries
     * (so a household keeps one value over time); otherwise derive it deterministically from the
     * normalized last name and address, so independent joiners compute the same value.
     */
    private static String householdId(String lastName, String address, List<Owner> household) {
        for (Owner member : household) {
            String existing = member.getHouseholdId();
            if (existing != null && !existing.isBlank()) {
                return existing;
            }
        }
        return "H-" + sha256Hex(lastName + "\n" + address).substring(0, 12).toUpperCase(Locale.ROOT);
    }

    private static String sha256Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /** Comparison key: lower-cased, with leading/trailing and repeated whitespace collapsed. */
    private static String key(String value) {
        if (value == null) {
            return "";
        }
        return value.strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
