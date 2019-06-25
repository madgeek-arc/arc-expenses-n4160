package gr.athenarc.n4160.mailer.domain;


import gr.athenarc.n4160.mailer.mailEntities.RejectBudgetEmailEntity;
import gr.athenarc.n4160.mailer.mailEntities.RejectEmailEntity;
import gr.athenarc.n4160.mailer.mailEntities.TemplateLoader;

import java.io.IOException;

public class RejectBudgetMessage extends MailMessage {

    public RejectBudgetMessage(String to, String request_id, String project_acronym, String creation_date,String url ) throws IOException {
        super("4485helpdesk@athena-innovation.gr", "ARC Helpdesk", to, "Απόρριψη του αιτήματος " + request_id, "");
        String format = TemplateLoader.loadFilledTemplate(new RejectBudgetEmailEntity(request_id,project_acronym,creation_date,url), "emails/reject_budget.html");
        setBody(format);
    }
}
