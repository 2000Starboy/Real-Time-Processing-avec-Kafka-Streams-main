package ma.enset.TP5_Kafka_Streams;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class ClickCountApi {

    private final AtomicLong clickCount = new AtomicLong(0);

    @KafkaListener(topics = "click-counts", groupId = "click-count-consumer-group")
    public void consumeClickCount(Long count) {
        if (count != null) {
            clickCount.set(count);
            System.out.println("Received updated click count: " + count);
        }
    }

    @GetMapping("/clicks/count")
    public String getClickCount() {
        return "Total Clicks: " + clickCount.get();
    }
}