package org.springframework.samples.petclinic.rest.controller.v1;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Idempotent-create rule for owners: a create carrying an {@code Idempotency-Key} header
 * is remembered under that key, so a repeat with the same key returns the originally
 * created owner instead of creating a duplicate.
 */
final class IdempotencyKeys {

    private static final Map<String, Owner> SEEN = new ConcurrentHashMap<>();

    private IdempotencyKeys() {
    }

    /** The current request's {@code Idempotency-Key} header, or {@code null} when absent. */
    static String current() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes servlet) {
            return servlet.getRequest().getHeader("Idempotency-Key");
        }
        return null;
    }

    /** The owner already created under {@code key}, or {@code null} when the key is new or absent. */
    static Owner seen(String key) {
        return key == null ? null : SEEN.get(key);
    }

    /** Remember {@code owner} as the result created under {@code key} (a no-op when the key is absent). */
    static void remember(String key, Owner owner) {
        if (key != null) {
            SEEN.put(key, owner);
        }
    }
}
