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

package org.mimacom.sample.integration.patterns.search.service;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.apache.activemq.store.jdbc.JDBCPersistenceAdapter;
import org.apache.activemq.store.jdbc.adapter.OracleJDBCAdapter;
import org.mimacom.sample.integration.patterns.search.service.service.IndexUserListenerEndpoint;
import org.mimacom.sample.integration.patterns.search.service.service.SearchService;
import org.mimacom.sample.integration.patterns.search.service.web.SearchServiceController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.transaction.PlatformTransactionManager;

import javax.jms.ConnectionFactory;
import javax.sql.DataSource;

import static java.net.URI.create;
import static java.util.Collections.singletonList;

@SpringBootApplication
@EnableJms
public class SearchServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(SearchServiceApplication.class, args);
  }

  @Bean
  public SearchServiceController searchServiceController(SearchService searchService) {
    return new SearchServiceController(searchService);
  }

  @Bean
  public SearchService searchService() {
    return new SearchService();
  }


  @Profile("jms")
  @Configuration
  static class JmsSearchServiceConfiguration {

    @Bean
    public IndexUserListenerEndpoint indexUserListenerEndpoint(SearchService searchService) {
      return new IndexUserListenerEndpoint(searchService);
    }

    @Bean
    public DefaultJmsListenerContainerFactory defaultJmsListenerContainerFactory(@SuppressWarnings("SpringJavaAutowiringInspection") PlatformTransactionManager transactionManager, ConnectionFactory connectionFactory) {
      DefaultJmsListenerContainerFactory jmsListenerContainerFactory = new DefaultJmsListenerContainerFactory();
      jmsListenerContainerFactory.setTransactionManager(transactionManager);
      jmsListenerContainerFactory.setSessionTransacted(true);
      jmsListenerContainerFactory.setConnectionFactory(connectionFactory);

      return jmsListenerContainerFactory;
    }

    @Bean
    public ConnectionFactory connectionFactory() {
      return new ActiveMQConnectionFactory("vm://localhost");
    }

    @Bean
    public BrokerService brokerService(@SuppressWarnings("SpringJavaAutowiringInspection") DataSource dataSource) throws Exception {
      JDBCPersistenceAdapter persistenceAdapter = new JDBCPersistenceAdapter();
      persistenceAdapter.setDataSource(dataSource);
      persistenceAdapter.setAdapter(new OracleJDBCAdapter());
      persistenceAdapter.setCreateTablesOnStartup(true);

      TransportConnector transportConnector = new TransportConnector();
      transportConnector.setUri(create("tcp://localhost:0"));
      transportConnector.setDiscoveryUri(create("multicast://default"));

      BrokerService brokerService = new BrokerService();
      brokerService.setPersistenceAdapter(persistenceAdapter);
      brokerService.setUseJmx(true);
      brokerService.setUseShutdownHook(true);
      brokerService.addNetworkConnector("multicast://default");
      brokerService.setTransportConnectors(singletonList(transportConnector));

      return brokerService;
    }

  }


}
