package gr.athenarc.n4160.mailer.domain;


import gr.athenarc.n4160.mailer.mailEntities.ApproveBudgetEmailEntity;
import gr.athenarc.n4160.mailer.mailEntities.ApproveEmailEntity;
import gr.athenarc.n4160.mailer.mailEntities.TemplateLoader;

import java.io.IOException;

public class ApproveBudgetMessage extends MailMessage {

    public ApproveBudgetMessage(String to, String request_id, String project_acronym, String creation_date, String url ) throws IOException {
        super("4485helpdesk@athena-innovation.gr", "ARC Helpdesk", to, "Ενημέρωση σχετικά με το αίτημα " + request_id, "");
        String format = TemplateLoader.loadFilledTemplate(new ApproveBudgetEmailEntity(request_id,project_acronym,creation_date,url), "emails/approve_budget.html");
        setBody(format);
    }
}
