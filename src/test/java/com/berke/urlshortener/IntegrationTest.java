package com.berke.urlshortener;

import com.berke.urlshortener.dto.CreateShortUrlRequest;
import com.berke.urlshortener.dto.ShortUrlResponse;
import com.berke.urlshortener.entity.ShortUrl;
import com.berke.urlshortener.repository.ShortUrlRepository;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) 
@Testcontainers
class IntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ShortUrlRepository repository;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    static RedisContainer redis = new RedisContainer(DockerImageName.parse("redis:7.2.4-alpine"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }
    
    @BeforeAll
    static void startContainers() {
        postgres.start();
        redis.start();
    }
    
    @AfterAll
    static void stopContainers() {
        postgres.stop();
        redis.stop();
    }

    @Test
    @DisplayName("End-to-End: Link Oluştur ve Veritabanını Kontrol Et")
    void shouldCreateShortUrlAndPersistToDatabase() {

        String originalUrl = "https://www.github.com/berke";
        CreateShortUrlRequest request = new CreateShortUrlRequest(originalUrl, null);

        String url = "http://localhost:" + port + "/api/v1/urls";
        ResponseEntity<ShortUrlResponse> response = restTemplate.postForEntity(url, request, ShortUrlResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        String shortCode = response.getBody().getShortCode();
        assertThat(shortCode).isNotEmpty();

        Optional<ShortUrl> dbRecord = repository.findByShortCode(shortCode);
        assertThat(dbRecord).isPresent();
        assertThat(dbRecord.get().getOriginalUrl()).isEqualTo(originalUrl);
    }
}