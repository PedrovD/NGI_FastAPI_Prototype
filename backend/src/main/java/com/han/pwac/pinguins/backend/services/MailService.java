package com.han.pwac.pinguins.backend.services;

import com.han.pwac.pinguins.backend.domain.Mail;
import com.han.pwac.pinguins.backend.exceptions.MailFailedToSendException;
import com.han.pwac.pinguins.backend.services.contract.IMailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Properties;

@Service
public class MailService implements IMailService {
    @Value("${mailhog.host_name}")
    private String hostName;
    @Value("${mailhog.host_port}")
    private String hostPort;

    private MimeMessage createMimeMessage(JavaMailSender mailSender, Mail mail) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();

        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);

            mimeMessageHelper.setFrom(mail.from());
            mimeMessageHelper.setTo(mail.to());
            mimeMessageHelper.setText(mail.body().toString(), true);
            mimeMessageHelper.setSubject(mail.subject());
        } catch (MessagingException e) {
            throw new MailFailedToSendException("An e-mail failed to send, please try again.", mail);
        }

        return mimeMessage;
    }

    @Override
    public void sendMail(Mail... mails) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        mailSender.setHost(hostName);
        mailSender.setPort(Integer.parseInt(hostPort));

        Properties properties = mailSender.getJavaMailProperties();
        properties.put("mail.transport.protocol", "smtp");

        sendMail(mailSender, mails);
    }

    protected void sendMail(JavaMailSender mailSender, Mail... mails) {
        if (mails.length == 0) {
            return;
        }

        MimeMessage[] messages = new MimeMessage[mails.length];
        for (int i = 0; i < mails.length; i++) {
            Mail mail = mails[i];

            MimeMessage message = createMimeMessage(mailSender, mail);
            messages[i] = message;
        }

        mailSender.send(messages);
    }
}
