package edu.handong.csee.histudy.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class ImagePathMapperTest {

  private ImagePathMapper mapper;

  @BeforeEach
  void init() {
    mapper = new ImagePathMapper();
    ReflectionTestUtils.setField(mapper, "origin", "http://localhost:8080");
    ReflectionTestUtils.setField(mapper, "imageBasePath", "/images/");
  }

  @Test
  void 구_리포트이미지경로는_신규images기반URL로_변환된다() {
    String fullPath = mapper.getFullPath("/reports/images/old-report.png");

    assertThat(fullPath).isEqualTo("http://localhost:8080/images/reports/old-report.png");
  }

  @Test
  void 구_리포트이미지URL입력은_reports상대경로로_정규화된다() {
    List<String> normalized =
        mapper.extractFilename(List.of("http://localhost:8080/reports/images/old-report.png"));

    assertThat(normalized).containsExactly("reports/old-report.png");
  }

  @Test
  void 과거DB의_순수파일명도_여전히접근가능한URL을_만든다() {
    String fullPath = mapper.getFullPath("legacy-report.png");

    assertThat(fullPath).isEqualTo("http://localhost:8080/images/legacy-report.png");
  }

  @Test
  void 경로목록에_null요소가_있어도_NPE가발생하지않는다() {
    List<String> normalized = mapper.extractFilename(Arrays.asList("banner/main.png", null));

    assertThat(normalized).containsExactly("banner/main.png", null);
  }
}
