package com.moh4lych.springdi.events;

import com.moh4lych.springdi.entities.Beer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.Authentication;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class BeerCreatedEvent {
    private Beer beer;

    private Authentication authentication;
}
