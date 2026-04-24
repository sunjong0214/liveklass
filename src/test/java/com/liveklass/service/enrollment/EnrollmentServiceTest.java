package com.liveklass.service.enrollment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class EnrollmentServiceTest {

    @Autowired
    private EnrollmentService enrollmentService;

    @Test
    @DisplayName("수강 신청 기능이 정상 작동해야 한다.")
    void enrollTest() {
        // TODO: 수강 신청 테스트 구현
    }

    @Test
    @DisplayName("동시에 수강 신청이 들어올 경우 정원 초과를 방지해야 한다.")
    void concurrencyEnrollTest() {
        // TODO: 동시성 테스트 구현
    }
}
