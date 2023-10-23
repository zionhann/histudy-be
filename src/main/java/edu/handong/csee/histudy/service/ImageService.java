package edu.handong.csee.histudy.service;

import edu.handong.csee.histudy.domain.Image;
import edu.handong.csee.histudy.exception.FileTransferException;
import edu.handong.csee.histudy.exception.ReportNotFoundException;
import edu.handong.csee.histudy.repository.GroupReportRepository;
import edu.handong.csee.histudy.util.Utils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.springframework.util.ResourceUtils.isUrl;

@Service
@Transactional
public class ImageService {

    @Value("${custom.resource.location}")
    private String imageBaseLocation;
    private final GroupReportRepository groupReportRepository;

    public ImageService(GroupReportRepository groupReportRepository) {
        this.groupReportRepository = groupReportRepository;
    }

    public String getImagePaths(MultipartFile imageAsFormData, Integer tag, Optional<Long> reportIdOr) {
        if (reportIdOr.isPresent()) {
            Long id = reportIdOr.get();
            Optional<String> sameResource = getSameContent(imageAsFormData, id);

            if (sameResource.isPresent()) {
                return sameResource.get();
            }
        }
        int year = Utils.getCurrentYear();
        int semester = Utils.getCurrentSemester();
        String formattedDateTime = Utils.getCurrentFormattedDateTime("yyyyMMdd_HHmmss");

        String originalName = Objects.requireNonNullElse(imageAsFormData.getOriginalFilename(), ".jpg");
        String extension = originalName.substring(originalName.lastIndexOf("."));

        // yyyy-{1|2}-group{%02d}-report_{yyyyMMdd}_{HHmmss}.{extension}
        // e.g. 2023-2-group1-report_20230923_123456.jpg
        String pathname = String.format("%d-%d-group%02d-report_%s%s",
                year,
                semester,
                tag,
                formattedDateTime,
                extension);
        return saveImage(imageAsFormData, pathname);
    }

    private String saveImage(
            MultipartFile image,
            String pathname) {
        try {
            File file = new File(imageBaseLocation + pathname);
            File dir = file.getParentFile();

            if (!dir.exists()) {
                dir.mkdirs();
            }
            image.transferTo(file);
            return pathname;
        } catch (IOException e) {
            throw new FileTransferException();
        }
    }

    private Optional<String> getSameContent(MultipartFile src, Long reportId) {
        List<String> targetPaths = groupReportRepository.findById(reportId)
                .orElseThrow(ReportNotFoundException::new)
                .getImages()
                .stream()
                .map(Image::getPath)
                .toList();

        return targetPaths.stream()
                .filter(path -> {
                    try {
                        return (isUrl(path))
                                ? contentMatches(src, new URL(path))
                                : contentMatches(src, Path.of(imageBaseLocation + path));
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                }).findAny();
    }

    private boolean contentMatches(MultipartFile src, Path targetPath) {
        try {
            byte[] targetContent = Files.readAllBytes(targetPath);
            return contentMatches(src.getBytes(), targetContent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean contentMatches(MultipartFile src, URL targetPath) {
        try (InputStream in = targetPath.openStream()) {
            byte[] targetContent = IOUtils.toByteArray(in);
            return contentMatches(src.getBytes(), targetContent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean contentMatches(byte[] sourceContent, byte[] targetContent) {
        return Arrays.equals(sourceContent, targetContent);
    }

    public Resource fetchImage(String imageName) {
        try {
            Path path = Paths.get(imageBaseLocation + imageName);
            Resource resource = new UrlResource(path.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            throw new RuntimeException();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
