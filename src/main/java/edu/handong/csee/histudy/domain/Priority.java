package edu.handong.csee.histudy.domain;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Priority {
  HIGH(0),
  MEDIUM(1),
  LOW(2);

  private final int number;

  public static Priority of(int number) {
    return Arrays.stream(Priority.values())
        .filter(p -> p.getNumber() == number)
        .findFirst()
        .orElseThrow();
  }
}
