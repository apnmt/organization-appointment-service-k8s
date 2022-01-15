package de.apnmt.organizationappointment.web.rest;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import de.apnmt.organizationappointment.IntegrationTest;
import de.apnmt.organizationappointment.common.domain.WorkingHour;
import de.apnmt.organizationappointment.common.repository.WorkingHourRepository;
import de.apnmt.organizationappointment.common.web.rest.WorkingHourResource;
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
 * Integration tests for the {@link WorkingHourResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
class WorkingHourResourceIT {

    private static final LocalDateTime DEFAULT_START_AT = LocalDateTime.of(1990, 1, 1, 0, 0, 11);
    private static final LocalDateTime UPDATED_START_AT = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);

    private static final LocalDateTime DEFAULT_END_AT = LocalDateTime.of(1990, 1, 1, 0, 0, 11);
    private static final LocalDateTime UPDATED_END_AT = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/working-hours";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private WorkingHourRepository workingHourRepository;

    @Autowired
    private MockMvc restWorkingHourMockMvc;

    private WorkingHour workingHour;

    /**
     * Create an entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static WorkingHour createEntity() {
        WorkingHour workingHour = new WorkingHour().id(1L).startAt(DEFAULT_START_AT).endAt(DEFAULT_END_AT).employeeId(1L);
        return workingHour;
    }

    @BeforeEach
    public void initTest() {
        workingHourRepository.deleteAll();
        workingHour = createEntity();
    }

    @Test
    void getAllWorkingHours() throws Exception {
        // Initialize the database
        workingHourRepository.save(workingHour);

        // Get all the workingHourList
        restWorkingHourMockMvc.perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(workingHour.getId().intValue())))
            .andExpect(jsonPath("$.[*].startAt").value(hasItem(DEFAULT_START_AT.toString())))
            .andExpect(jsonPath("$.[*].endAt").value(hasItem(DEFAULT_END_AT.toString())));
    }

    @Test
    void getWorkingHour() throws Exception {
        // Initialize the database
        workingHourRepository.save(workingHour);

        // Get the workingHour
        restWorkingHourMockMvc.perform(get(ENTITY_API_URL_ID, workingHour.getId().intValue()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(workingHour.getId().intValue()))
            .andExpect(jsonPath("$.startAt").value(DEFAULT_START_AT.toString()))
            .andExpect(jsonPath("$.endAt").value(DEFAULT_END_AT.toString()));
    }

    @Test
    void getNonExistingWorkingHour() throws Exception {
        // Get the workingHour
        restWorkingHourMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }
}
