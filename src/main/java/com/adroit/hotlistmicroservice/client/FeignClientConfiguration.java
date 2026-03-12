package com.adroit.hotlistmicroservice.client;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;

@Configuration
public class FeignClientConfiguration {


   /* @Bean
    public RequestInterceptor feignRequestInterceptor(){
      return new RequestInterceptor() {
          @Override
          public void apply(RequestTemplate requestTemplate) {
              String token= extractToken();
              if(token!=null && !token.isEmpty()){
                    requestTemplate.header("Authorization","Bearer "+token);
              }
          }
          private String extractToken(){
              var context= SecurityContextHolder.getContext();
              if (context.getAuthentication() != null && context.getAuthentication().getCredentials() != null) {
                  return context.getAuthentication().getCredentials().toString();
              }
              return null;
          }
      };

    }  */

    @Bean
    public ErrorDecoder errorDecoder(){
        return new CustomErrorDecoder();
    }
}
