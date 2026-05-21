package com.jababdihi.backend.incident;

import com.jababdihi.backend.common.ActorRole;
import com.jababdihi.backend.common.CategoryCode;
import com.jababdihi.backend.common.LanguageCode;
import java.util.Arrays;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PublicReferenceService {
  private final CategoryRepository categoryRepository;

  PublicReferenceService(CategoryRepository categoryRepository) {
    this.categoryRepository = categoryRepository;
  }

  public ActorsResponse actors(LanguageCode language) {
    return new ActorsResponse(
        Arrays.stream(ActorRole.values())
            .filter(ActorRole::isPubliclyVisible)
            .map(
                actor ->
                    new ActorResponse(
                        actor.name(),
                        actor.label(language),
                        actor.color(),
                        actor == ActorRole.GOVERNMENT))
            .toList());
  }

  @Transactional(readOnly = true)
  public CategoriesResponse categories(LanguageCode language) {
    var categories = categoryRepository.findAll(Sort.by("displayOrder").ascending());
    if (categories.isEmpty()) {
      return new CategoriesResponse(
          CategoryCode.all().stream()
              .map(
                  category ->
                      new CategoryResponse(
                          category.name(), category.label(language), category.defaultVisible()))
              .toList());
    }
    return new CategoriesResponse(
        categories.stream()
            .map(
                category ->
                    new CategoryResponse(
                        category.getCode(),
                        language == LanguageCode.BN ? category.getLabelBn() : category.getLabelEn(),
                        category.isDefaultVisible()))
            .toList());
  }
}
