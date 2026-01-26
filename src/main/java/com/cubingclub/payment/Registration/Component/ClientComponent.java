package com.cubingclub.payment.Registration.Component;

import com.cubingclub.payment.Registration.DTO.CompetitionDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ClientComponent {
    @Value("${ranking.uri}")
    private String rankingUri;

    private final RestTemplate restTemplate;

    public ClientComponent(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    @Cacheable(value = "competition", key = "#competitionId")
    public CompetitionDTO getCompetitionDTO(Long competitionId){
        return restTemplate.getForObject(rankingUri + "/comp/{competitionId}", CompetitionDTO.class, competitionId);
    }
}
