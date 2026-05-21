package com.jababdihi.backend.incident;

public record LocationResponse(
    String country, String division, String district, String upazila, String displayText) {}
