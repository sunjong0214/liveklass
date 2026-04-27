# LiveKlass - 수강 신청 시스템

## 프로젝트 개요
실시간 강의 플랫폼의 핵심인 강의 관리 및 선착순 수강 신청 시스템입니다. 대량의 동시 요청 상황에서도 정확한 정원 관리를 보장하며, 정원 초과 시의 대기열 처리 및 결제 후 취소 제한 등의 비즈니스 규칙을 구현했습니다.

## 기술 스택
- Language: Java 17
- Framework: Spring Boot 4.0.6
- Database: Prod
- ORM: Spring Data JPA
- Build Tool: Gradle

## 실행 방법
```cmd
./gradlew bootRun
```

## API 목록 및 예시

### 1. 회원 관리
- **회원 가입**
  - `POST /api/members/join`
  - Request Body:
    ```json
    {
      "email": "user@example.com",
      "name": "홍길동"
    }
    ```
- **강사(Creator) 등록**
  - `POST /api/members/creator`
  - Request Body:
    ```json
    {
      "memberId": 1,
      "bio": "안녕하세요, 강사입니다."
    }
    ```

### 2. 강의 관리
- **강의 등록**
  - `POST /api/lectures`
  - Header: `X-Creator-Id: {creatorId}`
  - Request Body:
    ```json
    {
      "title": "강의 제목",
      "description": "강의 설명",
      "price": 10000,
      "maxCapacity": 50,
      "startDate": "2026-05-01T10:00:00",
      "endDate": "2026-05-31T18:00:00"
    }
    ```
- **강의 목록 조회**
  - `GET /api/lectures?status=OPEN&page=0&size=10`
  - Response Body (Pageable `LectureResponse`):
    ```json
    {
      "content": [
        {
          "id": 1,
          "creatorId": 1,
          "title": "강의 제목",
          "price": 10000,
          "maxCapacity": 50,
          "currentEnrollmentCount": 10,
          "startDate": "2026-05-01T10:00:00",
          "endDate": "2026-05-31T18:00:00",
          "status": "OPEN",
          "createdAt": "2026-04-27T10:00:00"
        }
      ],
      "totalPages": 1,
      "totalElements": 1
    }
    ```
- **강의 상세 조회**
  - `GET /api/lectures/{lectureId}`
  - Response Body (`LectureDetailResponse`):
    ```json
    {
      "id": 1,
      "creatorId": 1,
      "title": "강의 제목",
      "description": "강의 설명",
      "price": 10000,
      "maxCapacity": 50,
      "currentEnrollmentCount": 10,
      "startDate": "2026-05-01T10:00:00",
      "endDate": "2026-05-31T18:00:00",
      "status": "OPEN",
      "createdAt": "2026-04-27T10:00:00"
    }
    ```
- **수강생 목록 조회 (크리에이터 전용)**
  - `GET /api/lectures/{lectureId}/students`
  - Header: `X-Creator-Id: {creatorId}`
  - Response Body (Pageable `LectureStudentResponse`):
    ```json
    {
      "content": [
        {
          "memberId": 1,
          "name": "홍길동",
          "email": "user@example.com",
          "status": "CONFIRMED",
          "enrolledAt": "2026-04-27T11:00:00"
        }
      ]
    }
    ```

### 3. 수강 신청 관리
- **수강 신청**
  - `POST /api/enrollments`
  - Header: `X-Member-Id: {memberId}`
  - Request Body:
    ```json
    {
      "lectureId": 1
    }
    ```
- **결제 확정**
  - `POST /api/enrollments/{enrollmentId}/confirm`
- **수강 취소**
  - `POST /api/enrollments/{enrollmentId}/cancel`
- **내 신청 목록 조회**
  - `GET /api/enrollments/me`
  - Header: `X-Member-Id: {memberId}`
  - Response Body (Pageable `EnrollmentResponse`):
    ```json
    {
      "content": [
        {
          "id": 1,
          "memberId": 1,
          "lectureId": 1,
          "status": "CONFIRMED",
          "enrolledAt": "2026-04-27T11:00:00",
          "paymentAt": "2026-04-27T11:05:00"
        }
      ]
    }
    ```

## 데이터 모델 설명

### 1. Member (사용자)
- 시스템의 기본 사용자 정보 및 식별자를 관리하는 독립 엔티티입니다.
- `email`: 사용자 이메일 주소(Unique)
- `name`: 사용자 이름

### 2. CreatorProfile (강사 프로필)
- `memberId`: Member와 1:1 관계 논리적 매핑

### 3. Lecture (강의)
- `creatorId`: 해당 강의를 개설한 강사(`CreatorProfile`)와 1:1 논리적 매핑
- `maxCapacity`: 강의의 최대 수강 정원
- `currentEnrollmentCount`: 현재 수강 확정 및 대기 중인 인원수
- `status`: 강의 상태 (`DRAFT`, `OPEN`, `CLOSED`)

### 4. Enrollment (수강 신청)
- `memberId`: 수강 신청한 사용자와의 1:1 논리적 매핑
- `lectureId`: 수강한 강의와의 1:1 논리적 매핑
- 복합 유니크 제약 조건을 통해 동일 사용자의 중복 신청을 데이터베이스 레벨에서 방지
- `status`: 신청 상태 (`PENDING`, `CONFIRMED`, `CANCELLED`)
- `paymentAt`: 결제 확정 시각, 비즈니스 규칙(7일 이내 취소)의 기준점

### 5. Waitlist (대기열)
- `memberId`: 수강 대기 중인 사용자와의 1:1 논리적 매핑
- `lectureId`: 수강 대기 중인 강의와의 1:1 논리적 매핑


## 요구사항 해석 및 가정
- 정원 관리: 신청 시 즉시 정원을 점유(PENDING)하며, 취소 시 대기열의 최우선 순위자가 자리를 승계
- 취소 제한: 결제 확정(CONFIRMED) 후 7일 이내에만 취소가 가능하도록 비즈니스 규칙을 설정

## 설계 결정과 이유
### 1. 동시성 제어
- **원자적 업데이트 / 비관적 락을 통한 동시성 제어:** 유니크 인덱스와 'UPDATE ... SET ...' 쿼리를 사용하여 수강 신청 동시성 제어.

### 2. 크리에이터 전용 기능 및 성능 최적화
- **N+1 문제 해결:** 페이지 단위로 Enrollment 목록을 먼저 조회한 후, 해당 Enrollment와 연관된 memberId들을 IN 절로 한번에 가져오도록 구현

### 3. 대기열 구현
- 대기열 구현: 정원 초과 시 자동으로 대기열로 전환되며, 기존 수강생 취소 시 선착순으로 자동 수강 승격

## 테스트 실행 방법
명령어: `./gradlew test`

### 주요 테스트 시나리오 (EnrollmentServiceTest)
1. **기본 수강 신청 프로세스**
   - **수강 신청 성공**: 잔여 정원이 있을 때 신청 시 `PENDING` 상태로 등록되며, 강의의 현재 신청 인원(`currentEnrollmentCount`) 증가
   - **대기열 자동 등록**: 강의 정원이 초과된 상태에서 신청할 경우, `Waitlist` 엔티티에 INSERT, 선착순 대기 상태로 전환

2. **동시성 제어 및 데이터 정합성**
   - **중복 신청 방지**: 동시에 여러 번의 수강 신청 요청이 들어올 경우 단 1건만 신청되도록 보장
   - **취소 및 신청 상황 검증**: 여러 사용자가 동시에 수강을 취소하고 새로운 사용자들이 신청할 때 강의의 신청 인원 카운트와 실제 수강 신청 내역의 합계가 일치하는지 검증

## 미구현 / 제약사항
- 회원 가입 및 로그인 기능은 생략, 회원 ID를 기반으로 작동

## AI 활용 범위
- **생산성 향상 및 반복 작업 위임**: 기본적인 CRUD 로직, 글로벌 에러 핸들러, DTO 매핑 등 정형화된 코드 작성을 위임하여 핵심 비즈니스 로직 설계에 집중할 수 있는 시간을 확보
- **코드 리뷰 및 아키텍처 토론**: 작성된 코드를 AI와 함께 리뷰하며 잠재적인 버그(N+1 문제, 레이스 컨디션 등)를 식별하고, 더 나은 설계 방향에 대해 논의
- **인사이트 확보 및 문제 해결**: 복잡한 동시성 제어 전략이나 도메인 모델 설계 시, 해결책이 모호한 지점에서 대화를 통해 다양한 인사이트 확보 
- **테스트 코드 작성**: 'ExecutorService'와 'CountDownLatch' 등을 활용한 멀티스레드 환경에서의 테스트 코드 작성
