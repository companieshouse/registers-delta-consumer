package uk.gov.companieshouse.registers.consumer.kafka;

import java.time.Duration;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

final class KafkaUtils {

    static final String MAIN_TOPIC = "registers-delta";
    static final String RETRY_TOPIC = "registers-delta-registers-delta-consumer-retry";
    static final String ERROR_TOPIC = "registers-delta-registers-delta-consumer-error";
    static final String INVALID_TOPIC = "registers-delta-registers-delta-consumer-invalid";

    private KafkaUtils() {
    }

    static int noOfRecordsForTopic(ConsumerRecords<?, ?> records, String topic) {
        int count = 0;
        for (ConsumerRecord<?, ?> ignored : records.records(topic)) {
            count++;
        }
        return count;
    }

    static Duration kafkaPollingDuration() {
        String kafkaPollingDuration = System.getenv().containsKey("KAFKA_POLLING_DURATION") ?
                System.getenv("KAFKA_POLLING_DURATION") : "1000";
        return Duration.ofMillis(Long.parseLong(kafkaPollingDuration));
    }
}
