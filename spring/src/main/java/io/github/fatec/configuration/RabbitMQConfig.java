package io.github.fatec.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

// Filas da Campanha com retry
// A volta usa a exchange campanha.retry (direct) e não a vaquinha.eventos: assim a nova tentativa
// chega SÓ nesta fila, e não em todas as filas que escutam doacao.realizada.
@Configuration
public class RabbitMQConfig {

    @Value("${event.exchange}")
    private String exchange;

    @Value("${event.retry.exchange}")
    private String retryExchangeNome;

    @Value("${event.retry.delay}")
    private Duration retryDelay;

    @Value("${event.doacao-realizada.queue}")
    private String queue;

    @Value("${event.doacao-realizada.dead-letter}")
    private String deadLetter;

    @Value("${event.doacao-realizada.parking-lot}")
    private String parkingLot;

    @Value("${event.doacao-realizada.routing-key}")
    private String routingKey;

    @Bean
    public TopicExchange eventosExchange() {
        return new TopicExchange(exchange, true, false);
    }

    @Bean
    public DirectExchange retryExchange() {
        return new DirectExchange(retryExchangeNome);
    }

    @Bean
    public MessageConverter messageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }

    // ---------- Filas

    @Bean
    public Queue doacaoRealizadaQueue() {
        return QueueBuilder.durable(queue)
                .deadLetterExchange(retryExchangeNome)
                .deadLetterRoutingKey(deadLetter)
                .build();
    }

    @Bean
    public Queue doacaoRealizadaDeadLetter() {
        return QueueBuilder.durable(deadLetter)
                .ttl((int) retryDelay.toMillis())
                .deadLetterExchange(retryExchangeNome)
                .deadLetterRoutingKey(queue)
                .build();
    }

    @Bean
    public Queue doacaoRealizadaParkingLot() {
        return QueueBuilder.durable(parkingLot).build();
    }

    // ---------- Ligações

    // Mensagem nova: vem da Doação pela vaquinha.eventos
    @Bean
    public Binding doacaoRealizadaBinding() {
        return BindingBuilder.bind(doacaoRealizadaQueue()).to(eventosExchange()).with(routingKey);
    }

    // Nova tentativa: volta da dead-letter pela campanha.retry
    @Bean
    public Binding doacaoRealizadaRetryBinding() {
        return BindingBuilder.bind(doacaoRealizadaQueue()).to(retryExchange()).with(queue);
    }

    @Bean
    public Binding doacaoRealizadaDeadLetterBinding() {
        return BindingBuilder.bind(doacaoRealizadaDeadLetter()).to(retryExchange()).with(deadLetter);
    }

    @Bean
    public Binding doacaoRealizadaParkingLotBinding() {
        return BindingBuilder.bind(doacaoRealizadaParkingLot()).to(retryExchange()).with(parkingLot);
    }
}