package edu.handong.csee.histudy.report;

import edu.handong.csee.histudy.domain.Group;
import edu.handong.csee.histudy.domain.Report;
import edu.handong.csee.histudy.domain.Role;
import edu.handong.csee.histudy.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
@ActiveProfiles("dev")
public class ManageReportTests {

    @DisplayName("참여한 인원을 선택할 수 있다.")
    @Test
    public void ManageReportTests_16() {
        // given
        User user1 = User.builder()
                .id("123")
                .sid("22300000")
                .email("test@histudy.com")
                .name("user1")
                .role(Role.USER)
                .build();
        User user2 = User.builder()
                .id("123")
                .sid("22300000")
                .email("test@histudy.com")
                .name("user2")
                .role(Role.USER)
                .build();
        Report report = Report.builder()
                .title("title")
                .content("content")
                .startTime(LocalTime.of(12, 30))
                .endTime(LocalTime.of(13, 30))
                .build();

        Group group = new Group(1);
        user1.belongTo(group);
        user2.belongTo(group);
        report.writtenBy(group);

        // when
        report.addParticipants(List.of(user1));

        // then
        assertEquals(2, report.getGroup().getUsers().size());
        assertEquals(1, report.getParticipants().size());
    }
}
