package de.apnmt.organizationappointment.domain;

import static org.assertj.core.api.Assertions.assertThat;

import de.apnmt.organizationappointment.common.domain.WorkingHour;
import de.apnmt.organizationappointment.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class WorkingHourTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(WorkingHour.class);
        WorkingHour workingHour1 = new WorkingHour();
        workingHour1.setId(1L);
        WorkingHour workingHour2 = new WorkingHour();
        workingHour2.setId(workingHour1.getId());
        assertThat(workingHour1).isEqualTo(workingHour2);
        workingHour2.setId(2L);
        assertThat(workingHour1).isNotEqualTo(workingHour2);
        workingHour1.setId(null);
        assertThat(workingHour1).isNotEqualTo(workingHour2);
    }
}
