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
