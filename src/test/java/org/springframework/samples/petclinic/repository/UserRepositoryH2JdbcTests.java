package org.springframework.samples.petclinic.repository;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles({"h2", "jdbc"})
@TestPropertySource(properties = {
    "spring.sql.init.platform=h2",
    "spring.h2.console.enabled=false"
})
class UserRepositoryH2JdbcTests extends AbstractUserRepositoryTests {

}
