package com.jababdihi.backend.incident;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jababdihi.backend.common.LanguageCode;
import com.jababdihi.backend.common.PageResponse;
import com.jababdihi.backend.config.SecurityConfig;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PublicIncidentController.class)
@Import(SecurityConfig.class)
class PublicIncidentControllerTests {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private PublicIncidentService incidentService;

  @MockitoBean private PublicReferenceService referenceService;

  @Test
  void incidentsEndpointIsPublic() throws Exception {
    when(incidentService.findIncidents(
            eq("GOVERNMENT"), eq(List.of("EXTORTION")), eq("NEWEST"), eq("bn"), eq(1), eq(20)))
        .thenReturn(new PageResponse<>(List.of(), 1, 20, 0, 0));

    mockMvc
        .perform(
            get("/api/incidents")
                .param("actorRole", "GOVERNMENT")
                .param("category", "EXTORTION")
                .param("sort", "NEWEST")
                .param("language", "bn")
                .param("page", "1")
                .param("pageSize", "20"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.page").value(1))
        .andExpect(jsonPath("$.pageSize").value(20));
  }

  @Test
  void categoriesEndpointUsesRequestedLanguage() throws Exception {
    when(referenceService.categories(any(LanguageCode.class)))
        .thenReturn(
            new CategoriesResponse(List.of(new CategoryResponse("EXTORTION", "চাঁদাবাজি", true))));

    mockMvc
        .perform(get("/api/categories").param("language", "bn"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items[0].code").value("EXTORTION"))
        .andExpect(jsonPath("$.items[0].label").value("চাঁদাবাজি"));
  }
}
