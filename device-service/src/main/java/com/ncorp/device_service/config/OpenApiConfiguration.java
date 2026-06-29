package com.ncorp.device_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {
    @Bean
    public OpenAPI deviceServiceApiDocs(){
        return new OpenAPI()
                .info(new Info()
                        .title("Device Service Api")
                        .description("Device Service API for Home Energy Tracker")
                        .license(getLicense())
                        .contact(getContact())
                        .version("1.0.0"));

    }

    private static Contact getContact(){
        Contact contact = new Contact();
        contact.setUrl("https://github.com/NamanSharma1201");
        contact.setEmail("naman.sharma.122003@gmail.com");
        return contact;
    }


    private static License getLicense(){
        License license = new License();
        license.setName("Creative Common Attribution-NonCommercial 4.0 International License");
        return license;
    }
}
