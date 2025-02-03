package com.moh4lych.springdi.listeners;

import com.moh4lych.springdi.events.BeerCreatedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

public class BeerCreatedListener {

    @Async
    @EventListener
    public void handelCreatedEvent(BeerCreatedEvent beerCreatedEvent) {
        System.out.println("beer = " + beerCreatedEvent.getBeer().getId());
    }
}
