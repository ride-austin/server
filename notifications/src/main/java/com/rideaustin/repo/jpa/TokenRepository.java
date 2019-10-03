package com.rideaustin.repo.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

import com.rideaustin.model.Token;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.user.User;

public interface TokenRepository extends JpaRepository<Token, Long>, QueryDslPredicateExecutor<Token> {

  @Query("select t from Token t where t.user = ?1 and t.avatarType = ?2 order by t.id desc")
  List<Token> findByUserAndAvatarType(User user, AvatarType avatarType);

}
