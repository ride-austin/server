package com.rideaustin.repo.dsl;

import static java.util.stream.Collectors.toList;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.DateTimeExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.rideaustin.model.promocodes.QPromocodeRedemption;
import com.rideaustin.report.entry.PromocodeRedemptionsCountByDateReportEntry;
import com.rideaustin.report.entry.PromocodeRedemptionsCountByHourReportEntry;
import com.rideaustin.report.enums.GroupByTimePeriod;

@Repository
public class PromocodeRedemptionReportDslRepository extends AbstractDslRepository {

  private static final QPromocodeRedemption qRedemption = QPromocodeRedemption.promocodeRedemption;
  private static final String DATE_EXPRESSION = "date(convert_tz({0}, 'UTC', 'US/Central'))";
  private static final String HOUR_EXPRESSION = "hour(convert_tz({0}, 'UTC', 'US/Central'))";

  public List<PromocodeRedemptionsCountByDateReportEntry> groupRedemptionsByTimePeriod(Instant startDate, Instant endDate,
    String codeLiteral, GroupByTimePeriod groupingPeriod) {
    Date start = Date.from(startDate);
    Date end = Date.from(endDate);

    ImmutableList.Builder<Expression> fieldsBuilder = ImmutableList.<Expression>builder()
      .add(createDateExpression(), qRedemption.id.count());
    if (groupingPeriod == GroupByTimePeriod.HOUR) {
      fieldsBuilder.add(createHourExpression());
    }

    return queryFactory.select(fieldsBuilder.build().toArray(new Expression[0]))
      .from(qRedemption)
      .where(
        qRedemption.promocode.codeLiteral.equalsIgnoreCase(codeLiteral)
          .and(qRedemption.createdDate.between(start, end))
      )
      .groupBy((Expression<?>[]) createGroupBy(groupingPeriod))
      .orderBy(createOrderBy(groupingPeriod))
      .fetch()
      .stream()
      .map(
        groupingPeriod == GroupByTimePeriod.HOUR ?
          PromocodeRedemptionsCountByHourReportEntry::new : PromocodeRedemptionsCountByDateReportEntry::new
      )
      .collect(toList());

  }

  private OrderSpecifier<?>[] createOrderBy(GroupByTimePeriod groupingPeriod) {
    return Arrays.stream(createGroupBy(groupingPeriod))
      .map(ComparableExpressionBase::asc)
      .toArray(OrderSpecifier[]::new);
  }

  private ComparableExpressionBase[] createGroupBy(GroupByTimePeriod period) {
    List<ComparableExpressionBase> expressions = Lists.newArrayList(createDateExpression());
    if (period == GroupByTimePeriod.HOUR) {
      expressions.add(createHourExpression());
    }
    return expressions.toArray(new ComparableExpressionBase[0]);
  }

  private DateTimeExpression<Date> createDateExpression() {
    return Expressions.dateTimeTemplate(Date.class, DATE_EXPRESSION, qRedemption.createdDate);
  }

  private NumberExpression<Integer> createHourExpression() {
    return Expressions.numberTemplate(Integer.class, HOUR_EXPRESSION, qRedemption.createdDate);
  }
}
