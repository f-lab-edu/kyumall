package com.kyumall.kyumallcommon.mail;


import org.springframework.stereotype.Service;

@Service
public class MailConsoleService implements MailService {

  @Override
  public void sendMail(String email) {
    System.out.println("---mail send---");
    System.out.println("email : " + email);
  }

  @Override
  public void sendMail(Mail mail) {
    System.out.println("---to---" + mail.getTo());
    System.out.println("---subject---" + mail.getSubject());
    System.out.println("---message---" + mail.getMessage());
  }
}
