package com.han.pwac.pinguins.backend.services.contract;

import com.han.pwac.pinguins.backend.domain.EmailBody;
import com.han.pwac.pinguins.backend.domain.EmailPart;

import java.util.Map;

public interface IMailCronService {
    /**
     * gets a batch of emails
     * @return a map containing the id of the user and the body of the email to send
     */
    Map<Integer, EmailPart> getBatchedMails();
}
