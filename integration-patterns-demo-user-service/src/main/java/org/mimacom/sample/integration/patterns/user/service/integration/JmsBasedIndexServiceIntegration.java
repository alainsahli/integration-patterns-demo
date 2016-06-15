package org.mimacom.sample.integration.patterns.user.service.integration;


import org.mimacom.sample.integration.patterns.search.service.messages.IndexUserMessage;
import org.mimacom.sample.integration.patterns.user.service.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class JmsBasedIndexServiceIntegration {

  private final JmsMessagingTemplate jmsMessagingTemplate;

  @Autowired
  public JmsBasedIndexServiceIntegration(JmsMessagingTemplate jmsMessagingTemplate) {
    this.jmsMessagingTemplate = jmsMessagingTemplate;
  }

  public void indexUser(User user) {
    this.jmsMessagingTemplate.convertAndSend("index-service-queue", new IndexUserMessage(user.getId(), user.getFirstName(), user.getLastName()));
  }

  public void indexUserSlow(User user, int waitTime) {
    IndexUserMessage indexUserMessage = new IndexUserMessage(user.getId(), user.getFirstName(), user.getLastName());
    indexUserMessage.setWaitTime(waitTime);

    this.jmsMessagingTemplate.convertAndSend("index-service-queue", indexUserMessage);
  }

}
