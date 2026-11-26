package com.ems.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.ems.util.EmailUtil.EmailContent;

/**
 * Sends notification emails.
 * - Emails are sent only after the surrounding DB transaction commits (no mail for rolled-back changes).
 * - A mail failure is logged and never breaks the business operation.
 * - When ems.mail.enabled=false (default) the email is only written to the log.
 */
@Service
public class EmailService {

	private static final Logger log = LoggerFactory.getLogger(EmailService.class);

	private final ObjectProvider<JavaMailSender> mailSender;
	private final boolean enabled;
	private final String from;

	public EmailService(ObjectProvider<JavaMailSender> mailSender,
			@Value("${ems.mail.enabled:false}") boolean enabled,
			@Value("${ems.mail.from:no-reply@ems.com}") String from) {
		this.mailSender = mailSender;
		this.enabled = enabled;
		this.from = from;
	}

	public void send(String to, EmailContent content) {
		if (to == null || to.isBlank()) {
			return;
		}
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					sendNow(to, content);
				}
			});
		} else {
			sendNow(to, content);
		}
	}

	private void sendNow(String to, EmailContent content) {
		JavaMailSender sender = enabled ? mailSender.getIfAvailable() : null;
		if (sender == null) {
			log.info("[EMAIL disabled] To: {} | Subject: {}\n{}", to, content.subject(), content.body());
			return;
		}
		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(from);
			message.setTo(to);
			message.setSubject(content.subject());
			message.setText(content.body());
			sender.send(message);
			log.info("Email sent to {}: {}", to, content.subject());
		} catch (RuntimeException e) {
			log.warn("Could not send email to {} ({}): {}", to, content.subject(), e.getMessage());
		}
	}
}
