package com.rideaustin.model.ride;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import com.rideaustin.model.BaseEntity;
import com.rideaustin.model.enums.CancellationReason;
import com.rideaustin.model.helper.CommentConverter;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "ride_cancellation_feedback")
public class RideCancellationFeedback extends BaseEntity {

  @Column(name = "ride_id", nullable = false)
  private long rideId;

  @Column(name = "submitted_by", nullable = false)
  private long submittedBy;

  @Column(name = "reason", nullable = false)
  @Enumerated(EnumType.STRING)
  private CancellationReason reason;

  @Column(name = "comment")
  @Convert(converter = CommentConverter.class)
  private String comment;

  public RideCancellationFeedback(long rideId, long submittedBy, CancellationReason reason, String comment) {
    this.rideId = rideId;
    this.submittedBy = submittedBy;
    this.reason = reason;
    this.comment = comment;
  }

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
