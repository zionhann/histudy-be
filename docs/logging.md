# 로깅 운영 계약

## 출력 대상

- 콘솔 로그는 컨테이너 표준 출력으로 기록합니다.
- 파일 로그는 `${LOG_PATH}/histudy-be.log`에 기록합니다.
- Docker Compose 기본값에서 `${LOG_PATH}`는 `/webapp/log`이며, `webapp_logs` 볼륨으로 영속화됩니다.
- 날짜와 크기를 함께 기준으로 압축 롤링하며, 파일명은 `histudy-be.YYYY-MM-DD.INDEX.log.gz` 형식입니다.

## 기본 보존 정책

| 설정 | 기본값 | 의미 |
| --- | --- | --- |
| `ROOT_LOG_LEVEL` | `INFO` | root logger의 최소 로그 레벨 |
| `LOGBACK_ROLLINGPOLICY_MAX_HISTORY` | `30` | 보존할 일수 |
| `LOGBACK_ROLLINGPOLICY_MAX_FILE_SIZE` | `100MB` | 개별 로그 파일의 최대 크기 |
| `LOGBACK_ROLLINGPOLICY_TOTAL_SIZE_CAP` | `2GB` | 롤링 로그 전체 용량 상한 |

환경별 값은 Docker Compose 환경 변수로 변경할 수 있습니다. `TOTAL_SIZE_CAP`은 `MAX_FILE_SIZE`보다
작지 않아야 합니다. 로컬 실행에서 `LOG_PATH`를 지정하지 않으면 `/tmp/histudy-be`를 사용합니다.

## 요청 상관관계 ID

요청 처리 계층이 MDC의 `request_id`를 설정하면 콘솔·파일 로그의 각 줄에 같은 값이 출력됩니다.
HTTP 요청/응답 헤더와 예외 응답의 `requestId`에도 같은 식별자를 사용하며, 요청 로깅과 예외 응답
계약은 각 기능 PR에서 함께 완성합니다.

요청 본문, 인증 토큰, 쿠키 등 민감할 수 있는 값은 공통 로그에 기록하지 않습니다.
