package org.mimacom.sample.integration.patterns.search.service;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.apache.activemq.store.jdbc.JDBCPersistenceAdapter;
import org.apache.activemq.store.jdbc.adapter.OracleJDBCAdapter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsMessagingTemplate;
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
  public ConnectionFactory connectionFactory() {
    return new ActiveMQConnectionFactory("vm://localhost");
  }

  @Bean
  public BrokerService brokerService(DataSource dataSource) throws Exception {
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

  @Bean
  public DefaultJmsListenerContainerFactory defaultJmsListenerContainerFactory(PlatformTransactionManager transactionManager, ConnectionFactory connectionFactory) {
    DefaultJmsListenerContainerFactory jmsListenerContainerFactory = new DefaultJmsListenerContainerFactory();
    jmsListenerContainerFactory.setTransactionManager(transactionManager);
    jmsListenerContainerFactory.setSessionTransacted(true);
    jmsListenerContainerFactory.setConnectionFactory(connectionFactory);

    return jmsListenerContainerFactory;
  }

  @Bean
  public JmsMessagingTemplate jmsMessagingTemplate(ConnectionFactory connectionFactory) {
    return new JmsMessagingTemplate(connectionFactory);
  }

}
