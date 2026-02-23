package edu.handong.csee.histudy.service;

import edu.handong.csee.histudy.domain.StudyApplicant;
import edu.handong.csee.histudy.domain.StudyGroup;
import edu.handong.csee.histudy.domain.User;
import edu.handong.csee.histudy.repository.AcademicTermRepository;
import edu.handong.csee.histudy.repository.CourseRepository;
import edu.handong.csee.histudy.repository.StudyApplicantRepository;
import edu.handong.csee.histudy.repository.StudyGroupRepository;
import edu.handong.csee.histudy.repository.StudyReportRepository;
import edu.handong.csee.histudy.repository.UserRepository;
import edu.handong.csee.histudy.service.repository.fake.FakeAcademicTermRepository;
import edu.handong.csee.histudy.service.repository.fake.FakeCourseRepository;
import edu.handong.csee.histudy.service.repository.fake.FakeStudyApplicationRepository;
import edu.handong.csee.histudy.service.repository.fake.FakeStudyGroupRepository;
import edu.handong.csee.histudy.service.repository.fake.FakeStudyReportRepository;
import edu.handong.csee.histudy.service.repository.fake.FakeUserRepository;
import edu.handong.csee.histudy.util.ImagePathMapper;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.util.ReflectionTestUtils;

abstract class TeamServiceTestSupport {

  protected TeamService teamService;
  protected StudyGroupRepository studyGroupRepository;
  protected UserRepository userRepository;
  protected AcademicTermRepository academicTermRepository;
  protected StudyApplicantRepository studyApplicantRepository;
  protected StudyReportRepository studyReportRepository;
  protected CourseRepository courseRepository;

  @BeforeEach
  void initTeamService() {
    studyGroupRepository = new FakeStudyGroupRepository();
    userRepository = new FakeUserRepository();
    academicTermRepository = new FakeAcademicTermRepository();
    studyApplicantRepository = new FakeStudyApplicationRepository();
    studyReportRepository = new FakeStudyReportRepository();
    courseRepository = new FakeCourseRepository();

    ImagePathMapper imagePathMapper = new ImagePathMapper();
    ReflectionTestUtils.setField(imagePathMapper, "origin", "http://localhost:8080");
    ReflectionTestUtils.setField(imagePathMapper, "imageBasePath", "/reports/images/");

    teamService =
        new TeamService(
            studyGroupRepository,
            userRepository,
            academicTermRepository,
            studyApplicantRepository,
            studyReportRepository,
            imagePathMapper);
  }

  protected boolean containsUsers(StudyGroup group, User... expectedUsers) {
    Set<User> groupUsers =
        group.getMembers().stream().map(StudyApplicant::getUser).collect(Collectors.toSet());
    return Arrays.stream(expectedUsers).allMatch(groupUsers::contains);
  }
}
