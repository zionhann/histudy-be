# 테스트 구조

이 문서는 테스트 코드를 작성할 때 지켜야 할 구조에 대해 다룹니다.

## 파일 위치

- 파일 생성 위치: 대상 파일의 `src/test/java/**` 하위 패키지 구조를 그대로 반영
- 파일명: `<대상 파일 이름>Test.java` (e.g. `AbcService.java` -> `AbcServiceTest.java`)

## 프로퍼티

- immutable -> mutable 순으로 프로퍼티 배열 (immutable - mutable 프로퍼티 간 개행문자 삽입)
- immutable 필드는 테스트 케이스 간 중복되는 데이터 객체 생성 등으로 활용 가능함

```java
public class Foo {
    private final int immutable = 1;

    private String mutable = "test";
}

```

## 테스트 메서드

- BDD(Behavior Driven Development) 스타일로 작성
- 테스트 메서드는 한글로 작성, 띄어쓰기는 `_`(underscore)로 대치, <when>-<then> 형식으로 간결하게
- 테스트 구현은 주석으로 given-when-then을 구분하고, 각 영역에 맞게 작성
- then에 해당하는 Assertion은 `assertj`를 활용

```java

@Test
void Foo하면_Bar한다() {
    //given

    //when

    //then
}
```
