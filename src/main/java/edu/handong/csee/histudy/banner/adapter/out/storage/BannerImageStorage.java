package edu.handong.csee.histudy.banner.adapter.out.storage;

import static edu.handong.csee.histudy.util.ImageDirectories.BANNER;
import static org.springframework.util.ResourceUtils.isUrl;

import edu.handong.csee.histudy.banner.adapter.out.storage.event.BannerImageDeleteAfterCommitEvent;
import edu.handong.csee.histudy.banner.adapter.out.storage.event.BannerImageDeleteAfterRollbackEvent;
import edu.handong.csee.histudy.exception.FileTransferException;
import edu.handong.csee.histudy.exception.MissingParameterException;
import edu.handong.csee.histudy.util.Utils;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class BannerImageStorage {

  private static final long MAX_IMAGE_SIZE_BYTES = 5L * 1024 * 1024;
  private static final Set<String> ALLOWED_IMAGE_EXTENSIONS =
      Set.of("jpg", "jpeg", "png", "gif", "webp", "svg", "avif", "bmp", "tiff");
  private static final String IMAGE_NAME_FALLBACK_LABEL = "image";
  private static final String MESSAGE_MAX_FILE_SIZE = "이미지 파일 크기는 5MB 이하여야 합니다.";
  private static final String MESSAGE_IMAGE_ONLY = "이미지 파일만 업로드할 수 있습니다.";

  private final ApplicationEventPublisher eventPublisher;

  @Value("${custom.resource.location}")
  private String imageBaseLocation;

  public String store(MultipartFile image, String label) {
    validate(image);

    String extension = getExtension(image.getOriginalFilename());
    String normalizedLabel = normalizeLabelForFilename(label);
    String dateTime = Utils.getCurrentFormattedDateTime("yyyyMMdd_HHmmss");
    String random = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    String relativePath = BANNER + normalizedLabel + "_" + dateTime + "_" + random + extension;

    File file = resolveStorageFile(relativePath);
    File parent = file.getParentFile();

    if (!parent.exists() && !parent.mkdirs()) {
      throw new FileTransferException();
    }

    try {
      image.transferTo(file);
      return relativePath;
    } catch (IOException e) {
      throw new FileTransferException();
    }
  }

  public void deleteAfterCommit(String imagePath) {
    eventPublisher.publishEvent(new BannerImageDeleteAfterCommitEvent(imagePath));
  }

  public void deleteAfterRollback(String imagePath) {
    eventPublisher.publishEvent(new BannerImageDeleteAfterRollbackEvent(imagePath));
  }

  private void validate(MultipartFile image) {
    if (image.getSize() > MAX_IMAGE_SIZE_BYTES) {
      throw new MissingParameterException(MESSAGE_MAX_FILE_SIZE);
    }

    String contentType = image.getContentType();
    if (contentType == null || !contentType.startsWith("image/")) {
      throw new MissingParameterException(MESSAGE_IMAGE_ONLY);
    }

    try (var inputStream = image.getInputStream()) {
      BufferedImage decodedImage = ImageIO.read(inputStream);
      if (decodedImage == null) {
        throw new MissingParameterException(MESSAGE_IMAGE_ONLY);
      }
    } catch (IOException e) {
      throw new MissingParameterException(MESSAGE_IMAGE_ONLY);
    }
  }

  private String getExtension(String originalFilename) {
    if (originalFilename == null || originalFilename.isBlank()) {
      return ".img";
    }

    String normalizedFilename = originalFilename.replace("\\", "/");
    String sanitizedFilename =
        normalizedFilename.substring(normalizedFilename.lastIndexOf('/') + 1);
    int extensionStart = sanitizedFilename.lastIndexOf('.');
    if (extensionStart < 0 || extensionStart == sanitizedFilename.length() - 1) {
      return ".img";
    }

    String extension = sanitizedFilename.substring(extensionStart + 1).toLowerCase(Locale.ROOT);
    if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
      return ".img";
    }

    return "." + extension;
  }

  private File resolveStorageFile(String relativePath) {
    Path basePath = Path.of(imageBaseLocation).toAbsolutePath().normalize();
    Path resolvedPath = basePath.resolve(stripLeadingPathSeparator(relativePath)).normalize();
    if (!resolvedPath.startsWith(basePath)) {
      throw new FileTransferException();
    }
    return resolvedPath.toFile();
  }

  private String stripLeadingPathSeparator(String path) {
    if (path == null) {
      return "";
    }
    if (path.startsWith("/") || path.startsWith("\\")) {
      return path.substring(1);
    }
    return path;
  }

  private void delete(String imagePath) {
    if (imagePath == null || isUrl(imagePath)) {
      return;
    }

    try {
      Files.deleteIfExists(resolveStorageFile(imagePath).toPath());
    } catch (IOException e) {
      throw new FileTransferException();
    }
  }

  public void deleteQuietly(String imagePath) {
    try {
      delete(imagePath);
    } catch (RuntimeException ignored) {
      // Intentionally ignore cleanup failures to avoid masking transaction completion.
    }
  }

  private String normalizeLabelForFilename(String label) {
    if (label == null) {
      return IMAGE_NAME_FALLBACK_LABEL;
    }

    String sanitized =
        label
            .trim()
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9_-]+", "-")
            .replaceAll("^-+|-+$", "");
    if (sanitized.isBlank()) {
      return IMAGE_NAME_FALLBACK_LABEL;
    }
    return sanitized;
  }
}
