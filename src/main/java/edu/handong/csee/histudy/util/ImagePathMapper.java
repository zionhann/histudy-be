package edu.handong.csee.histudy.util;

import edu.handong.csee.histudy.domain.ReportImage;
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

  private final String firebaseStoragePrefix = "https://firebasestorage.googleapis.com";

  public Map<Long, String> parseImageToMapWithFullPath(List<ReportImage> reportImages) {
    return reportImages.stream()
        .collect(Collectors.toUnmodifiableMap(ReportImage::getReportImageId, img -> getFullPath(img.getPath())));
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
    return (pathname.startsWith(firebaseStoragePrefix))
        ? pathname
        : origin + imageBasePath + pathname;
  }

  private String extractFilename(String pathname) {
    if (pathname.startsWith(firebaseStoragePrefix)) {
      return pathname;
    }
    int lastIndex = pathname.lastIndexOf('/');
    return (lastIndex >= 0) ? pathname.substring(lastIndex + 1) : pathname;
  }
}
