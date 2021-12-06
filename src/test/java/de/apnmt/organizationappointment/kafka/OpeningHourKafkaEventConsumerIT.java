package de.apnmt.organizationappointment.kafka;

import de.apnmt.common.ApnmtTestUtil;
import de.apnmt.common.TopicConstants;
import de.apnmt.common.event.ApnmtEvent;
import de.apnmt.common.event.ApnmtEventType;
import de.apnmt.common.event.value.OpeningHourEventDTO;
import de.apnmt.k8s.common.test.AbstractKafkaConsumerIT;
import de.apnmt.organizationappointment.OrganizationappointmentserviceApp;
import de.apnmt.organizationappointment.common.domain.OpeningHour;
import de.apnmt.organizationappointment.common.repository.OpeningHourRepository;
import de.apnmt.organizationappointment.common.service.mapper.OpeningHourEventMapper;
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
@EmbeddedKafka(partitions = 1, topics = {TopicConstants.OPENING_HOUR_CHANGED_TOPIC})
@SpringBootTest(classes = OrganizationappointmentserviceApp.class, properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
@AutoConfigureMockMvc
@DirtiesContext
public class OpeningHourKafkaEventConsumerIT extends AbstractKafkaConsumerIT {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private OpeningHourRepository openingHourRepository;

    @Autowired
    private OpeningHourEventMapper openingHourEventMapper;

    @BeforeEach
    public void initTest() {
        this.openingHourRepository.deleteAll();
        this.waitForAssignment();
    }

    @Test
    public void openingHourCreatedTest() throws InterruptedException {
        int databaseSizeBeforeCreate = this.openingHourRepository.findAll().size();
        ApnmtEvent<OpeningHourEventDTO> event = ApnmtTestUtil.createOpeningHourEvent(ApnmtEventType.openingHourCreated);

        this.kafkaTemplate.send(TopicConstants.OPENING_HOUR_CHANGED_TOPIC, event);

        Thread.sleep(1000);

        List<OpeningHour> openingHours = this.openingHourRepository.findAll();
        assertThat(openingHours).hasSize(databaseSizeBeforeCreate + 1);
        OpeningHour openingHour = openingHours.get(openingHours.size() - 1);
        OpeningHourEventDTO openingHourEventDTO = event.getValue();
        assertThat(openingHour.getId()).isEqualTo(openingHourEventDTO.getId());
        assertThat(openingHour.getStartTime()).isEqualTo(openingHourEventDTO.getStartTime());
        assertThat(openingHour.getEndTime()).isEqualTo(openingHourEventDTO.getEndTime());
        assertThat(openingHour.getDay()).isEqualTo(openingHourEventDTO.getDay());
        assertThat(openingHour.getOrganizationId()).isEqualTo(openingHourEventDTO.getOrganizationId());
    }

    @Test
    public void openingHourDeletedTest() throws InterruptedException {
        ApnmtEvent<OpeningHourEventDTO> event = ApnmtTestUtil.createOpeningHourEvent(ApnmtEventType.openingHourDeleted);
        OpeningHour openingHour = this.openingHourEventMapper.toEntity(event.getValue());
        this.openingHourRepository.save(openingHour);

        int databaseSizeBeforeCreate = this.openingHourRepository.findAll().size();

        this.kafkaTemplate.send(TopicConstants.OPENING_HOUR_CHANGED_TOPIC, event);

        Thread.sleep(1000);

        List<OpeningHour> openingHours = this.openingHourRepository.findAll();
        assertThat(openingHours).hasSize(databaseSizeBeforeCreate - 1);
    }

}
