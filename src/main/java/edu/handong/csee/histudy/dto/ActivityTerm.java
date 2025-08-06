package edu.handong.csee.histudy.dto;

public enum ActivityTerm {
  ALL("all"),
  CURRENT("current");

  private final String value;

  ActivityTerm(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static ActivityTerm fromString(String value) {
    for (ActivityTerm term : ActivityTerm.values()) {
      if (term.value.equalsIgnoreCase(value)) {
        return term;
      }
    }
    return ALL;
  }
}
