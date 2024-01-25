package com.kyumall.kyumallcommon.mail;

public interface MailService {
  void sendMail(String email);
  void sendMail(Mail mail);
}
