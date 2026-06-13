package com.logforwarder.atc;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class LogForwarderAtcIntegrationTest {

    private static final DockerImageName TIMESCALEDB_IMAGE =
            DockerImageName.parse("timescale/timescaledb:2.17.2-pg16");

    @Container
    static GenericContainer<?> timescaledb = new GenericContainer<>(TIMESCALEDB_IMAGE)
            .withExposedPorts(5432)
            .withEnv("POSTGRES_DB", "log_forwarder_atc")
            .withEnv("POSTGRES_USER", "atc")
            .withEnv("POSTGRES_PASSWORD", "atc")
            .waitingFor(Wait.forListeningPort());

    private static MockWebServer agentServer;

    @Autowired
    private TestRestTemplate restTemplate;

    @DynamicPropertySource
    static void registerDatasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () ->
                "jdbc:postgresql://%s:%d/log_forwarder_atc".formatted(
                        timescaledb.getHost(),
                        timescaledb.getMappedPort(5432)
                ));
        registry.add("spring.datasource.username", () -> "atc");
        registry.add("spring.datasource.password", () -> "atc");
    }

    @BeforeAll
    static void startAgentServer() throws IOException {
        agentServer = new MockWebServer();
        agentServer.start();
    }

    @AfterAll
    static void stopAgentServer() throws IOException {
        if (agentServer != null) {
            agentServer.shutdown();
        }
    }

    @Test
    void contextLoadsAndFlywayMigrates() {
        assertThat(timescaledb.isRunning()).isTrue();
    }

    @Test
    void registerAgentAndListInstances() {
        enqueueSuccessfulAgentResponses();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = """
                {
                  "hostname": "127.0.0.1",
                  "port": %d,
                  "process_id": 12345,
                  "timestamp": "2026-06-11T14:30:00Z"
                }
                """.formatted(agentServer.getPort());

        ResponseEntity<Map> putResponse = restTemplate.exchange(
                "/api/instances",
                org.springframework.http.HttpMethod.PUT,
                new HttpEntity<>(body, headers),
                Map.class
        );

        assertThat(putResponse.getStatusCode()).isIn(HttpStatus.CREATED, HttpStatus.OK);
        assertThat(putResponse.getBody()).isNotNull();
        assertThat(putResponse.getBody().get("hostname")).isEqualTo("127.0.0.1");
        assertThat(putResponse.getBody().get("port")).isEqualTo(agentServer.getPort());

        ResponseEntity<List> listResponse = restTemplate.getForEntity("/api/instances", List.class);

        assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listResponse.getBody()).hasSize(1);
    }

    @Test
    void notFoundReturnsConsistentErrorBody() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                "/api/instances/00000000-0000-0000-0000-000000000000",
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("error", "Instance not found");
        assertThat(response.getBody()).containsEntry("status", 404);
    }

    private static void enqueueSuccessfulAgentResponses() {
        agentServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("{\"status\":\"UP\",\"process_id\":12345}"));
        agentServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("{\"status\":\"READY\",\"process_id\":12345}"));
        agentServer.enqueue(new MockResponse().setBody("""
                log_forwarder_files_watched 1
                log_forwarder_lines_published_total 10
                log_forwarder_lines_read_total 8
                log_forwarder_pipeline_buffer_depth 0
                log_forwarder_publish_hibernating 0
                """));
    }
}
