package gr.athenarc.n4160.mailer.domain;


import gr.athenarc.n4160.mailer.mailEntities.CancelBudgetEmailEntity;
import gr.athenarc.n4160.mailer.mailEntities.CancelEmailEntity;
import gr.athenarc.n4160.mailer.mailEntities.TemplateLoader;

import java.io.IOException;

public class CancelBudgetMessage extends MailMessage {

    public CancelBudgetMessage(String to, String request_id, String project_acronym, String creation_date, String url ) throws IOException {
        super("4485helpdesk@athena-innovation.gr", "ARC Helpdesk", to, "Ακύρωση του αιτήματος " + request_id, "");
        String format = TemplateLoader.loadFilledTemplate(new CancelBudgetEmailEntity(request_id,project_acronym,creation_date,url), "emails/cancel_budget.html");
        setBody(format);
    }
}
