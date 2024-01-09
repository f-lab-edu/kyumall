package com.kyumall.kyumallcommon.mail;


import org.springframework.stereotype.Service;

@Service
public class MailConsoleService implements MailService {

  @Override
  public void sendMail(String email) {
    System.out.println("---mail send---");
    System.out.println("email : " + email);
  }
}
