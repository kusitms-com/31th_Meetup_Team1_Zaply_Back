# Zaply Server
대학생 IT경영학회 큐시즘 31기 밋업 프로젝트 1조 Zaply 백엔드 레포지토리
![1](https://github.com/user-attachments/assets/b8bfca4f-25d5-472b-9615-89f74ef7d35c)

<br></br>

## 👬 Member
|      정성호     |         염지은         |                                                                                                   
| :------------------------------------------------------------------------------: | :---------------------------------------------------------------------------------------------------------------------------------------------------: | 
|   <img src="https://avatars.githubusercontent.com/SeongHo5356?v=4" width=90px alt="정성호"/>       |   <img src="https://avatars.githubusercontent.com/yumzen?v=4" width=90px alt="염지은"/>                       |
|   [@SeongHo5356](https://github.com/SeongHo5356)   |    [@yumzen](https://github.com/yumzen)  | 

<br></br>

## 📝 Technology Stack
| Category             | Technology                                                                 |
|----------------------|---------------------------------------------------------------------------|
| **Language**         | Java 21                                                                 |
| **Framework**        | Spring Boot 3.3.10                                                        |
| **Databases**        | Postgresql, Redis                                                             |
| **Authentication**   | JWT, Spring Security, OAuth2.0                                           |
| **Development Tools**| Lombok                                                   |
| **API Documentation**| Swagger UI (SpringDoc)                                                   |
| **Storage**          | AWS S3, Naver Object Storage                                                                   |
| **Infrastructure**   | Terraform, NCP Server, HashiCorp Vault  |
| **Build Tools**      | Gradle    |
| **Monitoring** | Prometheus, Grafana, Loki, Promtail |

<br></br>

## 📅 ERD
<img width="1092" alt="스크린샷 2025-05-29 오후 9 35 28" src="https://github.com/user-attachments/assets/d1035e6d-0bd8-43ad-a944-c4053a3f85d0" />

<br></br>

## API 명세서
https://api.zapply.site/swagger-ui/index.html

<br></br>

## 🔨 Project Architecture
![image](https://github.com/user-attachments/assets/480e75ba-bae1-453c-94e7-dce74b89cb24)

<br></br>

## ⭐️ 기술스택/선정이유
### 1️⃣ Java 21

- Java 21은 최신 언어 기능(예: 패턴 매칭, 레코드, 가상 스레드 등)을 제공하여 코드의 가독성과 유지보수성을 높이며, 개발 생산성을 향상시킵니다.
- 최신 버전의 자바는 성능 최적화와 효율적인 메모리 관리 기능이 개선되어, 대규모 애플리케이션에서도 안정적이고 빠른 실행이 가능합니다.
- 장기 지원 버전이므로, 앞으로의 유지보수와 안정성 측면에서 신뢰할 수 있는 기반을 제공합니다.

### 2️⃣ SpringBoot 3.3.10

- 최신 버전의 Spring Boot는 스프링 프레임워크 및 관련 라이브러리와의 호환성이 뛰어나며, 보안 패치와 최신 기능들이 반영되어 있습니다.
- 자동 설정 기능과 다양한 내장 기능 덕분에 복잡한 설정 없이도 빠르게 애플리케이션을 개발할 수 있으며, 마이크로서비스 아키텍처 구축에 유리합니다.
- RESTful API, 데이터 액세스, 보안 등의 기능이 통합되어 있어 개발자가 비즈니스 로직에 집중할 수 있는 환경을 제공합니다.

### 3️⃣ SpringData JPA

- Spring Data JPA는 데이터베이스와의 인터랙션을 단순화하고, 불필요한 보일러플레이트 코드를 줄여 개발 효율성을 높여줍니다.

### 5️⃣ PostgreSQL

- PostgreSQL는 복잡한 쿼리 처리와 대규모 데이터셋 관리에 강점을 가집니다.

### 6️⃣ Docker compose

- Docker Compose는 여러 컨테이너를 손쉽게 구성하고 관리할 수 있도록 도와주어, 개발 및 배포 과정에서 환경 설정을 간소화합니다.

### 7️⃣ NCP(CLOVA studio, Server, Object Storage)

- NCP(네이버 클라우드 플랫폼)는 CLOVA Studio 및 서버 인프라 호스팅에 활용되어, 안정적이고 안전한 클라우드 환경을 제공함으로써 프로젝트의 운영 효율성을 높입니다.

### 8️⃣ Hashicorp Vault

- 민감한 데이터(플랫폼 별 사용자 accessToken 등)를 안전하게 관리하고, 보안성을 강화할 수 있습니다.

### 9️⃣ Terraform

- 인프라를 코드로 관리할 수 있게 해 주어, 반복 가능하고 일관된 인프라 구축 및 유지보수가 가능합니다.

### 9️⃣ Promtail, Loki, Prometheus, Grafana

- **Promtail → Loki**: 애플리케이션·시스템 로그를 수집·라벨링해 Loki에 전송 → 대량 로그를 효율적으로 인덱싱·검색합니다.
- **Prometheus**: Pull 방식으로 API 응답 시간·CPU·메모리 같은 시계열 메트릭을 스크랩·저장합니다.
- **Grafana**: Loki·Prometheus 데이터를 대시보드로 시각화합니다.

### 🔟 K6

- JavaScript로 실제 사용자 흐름(로그인→API 호출→스케줄링)을 스크립트화해 REST 성능을 측정합니다.
- `vus`, `stages`, `thresholds` 등 옵션으로 동시 사용자 수·스파이크·지속 테스트를 커스터마이징하면서 테스트합니다.
- 콘솔·JSON 출력으로 응답 시간·처리량·에러율 메트릭을 수집하고 Grafana에 연동해 결과 시각화합니다.


<br></br>

## 💬 Convention

**commit convention** <br>
`#이슈번호 conventionType: 구현한 내용` <br><br>


**convention Type** <br>
| convention type | description |
| --- | --- |
| `feat` | 새로운 기능 구현 |
| `chore` | 부수적인 코드 수정 및 기타 변경사항 |
| `docs` | 문서 추가 및 수정, 삭제 |
| `fix` | 버그 수정 |
| `test` | 테스트 코드 추가 및 수정, 삭제 |
| `refactor` | 코드 리팩토링 |

<br></br>

## 🪵 Branch
### 
- `컨벤션명/#이슈번호-작업내용`
- pull request를 통해 develop branch에 merge 후, branch delete
- 부득이하게 develop branch에 직접 commit 해야 할 경우, `!hotfix:` 사용

<br></br>

## 📁 Directory

```PlainText
src/
├── main/
│   ├── domain/
│   │   ├── entity/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   └── dto/
            ├── request/
            └── response/
│   ├── global/
│   │   ├── apiPayload/
│   │   ├── config/
│   │   ├── security/
		 
```

<br></br>

## 📈  부하테스트
각 플랫폼(Instagram, Facebook, Threads) API에는 계정/시간 당 발행 가능한 게시물 수에 제한이 있어, 부하 테스트에는 제약이 존재합니다. 이에 따라 저희는 즉시 발행이 아닌 **"예약 발행" API**를 활용한 부하 시뮬레이션 방식을 구성하였습니다.

|시나리오 ① 10명이 1초 동안 최대한의 요청을 보낸다.| 시나리오 ② 2000명이 1초 동안 최대한의 요청을 보낸다.|
| :-------| :-------|
|![image](https://github.com/user-attachments/assets/026eb04b-4aa3-4e23-8820-5d22f1d94d12)|![image](https://github.com/user-attachments/assets/b73b7838-48e6-4392-99cd-c6497a4958d1)|
|✅ 총 120개의 요청이 문제없이 처리됨 <br>  - 평균 요청 처리 시간 : 82.09 ms <br>  - 최소 요청 처리 시간 : 22.52ms <br>  - 최대 요청 처리 시간 : 164.64ms |✅ 총 4002개의 요청이 문제없이 처리됨<br> - 평균 요청 처리 시간 : 7.74s <br>  - 최소 요청 처리 시간 : 21.9s <br>  - 최대 요청 처리 시간 : 18.28s <br> - 95th 퍼센타일 : 14.95s|

<br>

| 시나리오 ③ 사용자 수 변동 시나리오 | 시나리오 ④ 응답 시간이 5초 이내인 최대 요청 수 파악|
| :-------|:----|
|![image](https://github.com/user-attachments/assets/c77e54f8-765f-4ef5-a79b-f8896eb761a7)|![image](https://github.com/user-attachments/assets/a856af66-9d1b-47df-b287-156c125bd9b3)|
|0초 ~ 2초 : `50명`, 2초 ~ 12초 : `300명`, 12초 ~ 17초 : `1000명`, 17초 ~ 18초 : `500명`| 5초가 지날 경우 사용자 이탈이 늘어날 것이라고 판단하여 1초 동안 `1000명`의 사용자가 요청을 보내 `요청 처리 시간이 5초 이내`인 요청 개수를 파악 |
|✅ 총 3789개의 요청이 문제없이 처리됨 <br>  - 평균 요청 처리 시간 : 1.94s <br>  - 최소 요청 처리 시간 : 20.53ms <br>  - 최대 요청 처리 시간 : 7.88s |✅ 총 2002개의 요청이 시간 내 처리됨 |

### 테스트 결과 분석
- 현재 시스템은 동시 약 `1,000건` 수준까지는 안정적으로 요청을 처리할 수 있는 것으로 보입니다. **시나리오 ③**처럼 사용자 수가 점차 증가하는 상황에서도 평균 응답 시간은 `1.94초`, 최대 응답 시간은 `7.88초`로, 대부분의 요청이 정상적으로 처리되었습니다.
- 하지만 **시나리오 ②**처럼 `2,000명`의 동시 요청이 들어오면 평균 응답 시간이 `7.74초`, 최대 `18.28초`까지 증가하면서 응답 지연이 발생하였습니다. 이 결과는 대규모 트래픽에 대한 성능 한계가 있음을 보여주며, 추후 이를 개선할 필요가 있습니다.
- **시나리오 ④**에서는 `1000명`의 사용자가 동시에 요청을 보낸 경우, 총 `2,002건`의 요청이 `5초` 이내에 처리되었습니다. 이는 현재 시스템이 실시간 대응보다는 예약 처리에 더 적합한 구조임을 보여줍니다.
- 일반적으로 사용자 이탈이 늘기 시작하는 5초 이내 응답을 기준으로 예상 접속자 수 약 `1,000명` 정도에 대해서는 충분히 안정적인 성능을 제공할 수 있다고 판단됩니다.
