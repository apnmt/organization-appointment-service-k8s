package de.apnmt.organizationappointment.web.rest;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import de.apnmt.organizationappointment.IntegrationTest;
import de.apnmt.organizationappointment.common.domain.ClosingTime;
import de.apnmt.organizationappointment.common.repository.ClosingTimeRepository;
import de.apnmt.organizationappointment.common.web.rest.ClosingTimeResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the {@link ClosingTimeResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
class ClosingTimeResourceIT {

    private static final LocalDateTime DEFAULT_START_AT = LocalDateTime.of(1990, 1, 1, 0, 0, 11);
    private static final LocalDateTime UPDATED_START_AT = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);

    private static final LocalDateTime DEFAULT_END_AT = LocalDateTime.of(1990, 1, 1, 0, 0, 11);
    private static final LocalDateTime UPDATED_END_AT = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/closing-times";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ClosingTimeRepository closingTimeRepository;

    @Autowired
    private MockMvc restClosingTimeMockMvc;

    private ClosingTime closingTime;

    /**
     * Create an entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ClosingTime createEntity() {
        ClosingTime closingTime = new ClosingTime().id(1L).startAt(DEFAULT_START_AT).endAt(DEFAULT_END_AT).organizationId(1L);
        return closingTime;
    }

    @BeforeEach
    public void initTest() {
        closingTimeRepository.deleteAll();
        closingTime = createEntity();
    }

    @Test
    void getAllClosingTimes() throws Exception {
        // Initialize the database
        closingTimeRepository.save(closingTime);

        // Get all the closingTimeList
        restClosingTimeMockMvc.perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(closingTime.getId().intValue())))
            .andExpect(jsonPath("$.[*].startAt").value(hasItem(DEFAULT_START_AT.toString())))
            .andExpect(jsonPath("$.[*].endAt").value(hasItem(DEFAULT_END_AT.toString())));
    }

    @Test
    void getClosingTime() throws Exception {
        // Initialize the database
        closingTimeRepository.save(closingTime);

        // Get the closingTime
        restClosingTimeMockMvc.perform(get(ENTITY_API_URL_ID, closingTime.getId().intValue()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(closingTime.getId().intValue()))
            .andExpect(jsonPath("$.startAt").value(DEFAULT_START_AT.toString()))
            .andExpect(jsonPath("$.endAt").value(DEFAULT_END_AT.toString()));
    }

    @Test
    void getNonExistingClosingTime() throws Exception {
        // Get the closingTime
        restClosingTimeMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }
}
