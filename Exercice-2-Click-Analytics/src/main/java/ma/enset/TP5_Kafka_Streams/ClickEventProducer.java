package ma.enset.TP5_Kafka_Streams;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ClickEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public ClickEventProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }


    @PostMapping("/register-click")
    public void registerClick() {

        kafkaTemplate.send("clicks", "user1", "click");
        System.out.println("Sent click event for user1");
    }
}