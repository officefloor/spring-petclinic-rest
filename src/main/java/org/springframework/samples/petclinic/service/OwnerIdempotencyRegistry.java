package org.springframework.samples.petclinic.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.stereotype.Component;

/**
 * Tracks the owner created for each {@code Idempotency-Key} seen on owner creation.
 *
 * <p>A create that repeats with a key already recorded here can return the originally
 * created owner instead of creating a duplicate. Keeping this bookkeeping in a
 * dedicated component lets the REST controller stay a thin, stateless delegator.
 */
@Component
public class OwnerIdempotencyRegistry {

    private final Map<String, Integer> ownerIdByKey = new ConcurrentHashMap<>();

    private final ClinicService clinicService;

    public OwnerIdempotencyRegistry(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    /**
     * The owner already created for {@code key}, or {@code null} when the key is
     * absent/blank or has not yet produced an owner.
     */
    public Owner findCreatedOwner(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        Integer ownerId = this.ownerIdByKey.get(key);
        return ownerId == null ? null : this.clinicService.findOwnerById(ownerId);
    }

    /** Record that {@code key} produced {@code owner}; a no-op when the key is absent/blank. */
    public void record(String key, Owner owner) {
        if (key != null && !key.isBlank()) {
            this.ownerIdByKey.put(key, owner.getId());
        }
    }
}
