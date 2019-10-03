package com.rideaustin.repo.dsl;

import java.util.List;

import javax.annotation.Nonnull;

import org.springframework.stereotype.Repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.rideaustin.model.QSupportTopic;
import com.rideaustin.model.SupportTopic;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.rest.model.ListSupportTopicParams;

@Repository
public class SupportTopicDslRepository extends AbstractDslRepository {

  private static final QSupportTopic qSupportTopic = QSupportTopic.supportTopic;

  public SupportTopic findOne(Long id) {
    return this.buildQuery(qSupportTopic)
      .where(qSupportTopic.id.eq(id))
      .fetchOne();
  }

  public List<SupportTopic> findSupportTopics(@Nonnull ListSupportTopicParams params) {
    BooleanBuilder builder = new BooleanBuilder();
    params.fill(builder);
    JPAQuery<SupportTopic> query = buildQuery(qSupportTopic)
      .where(builder);
    return query.fetch();
  }

  public SupportTopic save(SupportTopic entity) {
    return super.saveAny(entity);
  }

  public List<SupportTopic> findTopLevelForAvatarType(AvatarType avatarType) {
    return this.buildQuery(qSupportTopic)
      .where(
        qSupportTopic.avatarType.eq(avatarType),
        qSupportTopic.parent.isNull(),
        qSupportTopic.active.isTrue()
      )
      .fetch();
  }

}