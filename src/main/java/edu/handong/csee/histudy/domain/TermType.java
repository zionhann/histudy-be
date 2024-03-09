package edu.handong.csee.histudy.domain;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum TermType {
  NONE(0),
  SPRING(1),
  FALL(2),
  SUMMER(3),
  WINTER(4);

  private final int number;

  public static TermType parse(int number) {
    return Arrays.stream(TermType.values())
        .filter(season -> season.number == number)
        .findFirst()
        .orElseThrow();
  }
}
