package de.apnmt.organizationappointment.web.rest;

import de.apnmt.common.enumeration.Day;
import de.apnmt.organizationappointment.IntegrationTest;
import de.apnmt.organizationappointment.common.domain.*;
import de.apnmt.organizationappointment.common.repository.*;
import de.apnmt.organizationappointment.common.web.rest.AvailabilityResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link AvailabilityResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
class AvailabilityResourceIT {

    private static final Long DEFAULT_ID = 1L;
    private static final Long UPDATED_ID = 2L;

    private static final Long DEFAULT_ORGANIZATION_ID = 1L;
    private static final Long UPDATED_ORGANIZATION_ID = 2L;

    private static final Long DEFAULT_EMPLOYEE_ID = 1L;
    private static final Long UPDATED_EMPLOYEE_ID = 2L;

    private static final String ENTITY_API_URL = "/api/slots";

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private ClosingTimeRepository closingTimeRepository;

    @Autowired
    private OpeningHourRepository openingHourRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private WorkingHourRepository workingHourRepository;

    @Autowired
    private MockMvc restAppointmentMockMvc;

    private Appointment appointment;

    public static List<OpeningHour> createOpeningHours(Long organizationId) {
        OpeningHour monday = new OpeningHour()
            .id(1L)
            .day(Day.Monday)
            .organizationId(organizationId)
            .startTime(LocalTime.of(8, 0))
            .endTime(LocalTime.of(18, 0));
        OpeningHour tuesday = new OpeningHour()
            .id(2L)
            .day(Day.Tuesday)
            .organizationId(organizationId)
            .startTime(LocalTime.of(8, 0))
            .endTime(LocalTime.of(19, 0));
        OpeningHour wednesday = new OpeningHour()
            .id(3L)
            .day(Day.Wednesday)
            .organizationId(organizationId)
            .startTime(LocalTime.of(10, 0))
            .endTime(LocalTime.of(20, 0));
        OpeningHour thursday = new OpeningHour()
            .id(4L)
            .day(Day.Thursday)
            .organizationId(organizationId)
            .startTime(LocalTime.of(8, 0))
            .endTime(LocalTime.of(18, 0));
        OpeningHour friday = new OpeningHour()
            .id(5L)
            .day(Day.Monday)
            .organizationId(organizationId)
            .startTime(LocalTime.of(8, 0))
            .endTime(LocalTime.of(18, 0));

        return Arrays.asList(monday, tuesday, wednesday, thursday, friday);
    }

    public static ClosingTime createClosingTime(Long organizationId) {
        return new ClosingTime()
            .id(DEFAULT_ID)
            .organizationId(organizationId)
            .startAt(LocalDateTime.of(2021, 12, 24, 8, 0))
            .endAt(LocalDateTime.of(2021, 12, 27, 8, 0));
    }

    public static Service createService() {
        return new Service().id(DEFAULT_ID).duration(30);
    }

    public static WorkingHour createWorkingHour() {
        return new WorkingHour()
            .id(DEFAULT_ID)
            .employeeId(DEFAULT_ID)
            .startAt(LocalDateTime.of(2021, 12, 23, 8, 0))
            .endAt(LocalDateTime.of(2021, 12, 23, 12, 0));
    }

    @BeforeEach
    public void initTest() {
        this.appointmentRepository.deleteAll();
        this.workingHourRepository.deleteAll();
        this.serviceRepository.deleteAll();
        this.openingHourRepository.deleteAll();
        this.closingTimeRepository.deleteAll();
    }

    @Test
    void getAllSlotsFilteredByWorkingHour() throws Exception {
        // Initialize the database
        List<OpeningHour> openingHours = createOpeningHours(DEFAULT_ORGANIZATION_ID);
        this.openingHourRepository.saveAll(openingHours);
        Service service = createService();
        this.serviceRepository.save(service);
        WorkingHour workingHour = createWorkingHour();
        this.workingHourRepository.save(workingHour);

        // Get all the appointmentList
        this.restAppointmentMockMvc.perform(
                get(
                    ENTITY_API_URL +
                    "?date=" +
                    LocalDate.of(2021, 12, 23) +
                    "&serviceId=" +
                    DEFAULT_ID +
                    "&organizationId=" +
                    DEFAULT_ORGANIZATION_ID +
                    "&employeeId=" +
                    DEFAULT_EMPLOYEE_ID
                )
            )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.times", hasSize(8)))
            .andExpect(jsonPath("$.times.[0]").value("08:00:00"))
            .andExpect(jsonPath("$.times.[1]").value("08:30:00"))
            .andExpect(jsonPath("$.times.[2]").value("09:00:00"))
            .andExpect(jsonPath("$.times.[3]").value("09:30:00"))
            .andExpect(jsonPath("$.times.[4]").value("10:00:00"))
            .andExpect(jsonPath("$.times.[5]").value("10:30:00"))
            .andExpect(jsonPath("$.times.[6]").value("11:00:00"))
            .andExpect(jsonPath("$.times.[7]").value("11:30:00"));
    }

    @Test
    void getAllSlotsFilteredByWorkingHourAndAppointments() throws Exception {
        // Initialize the database
        List<OpeningHour> openingHours = createOpeningHours(DEFAULT_ORGANIZATION_ID);
        this.openingHourRepository.saveAll(openingHours);
        Service service = createService();
        this.serviceRepository.save(service);
        WorkingHour workingHour = createWorkingHour();
        this.workingHourRepository.save(workingHour);

        for (int i = 8; i < 12; i += 2) {
            LocalDateTime start = LocalDateTime.of(2021, 12, 23, i, 0, 0, 0);
            LocalDateTime end = LocalDateTime.of(2021, 12, 23, i, 30, 0, 0);
            Appointment appointment = new Appointment()
                .id((long) i)
                .organizationId(DEFAULT_ORGANIZATION_ID)
                .employeeId(DEFAULT_EMPLOYEE_ID)
                .startAt(start)
                .endAt(end);
            this.appointmentRepository.save(appointment);
        }

        // Get all the appointmentList
        this.restAppointmentMockMvc.perform(
                get(
                    ENTITY_API_URL +
                    "?date=" +
                    LocalDate.of(2021, 12, 23) +
                    "&serviceId=" +
                    DEFAULT_ID +
                    "&organizationId=" +
                    DEFAULT_ORGANIZATION_ID +
                    "&employeeId=" +
                    DEFAULT_EMPLOYEE_ID
                )
            )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.times", hasSize(6)))
            .andExpect(jsonPath("$.times.[0]").value("08:30:00"))
            .andExpect(jsonPath("$.times.[1]").value("09:00:00"))
            .andExpect(jsonPath("$.times.[2]").value("09:30:00"))
            .andExpect(jsonPath("$.times.[3]").value("10:30:00"))
            .andExpect(jsonPath("$.times.[4]").value("11:00:00"))
            .andExpect(jsonPath("$.times.[5]").value("11:30:00"));
    }

    @Test
    void getAllSlotsFilteredByMultipleWorkingHoursAndAppointments() throws Exception {
        // Initialize the database
        List<OpeningHour> openingHours = createOpeningHours(DEFAULT_ORGANIZATION_ID);
        this.openingHourRepository.saveAll(openingHours);
        Service service = createService();
        this.serviceRepository.save(service);
        WorkingHour workingHourBeforeBreak = createWorkingHour();
        this.workingHourRepository.save(workingHourBeforeBreak);
        WorkingHour workingHourAfterBreak = new WorkingHour()
            .id(UPDATED_ID)
            .employeeId(DEFAULT_ID)
            .startAt(LocalDateTime.of(2021, 12, 23, 13, 0))
            .endAt(LocalDateTime.of(2021, 12, 23, 18, 0));
        this.workingHourRepository.save(workingHourAfterBreak);

        for (int i = 8; i < 12; i += 2) {
            LocalDateTime start = LocalDateTime.of(2021, 12, 23, i, 0, 0, 0);
            LocalDateTime end = LocalDateTime.of(2021, 12, 23, i, 30, 0, 0);
            Appointment appointment = new Appointment()
                .id((long) i)
                .organizationId(DEFAULT_ORGANIZATION_ID)
                .employeeId(DEFAULT_EMPLOYEE_ID)
                .startAt(start)
                .endAt(end);
            this.appointmentRepository.save(appointment);
        }

        // Get all the appointmentList
        this.restAppointmentMockMvc.perform(
                get(
                    ENTITY_API_URL +
                    "?date=" +
                    LocalDate.of(2021, 12, 23) +
                    "&serviceId=" +
                    DEFAULT_ID +
                    "&organizationId=" +
                    DEFAULT_ORGANIZATION_ID +
                    "&employeeId=" +
                    DEFAULT_EMPLOYEE_ID
                )
            )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.times", hasSize(16)))
            .andExpect(jsonPath("$.times.[0]").value("08:30:00"))
            .andExpect(jsonPath("$.times.[1]").value("09:00:00"))
            .andExpect(jsonPath("$.times.[2]").value("09:30:00"))
            .andExpect(jsonPath("$.times.[3]").value("10:30:00"))
            .andExpect(jsonPath("$.times.[4]").value("11:00:00"))
            .andExpect(jsonPath("$.times.[5]").value("11:30:00"))
            .andExpect(jsonPath("$.times.[6]").value("13:00:00"))
            .andExpect(jsonPath("$.times.[15]").value("17:30:00"));
    }

    @Test
    void getAllSlotsFilteredByClosingTime() throws Exception {
        // Initialize the database
        List<OpeningHour> openingHours = createOpeningHours(DEFAULT_ORGANIZATION_ID);
        this.openingHourRepository.saveAll(openingHours);
        Service service = createService();
        this.serviceRepository.save(service);
        WorkingHour workingHour = createWorkingHour();
        this.workingHourRepository.save(workingHour);
        ClosingTime closingTime = createClosingTime(DEFAULT_ORGANIZATION_ID);
        this.closingTimeRepository.save(closingTime);

        // Get all the appointmentList
        this.restAppointmentMockMvc.perform(
                get(
                    ENTITY_API_URL +
                    "?date=" +
                    LocalDate.of(2021, 12, 24) +
                    "&serviceId=" +
                    DEFAULT_ID +
                    "&organizationId=" +
                    DEFAULT_ORGANIZATION_ID +
                    "&employeeId=" +
                    DEFAULT_EMPLOYEE_ID
                )
            )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.times", hasSize(0)));
    }
}
