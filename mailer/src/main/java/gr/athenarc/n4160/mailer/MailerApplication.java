package gr.athenarc.n4160.mailer;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MailerApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(MailerApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

	}
}
