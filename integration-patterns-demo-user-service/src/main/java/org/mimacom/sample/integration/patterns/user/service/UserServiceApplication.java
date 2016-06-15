package org.mimacom.sample.integration.patterns.user.service;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.apache.activemq.store.jdbc.JDBCPersistenceAdapter;
import org.apache.activemq.store.jdbc.adapter.OracleJDBCAdapter;
import org.mimacom.sample.integration.patterns.user.service.integration.AsyncSearchServiceIntegration;
import org.mimacom.sample.integration.patterns.user.service.integration.BulkHeadedSearchServiceIntegration;
import org.mimacom.sample.integration.patterns.user.service.integration.HystrixSearchServiceIntegration;
import org.mimacom.sample.integration.patterns.user.service.integration.JmsBasedIndexServiceIntegration;
import org.mimacom.sample.integration.patterns.user.service.integration.SimpleSearchServiceIntegration;
import org.mimacom.sample.integration.patterns.user.service.web.AsyncUserController;
import org.mimacom.sample.integration.patterns.user.service.web.BulkHeadedUserController;
import org.mimacom.sample.integration.patterns.user.service.web.HystrixUserController;
import org.mimacom.sample.integration.patterns.user.service.web.JmsAndHystrixUserController;
import org.mimacom.sample.integration.patterns.user.service.web.SimpleUserController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.jms.ConnectionFactory;
import javax.sql.DataSource;

import static java.net.URI.create;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

@SpringBootApplication
public class UserServiceApplication extends WebMvcConfigurerAdapter {

  @Override
  public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
    ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
    threadPoolTaskExecutor.setCorePoolSize(2);
    threadPoolTaskExecutor.setMaxPoolSize(2);
    threadPoolTaskExecutor.afterPropertiesSet();

    configurer.setTaskExecutor(threadPoolTaskExecutor);
  }

  @Bean
  public ConnectionFactory connectionFactory() {
    ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://localhost");
    connectionFactory.setTrustedPackages(asList("org.mimacom.sample.integration.patterns.search.service.messages", "java.lang"));

    return connectionFactory;
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
  public JmsMessagingTemplate jmsMessagingTemplate(ConnectionFactory connectionFactory) {
    return new JmsMessagingTemplate(connectionFactory);
  }

  //  @Bean
  public SimpleUserController simpleUserController(SimpleSearchServiceIntegration simpleSearchServiceIntegration) {
    return new SimpleUserController(simpleSearchServiceIntegration);
  }

  //  @Bean
  public AsyncUserController asyncUserController(AsyncSearchServiceIntegration asyncSearchServiceIntegration) {
    return new AsyncUserController(asyncSearchServiceIntegration);
  }

  //  @Bean
  public BulkHeadedUserController bulkHeadedUserController(BulkHeadedSearchServiceIntegration bulkHeadedSearchServiceIntegration) {
    return new BulkHeadedUserController(bulkHeadedSearchServiceIntegration);
  }

  //  @Bean
  public HystrixUserController hystrixUserController(HystrixSearchServiceIntegration hystrixSearchServiceIntegration) {
    return new HystrixUserController(hystrixSearchServiceIntegration);
  }

  @Bean
  public JmsAndHystrixUserController jmsAndHystrixUserController(HystrixSearchServiceIntegration hystrixSearchServiceIntegration, JmsBasedIndexServiceIntegration jmsBasedIndexServiceIntegration) {
    return new JmsAndHystrixUserController(hystrixSearchServiceIntegration, jmsBasedIndexServiceIntegration);
  }

  public static void main(String[] args) {
    SpringApplication.run(UserServiceApplication.class, args);
  }

}
