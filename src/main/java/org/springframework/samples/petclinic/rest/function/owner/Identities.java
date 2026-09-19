package org.springframework.samples.petclinic.rest.function.owner;

import java.util.List;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.util.Soundex;

/**
 * Shared identity logic for the create-owner pipeline. An owner's {@link Owner#getIdentityKey()
 * identity key} is the single fingerprint duplicate detection keys off: a hard duplicate repeats
 * an existing owner's key exactly, while a soft match sounds like an existing owner at the same
 * postcode without repeating the key. Both queries exclude soft-deleted owners, so a deleted
 * owner neither blocks nor flags a new one.
 */
final class Identities {

    private Identities() {
    }

    /**
     * The existing live owners whose {@link Owner#getIdentityKey() identity key} equals the
     * given owner's — the hard duplicates it repeats. Because the telephone is part of the key,
     * a match must share the telephone, so candidates are fetched by telephone and then compared
     * on the full key.
     */
    static List<Owner> duplicatesOf(OwnerRepository ownerRepository, Owner owner) {
        String identityKey = owner.getIdentityKey();
        return ownerRepository.findByTelephone(owner.getTelephone()).stream()
                .filter(existing -> !existing.isDeleted())
                .filter(existing -> identityKey.equals(existing.getIdentityKey()))
                .toList();
    }

    /**
     * The existing live owners that soft-match the given owner: same postcode and a
     * phonetically-equal last name ({@link Soundex}) but a different {@link Owner#getIdentityKey()
     * identity key}. Empty when the owner has no postcode, since a postcode match is required.
     */
    static List<Owner> softMatchesOf(OwnerRepository ownerRepository, Owner owner) {
        String postcode = owner.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            return List.of();
        }
        String soundex = Soundex.of(owner.getLastName());
        String identityKey = owner.getIdentityKey();
        return ownerRepository.findAll().stream()
                .filter(existing -> !existing.isDeleted())
                .filter(existing -> postcode.equals(existing.getPostcode()))
                .filter(existing -> soundex.equals(Soundex.of(existing.getLastName())))
                .filter(existing -> !identityKey.equals(existing.getIdentityKey()))
                .toList();
    }
}
