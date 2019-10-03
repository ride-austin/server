package com.rideaustin.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.core.diff.Diff;
import org.javers.core.diff.changetype.ValueChange;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Sets;
import com.rideaustin.model.ChangeDto;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.DriverAudited;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.DriverAuditedDslRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DriverAuditedService {

  protected static final String SYSTEM_USERNAME = "SYSTEM";
  private final DriverAuditedDslRepository driverAuditedDslRepository;

  @Transactional
  public void saveIfChanged(DriverAudited driverAudited, User actor) {
    DriverAudited lastSavedDriverAudited = driverAuditedDslRepository.getLastDriverAudited(driverAudited.getId());
    saveIfChangesMade(driverAudited, lastSavedDriverAudited, actor);
  }

  public List<ChangeDto> getDriverChanges(Driver driver, Date auditDay) {
    Iterable<DriverAudited> driverChanges = driverAuditedDslRepository.findByDayAndDriverId(driver.getId(), auditDay);
    List<ChangeDto> changes = new ArrayList<>();
    DriverAudited previous = getPrevious(driver, driverChanges);
    for (DriverAudited driverAudited : driverChanges) {
      if (previous != null) {
        changes.addAll(getRelevantValueChangeStream(previous, driverAudited).map(valueChange ->
          getModelChangeDto(driver, driverAudited, valueChange)).collect(Collectors.toList()));
      }
      previous = driverAudited;
    }
    return changes;
  }

  private DriverAudited getPrevious(Driver driver, Iterable<DriverAudited> driverChanges) {
    long lastRevisionNr = driverChanges.iterator().hasNext() ? driverChanges.iterator().next().getRevision() : Long.MAX_VALUE;
    return driverAuditedDslRepository.findEarlierRevision(driver.getId(), lastRevisionNr);
  }

  private void saveIfChangesMade(DriverAudited driverAudited, DriverAudited lastSavedDriverAudited, User actor) {
    if (lastSavedDriverAudited == null || getRelevantValueChangeStream(lastSavedDriverAudited, driverAudited).count() > 0) {
      driverAudited.setUsername(Optional.ofNullable(actor).map(User::getUsername).orElse(SYSTEM_USERNAME));
      driverAuditedDslRepository.saveAny(driverAudited);
    }
  }

  private ChangeDto getModelChangeDto(Driver driver, DriverAudited driverAudited, ValueChange valueChange) {
    return ChangeDto.builder().entityId(driver.getId())
      .changedFieldName(valueChange.getPropertyName())
      .entityName(driver.getFullName())
      .newValue(Optional.ofNullable(valueChange.getRight()).map(Object::toString).orElse(null))
      .previousValue(Optional.ofNullable(valueChange.getLeft()).map(Object::toString).orElse(null))
      .revision(driverAudited.getRevision())
      .revisionDate(driverAudited.getRevisionDate())
      .changedBy(driverAudited.getUsername())
      .build();
  }

  private Stream<ValueChange> getRelevantValueChangeStream(DriverAudited previous, DriverAudited newer) {
    Diff diff = getDiff(previous, newer);
    return diff.getChangesByType(ValueChange.class)
      .stream()
      .filter(change -> !Sets.newHashSet("revision", "username", "revisionDate").contains(change.getPropertyName()));
  }

  private Diff getDiff(DriverAudited previous, DriverAudited newer) {
    Javers javers = JaversBuilder.javers().build();
    long newerRevisionTemp = newer.getRevision();
    newer.setRevision(previous.getRevision());
    Diff diff = javers.compare(previous, newer);
    newer.setRevision(newerRevisionTemp);
    log.debug("Driver Audit - Javers diff: " + diff);
    log.debug("Javers diff for stack: ", new Exception());
    return diff;
  }
}
