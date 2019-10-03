package com.rideaustin.model;

import java.util.List;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.enums.FollowUpType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "support_topics")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportTopic {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", unique = true, nullable = false)
  private long id;

  @Column(name = "description", nullable = false)
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(name = "avatar_type")
  private AvatarType avatarType;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_topic_id")
  private SupportTopic parent;

  @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
  private List<SupportTopic> subTopics;

  @OneToOne(mappedBy = "parent", fetch = FetchType.EAGER)
  private SupportTopicForm form;

  @Column(name = "active")
  private boolean active;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(
    name = "support_topics_follow_up",
    joinColumns = @JoinColumn(name = "id", referencedColumnName = "id")
  )
  @Column(name = "follow_up_type")
  @Enumerated(EnumType.STRING)
  private Set<FollowUpType> followUpTypes;

}
