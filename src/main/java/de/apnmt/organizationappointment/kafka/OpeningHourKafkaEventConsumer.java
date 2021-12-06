package de.apnmt.organizationappointment.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.apnmt.common.TopicConstants;
import de.apnmt.common.event.ApnmtEvent;
import de.apnmt.common.event.value.OpeningHourEventDTO;
import de.apnmt.organizationappointment.common.async.controller.OpeningHourEventConsumer;
import de.apnmt.organizationappointment.common.service.OpeningHourService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class OpeningHourKafkaEventConsumer extends OpeningHourEventConsumer {

    private static final TypeReference<ApnmtEvent<OpeningHourEventDTO>> EVENT_TYPE = new TypeReference<>() {
    };

    private final Logger log = LoggerFactory.getLogger(OpeningHourKafkaEventConsumer.class);

    private final ObjectMapper objectMapper;

    public OpeningHourKafkaEventConsumer(OpeningHourService openingHourService, ObjectMapper objectMapper) {
        super(openingHourService);
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = {TopicConstants.OPENING_HOUR_CHANGED_TOPIC})
    public void receiveEvent(@Payload String message) {
        try {
            ApnmtEvent<OpeningHourEventDTO> event = this.objectMapper.readValue(message, EVENT_TYPE);
            super.receiveEvent(event);
        } catch (JsonProcessingException e) {
            this.log.error("Malformed message {} for topic {}. Event will be ignored.", message, TopicConstants.OPENING_HOUR_CHANGED_TOPIC);
        }
    }
}
