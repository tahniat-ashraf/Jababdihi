package com.jababdihi.backend.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jababdihi.backend.incident.CategoryRepository;
import com.jababdihi.backend.incident.IncidentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
    properties = {
      "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
          + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
          + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration,"
          + "org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration,"
          + "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration"
    })
@AutoConfigureMockMvc
class HealthEndpointSecurityTests {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private IncidentRepository incidentRepository;

  @MockitoBean private CategoryRepository categoryRepository;

  @Test
  void healthEndpointIsPublic() throws Exception {
    mockMvc.perform(get("/actuator/health")).andExpect(status().isOk());
  }
}
