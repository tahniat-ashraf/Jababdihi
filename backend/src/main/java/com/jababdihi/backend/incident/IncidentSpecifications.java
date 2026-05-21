package com.jababdihi.backend.incident;

import com.jababdihi.backend.common.ActorRole;
import com.jababdihi.backend.common.CategoryCode;
import com.jababdihi.backend.common.IncidentStatus;
import jakarta.persistence.criteria.Join;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

final class IncidentSpecifications {
  private static final List<IncidentStatus> PUBLISHED_STATUSES =
      List.of(IncidentStatus.AUTO_PUBLISHED, IncidentStatus.MANUALLY_PUBLISHED);

  private IncidentSpecifications() {}

  static Specification<Incident> publicFeed(
      ActorRole actorRole, Collection<CategoryCode> categories) {
    return publishedOnly().and(publicActorOnly(actorRole)).and(categoryFilter(categories));
  }

  static Specification<Incident> publishedDetail(UUID id) {
    return ((Specification<Incident>) (root, query, builder) -> builder.equal(root.get("id"), id))
        .and(publishedOnly())
        .and(publicActorOnly(null));
  }

  private static Specification<Incident> publishedOnly() {
    return (root, query, builder) -> root.get("status").in(PUBLISHED_STATUSES);
  }

  private static Specification<Incident> publicActorOnly(ActorRole actorRole) {
    return (root, query, builder) -> {
      if (actorRole != null) {
        return builder.equal(root.get("actorRole"), actorRole);
      }
      return root.get("actorRole").in(List.of(ActorRole.GOVERNMENT, ActorRole.OPPOSITION));
    };
  }

  private static Specification<Incident> categoryFilter(Collection<CategoryCode> categories) {
    return (root, query, builder) -> {
      if (categories == null || categories.isEmpty()) {
        return builder.conjunction();
      }
      Join<Incident, Category> category = root.join("categories");
      if (query != null) {
        query.distinct(true);
      }
      return category.get("code").in(categories.stream().map(Enum::name).toList());
    };
  }
}
