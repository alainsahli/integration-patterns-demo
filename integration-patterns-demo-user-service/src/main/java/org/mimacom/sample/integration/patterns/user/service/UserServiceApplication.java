package org.mimacom.sample.integration.patterns.user.service;

import org.mimacom.sample.integration.patterns.user.service.integration.AsyncSearchServiceIntegration;
import org.mimacom.sample.integration.patterns.user.service.integration.SimpleSearchServiceIntegration;
import org.mimacom.sample.integration.patterns.user.service.web.AsyncUserController;
import org.mimacom.sample.integration.patterns.user.service.web.SimpleUserController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@SpringBootApplication
public class UserServiceApplication extends WebMvcConfigurerAdapter {

  public static void main(String[] args) {
    SpringApplication.run(UserServiceApplication.class, args);
  }

  @Override
  public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
    ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
    threadPoolTaskExecutor.setCorePoolSize(2);
    threadPoolTaskExecutor.setMaxPoolSize(2);
    threadPoolTaskExecutor.afterPropertiesSet();

    configurer.setTaskExecutor(threadPoolTaskExecutor);
  }

  @Bean
  public SimpleUserController simpleUserController(SimpleSearchServiceIntegration simpleSearchServiceIntegration) {
    return new SimpleUserController(simpleSearchServiceIntegration);
  }

//  @Bean
  public AsyncUserController asyncUserController(AsyncSearchServiceIntegration asyncSearchServiceIntegration) {
    return new AsyncUserController(asyncSearchServiceIntegration);
  }

}
