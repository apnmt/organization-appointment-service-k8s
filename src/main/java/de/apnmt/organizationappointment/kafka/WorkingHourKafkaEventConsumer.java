package de.apnmt.organizationappointment.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.apnmt.common.TopicConstants;
import de.apnmt.common.event.ApnmtEvent;
import de.apnmt.common.event.value.WorkingHourEventDTO;
import de.apnmt.organizationappointment.common.async.controller.WorkingHourEventConsumer;
import de.apnmt.organizationappointment.common.service.WorkingHourService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class WorkingHourKafkaEventConsumer extends WorkingHourEventConsumer {

    private static final TypeReference<ApnmtEvent<WorkingHourEventDTO>> EVENT_TYPE = new TypeReference<>() {
    };

    private final Logger log = LoggerFactory.getLogger(WorkingHourKafkaEventConsumer.class);

    private final ObjectMapper objectMapper;

    public WorkingHourKafkaEventConsumer(WorkingHourService workingHourService, ObjectMapper objectMapper) {
        super(workingHourService);
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = {TopicConstants.WORKING_HOUR_CHANGED_TOPIC})
    public void receiveEvent(@Payload String message) {
        try {
            log.info("Received event {} from kafka topic {}", message, TopicConstants.WORKING_HOUR_CHANGED_TOPIC);
            ApnmtEvent<WorkingHourEventDTO> event = this.objectMapper.readValue(message, EVENT_TYPE);
            super.receiveEvent(event);
        } catch (JsonProcessingException e) {
            this.log.error("Malformed message {} for topic {}. Event will be ignored.", message, TopicConstants.WORKING_HOUR_CHANGED_TOPIC);
        }
    }
}
