package com.rideaustin.rest.model;

import java.util.Objects;

import com.querydsl.core.BooleanBuilder;
import com.rideaustin.model.user.QRider;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class ListRidersParams extends ListAvatarsParams {

  @ApiModelProperty(value = "Rider ID", example = "1")
  private Long riderId;
  @ApiModelProperty(value = "City ID", example = "1")
  private Long cityId;

  public void fill(BooleanBuilder builder) {
    QRider qRider = QRider.rider;
    fill(builder, qRider.user);
    if (riderId != null) {
      builder.and(qRider.id.eq(riderId));
    }

    if (getActive() != null) {
      builder.and(qRider.active.eq(getActive()));
    }
    if (cityId != null) {
      builder.and(qRider.cityId.eq(cityId));
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    ListRidersParams that = (ListRidersParams) o;

    return Objects.equals(riderId, that.riderId);

  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (riderId != null ? riderId.hashCode() : 0);
    return result;
  }
}
