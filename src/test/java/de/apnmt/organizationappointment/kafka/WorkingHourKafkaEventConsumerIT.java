package de.apnmt.organizationappointment.kafka;

import de.apnmt.common.ApnmtTestUtil;
import de.apnmt.common.TopicConstants;
import de.apnmt.common.event.ApnmtEvent;
import de.apnmt.common.event.ApnmtEventType;
import de.apnmt.common.event.value.WorkingHourEventDTO;
import de.apnmt.k8s.common.test.AbstractKafkaConsumerIT;
import de.apnmt.organizationappointment.OrganizationappointmentserviceApp;
import de.apnmt.organizationappointment.common.domain.WorkingHour;
import de.apnmt.organizationappointment.common.repository.WorkingHourRepository;
import de.apnmt.organizationappointment.common.service.mapper.WorkingHourEventMapper;
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
@EmbeddedKafka(partitions = 1, topics = {TopicConstants.WORKING_HOUR_CHANGED_TOPIC})
@SpringBootTest(classes = OrganizationappointmentserviceApp.class, properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
@AutoConfigureMockMvc
@DirtiesContext
public class WorkingHourKafkaEventConsumerIT extends AbstractKafkaConsumerIT {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private WorkingHourRepository workingHourRepository;

    @Autowired
    private WorkingHourEventMapper workingHourEventMapper;

    @BeforeEach
    public void initTest() {
        this.workingHourRepository.deleteAll();
        this.waitForAssignment();
    }

    @Test
    public void workingHourCreatedTest() throws InterruptedException {
        int databaseSizeBeforeCreate = this.workingHourRepository.findAll().size();
        ApnmtEvent<WorkingHourEventDTO> event = ApnmtTestUtil.createWorkingHourEvent(ApnmtEventType.workingHourCreated);

        this.kafkaTemplate.send(TopicConstants.WORKING_HOUR_CHANGED_TOPIC, event);

        Thread.sleep(1000);

        List<WorkingHour> workingHours = this.workingHourRepository.findAll();
        assertThat(workingHours).hasSize(databaseSizeBeforeCreate + 1);
        WorkingHour workingHour = workingHours.get(workingHours.size() - 1);
        WorkingHourEventDTO workingHourEventDTO = event.getValue();
        assertThat(workingHour.getId()).isEqualTo(workingHourEventDTO.getId());
        assertThat(workingHour.getStartAt()).isEqualTo(workingHourEventDTO.getStartAt());
        assertThat(workingHour.getEndAt()).isEqualTo(workingHourEventDTO.getEndAt());
        assertThat(workingHour.getEmployeeId()).isEqualTo(workingHourEventDTO.getEmployeeId());
    }

    @Test
    public void workingHourUpdatedTest() throws InterruptedException {
        WorkingHourEventDTO eventDTO = ApnmtTestUtil.createWorkingHourEventDTO();
        WorkingHour wh = this.workingHourEventMapper.toEntity(eventDTO);
        this.workingHourRepository.save(wh);

        int databaseSizeBeforeCreate = this.workingHourRepository.findAll().size();
        ApnmtEvent<WorkingHourEventDTO> event = ApnmtTestUtil.createWorkingHourEvent(ApnmtEventType.workingHourCreated);
        event.getValue().setEmployeeId(10L);

        this.kafkaTemplate.send(TopicConstants.WORKING_HOUR_CHANGED_TOPIC, event);

        Thread.sleep(1000);

        List<WorkingHour> workingHours = this.workingHourRepository.findAll();
        assertThat(workingHours).hasSize(databaseSizeBeforeCreate);
        WorkingHour workingHour = workingHours.get(workingHours.size() - 1);
        WorkingHourEventDTO workingHourEventDTO = event.getValue();
        assertThat(workingHour.getId()).isEqualTo(workingHourEventDTO.getId());
        assertThat(workingHour.getStartAt()).isEqualTo(workingHourEventDTO.getStartAt());
        assertThat(workingHour.getEndAt()).isEqualTo(workingHourEventDTO.getEndAt());
        assertThat(workingHour.getEmployeeId()).isNotEqualTo(eventDTO.getEmployeeId());
        assertThat(workingHour.getEmployeeId()).isEqualTo(workingHourEventDTO.getEmployeeId());
    }

    @Test
    public void workingHourDeletedTest() throws InterruptedException {
        ApnmtEvent<WorkingHourEventDTO> event = ApnmtTestUtil.createWorkingHourEvent(ApnmtEventType.workingHourDeleted);
        WorkingHour workingHour = this.workingHourEventMapper.toEntity(event.getValue());
        this.workingHourRepository.save(workingHour);

        int databaseSizeBeforeCreate = this.workingHourRepository.findAll().size();

        this.kafkaTemplate.send(TopicConstants.WORKING_HOUR_CHANGED_TOPIC, event);

        Thread.sleep(1000);

        List<WorkingHour> workingHours = this.workingHourRepository.findAll();
        assertThat(workingHours).hasSize(databaseSizeBeforeCreate - 1);
    }

}
