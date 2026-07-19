package org.springframework.samples.petclinic.repository;

import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * <p> Integration test using the jpa profile.
 *
 * @author Rod Johnson
 * @author Sam Brannen
 * @author Michael Isvy
 * @see AbstractRepositoryTests AbstractRepositoryTests for more details. </p>
 */

@SpringBootTest
@ActiveProfiles({"jpa", "hsqldb"})
class RepositoryJpaTests extends AbstractRepositoryTests {

    @Autowired
    EntityManager entityManager;

    @Override
    void clearCache() {
        entityManager.clear();
    }
}
