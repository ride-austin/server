package com.rideaustin.repo.dsl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.SimplePath;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.rideaustin.model.BaseEntity;
import com.rideaustin.model.user.QUser;
import com.rideaustin.rest.model.PagingParams;

@Transactional
public class AbstractDslRepository {

  protected EntityManager entityManager;

  protected JPAQueryFactory queryFactory;

  @Value("${hibernate.jdbc.batch_size}")
  private int batchSize;

  @Inject
  public void setEntityManager(EntityManager entityManager) {
    this.entityManager = entityManager;
    this.queryFactory = new JPAQueryFactory(entityManager);
  }

  protected <T> JPAQuery<T> buildQuery(EntityPathBase<T> entityPath) {
    return queryFactory.selectFrom(entityPath);
  }

  protected <T> T get(Long id, Class<T> clazz) {
    return entityManager.find(clazz, id);
  }

  public <T extends BaseEntity> T save(T entity) {
    T saved = persistOrMerge(entity);
    entityManager.flush();
    return saved;
  }

  public <T> T saveAny(T entity) {
    T saved = entityManager.merge(entity);
    entityManager.flush();
    return saved;
  }

  @Transactional
  @Retryable(value = {Exception.class}, maxAttempts = 5, backoff = @Backoff(delay = 1000))
  public <T extends BaseEntity> Collection<T> saveMany(List<T> entityList) {
    List<T> entities = new ArrayList<>(entityList.size());
    if (!entityList.isEmpty()) {
      for (T entity : entityList) {
        entities.add(persistOrMerge(entity));
        if (entities.size() % batchSize == 0) {
          entityManager.flush();
          entityManager.clear();
        }
      }
      entityManager.flush();
    }
    return entities;
  }

  @Transactional
  @Retryable(value = {Exception.class}, maxAttempts = 5, backoff = @Backoff(delay = 1000))
  public <T> void saveAnyMany(List<T> entityList) {
    List<T> entities = new ArrayList<>(entityList.size());
    if (!entityList.isEmpty()) {
      for (T entity : entityList) {
        entities.add(entityManager.merge(entity));
        if (entities.size() % batchSize == 0) {
          entityManager.flush();
          entityManager.clear();
        }
      }
      entityManager.flush();
    }
  }

  private <T extends BaseEntity> T persistOrMerge(T entity) {
    T savedEntity;
    if (entity.getId() == 0) {
      entityManager.persist(entity);
      savedEntity = entity;
    } else {
      savedEntity = entityManager.merge(entity);
    }
    return savedEntity;
  }

  public <T extends BaseEntity> void delete(T entity) {
    entityManager.remove(entity);
  }

  protected void addOffsetParams(@Nonnull JPAQuery jpaQuery, @Nonnull PagingParams paging) {
    jpaQuery.offset((long) (paging.getPage() * paging.getPageSize())).limit(paging.getPageSize());
  }

  /**
   * Append paging parameters when sorting occurs on {path} fields.
   *
   * @param query
   * @param paging
   * @param path
   * @return
   */
  protected <T> JPAQuery<T> appendPagingParams(@Nonnull JPAQuery<T> query, @Nonnull PagingParams paging, EntityPathBase<?> path) {
    addOffsetParams(query, paging);
    for (String sortField : paging.getSort()) {
      doAppendPagingParams(query, paging, path, sortField);
    }
    return query;
  }

  protected <T> JPAQuery<T> appendPagingParams(@Nonnull JPAQuery<T> query, @Nonnull PagingParams paging, EntityPathBase<?> path, String countField) {
    addOffsetParams(query, paging);
    for (String sortField : paging.getSort()) {
      if (sortField.equals(countField)) {
        NumberPath<Long> aliasQuantity = Expressions.numberPath(Long.class, countField);
        query.orderBy(paging.isDesc() ? aliasQuantity.desc() : aliasQuantity.asc());
      } else {
        doAppendPagingParams(query, paging, path, sortField);
      }
    }
    return query;
  }

  private <T> void doAppendPagingParams(@Nonnull JPAQuery<T> query, @Nonnull PagingParams paging, EntityPathBase<?> path, String sortField) {
    SimplePath<T> sortPath = (SimplePath<T>) Expressions.path(Object.class, path, sortField);
    query.orderBy(paging.isDesc() ? new OrderSpecifier(Order.DESC, sortPath) : new OrderSpecifier(Order.ASC, sortPath));
  }

  protected <T> Page<T> getPage(@Nonnull PagingParams paging, BooleanBuilder builder, EntityPathBase<T> path) {
    JPAQuery<T> query = buildQuery(path);
    query.where(builder);

    long total = query.fetchCount();
    List<T> content = appendPagingParams(query, paging, path).fetch();

    return new PageImpl<>(content, paging.toPageRequest(), total);
  }

  protected <T> Page<T> getPage(PagingParams paging, List<T> content, long total) {
    return new PageImpl<>(content, paging.toPageRequest(), total);
  }

  protected static BooleanExpression bitmaskPredicate(NumberPath<Integer> path, Integer bitmask) {
    return path.mod(bitmask * 2).divide(bitmask).floor().eq(1);
  }

  protected static StringExpression getFullName(final QUser user) {
    return user.firstname.concat(" ").concat(user.lastname);
  }

  protected static Expression<String> getFullName(final QUser qRiderUser, final StringPath overriddenFirstName,
    final StringPath overriddenLastName) {
    return overriddenFirstName.coalesce(qRiderUser.firstname).asString()
      .concat(" ")
      .concat(overriddenLastName.coalesce(qRiderUser.lastname));
  }

}
