package de.apnmt.organizationappointment.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.apnmt.common.TopicConstants;
import de.apnmt.common.event.ApnmtEvent;
import de.apnmt.common.event.value.ServiceEventDTO;
import de.apnmt.organizationappointment.common.async.controller.ServiceEventConsumer;
import de.apnmt.organizationappointment.common.service.ServiceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class ServiceKafkaEventConsumer extends ServiceEventConsumer {

    private static final TypeReference<ApnmtEvent<ServiceEventDTO>> EVENT_TYPE = new TypeReference<>() {
    };

    private final Logger log = LoggerFactory.getLogger(ServiceKafkaEventConsumer.class);

    private final ObjectMapper objectMapper;

    public ServiceKafkaEventConsumer(ServiceService serviceService, ObjectMapper objectMapper) {
        super(serviceService);
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = {TopicConstants.SERVICE_CHANGED_TOPIC})
    public void receiveEvent(@Payload String message) {
        try {
            ApnmtEvent<ServiceEventDTO> event = this.objectMapper.readValue(message, EVENT_TYPE);
            super.receiveEvent(event);
        } catch (JsonProcessingException e) {
            this.log.error("Malformed message {} for topic {}. Event will be ignored.", message, TopicConstants.SERVICE_CHANGED_TOPIC);
        }
    }
}
