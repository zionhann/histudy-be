package edu.handong.csee.histudy.service;

import edu.handong.csee.histudy.controller.form.BannerForm;
import edu.handong.csee.histudy.exception.MissingParameterException;
import java.net.URI;
import org.springframework.web.multipart.MultipartFile;

public record UpdateBannerCommand(
    Long bannerId, String label, String redirectUrl, Boolean active, MultipartFile image) {

  private static final String MESSAGE_MISSING_LABEL = "label은(는) 필수입니다.";
  private static final String MESSAGE_MISSING_UPDATE_PAYLOAD = "수정할 값이 없습니다.";
  private static final String MESSAGE_INVALID_REDIRECT_URL = "redirectUrl은 http/https URL이어야 합니다.";

  public UpdateBannerCommand {
    if (bannerId == null) {
      throw new MissingParameterException("배너 ID는 필수입니다.");
    }
    if (label != null) {
      label = normalizeRequiredLabel(label);
    }
    redirectUrl = normalizeRedirectUrlWhenPresent(redirectUrl);
    if (label == null && redirectUrl == null && active == null && !hasImage(image)) {
      throw new MissingParameterException(MESSAGE_MISSING_UPDATE_PAYLOAD);
    }
  }

  public static UpdateBannerCommand from(Long bannerId, BannerForm form) {
    if (form == null) {
      throw new MissingParameterException("요청 본문이 비어 있습니다.");
    }
    return new UpdateBannerCommand(
        bannerId, form.getLabel(), form.getRedirectUrl(), form.getActive(), form.getImage());
  }

  public boolean hasImage() {
    return hasImage(image);
  }

  public String resolveLabel(String currentLabel) {
    return label == null ? currentLabel : label;
  }

  public String resolveRedirectUrl(String currentRedirectUrl) {
    return redirectUrl == null ? currentRedirectUrl : redirectUrl;
  }

  public boolean resolveActive(boolean currentActive) {
    return active == null ? currentActive : active;
  }

  private static boolean hasImage(MultipartFile image) {
    return image != null && !image.isEmpty();
  }

  private static String normalizeRequiredLabel(String label) {
    if (label == null || label.isBlank()) {
      throw new MissingParameterException(MESSAGE_MISSING_LABEL);
    }
    return label.trim();
  }

  private static String normalizeRedirectUrlWhenPresent(String redirectUrl) {
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
