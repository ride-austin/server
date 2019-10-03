package com.rideaustin.service;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

import javax.inject.Inject;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import com.rideaustin.model.City;
import com.rideaustin.model.user.Avatar;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.jpa.SessionRepository;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.service.email.EmailService;
import com.rideaustin.service.model.AvatarUpdateDto;
import com.rideaustin.service.user.UserIsActivatedEmail;
import com.rideaustin.service.user.UserIsDeactivatedEmail;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BaseAvatarService {

  private final SessionRepository sessionRepository;
  private final EmailService emailService;
  private final CityService cityService;

  public void updateAvatarByAdmin(AvatarUpdateDto current, AvatarUpdateDto newAvatar) throws ServerError {
    if (current.isActive() != newAvatar.isActive()) {
      try {
        if (newAvatar.isActive()) {
          emailService.sendEmail(new UserIsActivatedEmail(newAvatar.getEmail(), newAvatar.getFullName(), getCityByAvatar(newAvatar)));
        } else {
          emailService.sendEmail(new UserIsDeactivatedEmail(newAvatar.getEmail(), newAvatar.getFullName(), getCityByAvatar(newAvatar)));
        }
      } catch (Exception e) {
        throw new ServerError(e);
      }
    }
  }

  public void enrichAvatarWithLastLoginDate(Iterable<? extends Avatar> avatars) {
    if (avatars == null || !avatars.iterator().hasNext()) {
      return;
    }
    List<User> users = StreamSupport.stream(avatars.spliterator(), false).map(Avatar::getUser).collect(toList());
    Map<Long, Date> lastLoginDates = sessionRepository.findLastLoginDateByUsers(users)
      .stream()
      .collect(toMap(Pair::getKey, Pair::getValue));
    for (Avatar avatar : avatars) {
      avatar.setLastLoginDate(lastLoginDates.get(avatar.getUser().getId()));
    }
  }

  public void enrichAvatarWithLastLoginDate(Avatar avatar) {
    User user = avatar.getUser();
    avatar.setLastLoginDate(sessionRepository.findLastLoginDateByUser(user));
  }

  private City getCityByAvatar(AvatarUpdateDto avatar) {
    return cityService.getById(avatar.getCityId());
  }
}
