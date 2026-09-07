package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code riskFlag}: {@code true} when the owner warrants a manual look, which
 * is any of three independent signals holding, otherwise {@code false}.
 *
 * <ul>
 *   <li>the owner is a possible duplicate ({@link Owner#isPossibleDuplicate()} — a soft match on
 *       lastName and postcode against an existing owner with a different telephone);</li>
 *   <li>the email domain is <em>disposable-adjacent</em> — not itself on the disposable blocklist
 *       (those are rejected outright with a 400 and never reach a created owner), but sharing the
 *       registrable label of a disposable domain: a subdomain of a disposable domain
 *       ({@code x.mailinator.com}) or the same second-level name under a different TLD
 *       ({@code mailinator.net}). See {@link #isDisposableAdjacent(String)};</li>
 *   <li>the city is over its soft capacity ({@link Owner#isCapacityWarning()} — it already held
 *       40 or more owners, approaching the hard per-city limit of 50).</li>
 * </ul>
 *
 * <p>All three inputs are persisted on the {@link Owner}, so the flag is derived identically for
 * the create response and for later reads.
 */
public final class RiskFlag {

    /** Second-level labels of the disposable domains — the "core" a disposable-adjacent domain shares. */
    private static final Set<String> DISPOSABLE_LABELS = CheckOwnerEmailDomain.DISPOSABLE_DOMAINS.stream()
            .map(RiskFlag::secondLevelLabel)
            .collect(Collectors.toUnmodifiableSet());

    private RiskFlag() {
    }

    /** True when any risk signal holds for {@code owner}. */
    public static boolean of(Owner owner) {
        return owner.isPossibleDuplicate()
                || owner.isCapacityWarning()
                || isDisposableAdjacent(owner.getEmail());
    }

    /**
     * True when {@code email}'s domain shares the second-level label of a disposable domain — i.e. it
     * is the disposable domain itself, a subdomain of one, or the same second-level name under another
     * TLD. An absent or domainless email is not adjacent.
     */
    public static boolean isDisposableAdjacent(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        int at = email.lastIndexOf('@');
        if (at < 0 || at == email.length() - 1) {
            return false;
        }
        String domain = email.substring(at + 1).trim().toLowerCase(Locale.ROOT);
        for (String label : domain.split("\\.")) {
            if (DISPOSABLE_LABELS.contains(label)) {
                return true;
            }
        }
        return false;
    }

    /** The second-level label of a registrable domain, e.g. {@code mailinator} from {@code mailinator.com}. */
    private static String secondLevelLabel(String domain) {
        int dot = domain.indexOf('.');
        return dot < 0 ? domain : domain.substring(0, dot);
    }
}
