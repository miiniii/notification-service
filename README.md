# 알림 발송 미들 서버 개발
### 개요
여러 서비스의 알림 요청을 중앙에서 처리 후, 안정적으로 발송하기 위한 알림 미들 서버 개발

### 아키텍처

### 기술스텍
- Java, Spring Boot, H2, Redis(Redis Stream - 메세지 큐 용도), gradle

### API 명세서

### 실행방법

### 디렉토리 구조
```
notification-service
├── api                  # 외부 요청을 받는 API 계층 
├── app                  # 애플리케이션 실행 모듈
├── application          # 유스케이스, 서비스, 포트 인터페이스
├── domain               # 핵심 도메인 모델
├── infrastructure       # 외부 기술 구현체 
└── scripts              # 테스트/검증용 파이썬 스크립트
```

#### 헥사고날 구조
```
api -> application -> domain
infrastructure -> domain
infrastructure -> application
```

## 1. 알림발송등록 API

### 1-1. 아키텍처
![img_1.png](img_1.png)

### 1-2. Redis Stream 구조 변화
변경 전
```
work 읽음
-> send 호출
-> SUCCESS / FAILED 저장
```
- 문제 : 실패 메세지를 단순히 실패로 처리
  - 일시적인 외부 장애 상황에서도 메세지 유실 가능성 존재
  - 재시도 가능한 실패와 최종 실패를 구분하지 못함

- 해결 방안 - 실패 처리 정책 변경
  - 재시도 가능한 실패 : WAIT로 이동 후 일정 시간 뒤(10초) 재처리
  - 최대 재시도 횟수(3회)를 초과한 실패 : DAED로 이동 후 최종 실패 저장

  
변경 후
```
WORK 읽음
-> send 호출
-> 성공 : SUCCESS 저장
-> 실패 + 재시도 가능 : WAIT 이동

WAIT 읽음
-> nextRetryAt 도달
-> WORK 재투입

WORK 재처리
-> 재시도 횟수(3회) 초과 실패 : DEAD 이동
-> FAILED 저장
```

#### 1-2-1. Redis Stream 데이터
![img_2.png](img_2.png)
#### 1-2-2. 추가 고려 사항
- 알림발송 중복 방지(멱등성 체크)
  - 가정 : 같은 메세지를 Work에서 두번 읽음, 여러 Consumer가 동시에 같은 메세지를 잡음
  - 적용 : notificationId, channel, status = SUCCESS 조건 조회
  - 문제 : 동시에 여러 Consumer가 같은 메세지 처리하는 상황 발생 가능
    - 조회와 저장사이의 빈틈에서 race condition 발생 가능
    
    <br>
  
- 알림 발송 동시성 제어(분산락 적용)
  - 가정 : 같은 메세지를 거의 동시에 처리(동시성 기반 중복 발송 문제)
  - 적용 : notificationId + channel 기준으로 락 생성
    - 다른 알림들은 병렬처리 + 같은 메세지의 중복 발송만 방지
>Consumer <br>
>락 먼저 획득 -> 멱등성 체크 -> sender 호출 -> 결과 저장/WAIT/DEAD  -> lock 해제
