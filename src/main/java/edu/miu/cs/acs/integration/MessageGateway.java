package edu.miu.cs.acs.integration;

import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;

@MessagingGateway
public interface MessageGateway {
    @Gateway(requestChannel = Channels.INPUT_CHANNEL)
    void sendToInputChannel(Object object);
}
