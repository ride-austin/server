package com.rideaustin.test.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.assertj.core.util.Lists;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;

public class DatabaseStateVerifier implements StateVerifier {
  private final List<String> TO_BE_VERIFIED = Lists.newArrayList("users", "riders", "drivers", "rides");

  private Map<String, Integer> initialState;

  private JdbcTemplate jdbcTemplate;

  @Inject
  public DatabaseStateVerifier(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public void initialState() {
    initialState = fetchState(jdbcTemplate);
  }

  @Override
  public void verify() {
    Map<String, Integer> finalState = fetchState(jdbcTemplate);
    checkStateDifference(initialState, finalState);
  }

  private Map<String, Integer> fetchState(JdbcTemplate jdbcTemplate) {
    Map<String, Integer> state = new HashMap<>();
    TO_BE_VERIFIED.forEach(table -> state.put(table, JdbcTestUtils.countRowsInTable(jdbcTemplate, table)));
    return state;
  }

  private void checkStateDifference(Map<String, Integer> initialState, Map<String, Integer> finalState) {
    MapDifference<String, Integer> difference = Maps.difference(initialState, finalState);
    Map<String, MapDifference.ValueDifference<Integer>> differenceMap = difference.entriesDiffering();
    if (!differenceMap.isEmpty()) {
      StringBuilder builder = new StringBuilder();
      differenceMap.forEach((tableName, diff) ->
        builder.append(String.format("%nFor %s: Count was %d, now %d", tableName, diff.leftValue(), diff.rightValue())));
      throw new RuntimeException("Database may be corrupted: " + builder.toString());
    }
  }
}
