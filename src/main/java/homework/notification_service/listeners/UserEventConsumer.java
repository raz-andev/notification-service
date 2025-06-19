package homework.notification_service.listeners;

import homework.notification_service.dto.UserEvent;
import homework.notification_service.services.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserEventConsumer {

    private final EmailService emailService;

    @Autowired
    public UserEventConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    @KafkaListener(topics = "user-events", groupId = "notification-group")
    public void ConsumerEvent(UserEvent event) {
        String message = event.getEventType().equals("CREATED")
                ? "Здравствуйте! Ваш аккаунт на сайте был успешно создан."
                : "Здравствуйте! Ваш аккаунт был удалён.";

        emailService.sendEmail(event.getEmail(), "Уведомление", message);
        log.info("Email-уведомление отправлено на почту: {}", event.getEmail());
    }
}
