package org.mimacom.sample.integration.patterns.search.service.service;

import org.mimacom.sample.integration.patterns.search.service.document.User;
import org.mimacom.sample.integration.patterns.search.service.messages.IndexUserMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;

import java.lang.invoke.MethodHandles;

public class IndexUserListenerEndpoint {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final SearchService searchService;

  public IndexUserListenerEndpoint(SearchService searchService) {
    this.searchService = searchService;
  }

  @JmsListener(destination = "index-service-queue")
  public void indexUserListenerEndpoint(IndexUserMessage indexUserMessage) {
    LOG.info("received index user message '{}' '{}'", indexUserMessage.getFirstName(), indexUserMessage.getLastName());

    if (indexUserMessage.getWaitTime() != null) {
      waitFor(indexUserMessage.getWaitTime());
    }

    this.searchService.indexUser(new User(indexUserMessage.getId(), indexUserMessage.getFirstName(), indexUserMessage.getLastName()));

    LOG.info("indexed user '{}' '{}'", indexUserMessage.getFirstName(), indexUserMessage.getLastName());
  }

  private static void waitFor(int waitTimeInSeconds) {
    try {
      Thread.sleep(waitTimeInSeconds * 1000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

}
