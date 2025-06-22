package homework.notification_service.consumers;

import homework.notification_service.dto.UserEvent;
import homework.notification_service.services.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserEventListener {

    private final EmailService emailService;

    @Autowired
    public UserEventListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @KafkaListener(topics = "user-events", groupId = "notification-group")
    public void listen(UserEvent event) {
        log.info("Received user event: {}", event.getEvent());
        String text;
        if ("CREATED".equalsIgnoreCase(event.getEvent())) {
            text = "Здравствуйте! Ваш аккаунт на сайте был успешно создан.";
        } else {
            text = "Здравствуйте! Ваш аккаунт был удалён.";
        }
        log.info("Email from ProducerUserEvent {}", event.getEmail());
        emailService.sendEmail(event.getEmail().trim(), "Уведомление", text);
    }
}
