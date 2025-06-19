package edu.handong.csee.histudy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import edu.handong.csee.histudy.domain.*;
import edu.handong.csee.histudy.exception.*;
import edu.handong.csee.histudy.repository.*;
import edu.handong.csee.histudy.service.repository.fake.*;
import edu.handong.csee.histudy.util.ImagePathMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
public class ImageServiceTest {

  private ImageService imageService;

  private final String origin = "http://localhost:8080";
  private final String imageBasePath = "/reports/images/";

  @TempDir private File tempDir;

  @BeforeEach
  void init() {
    StudyGroupRepository studyGroupRepository = new FakeStudyGroupRepository();
    UserRepository userRepository = new FakeUserRepository();
    AcademicTermRepository academicTermRepository = new FakeAcademicTermRepository();
    StudyApplicantRepository studyApplicantRepository = new FakeStudyApplicationRepository();
    StudyReportRepository studyReportRepository = new FakeStudyReportRepository();
    CourseRepository courseRepository = new FakeCourseRepository();
    ImagePathMapper imagePathMapper = new ImagePathMapper();

    ReflectionTestUtils.setField(imagePathMapper, "origin", origin);
    ReflectionTestUtils.setField(imagePathMapper, "imageBasePath", imageBasePath);

    imageService =
        new ImageService(
            academicTermRepository,
            userRepository,
            studyReportRepository,
            studyApplicantRepository,
            imagePathMapper,
            studyGroupRepository);

    ReflectionTestUtils.setField(imageService, "imageBaseLocation", tempDir + File.separator);

    // Setup
    AcademicTerm term =
        AcademicTerm.builder().academicYear(2025).semester(TermType.SPRING).isCurrent(true).build();
    academicTermRepository.save(term);

    User student1 =
        User.builder().sub("1").sid("22500101").email("user1@test.com").name("Foo").build();

    User student2 =
        User.builder().sub("2").sid("22500102").email("user2@test.com").name("Bar").build();

    userRepository.save(student1);
    userRepository.save(student2);

    Course course = new Course("Introduction to Test", "ECE00103", "John", term);
    courseRepository.saveAll(List.of(course));

    StudyApplicant studyApplicant1 =
        StudyApplicant.of(term, student1, List.of(student2), List.of(course));

    StudyApplicant studyApplicant2 =
        StudyApplicant.of(term, student2, List.of(student1), List.of(course));

    studyApplicantRepository.save(studyApplicant1);
    studyApplicantRepository.save(studyApplicant2);

    StudyGroup studyGroup = StudyGroup.of(1, term, List.of(studyApplicant1, studyApplicant2));
    studyGroupRepository.save(studyGroup);

    StudyReport report =
        StudyReport.builder()
            .title("title")
            .content("content")
            .totalMinutes(60L)
            .courses(List.of(course))
            .participants(List.of(student1))
            .images(List.of("/path/to/image1.png"))
            .studyGroup(studyGroup)
            .build();
    studyReportRepository.save(report);

    IntStream.range(0, report.getImages().size())
        .forEach(
            i ->
                ReflectionTestUtils.setField(
                    report.getImages().get(i), "reportImageId", (long) i + 1));
  }

  @Test
  void 스터디보고서_이미지_업로드_최초() throws IOException {
    // Given
    MultipartFile data = mock(MultipartFile.class);
    doNothing().when(data).transferTo(any(File.class));

    // When
    String imagePath = imageService.getImagePaths("user1@test.com", data, Optional.empty());

    // Then
    assertThat(imagePath).contains(origin, imageBasePath);
  }

  @Test
  void 스터디보고서_이미지_업로드_수정() throws IOException {
    try (MockedStatic<Files> fileMockedStatic = Mockito.mockStatic(Files.class)) {
      MultipartFile data = mock(MultipartFile.class);
      doNothing().when(data).transferTo(any(File.class));

      byte[] sameContent = "content".getBytes();
      fileMockedStatic.when(() -> Files.readAllBytes(any(Path.class))).thenReturn(sameContent);

      String imagePath = imageService.getImagePaths("user1@test.com", data, Optional.of(1L));

      assertThat(imagePath).contains(origin, imageBasePath);
    }
  }

  @Test
  void 스터디보고서_이미지_업로드_파일명이null인경우() throws IOException {
    // Given
    MultipartFile data = mock(MultipartFile.class);
    when(data.getOriginalFilename()).thenReturn(null);
    doNothing().when(data).transferTo(any(File.class));

    // When
    String imagePath = imageService.getImagePaths("user1@test.com", data, Optional.empty());

    // Then
    assertThat(imagePath).contains(origin, imageBasePath);
    assertThat(imagePath).contains(".jpg");
  }

  @Test
  void 스터디보고서_이미지_업로드_파일전송실패() throws IOException {
    // Given
    MultipartFile data = mock(MultipartFile.class);
    when(data.getOriginalFilename()).thenReturn("test.png");
    doThrow(new IOException("Transfer failed")).when(data).transferTo(any(File.class));

    // When & Then
    assertThatThrownBy(() -> imageService.getImagePaths("user1@test.com", data, Optional.empty()))
        .isInstanceOf(FileTransferException.class);
  }

  @Test
  void 스터디보고서_이미지_업로드_존재하지않는사용자() {
    // Given
    MultipartFile data = mock(MultipartFile.class);

    // When & Then
    assertThatThrownBy(() -> imageService.getImagePaths("nonexistent@test.com", data, Optional.empty()))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  void 스터디보고서_이미지_업로드_현재학기없음() {
    // Given
    MultipartFile data = mock(MultipartFile.class);
    
    // Setup - remove current term
    AcademicTermRepository academicTermRepository = new FakeAcademicTermRepository();
    UserRepository userRepository = new FakeUserRepository();
    StudyReportRepository studyReportRepository = new FakeStudyReportRepository();
    StudyApplicantRepository studyApplicantRepository = new FakeStudyApplicationRepository();
    StudyGroupRepository studyGroupRepository = new FakeStudyGroupRepository();
    ImagePathMapper imagePathMapper = new ImagePathMapper();

    ImageService testImageService = new ImageService(
        academicTermRepository,
        userRepository,
        studyReportRepository,
        studyApplicantRepository,
        imagePathMapper,
        studyGroupRepository);

    User student = User.builder().sub("1").sid("22500101").email("user1@test.com").name("Foo").build();
    userRepository.save(student);

    // When & Then
    assertThatThrownBy(() -> testImageService.getImagePaths("user1@test.com", data, Optional.empty()))
        .isInstanceOf(NoCurrentTermFoundException.class);
  }

  @Test
  void 스터디보고서_이미지_업로드_스터디그룹없음() {
    // Given
    MultipartFile data = mock(MultipartFile.class);
    
    // Setup - term exists but no study group
    AcademicTermRepository academicTermRepository = new FakeAcademicTermRepository();
    UserRepository userRepository = new FakeUserRepository();
    StudyReportRepository studyReportRepository = new FakeStudyReportRepository();
    StudyApplicantRepository studyApplicantRepository = new FakeStudyApplicationRepository();
    StudyGroupRepository studyGroupRepository = new FakeStudyGroupRepository();
    ImagePathMapper imagePathMapper = new ImagePathMapper();

    ImageService testImageService = new ImageService(
        academicTermRepository,
        userRepository,
        studyReportRepository,
        studyApplicantRepository,
        imagePathMapper,
        studyGroupRepository);

    AcademicTerm term = AcademicTerm.builder().academicYear(2025).semester(TermType.SPRING).isCurrent(true).build();
    academicTermRepository.save(term);

    User student = User.builder().sub("1").sid("22500101").email("user1@test.com").name("Foo").build();
    userRepository.save(student);

    // When & Then
    assertThatThrownBy(() -> testImageService.getImagePaths("user1@test.com", data, Optional.empty()))
        .isInstanceOf(StudyGroupNotFoundException.class);
  }

  @Test
  void 스터디보고서_이미지_업로드_존재하지않는리포트() {
    // Given
    MultipartFile data = mock(MultipartFile.class);

    // When & Then
    assertThatThrownBy(() -> imageService.getImagePaths("user1@test.com", data, Optional.of(999L)))
        .isInstanceOf(ReportNotFoundException.class);
  }
}
