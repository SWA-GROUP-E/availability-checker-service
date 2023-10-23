package edu.miu.cs.acs.integration;

import edu.miu.cs.acs.domain.ApiInfo;
import edu.miu.cs.acs.domain.UrlMessageProvider.controlflow.ApiControlFlow;
import edu.miu.cs.acs.domain.UrlMessageProvider.models.ApiTestStatus;
import edu.miu.cs.acs.domain.UrlMessageProvider.apicallservice.ApiTestService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.router.HeaderValueRouter;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

@Log4j2
@AllArgsConstructor
@Configuration
public class IntegrationFlows {

    private StreamBridge streamBridge;

    private IntegrationProperties integrationProperties;
    private ApiControlFlow controlFlow;

    @ServiceActivator(inputChannel = Channels.INPUT_CHANNEL, outputChannel = Channels.ROUTING_CHANNEL)
    public Message<ApiInfo> processInput(Message<String> inputMessage) {
        log.info("Processing input message: {}", inputMessage);
        String url = inputMessage.getPayload();
        Message<ApiInfo> outMessage = MessageBuilder
                .withPayload(ApiInfo
                        .builder()
                        .url(url)
                        .build())
                .copyHeaders(inputMessage.getHeaders())
                .build();
        ApiTestStatus testStatus = controlFlow.handle(url).getType();
        ServiceLine serviceLine;
        switch (testStatus) {
            case SUCCESSFUL -> serviceLine = ServiceLine.SUCCESSFUL;
            case UNAUTHORIZED -> serviceLine = ServiceLine.UNAUTHORIZED;
            default -> serviceLine = ServiceLine.FAILED;
        }
        return MessageBuilder
                .fromMessage(outMessage)
                .setHeader(HeaderUtils.SERVICE_LINE, serviceLine.getValue()).build();
    }

    @ServiceActivator(inputChannel = Channels.UNAUTHORIZED_API_CHANNEL, outputChannel = Channels.ROUTING_CHANNEL)
    public Message<ApiInfo> processUnauthorizedApi(Message<String> inputMessage) {
        log.info("Processing unauthorized api message: {}", inputMessage);

        // Put the API key search here
        String link = inputMessage.getPayload();

        long currentMills = System.currentTimeMillis();
        if (currentMills % 3 == 0) {
            return MessageBuilder
                    .withPayload(ApiInfo
                            .builder()
                            .url(link)
                            .build())
                    .copyHeaders(inputMessage.getHeaders())
                    .setHeader(HeaderUtils.SERVICE_LINE, ServiceLine.FAILED.getValue())
                    .build();
        }
        return MessageBuilder
                .withPayload(ApiInfo
                        .builder()
                        .url(link)
                        .apiKey(String.valueOf(currentMills))
                        .build())
                .copyHeaders(inputMessage.getHeaders())
                .setHeader(HeaderUtils.SERVICE_LINE, ServiceLine.SUCCESSFUL.getValue())
                .build();
    }

    @ServiceActivator(inputChannel = Channels.ROUTING_CHANNEL)
    @Bean
    public HeaderValueRouter router() {
        HeaderValueRouter router = new HeaderValueRouter(HeaderUtils.SERVICE_LINE);
        router.setChannelMapping(ServiceLine.SUCCESSFUL.getValue(), ServiceLine.SUCCESSFUL.getChannel());
        router.setChannelMapping(ServiceLine.FAILED.getValue(), ServiceLine.FAILED.getChannel());
        router.setChannelMapping(ServiceLine.UNAUTHORIZED.getValue(), ServiceLine.UNAUTHORIZED.getChannel());
        return router;
    }

    @ServiceActivator(inputChannel = Channels.SUCCESSFUL_API_CHANNEL)
    public void sendSuccessfulApiMessage(Message<ApiInfo> message) {
        streamBridge.send(integrationProperties.getSuccessDestination(), message);
        acknowledge(message);
        log.info("Sent message to {}. Message = {}", integrationProperties.getSuccessDestination(), message);
    }

    @ServiceActivator(inputChannel = Channels.FAILED_API_CHANNEL)
    public void sendFailedApiMessage(Message<ApiInfo> message) {
        streamBridge.send(integrationProperties.getFailedDestination(), message);
        acknowledge(message);
        log.info("Sent message to {}. Message = {}", integrationProperties.getFailedDestination(), message);
    }

    private void acknowledge(Message<?> message) {
        Acknowledgment acknowledgment = message.getHeaders().get(KafkaHeaders.ACKNOWLEDGMENT, Acknowledgment.class);
        if (acknowledgment != null) {
            acknowledgment.acknowledge();
            log.debug("Acknowledged message: {}", message);
        }
    }
}
