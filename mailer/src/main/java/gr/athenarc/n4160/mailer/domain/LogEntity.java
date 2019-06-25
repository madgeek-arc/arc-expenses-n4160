package gr.athenarc.n4160.mailer.domain;

import javax.persistence.*;
import java.util.Date;

@Entity(name = "emails_sent")
public class LogEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Column
    private String receiver;

    @Column
    private String subject;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_sent", nullable = false, updatable = false)
    private Date when;


    public LogEntity(String receiver, String subject, Date when) {
        this.receiver = receiver;
        this.subject = subject;
        this.when = when;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Date getWhen() {
        return when;
    }

    public void setWhen(Date when) {
        this.when = when;
    }
}
