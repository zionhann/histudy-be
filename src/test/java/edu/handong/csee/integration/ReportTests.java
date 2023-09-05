package edu.handong.csee.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.handong.csee.histudy.HistudyApplication;
import edu.handong.csee.histudy.controller.form.ReportForm;
import edu.handong.csee.histudy.domain.Role;
import edu.handong.csee.histudy.domain.StudyGroup;
import edu.handong.csee.histudy.domain.User;
import edu.handong.csee.histudy.dto.ReportDto;
import edu.handong.csee.histudy.repository.GroupReportRepository;
import edu.handong.csee.histudy.repository.StudyGroupRepository;
import edu.handong.csee.histudy.repository.UserRepository;
import edu.handong.csee.histudy.service.ReportService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = HistudyApplication.class)
@AutoConfigureMockMvc
@Transactional
public class ReportTests {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    private GroupReportRepository groupReportRepository;

    @Autowired
    ReportService reportService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private StudyGroupRepository studyGroupRepository;

    @DisplayName("관리자 권한으로 보고서를 열람할 수 있어야 함")
    @Test
    void ReportTests_18(@Value("${custom.jwt.secret.admin}") String adminToken) throws Exception {
        // Given
        User writer = User.builder()
                .sub("123")
                .sid("21800012")
                .name("test")
                .email("test@example.com")
                .role(Role.USER)
                .build();
        StudyGroup studyGroup = new StudyGroup(1, List.of(writer));

        userRepository.save(writer);
        studyGroupRepository.save(studyGroup);

        ReportForm form = ReportForm.builder()
                .title("title")
                .content("content")
                .totalMinutes(10L)
                .participants(List.of(writer.getSid()))
                .courses(List.of())
                .build();

        ReportDto.ReportInfo report = reportService.createReport(form, writer.getEmail());
        // When

        MvcResult mvcResult = mvc
                .perform(get("/api/team/reports/{id}", report.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();

        ReportDto.ReportInfo res =
                mapper.readValue(mvcResult.getResponse().getContentAsString(), ReportDto.ReportInfo.class);

        // Then
        assertThat(res.getTitle()).isEqualTo("title");
        assertThat(res.getContent()).isEqualTo("content");
    }
}
