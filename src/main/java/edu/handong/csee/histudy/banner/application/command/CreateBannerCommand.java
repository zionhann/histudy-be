package edu.handong.csee.histudy.banner.application.command;

import edu.handong.csee.histudy.banner.adapter.in.request.CreateBannerRequest;
import edu.handong.csee.histudy.exception.MissingParameterException;
import java.net.URI;
import org.springframework.web.multipart.MultipartFile;

public record CreateBannerCommand(
    String label, String redirectUrl, boolean active, MultipartFile image) {

  private static final String MESSAGE_MISSING_IMAGE = "이미지 파일은 필수입니다.";
  private static final String MESSAGE_MISSING_LABEL = "label은(는) 필수입니다.";
  private static final String MESSAGE_INVALID_REDIRECT_URL = "redirectUrl은 http/https URL이어야 합니다.";

  public CreateBannerCommand {
    label = normalizeRequiredLabel(label);
    redirectUrl = normalizeRedirectUrl(redirectUrl);
    if (image == null || image.isEmpty()) {
      throw new MissingParameterException(MESSAGE_MISSING_IMAGE);
    }
  }

  public static CreateBannerCommand from(CreateBannerRequest request) {
    if (request == null) {
      throw new MissingParameterException("요청 본문이 비어 있습니다.");
    }
    return new CreateBannerCommand(
        request.getLabel(),
        request.getRedirectUrl(),
        request.getActive() == null || request.getActive(),
        request.getImage());
  }

  private static String normalizeRequiredLabel(String label) {
    if (label == null || label.isBlank()) {
      throw new MissingParameterException(MESSAGE_MISSING_LABEL);
    }
    return label.trim();
  }

  private static String normalizeRedirectUrl(String redirectUrl) {
    if (redirectUrl == null) {
      return null;
    }

    String normalized = redirectUrl.trim();
    if (normalized.isEmpty()) {
      return null;
    }

    try {
      URI uri = URI.create(normalized);
      String scheme = uri.getScheme();
      if (!uri.isAbsolute() || scheme == null) {
        throw new IllegalArgumentException();
      }
      if (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https")) {
        throw new IllegalArgumentException();
      }
    } catch (IllegalArgumentException e) {
      throw new MissingParameterException(MESSAGE_INVALID_REDIRECT_URL);
    }

    return normalized;
  }
}
