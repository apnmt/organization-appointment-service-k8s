package de.apnmt.organizationappointment.domain;

import static org.assertj.core.api.Assertions.assertThat;

import de.apnmt.organizationappointment.common.domain.OpeningHour;
import de.apnmt.organizationappointment.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class OpeningHourTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(OpeningHour.class);
        OpeningHour openingHour1 = new OpeningHour();
        openingHour1.setId(1L);
        OpeningHour openingHour2 = new OpeningHour();
        openingHour2.setId(openingHour1.getId());
        assertThat(openingHour1).isEqualTo(openingHour2);
        openingHour2.setId(2L);
        assertThat(openingHour1).isNotEqualTo(openingHour2);
        openingHour1.setId(null);
        assertThat(openingHour1).isNotEqualTo(openingHour2);
    }
}
