package gr.athenarc.n4160.mailer.domain;


import gr.athenarc.n4160.mailer.mailEntities.InitialBudgetEmailEntity;
import gr.athenarc.n4160.mailer.mailEntities.InitialEmailEntity;
import gr.athenarc.n4160.mailer.mailEntities.TemplateLoader;

import java.io.IOException;

public class InitialBudgetMessage extends MailMessage {

    public InitialBudgetMessage(String to, String request_id, String project_acronym, String creation_date, String url ) throws IOException {
        super("4485helpdesk@athena-innovation.gr", "ARC Helpdesk", to, "Υποβολή αιτήματος " + request_id, "");
        String format = TemplateLoader.loadFilledTemplate(new InitialBudgetEmailEntity(request_id,project_acronym,creation_date,url), "emails/initial_budget.html");
        setBody(format);
    }
}
