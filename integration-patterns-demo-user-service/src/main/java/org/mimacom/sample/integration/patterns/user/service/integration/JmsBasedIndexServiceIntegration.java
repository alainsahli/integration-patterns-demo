/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mimacom.sample.integration.patterns.user.service.integration;


import org.mimacom.sample.integration.patterns.search.service.messages.IndexUserMessage;
import org.mimacom.sample.integration.patterns.user.service.domain.User;
import org.springframework.jms.core.JmsMessagingTemplate;

public class JmsBasedIndexServiceIntegration {

  private final JmsMessagingTemplate jmsMessagingTemplate;

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
