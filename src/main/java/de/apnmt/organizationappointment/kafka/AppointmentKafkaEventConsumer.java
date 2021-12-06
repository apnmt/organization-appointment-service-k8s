package de.apnmt.organizationappointment.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.apnmt.common.TopicConstants;
import de.apnmt.common.event.ApnmtEvent;
import de.apnmt.common.event.value.AppointmentEventDTO;
import de.apnmt.organizationappointment.common.async.controller.AppointmentEventConsumer;
import de.apnmt.organizationappointment.common.service.AppointmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class AppointmentKafkaEventConsumer extends AppointmentEventConsumer {

    private static final TypeReference<ApnmtEvent<AppointmentEventDTO>> EVENT_TYPE = new TypeReference<>() {
    };

    private final Logger log = LoggerFactory.getLogger(AppointmentKafkaEventConsumer.class);

    private final ObjectMapper objectMapper;

    public AppointmentKafkaEventConsumer(AppointmentService appointmentService, ObjectMapper objectMapper) {
        super(appointmentService);
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = {TopicConstants.APPOINTMENT_CHANGED_TOPIC})
    public void receiveEvent(@Payload String message) {
        try {
            ApnmtEvent<AppointmentEventDTO> event = this.objectMapper.readValue(message, EVENT_TYPE);
            super.receiveEvent(event);
        } catch (JsonProcessingException e) {
            this.log.error("Malformed message {} for topic {}. Event will be ignored.", message, TopicConstants.APPOINTMENT_CHANGED_TOPIC);
        }
    }
}
