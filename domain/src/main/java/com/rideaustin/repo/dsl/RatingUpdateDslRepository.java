package com.rideaustin.repo.dsl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.rideaustin.model.user.QAvatar;
import com.rideaustin.model.user.QRatingUpdate;
import com.rideaustin.model.user.QUser;
import com.rideaustin.model.user.RatingUpdate;
import com.rideaustin.rest.model.QRatingUpdateDto;
import com.rideaustin.rest.model.RatingUpdateDto;

@Repository
public class RatingUpdateDslRepository extends AbstractDslRepository {

  private static QRatingUpdate qRatingUpdate = QRatingUpdate.ratingUpdate;

  public RatingUpdate findOne(long id) {
    return get(id, RatingUpdate.class);
  }

  public List<RatingUpdateDto> getRatingsFor(Long avatarId) {
    return queryFactory.from(qRatingUpdate)
      .join(qRatingUpdate.ratedAvatar.user, QUser.user)
      .join(qRatingUpdate.ratedByAvatar.user, QUser.user)
      .select(createDto())
      .where(qRatingUpdate.ratedAvatar.id.eq(avatarId))
      .fetch();
  }

  public List<RatingUpdateDto> getRatingsBy(Long avatarId) {
    return queryFactory.from(qRatingUpdate)
      .join(qRatingUpdate.ratedAvatar, QAvatar.avatar)
      .join(qRatingUpdate.ratedByAvatar, QAvatar.avatar)
      .select(createDto())
      .where(qRatingUpdate.ratedByAvatar.id.eq(avatarId))
      .fetch();
  }

  public RatingUpdateDto findInfo(long id) {
    return queryFactory.from(qRatingUpdate)
      .select(createDto())
      .where(qRatingUpdate.id.eq(id))
      .fetchOne();
  }

  private QRatingUpdateDto createDto() {
    return new QRatingUpdateDto(qRatingUpdate.id, qRatingUpdate.ride.id, qRatingUpdate.ratedAvatar.id, getFullName(qRatingUpdate.ratedAvatar.user),
      qRatingUpdate.ratedByAvatar.id, getFullName(qRatingUpdate.ratedByAvatar.user), qRatingUpdate.rating,
      qRatingUpdate.comment, qRatingUpdate.createdDate, qRatingUpdate.updatedDate);
  }

  public void delete(long id) {
    queryFactory.delete(qRatingUpdate)
      .where(qRatingUpdate.id.eq(id))
      .execute();
  }
}