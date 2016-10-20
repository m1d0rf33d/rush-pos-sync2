package com.rush.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

import javax.sql.DataSource;

/**
 * Created by aomine on 10/20/16.
 */
@Configuration
@EnableResourceServer
public class OAuth2ResourceServerConfig extends ResourceServerConfigurerAdapter {


    @Override
    public void configure(final HttpSecurity http) throws Exception {
        http
                .requestMatchers().antMatchers("/api/**")
                .and()
                .authorizeRequests()
                .antMatchers("/api/**").access("#oauth2.hasScope('trust')");
    }
    // JDBC token store configuration
      @Bean
      public DataSource dataSource() {
          final DriverManagerDataSource dataSource = new DriverManagerDataSource();
          dataSource.setDriverClassName("com.mysql.jdbc.Driver");
          dataSource.setUrl("jdbc:mysql://localhost:3306/rush_pos_sync");
          dataSource.setUsername("root");
          dataSource.setPassword("root");
          return dataSource;
      }

      @Bean public TokenStore tokenStore() {
          return new JdbcTokenStore(dataSource()); }
}