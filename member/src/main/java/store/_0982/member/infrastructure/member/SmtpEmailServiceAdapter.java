package store._0982.member.infrastructure.member;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import store._0982.member.application.member.EmailService;

@Service
@RequiredArgsConstructor
public class SmtpEmailServiceAdapter implements EmailService {

    private final JavaMailSender javaMailSender;

    @Override
    public void sendEmail(String from, String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        javaMailSender.send(message);
    }

}
