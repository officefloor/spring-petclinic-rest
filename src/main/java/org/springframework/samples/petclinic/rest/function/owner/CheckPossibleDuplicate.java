package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.IdentityKey;
import org.springframework.samples.petclinic.mapper.Soundex;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags a soft (non-hard) duplicate: a <em>suspected</em>, undeclared duplicate. By the time this
 * step runs the request has already passed {@link CheckOwnerIdentityUnique}, so its
 * {@code identityKey} is unique. This step records a weaker signal: an existing (non-deleted)
 * owner whose <em>identityKey differs</em> yet whose {@code soundex(lastName)} and {@code postcode}
 * both match. The typical case is the same household with a different telephone: because the
 * telephone is part of the identity key, such an owner is no longer a hard duplicate, so it is
 * created but flagged.
 *
 * <p>The matched owner is created with {@code possibleDuplicate = true} and
 * {@code possibleDuplicateOf} set to the matching owner's id (the lowest matching id when several
 * match). When nothing matches, {@code possibleDuplicate} is {@code false} and
 * {@code possibleDuplicateOf} stays absent.
 *
 * <p>Runs after {@link CheckOwnerIdentityUnique} and before {@link SaveOwner} (so the new owner is
 * not yet among {@code findAll()} and cannot match itself), mutating the built {@link Owner} in
 * place.
 */
public class CheckPossibleDuplicate {

    public void service(@Val Owner built, OwnerRepository ownerRepository) {
        String identityKey = IdentityKey.of(built);
        Integer matchId = null;
        for (Owner existing : ownerRepository.findAll()) {
            if (Boolean.TRUE.equals(existing.getDeleted())) {
                continue; // a soft-deleted owner is not a suspected duplicate
            }
            if (!identityKey.equals(IdentityKey.of(existing))
                    && sameSoundex(built, existing) && samePostcode(built, existing)) {
                Integer id = existing.getId();
                if (id != null && (matchId == null || id < matchId)) {
                    matchId = id;
                }
            }
        }
        built.setPossibleDuplicate(matchId != null);
        built.setPossibleDuplicateOf(matchId);
    }

    private static boolean sameSoundex(Owner a, Owner b) {
        String sa = Soundex.of(a.getLastName());
        return !sa.isEmpty() && sa.equals(Soundex.of(b.getLastName()));
    }

    private static boolean samePostcode(Owner a, Owner b) {
        String pa = a.getPostcode();
        String pb = b.getPostcode();
        return pa != null && !pa.isBlank() && pa.equals(pb);
    }
}
