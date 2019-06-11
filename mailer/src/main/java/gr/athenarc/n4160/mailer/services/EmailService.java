package gr.athenarc.n4160.mailer.services;


import gr.athenarc.n4160.mailer.domain.MailMessage;

import java.util.List;

public interface EmailService {

    void sendMail(List<MailMessage> mailMessages);
}
