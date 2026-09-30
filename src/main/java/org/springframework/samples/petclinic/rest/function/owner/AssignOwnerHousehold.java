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
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Assigns a shared, stable {@code householdId} when the request opts in with
 * {@code sharesHousehold=true} and an existing owner shares the same household. What counts
 * as the same household — same lastName AND address, compared on their normalized forms — is
 * single-sourced in {@link #sameHousehold}, so the household id assigned here feeds the derived
 * {@link org.springframework.samples.petclinic.model.Owner#getIdentityKey() identityKey} and the
 * duplicate check consistently. The identifier is derived deterministically from the
 * normalized lastName and address, so every owner in one household receives the same value
 * regardless of creation order. Both the joining owner and any matching existing owner that
 * still lacks the id are stamped with it, so the household is linked on both sides. Runs
 * after {@link BuildOwner} has created the entity and before {@link CheckOwnerIdentityUnique}
 * (which sees the resolved {@code householdId}) and {@link SaveOwner} persists it. When the request
 * does not opt in, or opts in but no existing owner shares the household, the id is left unset.
 */
public class AssignOwnerHousehold {

    public void service(@Val OwnerFieldsDto request, @Val Owner owner, OwnerRepository ownerRepository) {
        if (!Boolean.TRUE.equals(request.getSharesHousehold())) {
            return;
        }
        List<Owner> matches = new ArrayList<>();
        String shared = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getId() != null && existing.getId().equals(owner.getId())) {
                continue;
            }
            if (sameHousehold(owner.getLastName(), owner.getAddress(),
                    existing.getLastName(), existing.getAddress())) {
                matches.add(existing);
                if (shared == null && existing.getHouseholdId() != null) {
                    shared = existing.getHouseholdId();
                }
            }
        }
        if (matches.isEmpty()) {
            return;
        }
        if (shared == null) {
            shared = deriveHouseholdId(normalize(owner.getLastName()),
                    NormalizeOwnerAddress.normalize(owner.getAddress()));
        }
        owner.setHouseholdId(shared);
        for (Owner existing : matches) {
            if (existing.getHouseholdId() == null) {
                existing.setHouseholdId(shared);
                ownerRepository.save(existing);
            }
        }
    }

    /**
     * The single source of truth for what "same household" means: identical lastName AND address.
     * The lastName is compared case-insensitively with internal whitespace collapsed; the address is
     * compared on its normalized form (see {@link NormalizeOwnerAddress#normalize}) so abbreviation,
     * case and whitespace differences never split one household.
     */
    static boolean sameHousehold(String lastNameA, String addressA, String lastNameB, String addressB) {
        return normalize(lastNameA).equals(normalize(lastNameB))
                && NormalizeOwnerAddress.normalize(addressA).equals(NormalizeOwnerAddress.normalize(addressB));
    }

    /** Lower-cased, trimmed, with internal whitespace runs collapsed to a single space. */
    static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    /**
     * Derives the stable household identifier {@code HH-<12 upper hex>} from the SHA-256 of the
     * normalized {@code lastName|address}, so two owners in the same household compute the same id.
     */
    private static String deriveHouseholdId(String lastName, String address) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest((lastName + "|" + address).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(12);
            for (int i = 0; i < 6; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return "HH-" + sb.toString().toUpperCase(Locale.ROOT);
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}
