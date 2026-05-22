package com.jababdihi.backend.incident;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "categories")
public class Category {
  @Id private String code;

  private String labelEn;
  private String labelBn;
  private boolean defaultVisible;
  private int displayOrder;

  public Category() {}

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getLabelEn() {
    return labelEn;
  }

  public void setLabelEn(String labelEn) {
    this.labelEn = labelEn;
  }

  public String getLabelBn() {
    return labelBn;
  }

  public void setLabelBn(String labelBn) {
    this.labelBn = labelBn;
  }

  public boolean isDefaultVisible() {
    return defaultVisible;
  }

  public void setDefaultVisible(boolean defaultVisible) {
    this.defaultVisible = defaultVisible;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }

  public void setDisplayOrder(int displayOrder) {
    this.displayOrder = displayOrder;
  }
}
