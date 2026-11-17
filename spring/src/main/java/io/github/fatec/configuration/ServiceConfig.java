package io.github.fatec.configuration;

import io.github.fatec.event.CampanhaPublicador;
import io.github.fatec.repository.CampanhaRepository;
import io.github.fatec.service.CampanhaService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Os services agora ficam no módulo domain, em Java puro (sem @Service).
// Esta classe diz ao Spring como criar cada um, entregando as implementações do módulo spring
// (CampanhaRepositoryImpl e CampanhaPublicadorImpl) para as interfaces que o service pede.
@Configuration
public class ServiceConfig {

    @Bean
    public CampanhaService campanhaService(CampanhaRepository repository, CampanhaPublicador publicador) {
        return new CampanhaService(repository, publicador);
    }
}
