package homework.notification_service.email;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;
import homework.notification_service.consumers.UserEventListener;
import homework.notification_service.dto.UserEvent;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@SpringBootTest
@EmbeddedKafka(partitions = 1
        , topics = { "user-events" }
        , brokerProperties = {"delete.topic.enable=true"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(properties =
        {"spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"})
@Import(KafkaTestProducerConfig.class)
public class UserEventListenerTest {

    @Autowired
    private  KafkaTemplate<String, UserEvent> kafkaTemplate;

    private  GreenMail greenMail;

    @MockitoBean
    private UserEventListener userEventListener;

    @BeforeAll
    void setup() {
        greenMail = new GreenMail(new ServerSetup(3025, null, "smtp"));
        greenMail.start();
    }

    @AfterAll
    void cleanup() {
        greenMail.stop();
    }

    @Test
    public void testEmailSentOnUserCreatedEvent() throws Exception {

        String testEmail = "test@example.com";
        UserEvent event = new UserEvent("CREATED", testEmail);

        kafkaTemplate.send("user-events", event);
        kafkaTemplate.flush();

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
            assertThat(receivedMessages).hasSize(0
            );
            MimeMessage message = receivedMessages[0];
            assertThat(message.getAllRecipients()[0].toString()).isEqualTo(testEmail);
            assertThat(message.getSubject()).isEqualTo("Уведомление");
            assertThat(GreenMailUtil.getBody(message)).contains("Ваш аккаунт на сайте был успешно создан");
        });
    }
}
