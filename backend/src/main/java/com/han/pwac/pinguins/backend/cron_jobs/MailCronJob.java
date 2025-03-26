package com.han.pwac.pinguins.backend.cron_jobs;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class MailCronJob {
    private static final String EVERY_DAY = "0 0 12 * * *";
    private static final String EVERY_MINUTE = "0 * * * * *";
    private static final String EVERY_SECOND = "* * * * * *";

    private final List<IMailCronService> mailCronServices;
    private final IMailService mailService;
    private final UserService userService;
    private final ICronJobDao cronJobDao;

    @Value("${frontend.url}")
    private String frontEndUrl;

    @Autowired
    public MailCronJob(List<IMailCronService> mailCronServices, IMailService mailService, UserService userService, ICronJobDao cronJobDao) {
        this.mailCronServices = mailCronServices;
        this.mailService = mailService;
        this.userService = userService;
        this.cronJobDao = cronJobDao;
    }

    private Mail createMail(int userId, List<EmailPart> parts) {
        EmailBody body = new EmailBody(parts, frontEndUrl);

        Locale loc = new Locale("nl", "nl");
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.DEFAULT, loc);
        String format = dateFormat.format(Calendar.getInstance().getTime());

        User user = userService.findById(userId)
                .orElseThrow(() -> new NotFoundException("Email kan niet aangemaakt worden omdat de gebruiker naar wie die verstuurd moet worden niet gevonden kan worden."));

        EmailPart main = body.getPart("main")
                .orElseThrow(() -> new EmailPartNotFound("main"));

        main.addHeading("Beste " + user.getUsername(), EmailPart.HeadingSize.H1);
        main.addMessage("Dit is het overzicht van " + format);
        main.addBorderAtBottom();

        EmailPart mainEnd = body.getPart("main-end")
                .orElseThrow(() -> new EmailPartNotFound("main-end"));

        mainEnd.addLinkButton("Ga naar website", frontEndUrl, true);


        return new Mail(
                "Dagelijkse samenvatting van " + format,
                user.getEmail(),
                body
        );
    }

    protected static HashMap<Integer, List<EmailPart>> reduceCombiner(HashMap<Integer, List<EmailPart>> accumulator, HashMap<Integer, List<EmailPart>> element) {
        element.forEach((key, value) -> {
            List<EmailPart> parts = accumulator.get(key);
            parts.addAll(value);
        });
        return accumulator;
    }

    @Scheduled(cron = EVERY_DAY)
    public void execute() {
        HashMap<Integer, List<EmailPart>> map = mailCronServices.stream()
                .map(IMailCronService::getBatchedMails)
                .reduce(new HashMap<>(32), (accumulator, element) -> {
                    element.forEach((key, value) -> {
                        List<EmailPart> parts = accumulator.computeIfAbsent(key, k -> new ArrayList<>(2));
                        parts.add(value);
                    });
                    return accumulator;
                }, MailCronJob::reduceCombiner);

        cronJobDao.create(map.size());
        if (map.isEmpty()) {
            return;
        }

        Mail[] mails = new Mail[map.size()];
        // using an atomic integer because the forEach function can be run on another thread according to Intellij
        AtomicInteger index = new AtomicInteger();
        map.forEach((userId, parts) -> {
            mails[index.get()] = this.createMail(userId, parts);
            index.getAndIncrement();
        });

        mailService.sendMail(mails);
    }

    public void setFrontEndUrl(String value) {
        this.frontEndUrl = value;
    }
}
