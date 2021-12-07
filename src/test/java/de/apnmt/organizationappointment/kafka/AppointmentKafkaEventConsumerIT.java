package de.apnmt.organizationappointment.kafka;

import de.apnmt.common.ApnmtTestUtil;
import de.apnmt.common.TopicConstants;
import de.apnmt.common.event.ApnmtEvent;
import de.apnmt.common.event.ApnmtEventType;
import de.apnmt.common.event.value.AppointmentEventDTO;
import de.apnmt.k8s.common.test.AbstractKafkaConsumerIT;
import de.apnmt.organizationappointment.OrganizationappointmentserviceApp;
import de.apnmt.organizationappointment.common.domain.Appointment;
import de.apnmt.organizationappointment.common.repository.AppointmentRepository;
import de.apnmt.organizationappointment.common.service.mapper.AppointmentEventMapper;
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
@EmbeddedKafka(partitions = 1, topics = {TopicConstants.APPOINTMENT_CHANGED_TOPIC})
@SpringBootTest(classes = OrganizationappointmentserviceApp.class, properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
@AutoConfigureMockMvc
@DirtiesContext
public class AppointmentKafkaEventConsumerIT extends AbstractKafkaConsumerIT {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private AppointmentEventMapper appointmentEventMapper;

    @BeforeEach
    public void initTest() {
        this.appointmentRepository.deleteAll();
        this.waitForAssignment();
    }

    @Test
    public void appointmentCreatedTest() throws InterruptedException {
        int databaseSizeBeforeCreate = this.appointmentRepository.findAll().size();
        ApnmtEvent<AppointmentEventDTO> event = ApnmtTestUtil.createAppointmentEvent(ApnmtEventType.appointmentCreated);

        this.kafkaTemplate.send(TopicConstants.APPOINTMENT_CHANGED_TOPIC, event);

        Thread.sleep(1000);

        List<Appointment> appointments = this.appointmentRepository.findAll();
        assertThat(appointments).hasSize(databaseSizeBeforeCreate + 1);
        Appointment appointment = appointments.get(appointments.size() - 1);
        AppointmentEventDTO appointmentEventDTO = event.getValue();
        assertThat(appointment.getId()).isEqualTo(appointmentEventDTO.getId());
        assertThat(appointment.getStartAt()).isEqualTo(appointmentEventDTO.getStartAt());
        assertThat(appointment.getEndAt()).isEqualTo(appointmentEventDTO.getEndAt());
        assertThat(appointment.getEmployeeId()).isEqualTo(appointmentEventDTO.getEmployeeId());
        assertThat(appointment.getOrganizationId()).isEqualTo(appointmentEventDTO.getOrganizationId());
    }

    @Test
    public void appointmentUpdatedTest() throws InterruptedException {
        AppointmentEventDTO eventDTO = ApnmtTestUtil.createAppointmentEventDTO();
        Appointment apnmt = this.appointmentEventMapper.toEntity(eventDTO);
        this.appointmentRepository.save(apnmt);

        int databaseSizeBeforeCreate = this.appointmentRepository.findAll().size();
        ApnmtEvent<AppointmentEventDTO> event = ApnmtTestUtil.createAppointmentEvent(ApnmtEventType.appointmentCreated);
        event.getValue().setEmployeeId(10L);

        this.kafkaTemplate.send(TopicConstants.APPOINTMENT_CHANGED_TOPIC, event);

        Thread.sleep(1000);

        List<Appointment> appointments = this.appointmentRepository.findAll();
        assertThat(appointments).hasSize(databaseSizeBeforeCreate);
        Appointment appointment = appointments.get(appointments.size() - 1);
        AppointmentEventDTO appointmentEventDTO = event.getValue();
        assertThat(appointment.getId()).isEqualTo(appointmentEventDTO.getId());
        assertThat(appointment.getStartAt()).isEqualTo(appointmentEventDTO.getStartAt());
        assertThat(appointment.getEndAt()).isEqualTo(appointmentEventDTO.getEndAt());
        assertThat(appointment.getEmployeeId()).isNotEqualTo(eventDTO.getEmployeeId());
        assertThat(appointment.getEmployeeId()).isEqualTo(appointmentEventDTO.getEmployeeId());
        assertThat(appointment.getOrganizationId()).isEqualTo(appointmentEventDTO.getOrganizationId());
    }

    @Test
    public void appointmentDeletedTest() throws InterruptedException {
        ApnmtEvent<AppointmentEventDTO> event = ApnmtTestUtil.createAppointmentEvent(ApnmtEventType.appointmentDeleted);
        Appointment appointment = this.appointmentEventMapper.toEntity(event.getValue());
        this.appointmentRepository.save(appointment);

        int databaseSizeBeforeCreate = this.appointmentRepository.findAll().size();

        this.kafkaTemplate.send(TopicConstants.APPOINTMENT_CHANGED_TOPIC, event);

        Thread.sleep(1000);

        List<Appointment> appointments = this.appointmentRepository.findAll();
        assertThat(appointments).hasSize(databaseSizeBeforeCreate - 1);
    }

}
