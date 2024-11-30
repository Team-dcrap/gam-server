package com.gam.api.config;

import com.slack.api.Slack;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class SlackConfig {

    @Value("${slack.url}")
    private String url;

    @Bean
    public Slack slack() {
        return Slack.getInstance();
    }
}
