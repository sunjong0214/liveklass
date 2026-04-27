package com.liveklass.service.enrollment;

import com.liveklass.domain.enrollment.Enrollment;
import com.liveklass.domain.enrollment.EnrollmentStatus;
import com.liveklass.domain.lecture.Lecture;
import com.liveklass.domain.lecture.LectureStatus;
import com.liveklass.domain.member.Member;
import com.liveklass.repository.enrollment.EnrollmentRepository;
import com.liveklass.repository.enrollment.WaitlistRepository;
import com.liveklass.repository.lecture.LectureRepository;
import com.liveklass.repository.member.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class EnrollmentServiceTest {

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private LectureRepository lectureRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private WaitlistRepository waitlistRepository;

    private Member member;
    private Lecture lecture;

    @BeforeEach
    void setUp() {
        enrollmentRepository.deleteAllInBatch();
        waitlistRepository.deleteAllInBatch();
        lectureRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();

        member = memberRepository.save(new Member("test@test.com", "테스터"));
        lecture = lectureRepository.save(Lecture.builder()
                .creatorId(member.getId())
                .title("테스트 강의")
                .price(10000L)
                .maxCapacity(1)
                .currentEnrollmentCount(0)
                .status(LectureStatus.OPEN)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(7))
                .build());
    }

    @Test
    @DisplayName("수강 신청 성공: 정원이 남았을 때 신청하면 PENDING 상태로 등록된다.")
    void enroll_Success() {
        // when
        Long enrollmentId = enrollmentService.enroll(member.getId(), lecture.getId());

        // then
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId).orElseThrow();
        assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.PENDING);

        Lecture updatedLecture = lectureRepository.findById(lecture.getId()).orElseThrow();
        assertThat(updatedLecture.getCurrentEnrollmentCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("대기열 등록: 정원이 꽉 찼을 때 신청하면 대기열에 등록된다.")
    void enroll_Waitlist() {
        // given
        enrollmentService.enroll(member.getId(), lecture.getId()); // 정원 1명 채움
        Member otherMember = memberRepository.save(new Member("other@test.com", "대기자"));

        // when
        Long waitlistId = enrollmentService.enroll(otherMember.getId(), lecture.getId());

        // then
        assertThat(waitlistRepository.existsById(waitlistId)).isTrue();
    }

    @Test
    @DisplayName("동시성: 동일 사용자가 동시에 여러 번 신청해도 1건만 성공한다")
    void enroll_Duplicate_Concurrency() throws InterruptedException {
        // given
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    enrollmentService.enroll(member.getId(), lecture.getId());
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    done.countDown();
                }
            });
        }

        ready.await();
        start.countDown();
        done.await();
        executor.shutdown();

        // then
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(threadCount - 1);
        assertThat(enrollmentRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("동시성: 취소와 신청이 동시에 발생해도 카운트가 정확하다")
    void cancelAndEnroll_Concurrency() throws InterruptedException {
        // given: 정원 5명, 5명 모두 수강 중
        Lecture concurrencyLecture = lectureRepository.save(Lecture.builder()
                .creatorId(member.getId())
                .title("동시성 테스트 강의")
                .price(10000L)
                .maxCapacity(5)
                .currentEnrollmentCount(0)
                .status(LectureStatus.OPEN)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(7))
                .build());

        List<Long> enrollmentIds = new ArrayList<>();
        List<Member> cancelMembers = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Member m = memberRepository.save(new Member("cancel" + i + "@test.com", "취소자" + i));
            cancelMembers.add(m);
            enrollmentIds.add(enrollmentService.enroll(m.getId(), concurrencyLecture.getId()));
        }

        List<Member> newMembers = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            newMembers.add(memberRepository.save(new Member("new" + i + "@test.com", "신규" + i)));
        }

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);

        for (int i = 0; i < 5; i++) {
            final Long enrollmentId = enrollmentIds.get(i);
            executor.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    enrollmentService.cancelEnrollment(enrollmentId);
                } catch (Exception e) {
                } finally {
                    done.countDown();
                }
            });

            final Member newMember = newMembers.get(i);
            executor.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    enrollmentService.enroll(newMember.getId(), concurrencyLecture.getId());
                } catch (Exception e) {
                } finally {
                    done.countDown();
                }
            });
        }

        ready.await();
        start.countDown();
        done.await();
        executor.shutdown();

        // then: 실제 PENDING 수강자 수 = lecture 카운트
        Lecture result = lectureRepository.findById(concurrencyLecture.getId()).orElseThrow();
        long actualPendingCount = enrollmentRepository.findAll().stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.PENDING)
                .count();

        assertThat(result.getCurrentEnrollmentCount()).isEqualTo((int) actualPendingCount);
    }
}
