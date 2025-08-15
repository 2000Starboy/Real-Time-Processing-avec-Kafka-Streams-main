package ma.enset.TP5_Kafka_Streams;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@Configuration
@EnableKafkaStreams
public class ClickAnalyticsStream {

    @Bean
    public KStream<String, Long> kStream(StreamsBuilder streamsBuilder) {
        KStream<String, String> clickStream = streamsBuilder
                .stream("clicks", Consumed.with(Serdes.String(), Serdes.String()));

        KStream<String, Long> countStream = clickStream
                .groupByKey(Grouped.with(Serdes.String(), Serdes.String()))
                .count() // Count occurrences for each key
                .toStream();

        countStream.to("click-counts", Produced.with(Serdes.String(), Serdes.Long()));

        System.out.println("Kafka Streams processor for click analytics started.");
        return countStream;
    }
}