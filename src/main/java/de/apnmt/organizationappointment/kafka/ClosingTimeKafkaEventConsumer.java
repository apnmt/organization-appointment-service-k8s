package de.apnmt.organizationappointment.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.apnmt.common.TopicConstants;
import de.apnmt.common.event.ApnmtEvent;
import de.apnmt.common.event.value.ClosingTimeEventDTO;
import de.apnmt.organizationappointment.common.async.controller.ClosingTimeEventConsumer;
import de.apnmt.organizationappointment.common.service.ClosingTimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class ClosingTimeKafkaEventConsumer extends ClosingTimeEventConsumer {

    private static final TypeReference<ApnmtEvent<ClosingTimeEventDTO>> EVENT_TYPE = new TypeReference<>() {
    };

    private final Logger log = LoggerFactory.getLogger(ClosingTimeKafkaEventConsumer.class);

    private final ObjectMapper objectMapper;

    public ClosingTimeKafkaEventConsumer(ClosingTimeService closingTimeService, ObjectMapper objectMapper) {
        super(closingTimeService);
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = {TopicConstants.CLOSING_TIME_CHANGED_TOPIC})
    public void receiveEvent(@Payload String message) {
        try {
            ApnmtEvent<ClosingTimeEventDTO> event = this.objectMapper.readValue(message, EVENT_TYPE);
            super.receiveEvent(event);
        } catch (JsonProcessingException e) {
            this.log.error("Malformed message {} for topic {}. Event will be ignored.", message, TopicConstants.CLOSING_TIME_CHANGED_TOPIC);
        }
    }
}
