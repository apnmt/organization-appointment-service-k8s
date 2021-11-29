package de.apnmt.organizationappointment.domain;

import static org.assertj.core.api.Assertions.assertThat;

import de.apnmt.organizationappointment.common.domain.ClosingTime;
import de.apnmt.organizationappointment.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ClosingTimeTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ClosingTime.class);
        ClosingTime closingTime1 = new ClosingTime();
        closingTime1.setId(1L);
        ClosingTime closingTime2 = new ClosingTime();
        closingTime2.setId(closingTime1.getId());
        assertThat(closingTime1).isEqualTo(closingTime2);
        closingTime2.setId(2L);
        assertThat(closingTime1).isNotEqualTo(closingTime2);
        closingTime1.setId(null);
        assertThat(closingTime1).isNotEqualTo(closingTime2);
    }
}
