package io.jexxa.adapters.esp;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;

import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class KafkaUtilities {
    public static Properties kafkaProperties()
    {
        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        consumerProps.put("schema.registry.url", "http://127.0.0.1:8081");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return consumerProps;
    }

    public static boolean topicExist(String topic){
        try (AdminClient admin = AdminClient.create(Collections.singletonMap(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, getBootstrapServers()))) {
            return admin.listTopics().names().get().contains(topic ) ;
        } catch (ExecutionException e) {
            throw new IllegalArgumentException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalArgumentException(e);
        }
    }

    public static String getBootstrapServers() {
        return String.format("PLAINTEXT://%s:%s", "127.0.0.1", 9092);
    }

    public static void deleteTopics(){
        try (AdminClient admin = AdminClient.create(Collections.singletonMap(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, getBootstrapServers()))) {
            //Do not delete system topics starting with the leading '_'
            var topics = admin.listTopics().names().get().stream().filter( element -> !element.startsWith("_")).toList();
            admin.deleteTopics(topics);
        } catch (ExecutionException  e) {
            throw new IllegalArgumentException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalArgumentException(e);
        }
    }


}
