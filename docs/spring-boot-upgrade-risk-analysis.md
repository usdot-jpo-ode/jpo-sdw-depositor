# Spring Boot Migration Risk Analysis

**From:** Spring Boot 3.1.3  
**To:** Spring Boot 3.4.x (latest stable)  
**Java:** 21 (unchanged)  
**Date:** 2026-04-23

---

## Summary

The migration from 3.1.3 → 3.4.x is broadly low-disruption because the application already uses the Jakarta EE namespace and modern reactive APIs. However, several third-party dependency mismatches —
most critically `reactor-spring` and the Kafka 4.0 / ZooKeeper docker-compose conflict — represent real build or runtime breakage risks that must be resolved before or alongside the bump.

---

## Risk Register

### CRITICAL

---

#### RISK-01 — `reactor-spring 1.0.1.RELEASE` is incompatible with modern Reactor

| Field                   | Detail                                                       |
|-------------------------|--------------------------------------------------------------|
| **File**                | `pom.xml`                                                    |
| **Dependency**          | `org.projectreactor:reactor-spring:1.0.1.RELEASE`            |
| **Affected components** | Spring context initialization, any Reactor-based bean wiring |

**Problem:**  
`org.projectreactor` is the pre-Pivotal, pre-Reactor 3.x artifact group (circa 2013–2014). Spring Boot 3.x's `spring-boot-starter-webflux` pulls in `io.projectreactor:reactor-core` 3.6.x. The two
Reactor generations are entirely different libraries: different package names, different threading models, different APIs. Having both on the classpath can cause `ClassNotFoundException`,
`NoSuchMethodError`, or silent behavioral degradation at startup.

**Evidence this artifact is likely unused:**  
The codebase uses `reactor.core.publisher.Mono` (from modern `io.projectreactor:reactor-core`), which is already transitively provided by `spring-boot-starter-webflux`. No code imports from
`org.projectreactor.*`.

**Remediation:**  
Remove the dependency entirely from `pom.xml`. The `io.projectreactor` libraries come transitively from `spring-boot-starter-webflux` and do not need to be declared directly.

```xml
<!-- REMOVE this block -->
<dependency>
    <groupId>org.projectreactor</groupId>
    <artifactId>reactor-spring</artifactId>
    <version>1.0.1.RELEASE</version>
</dependency>
```

**Verification:** Run `mvn dependency:tree` after removal and confirm `io.projectreactor:reactor-core` is still present transitively. Run `mvn test` to confirm all existing tests pass.

---

#### RISK-02 — Kafka 4.0.0 client is incompatible with ZooKeeper-based broker in `docker-compose.yml`

| Field                   | Detail                                                 |
|-------------------------|--------------------------------------------------------|
| **Files**               | `pom.xml`, `docker-compose.yml`                        |
| **Dependency**          | `org.apache.kafka:kafka_2.13:4.0.0`                    |
| **Affected components** | Local development environment, integration smoke tests |

**Problem:**  
Apache Kafka 4.0 removed ZooKeeper support entirely — it requires KRaft mode. The `docker-compose.yml` spins up `wurstmeister/zookeeper` and `wurstmeister/kafka` (a ZooKeeper-based Kafka 2.x image). A
Kafka 4.0 _client_ library is generally backward-compatible with older brokers for basic produce/consume, but:

1. The development broker (`wurstmeister/kafka`) is Kafka 2.x, which does not support some newer Kafka 4.0 client configurations and protocol features that may be exercised at connection time.
2. If manual smoke testing is done against the local `docker-compose` environment, the stack is in an inconsistent state regardless of the Spring Boot version bump.

Additionally, Spring Boot 3.4.x manages `spring-kafka` which pins a specific Kafka client version via BOM. If you are relying on the BOM for version resolution elsewhere, declaring `kafka_2.13:4.0.0`
directly may shadow or conflict with the managed version.

**Remediation:**

Replace the `zookeeper` and `kafka` services with the same `bitnamilegacy/kafka:3.8.0` KRaft image already used across the rest of the infrastructure. The `DOCKER_HOST_IP` variable maps to `KAFKA_CFG_ADVERTISED_LISTENERS` via the `EXTERNAL` listener.

```yaml
kafka:
  image: bitnamilegacy/kafka:3.8.0
  hostname: kafka
  restart: on-failure:3
  healthcheck:
    test: /opt/bitnami/kafka/bin/kafka-broker-api-versions.sh --bootstrap-server kafka:9092 --version || exit 1
    interval: 30s
    start_period: 30s
    timeout: 15s
    retries: 12
  ports:
    - "9092:9092"
  environment:
    KAFKA_ENABLE_KRAFT: "yes"
    KAFKA_CFG_PROCESS_ROLES: "broker,controller"
    KAFKA_CFG_CONTROLLER_LISTENER_NAMES: "CONTROLLER"
    KAFKA_CFG_LISTENERS: "PLAINTEXT://:9094,CONTROLLER://:9093,EXTERNAL://:9092"
    KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP: "CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT,EXTERNAL:PLAINTEXT"
    KAFKA_CFG_ADVERTISED_LISTENERS: "PLAINTEXT://kafka:9094,EXTERNAL://${DOCKER_HOST_IP:-kafka:9092}"
    KAFKA_BROKER_ID: "1"
    KAFKA_CFG_CONTROLLER_QUORUM_VOTERS: "1@kafka:9093"
    ALLOW_PLAINTEXT_LISTENER: "yes"
    KAFKA_CFG_NODE_ID: "1"
    KAFKA_CFG_DELETE_TOPIC_ENABLE: "true"
    KAFKA_CFG_AUTO_CREATE_TOPICS_ENABLE: "true"
  logging:
    options:
      max-size: "10m"
      max-file: "5"
```

---

### HIGH

---

#### RISK-03 — JMockit 1.49 may fail on newer JVM module restrictions

| Field                   | Detail                                                                                                                                                                      |
|-------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **File**                | `pom.xml`, `maven-surefire-plugin` configuration                                                                                                                            |
| **Dependency**          | `org.jmockit:jmockit:1.49`                                                                                                                                                  |
| **Affected components** | Entire test suite (`ApplicationTest`, `DepositorPropertiesTest`, `KafkaConsumerRestDepositorTest`, `DepositControllerTest`, `KafkaConsumerFactoryTest`, `SDWDepositorTest`) |

**Problem:**  
JMockit works via a Java agent (`-javaagent`) and bytecode manipulation. JMockit 1.49 (released ~2021) predates the stricter module access rules enforced in Java 17+ and exacerbated in Java 21. Spring
Boot's testing infrastructure and the Surefire plugin may enforce `--add-opens` flags differently across versions; if the required opens are not present, JMockit's instrumentation will throw
`InaccessibleObjectException` or silently fail to mock, causing tests to pass for the wrong reasons or fail with cryptic JVM errors.

Additionally, JMockit development has largely stalled; the project has not published a stable release compatible with Java 21's stricter reflection rules. `SDWDepositorTest` already mixes JMockit
annotations with Mockito (`mock(JavaMailSender.class)`), indicating partial awareness of this limitation.

**Remediation:**

- At minimum: verify all tests pass with `mvn test` after the Spring Boot bump. If tests fail with `InaccessibleObjectException`, add the required `--add-opens` JVM args to the Surefire configuration.
- Long-term (recommended): migrate tests from JMockit to Mockito + JUnit 5. The `@Tested`/`@Injectable` pattern maps directly to `@InjectMocks`/`@Mock` in Mockito.

**Surefire fallback (if JMockit fails):**

```xml

<argLine>
    ${argLine}
    -javaagent:${settings.localRepository}/org/jmockit/jmockit/${jmockit-version}/jmockit-${jmockit-version}.jar
    --add-opens java.base/java.lang=ALL-UNNAMED
    --add-opens java.base/java.lang.reflect=ALL-UNNAMED
    --add-opens java.base/java.util=ALL-UNNAMED
</argLine>
```

---

#### RISK-04 — `@PropertySource` on a `@ConfigurationProperties` class is fragile

| Field          | Detail                                                                         |
|----------------|--------------------------------------------------------------------------------|
| **File**       | `src/main/java/jpo/sdw/depositor/DepositorProperties.java`                     |
| **Annotation** | `@PropertySource("classpath:application.properties")` on `DepositorProperties` |

**Problem:**  
Spring Boot's `@ConfigurationProperties` classes load their values from the application's `Environment`, which is already populated with `application.properties`. Placing `@PropertySource` on the same
class is redundant and has been a long-standing source of subtle bugs: `@PropertySource` is processed during `@Configuration` class scanning, which runs later in the lifecycle than
`ConfigurationProperties` binding. In Spring Boot 3.2+, changes to the `ConfigurationProperties` binding lifecycle tightened this ordering, increasing the chance that `@PropertySource` on a non-
`@Configuration` class does not load properties in time.

**Remediation:**  
Remove `@PropertySource("classpath:application.properties")` from `DepositorProperties`. Spring Boot automatically loads `classpath:application.properties`; the annotation is not needed and could
interfere.

```java
// REMOVE this annotation:
@PropertySource("classpath:application.properties")
public class DepositorProperties implements EnvironmentAware {
```

---

#### RISK-05 — Dual web stack (`spring-boot-starter-web` + `spring-boot-starter-webflux`) on classpath

| Field            | Detail                                                                                     |
|------------------|--------------------------------------------------------------------------------------------|
| **File**         | `pom.xml`                                                                                  |
| **Dependencies** | `spring-boot-starter-web` (Tomcat/Servlet), `spring-boot-starter-webflux` (Netty/Reactive) |

**Problem:**  
When both starters are on the classpath, Spring Boot defaults to the Servlet stack (Tomcat). This means WebFlux auto-configuration (e.g., `WebClient` builder bean) is still registered, but the
application runs as a Servlet app. In Spring Boot 3.2+, auto-configuration ordering for mixed stacks changed. The `WebClient.Builder` bean may require explicit declaration if the auto-configured one
is not present in the expected context.

`spring-boot-starter-web` is not used anywhere in the codebase (no `@RestController`, no MVC endpoints). Its only effect is to start Tomcat, which consumes ~50MB of memory and exposes an unnecessary
HTTP port.

**Remediation:**  
Remove `spring-boot-starter-web` from `pom.xml`. `spring-boot-starter-webflux` provides all the reactive WebClient support needed. If the application needs a web server (e.g., for health probes), use
`spring-boot-starter-actuator` with `spring-boot-starter-webflux` instead.

```xml
<!-- REMOVE: -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

---

### MEDIUM

---

#### RISK-06 — `maven-surefire-plugin 3.0.0-M7` is a milestone release

| Field      | Detail                           |
|------------|----------------------------------|
| **File**   | `pom.xml`                        |
| **Plugin** | `maven-surefire-plugin:3.0.0-M7` |

**Problem:**  
Version `3.0.0-M7` is a milestone (pre-release) build. The stable `3.0.0` release and subsequent `3.1.x` / `3.2.x` releases include fixes for JMockit agent ordering, JUnit 4 / Vintage compatibility
with the JUnit Platform, and Java 21 process lifecycle management. Using a milestone in production CI is fragile and may behave differently from the Spring Boot parent's managed version.

**Remediation:**  
Upgrade to the latest stable Surefire release (3.2.x or higher). If the Spring Boot 3.4.x BOM manages a Surefire version, remove the explicit version pin and inherit from the parent.

```xml

<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <!-- Remove <version> and let Spring Boot BOM manage it,
         or pin to a stable release: -->
    <version>3.2.5</version>
    ...
</plugin>
```

---

#### RISK-07 — Spring Boot 3.2+ Virtual Threads interaction with blocking Kafka poll loop

| Field      | Detail                                                                               |
|------------|--------------------------------------------------------------------------------------|
| **File**   | `src/main/java/jpo/sdw/depositor/consumerdepositors/KafkaConsumerRestDepositor.java` |
| **Method** | `run(String... topics)`                                                              |

**Problem:**  
Spring Boot 3.2 introduced opt-in virtual thread support (`spring.threads.virtual.enabled=true`). In Spring Boot 3.4.x, virtual threads are more deeply integrated. `KafkaConsumerRestDepositor.run()`
executes an infinite blocking poll loop (`while (LoopController.loop()) { consumer.poll(...) }`) launched from a `@PostConstruct` method on a `@Component`.

If virtual threads are enabled (now common in 3.4.x deployments), the following risks apply:

- The blocking `poll()` call pins a virtual thread's carrier OS thread, negating scalability benefits and potentially causing thread starvation under load.
- `@PostConstruct` executes on the main application thread during context refresh; with virtual threads configured differently, timing of context shutdown signals may change.

**Remediation:**  
This risk is only triggered if `spring.threads.virtual.enabled=true` is set. Confirm this is not set in the deployment environment. If virtual threads are desired, refactor the Kafka poll loop to run
as an explicit platform thread using a `ThreadFactory` configured for blocking I/O:

```java
Thread.ofPlatform().

name("kafka-consumer").

start(() ->
  kafkaConsumerRestDepositor.

run(topics)
);
```

---

#### RISK-08 — `@Autowired` field injection on `Environment` in `@ConfigurationProperties`

| Field       | Detail                                                                       |
|-------------|------------------------------------------------------------------------------|
| **File**    | `src/main/java/jpo/sdw/depositor/DepositorProperties.java`                   |
| **Pattern** | `@Autowired private Environment environment` + `implements EnvironmentAware` |

**Problem:**  
`DepositorProperties` implements `EnvironmentAware` (correct pattern for getting the `Environment` in a `@ConfigurationProperties` bean) but also has `@Autowired private Environment environment`. This
is redundant and creates two injection paths for the same field. Spring Boot 3.x bean initialization ordering for `ConfigurationProperties` beans became stricter; in 3.4.x, field injection in
`@ConfigurationProperties` classes is not officially supported and may silently fail, leaving the `environment` field null. The `Environment` field is never used in any of the methods (only the
`environment` setter/getter exist; no business logic calls `this.environment`), so the immediate impact is low but it represents a latent defect.

**Remediation:**  
Remove `@Autowired` and the `Environment` field entirely, or keep only the `EnvironmentAware` implementation if the field is ever needed. Since `environment` is not used in any current logic, removing
it entirely is the cleanest fix.

---

#### RISK-09 — `WebClient` builder constructed manually in `DepositController` constructor

| Field       | Detail                                                              |
|-------------|---------------------------------------------------------------------|
| **File**    | `src/main/java/jpo/sdw/depositor/controller/DepositController.java` |
| **Pattern** | `WebClient.builder().baseUrl(...).build()` in constructor body      |

**Problem:**  
Spring Boot 3.x provides an auto-configured `WebClient.Builder` bean that applies global filters, codecs, and observability instrumentation (Micrometer tracing in 3.2+). Constructing `WebClient`
manually bypasses all of this. In Spring Boot 3.4.x, Micrometer tracing is enabled by default for WebClient, and manually-built clients will not emit traces or metrics.

This is a medium risk for observability rather than correctness, but it is worth addressing if trace propagation to the SDX endpoint is ever needed for debugging production issues.

**Remediation:**  
Inject `WebClient.Builder` via constructor injection and call `.build()` from it:

```java

@Autowired
public DepositController(DepositorProperties props, JavaMailSender mailSender, WebClient.Builder webClientBuilder) {
  WebClient client = webClientBuilder
    .baseUrl(props.getDestinationUrl())
    .defaultHeader("apikey", props.getApiKey())
    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
    .build();
    ...
}
```

---

### LOW

---

#### RISK-10 — `docker-compose.yml` uses deprecated `version: '2'` syntax

| Field     | Detail                                                     |
|-----------|------------------------------------------------------------|
| **Files** | `docker-compose.yml`, `docker-compose-confluent-cloud.yml` |

**Problem:**  
The Compose Specification (Docker Compose v2 CLI) has deprecated and removed the top-level `version` key. Docker Desktop and Docker Engine CLI now emit warnings for this field. This does not cause
breakage today but will become an error in future Docker Engine releases.

**Remediation:**  
Remove the `version: '2'` line from both compose files.

---

#### RISK-11 — JUnit 4 will require `junit-vintage-engine` if JUnit Platform is enabled

| Field     | Detail                      |
|-----------|-----------------------------|
| **Files** | `pom.xml`, all test classes |

**Problem:**  
`spring-boot-starter-test` in Spring Boot 3.x includes JUnit Jupiter (JUnit 5) by default and configures Surefire to use the JUnit Platform. Running JUnit 4 tests via the Platform requires
`junit-vintage-engine` on the test classpath. In 3.1.3, this is typically satisfied transitively; in 3.4.x with tighter BOM management, the vintage engine may need an explicit declaration.

**Remediation:**  
Verify the vintage engine is present after the bump:

```xml

<dependency>
    <groupId>org.junit.vintage</groupId>
    <artifactId>junit-vintage-engine</artifactId>
    <scope>test</scope>
</dependency>
```

Long-term: migrate to JUnit 5 (`@Test` from `org.junit.jupiter.api.Test`).

---

#### RISK-12 — `org.json` library bypasses Spring's Jackson auto-configuration

| Field          | Detail                   |
|----------------|--------------------------|
| **File**       | `pom.xml`                |
| **Dependency** | `org.json:json:20231013` |

**Problem:**  
The application constructs all JSON payloads manually using `org.json.JSONObject` and `JSONArray`, bypassing Jackson entirely. This is not a migration risk per se, but newer Spring Boot versions may
include security advisories or CVE patches for `org.json` that require version bumps not covered by the BOM. The `org.json` version is pinned to `20231013`; check for advisories when upgrading.

**Remediation:**  
No immediate action required. Run `mvn dependency:check` or review CVE databases for `org.json:json` after the Spring Boot bump. Consider migrating to Jackson's `ObjectMapper` in a future refactor for
consistency with the Spring ecosystem.

---

## Test Plan

This section covers manual verification steps that fall **outside** the scope of the existing automated test suite. The existing tests use JMockit mocks and do not test actual Kafka connectivity,
actual HTTP deposits, actual email delivery, or Docker container integration.

---

### TP-01 — Build verification

| Step | Action                                                                   | Expected result                                                                                                          |
|------|--------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------|
| 1    | Bump `spring-boot-starter-parent` version in `pom.xml` to target version | `pom.xml` saves cleanly                                                                                                  |
| 2    | `mvn clean package -DskipTests`                                          | Build succeeds with no compilation errors                                                                                |
| 3    | `mvn test`                                                               | All tests pass; no JMockit agent errors in Surefire output                                                               |
| 4    | `mvn verify`                                                             | JaCoCo report generated; no coverage gate failures                                                                       |
| 5    | `mvn dependency:tree \| grep reactor`                                    | Confirm `io.projectreactor:reactor-core` present; `org.projectreactor:reactor-spring` absent (after RISK-01 remediation) |

---

### TP-02 — Docker image builds and starts

| Step | Action                                                                                                                           | Expected result                                                                                                            |
|------|----------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------|
| 1    | `docker build -t jpo-sdw-depositor:test .`                                                                                       | Image builds successfully                                                                                                  |
| 2    | Run container with all required env vars but point `SDW_DESTINATION_URL` to a local mock server (e.g., `json-server` or Mockoon) | Container starts, Spring context initializes, log line `Starting KafkaConsumerRestDepositor listening to topic(s)` appears |
| 3    | Check for startup exceptions in logs                                                                                             | No `NoSuchMethodError`, `ClassNotFoundException`, or Spring `BeanCreationException`                                        |
| 4    | Stop container gracefully (`docker stop`)                                                                                        | Container exits cleanly (code 0 or 143) without hung threads                                                               |

---

### TP-03 — Kafka consumer connectivity (requires local Kafka broker)

| Step | Action                                                                                                                   | Expected result                                                                                                                                                              |
|------|--------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 1    | Start a KRaft-mode Kafka broker locally (see RISK-02 remediation)                                                        | Broker listening on port 9092                                                                                                                                                |
| 2    | Start the depositor container pointing to the local broker                                                               | Depositor subscribes to `SDW_SUBSCRIPTION_TOPIC`                                                                                                                             |
| 3    | Produce a plain-text message to the topic: `kafka-console-producer.sh --topic <topic> --bootstrap-server localhost:9092` | Depositor log shows `Depositing message` entry                                                                                                                               |
| 4    | Produce a JSON-structured message: `{"encodedMsg":"DEADBEEF","estimatedRemovalDate":"2026-12-01"}`                       | Depositor log shows `Depositing message` with JSON record; outgoing HTTP POST body contains `depositRequests` array with both `encodedMsg` and `estimatedRemovalDate` fields |
| 5    | Produce an invalid JSON string (e.g., `notjson`)                                                                         | Depositor log shows message deposited with fallback `encodedMsg` = raw string; no exception thrown                                                                           |
| 6    | Stop broker mid-run                                                                                                      | Depositor logs Kafka consumer errors; does not crash; resumes when broker restores                                                                                           |

---

### TP-04 — HTTP deposit to SDX (mock server or staging SDX)

| Step | Action                                                                | Expected result                                                                                                                         |
|------|-----------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------|
| 1    | Point `SDW_DESTINATION_URL` to a mock server that returns HTTP 200    | Depositor logs `Response received. Status: 200` at INFO level; no email sent                                                            |
| 2    | Point `SDW_DESTINATION_URL` to a mock server that returns HTTP 500    | Depositor logs `Response received. Status: 500` at ERROR level; email sent to `SDW_EMAIL_LIST`                                          |
| 3    | Point `SDW_DESTINATION_URL` to a mock server that returns HTTP 403    | Same as step 2                                                                                                                          |
| 4    | Configure an SMTP mock (e.g., MailHog) and verify the email structure | Email arrives with subject `ODE Failed to Deposit to SDX`; body contains status code and response body; `from` matches `SDW_EMAIL_FROM` |
| 5    | Configure an unreachable SMTP server                                  | Depositor logs `Unable to send deposit failure email` at ERROR; continues processing subsequent messages without crashing               |

---

### TP-05 — Confluent Cloud path (requires Confluent credentials or mock)

| Step | Action                                                                     | Expected result                                                                                                                                  |
|------|----------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------|
| 1    | Set `KAFKA_TYPE=CONFLUENT`, provide `CONFLUENT_KEY` and `CONFLUENT_SECRET` | SASL_SSL properties are loaded (verify via debug logging: `logging.level.org.apache.kafka=DEBUG`)                                                |
| 2    | Connect to a real Confluent Cloud cluster or mock SASL broker              | Consumer subscribes successfully                                                                                                                 |
| 3    | Omit `CONFLUENT_KEY` or `CONFLUENT_SECRET`                                 | Application logs warning `Something went wrong retrieving the environment variable`; does not throw NullPointerException; SASL config is skipped |

---

### TP-06 — Configuration validation at startup

| Step | Action                                                   | Expected result                                                                                                                                                       |
|------|----------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 1    | Start with `SDW_API_KEY` unset                           | Application fails to start; log contains `No API Key specified in configuration`; exit code non-zero                                                                  |
| 2    | Start with `SDW_EMAIL_FROM` set to `badformat`           | Application fails to start; log contains `From email is not a valid email address`                                                                                    |
| 3    | Start with `SDW_EMAIL_LIST` set to `bad@email...com`     | Application fails to start; log contains `Email list is not valid email address(es)`                                                                                  |
| 4    | Start with no `DOCKER_HOST_IP` and no `sdw.kafkaBrokers` | Application logs `Neither sdw.kafkaBrokers ode property nor DOCKER_HOST_IP environment variable are defined. Defaulting to localhost.`; starts using `localhost:9092` |
| 5    | Start with `SDW_SUBSCRIPTION_TOPIC` unset                | Application logs default topic name; subscribes to `topic.SDWDepositorInput`                                                                                          |

---

### TP-07 — Graceful shutdown under message load

| Step | Action                                                           | Expected result                                                                                                                                                                           |
|------|------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 1    | While actively consuming messages, send SIGTERM to the container | Application stops consuming; in-flight messages are committed or re-queued (auto-commit is enabled with 1s interval, so messages within the last 1s window may be reprocessed on restart) |
| 2    | Restart the container immediately after SIGTERM                  | Consumer group resumes from last committed offset; no messages are skipped                                                                                                                |

---

### TP-08 — Observability and logging

| Step | Action                                              | Expected result                                                        |
|------|-----------------------------------------------------|------------------------------------------------------------------------|
| 1    | Deploy with default Spring Boot 3.4.x configuration | No unexpected framework-level WARN or ERROR logs at startup            |
| 2    | Send a batch of 100 messages                        | Log output remains at INFO level; no OOM or thread contention warnings |
| 3    | Check `/actuator/health` (if actuator added)        | Returns `{"status":"UP"}`                                              |

---

## Dependency Change Summary

| Dependency                          | Current                  | Action                       | Reason                                                                                        |
|-------------------------------------|--------------------------|------------------------------|-----------------------------------------------------------------------------------------------|
| `spring-boot-starter-parent`        | 3.1.3                    | **Bump to 3.4.x**            | Target upgrade                                                                                |
| `org.projectreactor:reactor-spring` | 1.0.1.RELEASE            | **Remove**                   | Incompatible old artifact; replaced by transitive `io.projectreactor:reactor-core`            |
| `spring-boot-starter-web`           | inherited                | **Remove**                   | Unused; starts Tomcat unnecessarily; conflicts with reactive stack                            |
| `org.apache.kafka:kafka_2.13`       | 4.0.0                    | **Review**                   | Ensure compatible with broker version; consider aligning with Spring Boot BOM-managed version |
| `maven-surefire-plugin`             | 3.0.0-M7                 | **Upgrade to 3.2.x**         | Milestone release; stable alternatives available                                              |
| `junit:junit`                       | 4.13.2                   | **Add vintage engine**       | Required for JUnit 4 on JUnit Platform                                                        |
| `org.jmockit:jmockit`               | 1.49                     | **Verify/upgrade**           | May fail under Java 21 module restrictions                                                    |
| `docker-compose.yml` broker images  | wurstmeister (ZooKeeper) | **Replace with KRaft image** | ZooKeeper removed in Kafka 4.0                                                                |
