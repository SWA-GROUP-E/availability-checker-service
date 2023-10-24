package edu.miu.cs.acs.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.miu.cs.acs.domain.ApiInfo;
import edu.miu.cs.acs.domain.HealthyApiInfo;
import edu.miu.cs.acs.domain.UnsureApiInfo;
import edu.miu.cs.acs.domain.controlflow.ApiControlFlow;
import edu.miu.cs.acs.models.ApiTestStatus;
import edu.miu.cs.acs.models.FailedApiMessage;
import edu.miu.cs.acs.models.SuccessfulApiMessage;
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
        ApiInfo payload = new ApiInfo(url);

        Message<ApiInfo> outMessage = MessageBuilder
                .withPayload(payload)
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
    public Message<HealthyApiInfo> processUnauthorizedApi(Message<ApiInfo> inputMessage) throws JsonProcessingException {
        log.info("Processing unauthorized api message: {}", inputMessage);

        HealthyApiInfo payload = new HealthyApiInfo(
                inputMessage.getPayload().getUrl(),
                false,
                null
        );

        return MessageBuilder
                .withPayload(payload)
                .copyHeaders(inputMessage.getHeaders())
                .setHeader(HeaderUtils.SERVICE_LINE, ServiceLine.SUCCESSFUL.getValue())
                .build();
    }

    @ServiceActivator(inputChannel = Channels.SUCCESSFUL_API_CHANNEL, outputChannel = Channels.ROUTING_CHANNEL)
    public Message<HealthyApiInfo> processSuccessfulApi(Message<ApiInfo> inputMessage) throws JsonProcessingException {
        log.info("Processing successful api message: {}", inputMessage);

        String urlFromInputMessage = inputMessage.getPayload().getUrl();
        SuccessfulApiMessage apiMessage = (SuccessfulApiMessage) controlFlow.handle(urlFromInputMessage);

        HealthyApiInfo payload = new HealthyApiInfo(
                urlFromInputMessage,
                true,
                apiMessage.getApiKey()
        );

        return MessageBuilder
                .withPayload(payload)
                .copyHeaders(inputMessage.getHeaders())
                .setHeader(HeaderUtils.SERVICE_LINE, ServiceLine.SUCCESSFUL.getValue())
                .build();
    }

    @ServiceActivator(inputChannel = Channels.FAILED_API_CHANNEL, outputChannel = Channels.ROUTING_CHANNEL)
    public Message<UnsureApiInfo> processFailedApi(Message<ApiInfo> inputMessage) throws JsonProcessingException {
        log.info("Processing failed api message: {}", inputMessage);

        String inputMessageUrl = inputMessage.getPayload().getUrl();
        UnsureApiInfo payload = new UnsureApiInfo(
                inputMessageUrl,
                null
        );

        return MessageBuilder
                .withPayload(payload)
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
    public void sendSuccessfulApiMessage(Message<HealthyApiInfo> message) {
        streamBridge.send(integrationProperties.getSuccessDestination(), message);
        acknowledge(message);
        log.info("Sent message to {}. Message = {}", integrationProperties.getSuccessDestination(), message);
    }

    @ServiceActivator(inputChannel = Channels.FAILED_API_CHANNEL)
    public void sendFailedApiMessage(Message<FailedApiMessage> message) {
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
