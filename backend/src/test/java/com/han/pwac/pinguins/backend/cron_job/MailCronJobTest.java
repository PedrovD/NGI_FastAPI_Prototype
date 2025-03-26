package com.han.pwac.pinguins.backend.cron_job;

import com.han.pwac.pinguins.backend.cron_jobs.MailCronJob;
import com.han.pwac.pinguins.backend.domain.EmailBody;
import com.han.pwac.pinguins.backend.domain.EmailPart;
import com.han.pwac.pinguins.backend.domain.Mail;
import com.han.pwac.pinguins.backend.domain.User;
import com.han.pwac.pinguins.backend.exceptions.EmailPartNotFound;
import com.han.pwac.pinguins.backend.exceptions.NotFoundException;
import com.han.pwac.pinguins.backend.repository.contract.ICronJobDao;
import com.han.pwac.pinguins.backend.services.UserService;
import com.han.pwac.pinguins.backend.services.contract.IMailCronService;
import com.han.pwac.pinguins.backend.services.contract.IMailService;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MailCronJobTest {

    private static class MailCronServiceImpl implements IMailCronService {
        public static final String HEADER = "Mijn header:";

        @Override
        public Map<Integer, EmailPart> getBatchedMails() {
            HashMap<Integer, EmailPart> parts = new HashMap<>(3);

            EmailPart part = new EmailPart("main");
            part.addHeading(HEADER, EmailPart.HeadingSize.H3);
            part.addMessage("Mijn bericht:");

            parts.put(1, part);
            parts.put(2, part);
            parts.put(3, part);

            return parts;
        }
    }

    private static class MailCronServiceImpl2 implements IMailCronService {
        public static final String HEADER = "Mijn header versie 2:";

        @Override
        public Map<Integer, EmailPart> getBatchedMails() {
            HashMap<Integer, EmailPart> parts = new HashMap<>(3);

            EmailPart part = new EmailPart("main");
            part.addHeading(HEADER, EmailPart.HeadingSize.H3);
            part.addMessage("Mijn bericht:");

            parts.put(1, part);
            parts.put(4, part);
            parts.put(5, part);

            return parts;
        }
    }

    private static class MailCronServiceImplInvalidPart implements IMailCronService {

        @Override
        public Map<Integer, EmailPart> getBatchedMails() {
            HashMap<Integer, EmailPart> parts = new HashMap<>(3);

            EmailPart part = new EmailPart("oisjdjasjiodjioasijofijoaiojsfijosoijdfjoisdijofjio");
            part.addMessage("Mijn bericht:");

            parts.put(1, part);

            return parts;
        }
    }

    private static class MailCronJobHelper extends MailCronJob {

        public MailCronJobHelper(List<IMailCronService> mailCronServices, IMailService mailService, UserService userService, ICronJobDao cronJobDao) {
            super(mailCronServices, mailService, userService, cronJobDao);
        }

        public HashMap<Integer, List<EmailPart>> reduceCombinerHelper(HashMap<Integer, List<EmailPart>> accumulator, HashMap<Integer, List<EmailPart>> element) {
            return MailCronJob.reduceCombiner(accumulator, element);
        }
    }

    @Mock
    private List<IMailCronService> services;

    @Mock
    private IMailService mailService;

    @Mock
    private UserService userService;

    @Mock
    private ICronJobDao cronJobDao;

    @InjectMocks
    private MailCronJob mailCronJob;

    @BeforeEach
    public void before() {
        this.mailCronJob.setFrontEndUrl("web");
    }

    @Test
    public void test_MailCronJob_valid() {
        // arrange
        Stream<IMailCronService> stream = Stream.of(new MailCronServiceImpl());
        when(services.stream()).thenReturn(stream);

        String email = "email@email.com";
        when(userService.findById(anyInt())).thenReturn(Optional.of(new User(1, "name", "img", email)));

        ArgumentCaptor<Mail[]> mailsCaptor = ArgumentCaptor.forClass(Mail[].class);
        doNothing().when(mailService).sendMail(mailsCaptor.capture());

        // act
        mailCronJob.execute();

        // assert
        verify(services, times(1)).stream();
        verify(userService, times(3)).findById(anyInt());
        verify(mailService, times(1)).sendMail(any(Mail[].class));

        verify(cronJobDao, times(1)).create(anyInt());

        Mail[] mails = mailsCaptor.getValue();
        assertEquals(3, mails.length);
        for (Mail mail : mails) {
            assertEquals(email, mail.to());
            assertTrue(mail.body().contains(MailCronServiceImpl.HEADER));

            assertTrue(mail.body().contains("Beste"));
            assertTrue(mail.body().contains("Dit is het overzicht"));
            assertTrue(mail.body().contains("Ga naar website"));
            assertEquals(2, StringUtils.countMatches(mail.body(), EmailPart.BORDER));
        }
    }

    @Test
    public void test_MailCronJob_multipleServices() {
        // arrange
        Stream<IMailCronService> stream = Stream.of(new MailCronServiceImpl(), new MailCronServiceImpl2());
        when(services.stream()).thenReturn(stream);

        String email = "email@email.com";
        when(userService.findById(anyInt())).thenReturn(Optional.of(new User(1, "name", "img", email)));

        ArgumentCaptor<Mail[]> mailsCaptor = ArgumentCaptor.forClass(Mail[].class);
        doNothing().when(mailService).sendMail(mailsCaptor.capture());

        // act
        mailCronJob.execute();

        // assert
        verify(services, times(1)).stream();
        verify(userService, times(5)).findById(anyInt());
        verify(mailService, times(1)).sendMail(any(Mail[].class));

        verify(cronJobDao, times(1)).create(anyInt());

        Mail[] mails = mailsCaptor.getValue();
        assertEquals(5, mails.length);

        Mail first = mails[0];
        assertEquals(email, first.to());
        assertTrue(first.body().contains(MailCronServiceImpl.HEADER));
        assertTrue(first.body().contains(MailCronServiceImpl2.HEADER));

        for (int i = 1; i < 3; i++) {
            Mail mail = mails[i];
            assertEquals(email, mail.to());
            assertTrue(mail.body().contains(MailCronServiceImpl.HEADER));
        }

        for (int i = 3; i < 5; i++) {
            Mail mail = mails[i];
            assertEquals(email, mail.to());
            assertTrue(mail.body().contains(MailCronServiceImpl2.HEADER));
        }
    }

    @Test
    public void test_MailCronJob_userNotFound() {
        // arrange
        Stream<IMailCronService> stream = Stream.of(new MailCronServiceImpl());
        when(services.stream()).thenReturn(stream);

        when(userService.findById(anyInt())).thenReturn(Optional.empty());

        // act + assert
        assertThrows(NotFoundException.class, () -> mailCronJob.execute());

        verify(services, times(1)).stream();
        verify(userService, times(1)).findById(anyInt());
    }

    @Test
    public void test_MailCronJob_invalidPart() {
        // arrange
        Stream<IMailCronService> stream = Stream.of(new MailCronServiceImplInvalidPart());
        when(services.stream()).thenReturn(stream);

        // act + assert
        assertThrows(EmailPartNotFound.class, () -> mailCronJob.execute());

        verify(services, times(1)).stream();
    }

    @Test
    public void test_MailCronJob_noCronServices() {
        // arrange
        Stream<IMailCronService> stream = Stream.of();
        when(services.stream()).thenReturn(stream);

        // act
        mailCronJob.execute();

        // assert
        verify(services, times(1)).stream();
        verify(userService, times(0)).findById(anyInt());
        verify(mailService, times(0)).sendMail(any(Mail[].class));
    }

    @Test
    public void test_MailCronJob_reduceCombinerTest() {
        // Arrange
        HashMap<Integer, List<EmailPart>> parts1 = new HashMap<>();
        List<EmailPart> list2 = new ArrayList<>();
        list2.add(new EmailPart("part2"));
        parts1.put(1, list2);

        HashMap<Integer, List<EmailPart>> parts2 = new HashMap<>();
        List<EmailPart> list = new ArrayList<>();
        list.add(new EmailPart("part2"));
        parts2.put(1, list);

        // Act
        HashMap<Integer, List<EmailPart>> parts = new MailCronJobHelper(null, null, null, null).reduceCombinerHelper(parts1, parts2);

        // Assert
        Assertions.assertTrue(parts.containsKey(1));
        assertEquals(2, parts.get(1).size());
    }
}
