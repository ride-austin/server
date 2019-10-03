package com.rideaustin.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "support_topic_forms")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportTopicForm {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", unique = true, nullable = false)
  private long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "topic_id")
  private SupportTopic parent;

  @Column(name = "body")
  @Type(type = "text")
  private String body;

  @Column(name = "field_content")
  @Type(type = "text")
  private String fieldContent;

  @Column(name = "title")
  private String title;

  @Column(name = "header_text")
  private String headerText;

  @Column(name = "action_title")
  private String actionTitle;

  @Column(name = "action_type")
  private String actionType;

}
