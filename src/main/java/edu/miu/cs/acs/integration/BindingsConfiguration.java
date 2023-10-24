package edu.miu.cs.acs.integration;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

@Log4j2
@AllArgsConstructor
@Configuration
public class BindingsConfiguration {

    private MessageGateway messageGateway;

    @Bean
    public Consumer<Message<String>> inputDataConsumer() {
        return inMessage -> {
            log.info("Received input message: {}", inMessage);
            messageGateway.sendToInputChannel(inMessage);
        };
    }
}
