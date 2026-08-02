package org.springframework.samples.petclinic.acceptance;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import tools.jackson.databind.node.ObjectNode;

/**
 * cp06: duplicate detection ignores letter case (and, at the service layer,
 * surrounding or repeated whitespace). Email is the freely-textual field a
 * base-valid owner exposes, so it is used to assert the case dimension. The
 * whitespace dimension cannot be exercised through the REST API — the schema
 * rejects padded values with 400 before dedup runs (same reason cp05 only
 * asserts case) — but is handled by the normalization in the service layer.
 */
@Tag("cp06")
class Cp06Tests extends AcceptanceBase {

	@Test
	void coreRejectsCaseInsensitiveDuplicateEmail() throws Exception {
		String email = uniqueEmail();
		ObjectNode a = ownerNode();
		a.put("email", email);
		createOwnerOk(a);

		ObjectNode b = ownerNode(); // everything else distinct
		b.put("email", email.toUpperCase()); // same email, upper-cased
		createOwner(b).andExpect(status().isConflict());
	}

	@Test
	void functionalityAllowsGenuinelyDistinctEmails() throws Exception {
		ObjectNode a = ownerNode();
		a.put("email", uniqueEmail());
		createOwnerOk(a);

		ObjectNode b = ownerNode();
		b.put("email", uniqueEmail());
		createOwner(b).andExpect(status().is2xxSuccessful());
	}
}
