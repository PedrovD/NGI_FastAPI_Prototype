package com.han.pwac.pinguins.backend.services;

import com.han.pwac.pinguins.backend.domain.EmailBody;
import com.han.pwac.pinguins.backend.domain.Mail;
import com.han.pwac.pinguins.backend.exceptions.MailFailedToSendException;
import jakarta.mail.*;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MailServiceTest {
    private static class MailServiceTestHelper extends MailService {
        @Override
        public void sendMail(JavaMailSender mailSender, Mail... mails) {
            super.sendMail(mailSender, mails);
        }
    }

    private static class MailHelper extends Mail {

        public static final String FROM_ADDRESS = "from@email.com";

        public MailHelper(String subject, String to, EmailBody body) {
            super(subject, to, body);
        }

        @Override
        public String from() {
            return FROM_ADDRESS;
        }
    }

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    private final MailServiceTestHelper helper = new MailServiceTestHelper();

    @Test
    public void test_MailService_mailsSent() throws MessagingException, IOException {
        // Arrange
        ArgumentCaptor<Address> addressCaptor = ArgumentCaptor.forClass(Address.class);
        ArgumentCaptor<Multipart> setContentCaptor = ArgumentCaptor.forClass(Multipart.class);
        ArgumentCaptor<MimeMessage[]> sendCaptor = ArgumentCaptor.forClass(MimeMessage[].class);

        doNothing().when(mimeMessage).setRecipient(any(Message.RecipientType.class), addressCaptor.capture());
        doNothing().when(mimeMessage).setContent(setContentCaptor.capture());

        doNothing().when(mailSender).send(sendCaptor.capture());

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);


        String receiver = "receiver@email.com";

        EmailBody body = new EmailBody(false, "web");
        body.getBuilder().append("My email text");

        String subject = "My new email";

        Mail mail = new MailHelper(
                subject,
                receiver,
                body
        );

        // Act
        helper.sendMail(mailSender, mail);

        // assert
        verify(mailSender, times(1)).send(any(MimeMessage[].class));
        verify(mimeMessage, times(1)).setFrom(any(Address.class));

        verify(mimeMessage, times(1)).setRecipient(eq(Message.RecipientType.TO), any(Address.class));
        assertEquals(receiver, addressCaptor.getValue().toString());

        verify(mimeMessage, times(1)).setContent(any(Multipart.class));
        Multipart multipart = setContentCaptor.getValue();
        BodyPart bodyPart = multipart.getBodyPart(0);
        Multipart innerPart = (Multipart)bodyPart.getContent();
        Object htmlText = innerPart.getBodyPart(0).getContent();
        assertEquals(body.toString(), htmlText);

        verify(mimeMessage, times(1)).setSubject(subject);

        assertEquals(1, sendCaptor.getValue().length);
    }

    @Test
    public void test_MailService_multiMailsSent() throws MessagingException, IOException {
        // Arrange
        ArgumentCaptor<Address> addressCaptor = ArgumentCaptor.forClass(Address.class);
        ArgumentCaptor<Multipart> setContentCaptor = ArgumentCaptor.forClass(Multipart.class);
        ArgumentCaptor<MimeMessage[]> sendCaptor = ArgumentCaptor.forClass(MimeMessage[].class);

        doNothing().when(mimeMessage).setRecipient(any(Message.RecipientType.class), addressCaptor.capture());
        doNothing().when(mimeMessage).setContent(setContentCaptor.capture());

        doNothing().when(mailSender).send(sendCaptor.capture());

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);


        String receiver = "receiver@email.com";

        EmailBody body = new EmailBody(false, "web");
        body.getBuilder().append("My email text");

        String subject = "My new email";

        Mail mail = new MailHelper(
                subject,
                receiver,
                body
        );

        String receiver2 = "receiver2@email.com";

        EmailBody body2 = new EmailBody(false, "web");
        body.getBuilder().append("My email 2 text");

        String subject2 = "My second email";

        Mail mail2 = new MailHelper(
                subject2,
                receiver2,
                body2
        );

        // Act
        helper.sendMail(mailSender, mail, mail2);

        // assert
        verify(mailSender, times(1)).send(any(MimeMessage.class), any(MimeMessage.class));
        verify(mimeMessage, times(2)).setFrom(any(Address.class));

        verify(mimeMessage, times(2)).setRecipient(eq(Message.RecipientType.TO), any(Address.class));
        assertEquals(receiver2, addressCaptor.getValue().toString());

        verify(mimeMessage, times(2)).setContent(any(Multipart.class));
        Multipart multipart = setContentCaptor.getValue();
        BodyPart bodyPart = multipart.getBodyPart(0);
        Multipart innerPart = (Multipart)bodyPart.getContent();
        Object htmlText = innerPart.getBodyPart(0).getContent();
        assertEquals(body2.toString(), htmlText);

        verify(mimeMessage, times(2)).setSubject(anyString());

        assertEquals(2, sendCaptor.getValue().length);
    }

    @Test
    public void test_MailService_messagingExceptionThrown1() throws MessagingException, IOException {
        // Arrange
        ArgumentCaptor<Address> addressCaptor = ArgumentCaptor.forClass(Address.class);
        ArgumentCaptor<Multipart> setContentCaptor = ArgumentCaptor.forClass(Multipart.class);

        doNothing().when(mimeMessage).setRecipient(any(Message.RecipientType.class), addressCaptor.capture());
        doNothing().when(mimeMessage).setContent(setContentCaptor.capture());
        doThrow(MessagingException.class).when(mimeMessage).setSubject(anyString());

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        String receiver = "receiver@email.com";

        EmailBody body = new EmailBody(false, "web");
        body.getBuilder().append("My email text");

        String subject = "My new email";

        Mail mail = new MailHelper(
                subject,
                receiver,
                body
        );

        // Act + assert
        assertThrows(MailFailedToSendException.class, () -> helper.sendMail(mailSender, mail));

        verify(mimeMessage, times(1)).setFrom(any(Address.class));

        verify(mimeMessage, times(1)).setRecipient(eq(Message.RecipientType.TO), any(Address.class));
        assertEquals(receiver, addressCaptor.getValue().toString());

        verify(mimeMessage, times(1)).setContent(any(Multipart.class));
        Multipart multipart = setContentCaptor.getValue();
        BodyPart bodyPart = multipart.getBodyPart(0);
        Multipart innerPart = (Multipart)bodyPart.getContent();
        Object htmlText = innerPart.getBodyPart(0).getContent();
        assertEquals(body.toString(), htmlText);

        verify(mimeMessage, times(1)).setSubject(subject);
    }

    @Test
    public void test_MailService_messagingExceptionThrown2() throws MessagingException {
        // Arrange
        ArgumentCaptor<Address> addressCaptor = ArgumentCaptor.forClass(Address.class);

        doThrow(MessagingException.class).when(mimeMessage).setRecipient(any(Message.RecipientType.class), addressCaptor.capture());


        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        String receiver = "receiver@email.com";

        EmailBody body = new EmailBody(false, "web");
        body.getBuilder().append("My email text");

        String subject = "My new email";

        Mail mail = new MailHelper(
                subject,
                receiver,
                body
        );

        // Act + assert
        assertThrows(MailFailedToSendException.class, () -> helper.sendMail(mailSender, mail));

        verify(mimeMessage, times(1)).setFrom(any(Address.class));

        verify(mimeMessage, times(1)).setRecipient(eq(Message.RecipientType.TO), any(Address.class));
        assertEquals(receiver, addressCaptor.getValue().toString());
    }

    @Test
    public void test_MailService_messagingExceptionThrown3() throws MessagingException {
        // Arrange
        doThrow(MessagingException.class).when(mimeMessage).setFrom(any(Address.class));


        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        String receiver = "receiver@email.com";

        EmailBody body = new EmailBody(false, "web");
        body.getBuilder().append("My email text");

        String subject = "My new email";

        Mail mail = new MailHelper(
                subject,
                receiver,
                body
        );

        // Act + assert
        assertThrows(MailFailedToSendException.class, () -> helper.sendMail(mailSender, mail));

        verify(mimeMessage, times(1)).setFrom(any(Address.class));
    }

    @Test
    public void test_MailService_noMailsSent() {
        // Act
        helper.sendMail(mailSender);

        // Assert
        verify(mailSender, times(0)).send(any(MimeMessage.class));
    }
}
