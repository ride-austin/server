package com.rideaustin.model;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import com.rideaustin.model.enums.LostAndFoundRequestType;
import com.rideaustin.model.helper.CommentConverter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "lost_and_found_requests")
public class LostAndFoundRequest extends BaseEntity {

  @Column(name = "type")
  @Enumerated(EnumType.STRING)
  private LostAndFoundRequestType type;

  @Column(name = "requested_by")
  private Long requestedBy;

  @Column(name = "content")
  @Convert(converter = CommentConverter.class)
  private String content;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

}
