package de.apnmt.organizationappointment.kafka;

import de.apnmt.common.ApnmtTestUtil;
import de.apnmt.common.TopicConstants;
import de.apnmt.common.event.ApnmtEvent;
import de.apnmt.common.event.ApnmtEventType;
import de.apnmt.common.event.value.ServiceEventDTO;
import de.apnmt.k8s.common.test.AbstractKafkaConsumerIT;
import de.apnmt.organizationappointment.OrganizationappointmentserviceApp;
import de.apnmt.organizationappointment.common.domain.Service;
import de.apnmt.organizationappointment.common.repository.ServiceRepository;
import de.apnmt.organizationappointment.common.service.mapper.ServiceEventMapper;
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
@EmbeddedKafka(partitions = 1, topics = {TopicConstants.SERVICE_CHANGED_TOPIC})
@SpringBootTest(classes = OrganizationappointmentserviceApp.class, properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
@AutoConfigureMockMvc
@DirtiesContext
public class ServiceKafkaEventConsumerIT extends AbstractKafkaConsumerIT {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private ServiceEventMapper serviceEventMapper;

    @BeforeEach
    public void initTest() {
        this.serviceRepository.deleteAll();
        this.waitForAssignment();
    }

    @Test
    public void serviceCreatedTest() throws InterruptedException {
        int databaseSizeBeforeCreate = this.serviceRepository.findAll().size();
        ApnmtEvent<ServiceEventDTO> event = ApnmtTestUtil.createServiceEvent(ApnmtEventType.serviceCreated);

        this.kafkaTemplate.send(TopicConstants.SERVICE_CHANGED_TOPIC, event);

        Thread.sleep(1000);

        List<Service> services = this.serviceRepository.findAll();
        assertThat(services).hasSize(databaseSizeBeforeCreate + 1);
        Service service = services.get(services.size() - 1);
        ServiceEventDTO serviceEventDTO = event.getValue();
        assertThat(service.getId()).isEqualTo(serviceEventDTO.getId());
        assertThat(service.getDuration()).isEqualTo(serviceEventDTO.getDuration());
    }

    @Test
    public void serviceDeletedTest() throws InterruptedException {
        ApnmtEvent<ServiceEventDTO> event = ApnmtTestUtil.createServiceEvent(ApnmtEventType.serviceDeleted);
        Service service = this.serviceEventMapper.toEntity(event.getValue());
        this.serviceRepository.save(service);

        int databaseSizeBeforeCreate = this.serviceRepository.findAll().size();

        this.kafkaTemplate.send(TopicConstants.SERVICE_CHANGED_TOPIC, event);

        Thread.sleep(1000);

        List<Service> services = this.serviceRepository.findAll();
        assertThat(services).hasSize(databaseSizeBeforeCreate - 1);
    }

}
