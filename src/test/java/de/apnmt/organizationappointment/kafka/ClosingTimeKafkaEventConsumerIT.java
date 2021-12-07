package de.apnmt.organizationappointment.kafka;

import de.apnmt.common.ApnmtTestUtil;
import de.apnmt.common.TopicConstants;
import de.apnmt.common.event.ApnmtEvent;
import de.apnmt.common.event.ApnmtEventType;
import de.apnmt.common.event.value.ClosingTimeEventDTO;
import de.apnmt.k8s.common.test.AbstractKafkaConsumerIT;
import de.apnmt.organizationappointment.OrganizationappointmentserviceApp;
import de.apnmt.organizationappointment.common.domain.ClosingTime;
import de.apnmt.organizationappointment.common.repository.ClosingTimeRepository;
import de.apnmt.organizationappointment.common.service.mapper.ClosingTimeEventMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@EnableKafka
@EmbeddedKafka(partitions = 1, topics = {TopicConstants.CLOSING_TIME_CHANGED_TOPIC})
@SpringBootTest(classes = OrganizationappointmentserviceApp.class, properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
@AutoConfigureMockMvc
@DirtiesContext
public class ClosingTimeKafkaEventConsumerIT extends AbstractKafkaConsumerIT {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private ClosingTimeRepository closingTimeRepository;

    @Autowired
    private ClosingTimeEventMapper closingTimeEventMapper;

    @BeforeEach
    public void initTest() {
        this.closingTimeRepository.deleteAll();
        this.waitForAssignment();
    }

    @Test
    public void closingTimeCreatedTest() throws InterruptedException {
        int databaseSizeBeforeCreate = this.closingTimeRepository.findAll().size();
        ApnmtEvent<ClosingTimeEventDTO> event = ApnmtTestUtil.createClosingTimeEvent(ApnmtEventType.closingTimeCreated);

        this.kafkaTemplate.send(TopicConstants.CLOSING_TIME_CHANGED_TOPIC, event);

        Thread.sleep(1000);

        List<ClosingTime> closingTimes = this.closingTimeRepository.findAll();
        assertThat(closingTimes).hasSize(databaseSizeBeforeCreate + 1);
        ClosingTime closingTime = closingTimes.get(closingTimes.size() - 1);
        ClosingTimeEventDTO closingTimeEventDTO = event.getValue();
        assertThat(closingTime.getId()).isEqualTo(closingTimeEventDTO.getId());
        assertThat(closingTime.getStartAt()).isEqualTo(closingTimeEventDTO.getStartAt());
        assertThat(closingTime.getEndAt()).isEqualTo(closingTimeEventDTO.getEndAt());
        assertThat(closingTime.getOrganizationId()).isEqualTo(closingTimeEventDTO.getOrganizationId());
    }

    @Test
    public void closingTimeUpdatedTest() throws InterruptedException {
        ClosingTimeEventDTO eventDTO = ApnmtTestUtil.createClosingTimeEventDTO();
        ClosingTime ct = this.closingTimeEventMapper.toEntity(eventDTO);
        this.closingTimeRepository.save(ct);

        int databaseSizeBeforeCreate = this.closingTimeRepository.findAll().size();
        ApnmtEvent<ClosingTimeEventDTO> event = ApnmtTestUtil.createClosingTimeEvent(ApnmtEventType.closingTimeCreated);
        event.getValue().setOrganizationId(10L);

        this.kafkaTemplate.send(TopicConstants.CLOSING_TIME_CHANGED_TOPIC, event);

        Thread.sleep(1000);

        List<ClosingTime> closingTimes = this.closingTimeRepository.findAll();
        assertThat(closingTimes).hasSize(databaseSizeBeforeCreate);
        ClosingTime closingTime = closingTimes.get(closingTimes.size() - 1);
        ClosingTimeEventDTO closingTimeEventDTO = event.getValue();
        assertThat(closingTime.getId()).isEqualTo(closingTimeEventDTO.getId());
        assertThat(closingTime.getStartAt()).isEqualTo(closingTimeEventDTO.getStartAt());
        assertThat(closingTime.getEndAt()).isEqualTo(closingTimeEventDTO.getEndAt());
        assertThat(closingTime.getOrganizationId()).isNotEqualTo(eventDTO.getOrganizationId());
        assertThat(closingTime.getOrganizationId()).isEqualTo(closingTimeEventDTO.getOrganizationId());
    }

    @Test
    public void closingTimeDeletedTest() throws InterruptedException {
        ApnmtEvent<ClosingTimeEventDTO> event = ApnmtTestUtil.createClosingTimeEvent(ApnmtEventType.closingTimeDeleted);
        ClosingTime closingTime = this.closingTimeEventMapper.toEntity(event.getValue());
        this.closingTimeRepository.save(closingTime);

        int databaseSizeBeforeCreate = this.closingTimeRepository.findAll().size();

        this.kafkaTemplate.send(TopicConstants.CLOSING_TIME_CHANGED_TOPIC, event);

        Thread.sleep(1000);

        List<ClosingTime> closingTimes = this.closingTimeRepository.findAll();
        assertThat(closingTimes).hasSize(databaseSizeBeforeCreate - 1);
    }

}
