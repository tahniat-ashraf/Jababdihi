package com.jababdihi.backend.incident;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jababdihi.backend.common.PageResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class PublicIncidentServiceTests {

  private final IncidentRepository incidentRepository = mock(IncidentRepository.class);
  private final IncidentMapper incidentMapper = mock(IncidentMapper.class);
  private final PublicIncidentService service =
      new PublicIncidentService(incidentRepository, incidentMapper);

  @Test
  void findIncidentsUsesOneBasedPaginationAndCapsPageSize() {
    when(incidentRepository.findAll(anySpecification(), any(Pageable.class)))
        .thenReturn(Page.<Incident>empty());

    PageResponse<IncidentSummaryResponse> response =
        service.findIncidents(
            "GOVERNMENT", List.of("EXTORTION,ABUSE_OF_POWER"), "MOST_CORROBORATED", "en", 2, 100);

    ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
    verify(incidentRepository).findAll(anySpecification(), pageable.capture());

    assertThat(response.page()).isEqualTo(2);
    assertThat(response.pageSize()).isEqualTo(50);
    assertThat(pageable.getValue().getPageNumber()).isEqualTo(1);
    assertThat(pageable.getValue().getPageSize()).isEqualTo(50);
    assertThat(pageable.getValue().getSort().getOrderFor("confidenceScore")).isNotNull();
  }

  @Test
  void findIncidentsRejectsUnknownActorRole() {
    assertThatThrownBy(
            () -> service.findIncidents("UNKNOWN", List.of(), "RECOMMENDED", "bn", 1, 20))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("actorRole must be GOVERNMENT or OPPOSITION");
  }

  @Test
  void findIncidentsRejectsUnsupportedCategory() {
    assertThatThrownBy(
            () ->
                service.findIncidents(
                    "GOVERNMENT", List.of("NOT_A_CATEGORY"), "RECOMMENDED", "bn", 1, 20))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("unsupported category");
  }

  @SuppressWarnings("unchecked")
  private Specification<Incident> anySpecification() {
    return any(Specification.class);
  }
}
