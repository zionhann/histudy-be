package edu.handong.csee.histudy.util;

import static edu.handong.csee.histudy.util.ImageDirectories.BANNER;
import static edu.handong.csee.histudy.util.ImageDirectories.LEGACY_REPORTS_BASE_PATH;
import static edu.handong.csee.histudy.util.ImageDirectories.REPORTS;

import edu.handong.csee.histudy.domain.ReportImage;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ImagePathMapper {

  @Value("${custom.jwt.issuer}")
  private String origin;

  @Value("${custom.resource.path}")
  private String imageBasePath;

  private static final String FIREBASE_STORAGE_PREFIX = "https://firebasestorage.googleapis.com";

  public Map<Long, String> parseImageToMapWithFullPath(List<ReportImage> reportImages) {
    return reportImages.stream()
        .collect(
            Collectors.toUnmodifiableMap(
                ReportImage::getReportImageId, img -> getFullPath(img.getPath())));
  }

  public List<String> extractFilename(List<String> pathname) {
    if (pathname == null) {
      return null;
    }
    return pathname.stream().map(this::extractFilename).toList();
  }

  public String getFullPath(String pathname) {
    if (pathname == null) {
      return null;
    }

    if (pathname.startsWith(FIREBASE_STORAGE_PREFIX)) {
      return pathname;
    }

    String normalized = normalizeLocalPath(pathname);
    return origin + ensureTrailingSlash(imageBasePath) + normalized;
  }

  private String extractFilename(String pathname) {
    if (pathname.startsWith(FIREBASE_STORAGE_PREFIX)) {
      return pathname;
    }
    return normalizeLocalPath(pathname);
  }

  private String normalizeLocalPath(String pathname) {
    String path = pathname.trim();

    if (path.startsWith("http://") || path.startsWith("https://")) {
      String sameOriginBase = origin + ensureTrailingSlash(imageBasePath);
      if (path.startsWith(sameOriginBase)) {
        path = path.substring(sameOriginBase.length());
      } else {
        try {
          path = URI.create(path).getPath();
        } catch (IllegalArgumentException e) {
          return extractLastSegment(path);
        }
      }
    }

    path = stripLeadingSlash(path);

    String basePath = stripSlashes(imageBasePath);
    if (!basePath.isBlank() && path.startsWith(basePath + "/")) {
      path = path.substring(basePath.length() + 1);
    }

    if (path.startsWith(LEGACY_REPORTS_BASE_PATH)) {
      return REPORTS + path.substring(LEGACY_REPORTS_BASE_PATH.length());
    }

    if (path.startsWith(REPORTS) || path.startsWith(BANNER)) {
      return path;
    }

    return path.contains("/") ? extractLastSegment(path) : path;
  }

  private String extractLastSegment(String path) {
    int lastIndex = path.lastIndexOf('/');
    return (lastIndex >= 0) ? path.substring(lastIndex + 1) : path;
  }

  private String ensureTrailingSlash(String path) {
    if (path == null || path.isBlank()) {
      return "/";
    }
    return path.endsWith("/") ? path : path + "/";
  }

  private String stripLeadingSlash(String path) {
    return path.startsWith("/") ? path.substring(1) : path;
  }

  private String stripSlashes(String path) {
    if (path == null || path.isBlank()) {
      return "";
    }
    String withoutLeading = stripLeadingSlash(path);
    return withoutLeading.endsWith("/")
        ? withoutLeading.substring(0, withoutLeading.length() - 1)
        : withoutLeading;
  }
}
