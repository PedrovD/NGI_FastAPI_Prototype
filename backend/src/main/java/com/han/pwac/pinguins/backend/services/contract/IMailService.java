package com.han.pwac.pinguins.backend.services.contract;

import com.han.pwac.pinguins.backend.domain.Mail;
import org.springframework.web.context.request.async.DeferredResult;

public interface IMailService {
    void sendMail(Mail... mail);
}
