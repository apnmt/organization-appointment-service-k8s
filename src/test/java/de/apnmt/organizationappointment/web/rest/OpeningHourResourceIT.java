package de.apnmt.organizationappointment.web.rest;

import java.time.LocalTime;
import java.util.List;

import de.apnmt.common.enumeration.Day;
import de.apnmt.organizationappointment.IntegrationTest;
import de.apnmt.organizationappointment.common.domain.ClosingTime;
import de.apnmt.organizationappointment.common.domain.OpeningHour;
import de.apnmt.organizationappointment.common.repository.OpeningHourRepository;
import de.apnmt.organizationappointment.common.web.rest.OpeningHourResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the {@link OpeningHourResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
class OpeningHourResourceIT {

    private static final Day DEFAULT_DAY = Day.Monday;
    private static final Day UPDATED_DAY = Day.Tuesday;

    private static final LocalTime DEFAULT_START_TIME = LocalTime.of(1, 1, 11);
    private static final LocalTime UPDATED_START_TIME = LocalTime.now();

    private static final LocalTime DEFAULT_END_TIME = LocalTime.of(1, 1, 11);
    private static final LocalTime UPDATED_END_TIME = LocalTime.now();

    private static final String ENTITY_API_URL = "/api/opening-hours";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private OpeningHourRepository openingHourRepository;

    @Autowired
    private MockMvc restOpeningHourMockMvc;

    private OpeningHour openingHour;

    /**
     * Create an entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static OpeningHour createEntity() {
        OpeningHour openingHour = new OpeningHour().id(1L).day(DEFAULT_DAY).startTime(DEFAULT_START_TIME).endTime(DEFAULT_END_TIME).organizationId(1L);
        return openingHour;
    }

    @BeforeEach
    public void initTest() {
        openingHourRepository.deleteAll();
        openingHour = createEntity();
    }

    @Test
    void getAllOpeningHours() throws Exception {
        // Initialize the database
        openingHourRepository.save(openingHour);

        // Get all the openingHourList
        restOpeningHourMockMvc.perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(openingHour.getId().intValue())))
            .andExpect(jsonPath("$.[*].day").value(hasItem(DEFAULT_DAY.toString())))
            .andExpect(jsonPath("$.[*].startTime").value(hasItem(DEFAULT_START_TIME.toString())))
            .andExpect(jsonPath("$.[*].endTime").value(hasItem(DEFAULT_END_TIME.toString())));
    }

    @Test
    void getOpeningHour() throws Exception {
        // Initialize the database
        openingHourRepository.save(openingHour);

        // Get the openingHour
        restOpeningHourMockMvc.perform(get(ENTITY_API_URL_ID, openingHour.getId().intValue()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(openingHour.getId().intValue()))
            .andExpect(jsonPath("$.day").value(DEFAULT_DAY.toString()))
            .andExpect(jsonPath("$.startTime").value(DEFAULT_START_TIME.toString()))
            .andExpect(jsonPath("$.endTime").value(DEFAULT_END_TIME.toString()));
    }

    @Test
    void getNonExistingOpeningHour() throws Exception {
        // Get the openingHour
        restOpeningHourMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    void deleteAllOpeningHours() throws Exception {
        // Initialize the database
        this.openingHourRepository.save(this.openingHour);
        OpeningHour openingHour = createEntity();
        openingHour.setId(1256L);
        this.openingHourRepository.save(openingHour);

        int databaseSizeBeforeDelete = this.openingHourRepository.findAll().size();

        // Delete the appointment
        this.restOpeningHourMockMvc.perform(delete(ENTITY_API_URL).accept(MediaType.APPLICATION_JSON)).andExpect(status().isNoContent());

        // Validate the database contains no more item
        List<OpeningHour> list = this.openingHourRepository.findAll();
        assertThat(list).hasSize(databaseSizeBeforeDelete - 1);
    }
}
