package com.han.pwac.pinguins.backend.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:application.properties")
// ^ Makes it so the application.properties decide how to connect to the database
public class DBConfig {

}
