package ma.weatherapp;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class WeatherAnalysisStream {




    public static class WeatherData {
        public String station;
        public double temperature;
        public double humidity;
    }

    // Represents the state of our aggregation
    public static class AggregationState {
        public long count = 0;
        public double tempSum = 0;
        public double humiditySum = 0;
    }

    // --- Main Application ---
    public static void main(String[] args) {
        // 1. Configure Kafka Streams
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "weather-analysis-app");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // Custom Serdes for our POJOs
        final Serde<WeatherData> weatherDataSerde = new JsonSerde<>(WeatherData.class);
        final Serde<AggregationState> aggregationStateSerde = new JsonSerde<>(AggregationState.class);

        // 2. Build the processing topology
        StreamsBuilder builder = new StreamsBuilder();
        buildTopology(builder, weatherDataSerde, aggregationStateSerde);
        Topology topology = builder.build();
        System.out.println(topology.describe());

        // 3. Create and start the Kafka Streams application
        final KafkaStreams streams = new KafkaStreams(topology, props);
        final CountDownLatch latch = new CountDownLatch(1);

        // 4. Add a shutdown hook for graceful shutdown [cite: 36]
        Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
            @Override
            public void run() {
                streams.close();
                latch.countDown();
            }
        });

        try {
            streams.start();
            latch.await();
        } catch (Throwable e) {
            System.exit(1);
        }
        System.exit(0);
    }

    private static void buildTopology(StreamsBuilder builder, Serde<WeatherData> weatherDataSerde, Serde<AggregationState> aggregationStateSerde) {
        // Step 1: Read from the 'weather-data' topic [cite: 12]
        KStream<String, String> sourceStream = builder.stream("weather-data");

        KTable<String, String> stationAveragesTable = sourceStream
                // --- Transformation Steps ---
                .flatMapValues(value -> {
                    // Parse the CSV-like string: "station,temperature,humidity"
                    try {
                        String[] parts = value.split(",");
                        if (parts.length != 3) return java.util.Collections.emptyList();

                        WeatherData data = new WeatherData();
                        data.station = parts[0].trim();
                        data.temperature = Double.parseDouble(parts[1].trim());
                        data.humidity = Double.parseDouble(parts[2].trim());
                        return java.util.Collections.singletonList(data);
                    } catch (Exception e) {
                        // Skip malformed records
                        return java.util.Collections.emptyList();
                    }
                })
                // Step 2: Filter for high temperatures [cite: 14]
                .filter((key, data) -> data.temperature > 30.0)
                // Step 3: Convert temperature to Fahrenheit [cite: 18, 20]
                .mapValues(data -> {
                    data.temperature = (data.temperature * 9 / 5) + 32;
                    return data;
                })
                // Step 4: Group by station [cite: 24]
                .groupBy((key, data) -> data.station, Grouped.with(Serdes.String(), weatherDataSerde))
                // Step 5: Calculate average temperature and humidity [cite: 26]
                .aggregate(
                        AggregationState::new, /* Initializer */
                        (stationKey, newData, aggState) -> { /* Aggregator */
                            aggState.count++;
                            aggState.tempSum += newData.temperature;
                            aggState.humiditySum += newData.humidity;
                            return aggState;
                        },
                        Materialized.with(Serdes.String(), aggregationStateSerde)
                )
                // Map the aggregated state to a final, readable string format
                .mapValues((readOnlyKey, aggState) -> {
                    if (aggState.count == 0) return "No data";
                    double avgTemp = aggState.tempSum / aggState.count;
                    double avgHumidity = aggState.humiditySum / aggState.count;
                    return String.format("Température Moyenne = %.2f°F, Humidité Moyenne = %.2f%%", avgTemp, avgHumidity);
                });

        // Step 6: Write the results to the 'station-averages' topic [cite: 32]
        stationAveragesTable.toStream().to("station-averages", Produced.with(Serdes.String(), Serdes.String()));
    }
}

// --- Helper class for JSON Serialization/Deserialization ---
class JsonSerde<T> implements Serde<T> {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Class<T> targetClass;

    public JsonSerde(Class<T> targetClass) {
        this.targetClass = targetClass;
    }

    @Override
    public Serializer<T> serializer() {
        return (topic, data) -> {
            try {
                if (data == null) return null;
                return objectMapper.writeValueAsBytes(data);
            } catch (IOException e) {
                throw new SerializationException("Error serializing JSON message", e);
            }
        };
    }

    @Override
    public Deserializer<T> deserializer() {
        return (topic, data) -> {
            try {
                if (data == null || data.length == 0) return null;
                return objectMapper.readValue(data, targetClass);
            } catch (IOException e) {
                throw new SerializationException("Error deserializing JSON message", e);
            }
        };
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {}

    @Override
    public void close() {}
}
