package com.docappoint.docappoint;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "DATABASE_URL=jdbc:postgresql://localhost:5432/docappoint_test",
    "DATABASE_USERNAME=test",
    "DATABASE_PASSWORD=test",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect",
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.datasource.hikari.initialization-fail-timeout=-1"
})
class DocappointApplicationTests {

	@Test
	void contextLoads() {
	}

}
