package no.digdir.meldingsutveksling.loggingproxy.config

import com.fasterxml.jackson.databind.JsonNode
import io.confluent.kafka.serializers.KafkaJsonDeserializer
import io.confluent.kafka.serializers.KafkaJsonSerializer
import no.digdir.meldingsutveksling.loggingproxy.StatusKey
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.LongDeserializer
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderOptions

@Configuration
class LoggingProxyConfig {
    @Autowired
    lateinit var properties: LoggingProxyProperties

    @Bean
    fun producerFactory(kafkaProperties: KafkaProperties): ProducerFactory<String, JsonNode> {
        return DefaultKafkaProducerFactory(kafkaProperties.buildProducerProperties())
    }

    @Bean
    fun kafkaTemplate(producerFactory: ProducerFactory<String, JsonNode>, props: LoggingProxyProperties): KafkaTemplate<String, JsonNode> {
        val kt = KafkaTemplate(producerFactory)
        kt.defaultTopic = props.statusTopic
        return kt
    }

    @Bean
    fun kafkaSender(kafkaProperties: KafkaProperties): KafkaSender<Nothing, JsonNode> {
        val senderOptions = SenderOptions.create<Nothing, JsonNode>(kafkaProperties.buildProducerProperties())
        return KafkaSender.create(senderOptions)
    }

    @Bean
    fun countListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<StatusKey, Long> {
        val props = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to properties.bootstrapServer,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to LongDeserializer::class.java,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
            ConsumerConfig.GROUP_ID_CONFIG to "count-consumer2"
        )
        val keyProps = hashMapOf<String, Any>("json.value.type" to StatusKey::class.java)
        val keySerializer = KafkaJsonSerializer<StatusKey>()
        keySerializer.configure(keyProps, false)
        val keyDeserializer = KafkaJsonDeserializer<StatusKey>()
        keyDeserializer.configure(keyProps, false)
        val keySerde = Serdes.serdeFrom(keySerializer, keyDeserializer)

        val factory = ConcurrentKafkaListenerContainerFactory<StatusKey, Long>()
        val consumerFactory = DefaultKafkaConsumerFactory<StatusKey, Long>(props)
        consumerFactory.setKeyDeserializer(keySerde.deserializer())
        factory.consumerFactory = consumerFactory
        return factory

    }

}