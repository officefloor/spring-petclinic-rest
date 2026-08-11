package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.Membership;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Derives the read-time {@code membershipLevel} on an owner, applying the household ceiling: a
 * new owner's level may not exceed <em>one above the current maximum</em> derived level among the
 * <em>other</em> members of its household (those sharing its {@code householdId}, matched exactly
 * as {@link CountHouseholdMembers} counts them). With no other household member — a household of
 * one, or no {@code householdId} — no cap applies and the plain derived level stands.
 *
 * <p>Each sibling's level is recomputed here against the current household size so it reflects the
 * household as it stands now, mirroring how {@link CountHouseholdMembers} derives {@code
 * householdSize}. Runs after {@link CountHouseholdMembers} (so the owner's own {@code
 * householdSize} is set) and before the responder, mutating the owner in place; the mapper reads
 * the stored level.
 */
public class CapMembershipLevel {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        int level = Membership.levelOf(owner);
        String householdId = owner.getHouseholdId();
        if (householdId != null) {
            List<Owner> household = new ArrayList<>();
            for (Owner existing : ownerRepository.findAll()) {
                if (householdId.equals(existing.getHouseholdId())) {
                    household.add(existing);
                }
            }
            int size = household.size();
            Integer maxOther = null;
            for (Owner member : household) {
                if (owner.getId() != null && owner.getId().equals(member.getId())) {
                    continue; // exclude the owner itself
                }
                member.setHouseholdSize(size); // so the sibling's level sees the current household
                int memberLevel = Membership.levelOf(member);
                if (maxOther == null || memberLevel > maxOther) {
                    maxOther = memberLevel;
                }
            }
            if (maxOther != null) {
                level = Math.min(level, maxOther + 1);
            }
        }
        owner.setMembershipLevel(level);
    }
}
