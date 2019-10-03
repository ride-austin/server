package com.rideaustin.events.listeners;

import javax.inject.Inject;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.events.LostAndFoundTrackEvent;
import com.rideaustin.model.LostAndFoundRequest;
import com.rideaustin.repo.dsl.LostAndFoundDslRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class LostAndFoundTrackListener {

  private final LostAndFoundDslRepository repository;

  @EventListener
  @Transactional
  public void handle(LostAndFoundTrackEvent event) {
    repository.saveAny(
      LostAndFoundRequest
        .builder()
        .type(event.getType())
        .requestedBy(event.getRequestedBy())
        .content(event.getContent())
        .build()
    );
  }
}
