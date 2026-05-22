package com.jababdihi.backend.location;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "locations")
public class Location {
  @Id @GeneratedValue private UUID id;

  private String country = "Bangladesh";
  private String division;
  private String district;
  private String upazila;
  private String unionOrArea;
  private BigDecimal latitude;
  private BigDecimal longitude;

  public Location() {}

  public UUID getId() {
    return id;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public String getDivision() {
    return division;
  }

  public void setDivision(String division) {
    this.division = division;
  }

  public String getDistrict() {
    return district;
  }

  public void setDistrict(String district) {
    this.district = district;
  }

  public String getUpazila() {
    return upazila;
  }

  public void setUpazila(String upazila) {
    this.upazila = upazila;
  }

  public String getUnionOrArea() {
    return unionOrArea;
  }

  public void setUnionOrArea(String unionOrArea) {
    this.unionOrArea = unionOrArea;
  }

  public BigDecimal getLatitude() {
    return latitude;
  }

  public void setLatitude(BigDecimal latitude) {
    this.latitude = latitude;
  }

  public BigDecimal getLongitude() {
    return longitude;
  }

  public void setLongitude(BigDecimal longitude) {
    this.longitude = longitude;
  }
}
