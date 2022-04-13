package de.apnmt.organizationappointment.web.rest;

import de.apnmt.organizationappointment.IntegrationTest;
import de.apnmt.organizationappointment.common.domain.ClosingTime;
import de.apnmt.organizationappointment.common.domain.Service;
import de.apnmt.organizationappointment.common.repository.ServiceRepository;
import de.apnmt.organizationappointment.common.web.rest.ServiceResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the {@link ServiceResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
class ServiceResourceIT {

    private static final Integer DEFAULT_DURATION = 1;
    private static final Integer UPDATED_DURATION = 2;

    private static final String ENTITY_API_URL = "/api/services";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private MockMvc restServiceMockMvc;

    private Service service;

    /**
     * Create an entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Service createEntity() {
        Service service = new Service().id(1L).duration(DEFAULT_DURATION);
        return service;
    }

    @BeforeEach
    public void initTest() {
        serviceRepository.deleteAll();
        service = createEntity();
    }

    @Test
    void getAllServices() throws Exception {
        // Initialize the database
        serviceRepository.save(service);

        // Get all the serviceList
        restServiceMockMvc.perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(service.getId().intValue())))
            .andExpect(jsonPath("$.[*].duration").value(hasItem(DEFAULT_DURATION)));
    }

    @Test
    void getService() throws Exception {
        // Initialize the database
        serviceRepository.save(service);

        // Get the service
        restServiceMockMvc.perform(get(ENTITY_API_URL_ID, service.getId().intValue()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(service.getId().intValue()))
            .andExpect(jsonPath("$.duration").value(DEFAULT_DURATION));
    }

    @Test
    void getNonExistingService() throws Exception {
        // Get the service
        restServiceMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    void deleteAllServices() throws Exception {
        // Initialize the database
        this.serviceRepository.save(this.service);
        Service service = createEntity();
        service.setId(1255L);
        this.serviceRepository.save(service);

        int databaseSizeBeforeDelete = this.serviceRepository.findAll().size();

        // Delete the appointment
        this.restServiceMockMvc.perform(delete(ENTITY_API_URL).accept(MediaType.APPLICATION_JSON)).andExpect(status().isNoContent());

        // Validate the database contains no more item
        List<Service> list = this.serviceRepository.findAll();
        assertThat(list).hasSize(databaseSizeBeforeDelete - 1);
    }
}
