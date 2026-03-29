import json
import time
import urllib.request
import urllib.error
from datetime import datetime


URL = "http://localhost:8080/api/notifications"


def log(level: str, message: str) -> None:
    now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    print(f"[{now}] [{level}] {message}")


def build_payloads() -> list[dict]:
    return [
        {
            "userId": 1,
            "service": "STOCK",
            "channel": "KAKAO_TALK",
            "title": "포스코DX",
            "body": "주식 가격이 5% 떨어졌어요",
            "targetUrl": "/stocks/A022100"
        },
        {
            "userId": 2,
            "service": "GAME",
            "channel": "SMS",
            "title": "용사단 키우기",
            "body": "누가 기다리고 있어요",
            "targetUrl": "/games/hero-party"
        },
        {
            "userId": 3,
            "service": "NEWS",
            "channel": "KAKAO_TALK",
            "title": "삼성전자 잠정실적 발표",
            "body": "4분기 영업이익이 발표됐어요",
            "targetUrl": "/news/disclosures/2026-q4-samsung"
        },
        {
            "userId": 4,
            "service": "BILLING",
            "channel": "SMS",
            "title": "내일 예정된 고정지출",
            "body": "보험료 2건이 내일 출금될 예정이에요",
            "targetUrl": "/billing/fixed-expenses"
        },
        {
            "userId": 5,
            "service": "SHOPPING",
            "channel": "KAKAO_TALK",
            "title": "토스쇼핑",
            "body": "15만원 쿠폰 선물하기 이벤트가 도착했어요",
            "targetUrl": "/shopping/events/coupon-gift"
        },
        {
            "userId": 6,
            "service": "PAYMENT",
            "channel": "EMAIL",
            "title": "결제 완료",
            "body": "12,500원 결제가 완료되었어요",
            "targetUrl": "/payments/history/12500"
        },
        {
            "userId": 7,
            "service": "DELIVERY",
            "channel": "EMAIL",
            "title": "배송 출발",
            "body": "주문하신 상품이 배송을 시작했어요",
            "targetUrl": "/orders/20260322-0007"
        },
        {
            "userId": 8,
            "service": "PROMOTION",
            "channel": "EMAIL",
            "title": "오늘만 특가",
            "body": "관심 상품 특가가 시작됐어요",
            "targetUrl": "/promotion/today-sale"
        },
        {
            "userId": 9,
            "service": "SECURITY",
            "channel": "SMS",
            "title": "새 기기 로그인 감지",
            "body": "새로운 기기에서 로그인이 감지되었어요",
            "targetUrl": "/security/login-history"
        },
        {
            "userId": 10,
            "service": "BANK",
            "channel": "KAKAO_TALK",
            "title": "입금 완료",
            "body": "50,000원이 입금되었어요",
            "targetUrl": "/bank/transactions/50000"
        },
        {
            "userId": 11,
            "service": "PAYMENT",
            "channel": "EMAIL",
            "title": "FAIL 결제 실패 테스트",
            "body": "이건 실패 테스트용 메일입니다.",
            "targetUrl": "/payments/fail-test"
        }
    ]


def send_one(index: int, payload: dict) -> tuple[bool, float]:
    data = json.dumps(payload).encode("utf-8")

    req = urllib.request.Request(
        url=URL,
        data=data,
        headers={"Content-Type": "application/json"},
        method="POST"
    )

    log("INFO", f"[{index}] 요청 시작")
    log("INFO", f"[{index}] Request Body = {json.dumps(payload, ensure_ascii=False)}")

    start = time.time()

    try:
        with urllib.request.urlopen(req) as response:
            elapsed_ms = round((time.time() - start) * 1000, 2)
            response_body = response.read().decode("utf-8")

            log("INFO", f"[{index}] 성공 - status={response.status}, elapsedMs={elapsed_ms}")

            try:
                parsed = json.loads(response_body)
                log("INFO", f"[{index}] Response = {json.dumps(parsed, ensure_ascii=False)}")
            except json.JSONDecodeError:
                log("INFO", f"[{index}] Response = {response_body}")

            return True, elapsed_ms

    except urllib.error.HTTPError as e:
        elapsed_ms = round((time.time() - start) * 1000, 2)
        error_body = e.read().decode("utf-8")

        log("ERROR", f"[{index}] HTTP 오류 - status={e.code}, elapsedMs={elapsed_ms}")
        log("ERROR", f"[{index}] Error Body = {error_body}")
        return False, elapsed_ms

    except urllib.error.URLError as e:
        elapsed_ms = round((time.time() - start) * 1000, 2)
        log("ERROR", f"[{index}] 연결 실패 - reason={e.reason}, elapsedMs={elapsed_ms}")
        return False, elapsed_ms


def send_many(delay_sec: float = 0.1) -> None:
    payloads = build_payloads()

    log("INFO", f"알림 대량 요청 테스트 시작 - totalCount={len(payloads)}, delaySec={delay_sec}")

    success_count = 0
    fail_count = 0
    elapsed_list = []

    whole_start = time.time()

    for index, payload in enumerate(payloads, start=1):
        success, elapsed_ms = send_one(index, payload)
        elapsed_list.append(elapsed_ms)

        if success:
            success_count += 1
        else:
            fail_count += 1

        if index < len(payloads):
            time.sleep(delay_sec)

    whole_elapsed_ms = round((time.time() - whole_start) * 1000, 2)
    avg_elapsed_ms = round(sum(elapsed_list) / len(elapsed_list), 2) if elapsed_list else 0.0

    log("INFO", "========== 테스트 결과 ==========")
    log("INFO", f"총 요청 수 = {len(payloads)}")
    log("INFO", f"성공 수 = {success_count}")
    log("INFO", f"실패 수 = {fail_count}")
    log("INFO", f"평균 응답 시간(ms) = {avg_elapsed_ms}")
    log("INFO", f"총 소요 시간(ms) = {whole_elapsed_ms}")


if __name__ == "__main__":
    send_many(delay_sec=0.1)