package edu.miu.cs.acs.integration;

import edu.miu.cs.acs.domain.ApiInfo;
import edu.miu.cs.acs.domain.HealthyApiInfo;
import edu.miu.cs.acs.domain.UnsureApiInfo;
import edu.miu.cs.acs.domain.controlflow.BusinessOrchestrator;
import edu.miu.cs.acs.models.ApiTestStatus;
import edu.miu.cs.acs.models.CheckedApiMessage;
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

/**
 * This class is used to define all the service activators that handle the output and input channels messages
 */
import java.util.Objects;

@Log4j2
@AllArgsConstructor
@Configuration
public class IntegrationFlows {

    private StreamBridge streamBridge;
    private IntegrationProperties integrationProperties;
    private BusinessOrchestrator businessOrchestrator;

    /**
     * Input channel handler
     * @param inputMessage
     * @return inputMessage
     */
    @ServiceActivator(inputChannel = Channels.INPUT_CHANNEL, outputChannel = Channels.ROUTING_CHANNEL)
    public Message<ApiInfo> processInput(Message<String> inputMessage) {
        log.info("Processing input message: {}", inputMessage);
        String url = inputMessage.getPayload();
        ApiInfo payload = new ApiInfo(url);

        Message<ApiInfo> outMessage = MessageBuilder
                .withPayload(payload)
                .copyHeaders(inputMessage.getHeaders())
                .build();

        ApiTestStatus testStatus = businessOrchestrator.testApi(url).getTestStatus();
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

    /**
     * handles APIs that are free and dont require a key
     * @param inputMessage
     * @return
     */
    @ServiceActivator(inputChannel = Channels.UNAUTHORIZED_API_CHANNEL, outputChannel = Channels.ROUTING_CHANNEL)
    public Message<ApiInfo> processUnauthorizedApi(Message<ApiInfo> inputMessage) {
        log.info("Processing unauthorized api message: {}", inputMessage);

        String url = inputMessage.getPayload().getUrl();
        CheckedApiMessage checkedApiMessage = businessOrchestrator.tryToExtractKey(url);
        ApiTestStatus testStatus = checkedApiMessage.getTestStatus();
        ServiceLine serviceLine;
        ApiInfo apiInfo;
        if (Objects.requireNonNull(testStatus) == ApiTestStatus.SUCCESSFUL_AUTHORIZED) {
            serviceLine = ServiceLine.SUCCESSFUL;
            apiInfo = HealthyApiInfo.builder()
                    .url(url)
                    .apiKey(checkedApiMessage.getApiKey())
                    .needsKey(true)
                    .build();
        } else {
            serviceLine = ServiceLine.FAILED;
            apiInfo = UnsureApiInfo.builder()
                    .url(url)
                    .extraInfo("unauthorized")
                    .build();
        }
        return MessageBuilder
                .withPayload(apiInfo)
                .copyHeaders(inputMessage.getHeaders())
                .setHeader(HeaderUtils.SERVICE_LINE, serviceLine.getValue())
                .build();
    }

    /**
     * Message Header based routing
     * @return
     */
    @Bean
    @ServiceActivator(inputChannel = Channels.ROUTING_CHANNEL)
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
    public void sendFailedApiMessage(Message<UnsureApiInfo> message) {
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
