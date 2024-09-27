package uk.gov.companieshouse.registers.consumer.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.retrytopic.RetryTopicHeaders;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.registers.consumer.exception.RetryableException;
import uk.gov.companieshouse.registers.consumer.service.DeltaServiceRouter;

@Component
public class Consumer {

    private final DeltaServiceRouter router;
    private final MessageFlags messageFlags;

    public Consumer(DeltaServiceRouter router, MessageFlags messageFlags) {
        this.router = router;
        this.messageFlags = messageFlags;
    }

    @KafkaListener(
            id = "${consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory",
            topics = {"${consumer.topic}"},
            groupId = "${consumer.group-id}"
    )
    public void consume(Message<ChsDelta> message,
            @Header(name = RetryTopicHeaders.DEFAULT_HEADER_ATTEMPTS, required = false) Integer attempt,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) Integer partition,
            @Header(KafkaHeaders.OFFSET) Long offset) {
        try {
            router.route(message.getPayload());
        } catch (RetryableException ex) {
            messageFlags.setRetryable(true);
            throw ex;
        }
    }
}
