package com.kyumall.kyumallcommon.mail.repository;

import com.kyumall.kyumallcommon.mail.domain.EmailTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, String> {
}
