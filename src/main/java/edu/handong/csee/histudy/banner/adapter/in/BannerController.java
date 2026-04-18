package edu.handong.csee.histudy.banner.adapter.in;

import edu.handong.csee.histudy.banner.adapter.in.request.CreateBannerRequest;
import edu.handong.csee.histudy.banner.adapter.in.request.ReorderBannersRequest;
import edu.handong.csee.histudy.banner.adapter.in.request.UpdateBannerRequest;
import edu.handong.csee.histudy.banner.adapter.in.response.AdminBannerResponse;
import edu.handong.csee.histudy.banner.adapter.in.response.PublicBannerResponse;
import edu.handong.csee.histudy.banner.application.BannerService;
import edu.handong.csee.histudy.banner.application.command.CreateBannerCommand;
import edu.handong.csee.histudy.banner.application.command.DeleteBannerCommand;
import edu.handong.csee.histudy.banner.application.command.ReorderBannersCommand;
import edu.handong.csee.histudy.banner.application.command.UpdateBannerCommand;
import edu.handong.csee.histudy.domain.Role;
import edu.handong.csee.histudy.exception.ForbiddenException;
import io.jsonwebtoken.Claims;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class BannerController {

  private final BannerService bannerService;

  @GetMapping("/api/public/banners")
  public List<PublicBannerResponse> getPublicBanners() {
    return bannerService.getPublicBanners();
  }

  @GetMapping("/api/admin/banners")
  public ResponseEntity<List<AdminBannerResponse>> getBanners(@RequestAttribute Claims claims) {
    requireAdmin(claims);
    return ResponseEntity.ok(bannerService.getAdminBanners());
  }

  @PostMapping(value = "/api/admin/banners", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<AdminBannerResponse> createBanner(
      @ModelAttribute CreateBannerRequest request, @RequestAttribute Claims claims) {
    requireAdmin(claims);
    AdminBannerResponse created = bannerService.createBanner(CreateBannerCommand.from(request));
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @PatchMapping(
      value = "/api/admin/banners/{bannerId}",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<AdminBannerResponse> updateBanner(
      @PathVariable Long bannerId,
      @ModelAttribute UpdateBannerRequest request,
      @RequestAttribute Claims claims) {
    requireAdmin(claims);
    AdminBannerResponse updated =
        bannerService.updateBanner(UpdateBannerCommand.from(bannerId, request));
    return ResponseEntity.ok(updated);
  }

  @PatchMapping("/api/admin/banners/reorder")
  public ResponseEntity<Void> reorderBanners(
      @RequestBody ReorderBannersRequest request, @RequestAttribute Claims claims) {
    requireAdmin(claims);
    bannerService.reorderBanners(ReorderBannersCommand.from(request));
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/api/admin/banners/{bannerId}")
  public ResponseEntity<Void> deleteBanner(
      @PathVariable Long bannerId, @RequestAttribute Claims claims) {
    requireAdmin(claims);
    bannerService.deleteBanner(new DeleteBannerCommand(bannerId));
    return ResponseEntity.ok().build();
  }

  private void requireAdmin(Claims claims) {
    if (!Role.isAuthorized(claims, Role.ADMIN)) {
      throw new ForbiddenException();
    }
  }
}
