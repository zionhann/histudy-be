package edu.handong.csee.histudy.domain;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Season {
  NONE(0),
  SPRING(1),
  FALL(2),
  SUMMER(3),
  WINTER(4);

  private final int number;

  public static Season parse(int number) {
    return Arrays.stream(Season.values())
        .filter(season -> season.number == number)
        .findFirst()
        .orElseThrow();
  }
}
