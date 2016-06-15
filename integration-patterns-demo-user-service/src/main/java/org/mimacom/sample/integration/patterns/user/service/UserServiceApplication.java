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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
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
public class UserServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(UserServiceApplication.class, args);
  }


  @Profile("simple")
  @Configuration
  static class SimpleUserServiceConfiguration {

    @Bean
    public SimpleSearchServiceIntegration simpleSearchServiceIntegration(@Value("${search-service-url}") String searchServiceUrl) {
      return new SimpleSearchServiceIntegration(searchServiceUrl);
    }

    @Bean
    public SimpleUserController simpleUserController(SimpleSearchServiceIntegration simpleSearchServiceIntegration) {
      return new SimpleUserController(simpleSearchServiceIntegration);
    }

  }


  @Profile("async")
  @Configuration
  static class AsyncUserServiceConfiguration extends WebMvcConfigurerAdapter {

    @Bean
    public AsyncSearchServiceIntegration asyncSearchServiceIntegration(@Value("${search-service-url}") String searchServiceUrl) {
      return new AsyncSearchServiceIntegration(searchServiceUrl);
    }

    @Bean
    public AsyncUserController asyncUserController(AsyncSearchServiceIntegration asyncSearchServiceIntegration) {
      return new AsyncUserController(asyncSearchServiceIntegration);
    }

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
      ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
      threadPoolTaskExecutor.setCorePoolSize(2);
      threadPoolTaskExecutor.setMaxPoolSize(2);
      threadPoolTaskExecutor.afterPropertiesSet();

      configurer.setTaskExecutor(threadPoolTaskExecutor);
    }

  }


  @Profile("bulkhead")
  @Configuration
  static class BulkHeadUserServiceConfiguration {

    @Bean
    public BulkHeadedSearchServiceIntegration bulkHeadedSearchServiceIntegration(@Value("${search-service-url}") String searchServiceUrl) {
      return new BulkHeadedSearchServiceIntegration(searchServiceUrl);
    }

    @Bean
    public BulkHeadedUserController bulkHeadedUserController(BulkHeadedSearchServiceIntegration bulkHeadedSearchServiceIntegration) {
      return new BulkHeadedUserController(bulkHeadedSearchServiceIntegration);
    }

  }


  @Profile("hystrix")
  @Configuration
  static class HystrixUserServiceConfiguration {

    @Bean
    public HystrixSearchServiceIntegration hystrixSearchServiceIntegration(@Value("${search-service-url}") String searchServiceUrl) {
      return new HystrixSearchServiceIntegration(searchServiceUrl);
    }

    @Bean
    public HystrixUserController hystrixUserController(HystrixSearchServiceIntegration hystrixSearchServiceIntegration) {
      return new HystrixUserController(hystrixSearchServiceIntegration);
    }

  }


  @Profile("jms")
  @Configuration
  static class JmsUserServiceConfiguration {

    @Bean
    public JmsAndHystrixUserController jmsAndHystrixUserController(HystrixSearchServiceIntegration hystrixSearchServiceIntegration, JmsBasedIndexServiceIntegration jmsBasedIndexServiceIntegration) {
      return new JmsAndHystrixUserController(hystrixSearchServiceIntegration, jmsBasedIndexServiceIntegration);
    }

    @Bean
    public HystrixSearchServiceIntegration hystrixSearchServiceIntegration(@Value("${search-service-url}") String searchServiceUrl) {
      return new HystrixSearchServiceIntegration(searchServiceUrl);
    }

    @Bean
    public JmsBasedIndexServiceIntegration jmsBasedIndexServiceIntegration(JmsMessagingTemplate jmsMessagingTemplate) {
      return new JmsBasedIndexServiceIntegration(jmsMessagingTemplate);
    }

    @Bean
    public JmsMessagingTemplate jmsMessagingTemplate(ConnectionFactory connectionFactory) {
      return new JmsMessagingTemplate(connectionFactory);
    }

    @Bean
    public ConnectionFactory connectionFactory() {
      ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://localhost");
      connectionFactory.setTrustedPackages(asList("org.mimacom.sample.integration.patterns.search.service.messages", "java.lang"));

      return connectionFactory;
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
