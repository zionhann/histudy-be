package edu.handong.csee.histudy.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
class BannerImageCleanupListenerTest {

  @TempDir Path tempDir;

  @Autowired private BannerImageStorage bannerImageStorage;

  @Autowired private TransactionTemplate transactionTemplate;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(bannerImageStorage, "imageBaseLocation", tempDir.toString() + "/");
  }

  @Test
  void 커밋되면_삭제예약한_배너이미지를_삭제한다() throws Exception {
    // Given
    Path imagePath = createImageFile("banner/commit.png");

    // When
    transactionTemplate.executeWithoutResult(
        status -> bannerImageStorage.deleteAfterCommit("banner/commit.png"));

    // Then
    assertThat(Files.exists(imagePath)).isFalse();
  }

  @Test
  void 롤백되면_삭제예약한_배너이미지를_삭제한다() throws Exception {
    // Given
    Path imagePath = createImageFile("banner/rollback.png");

    // When
    transactionTemplate.executeWithoutResult(
        status -> {
          bannerImageStorage.deleteAfterRollback("banner/rollback.png");
          status.setRollbackOnly();
        });

    // Then
    assertThat(Files.exists(imagePath)).isFalse();
  }

  private Path createImageFile(String relativePath) throws Exception {
    Path imagePath = tempDir.resolve(relativePath);
    Files.createDirectories(imagePath.getParent());
    Files.writeString(imagePath, "banner-image");
    return imagePath;
  }
}
