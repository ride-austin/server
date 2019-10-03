package com.rideaustin.report;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.reflections.Reflections;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;

import com.rideaustin.report.model.ReportComponent;
import com.rideaustin.report.model.ReportMetadata;
import com.rideaustin.report.model.ReportParameter;
import com.rideaustin.report.params.ReportParameterProvider;
import com.rideaustin.rest.model.ListReportParams;
import com.rideaustin.rest.model.PagingParams;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReportsMetadataProvider implements ApplicationListener<ContextRefreshedEvent> {

  private final Set<ReportMetadata> metadata = new HashSet<>();
  private final BeanFactory beanFactory;

  public void onApplicationEvent(ContextRefreshedEvent event) {
    Set<Class<?>> reports = new Reflections(this.getClass().getPackage().getName()).getTypesAnnotatedWith(ReportComponent.class);
    for (Class<?> report : reports) {
      ReportComponent annotation = report.getAnnotation(ReportComponent.class);
      metadata.add(createMetadata(report, annotation));
    }
  }

  public Page<ReportMetadata> findReports(ListReportParams searchCriteria, PagingParams paging) {
    Comparator<ReportMetadata> comparator = Comparator.comparing(ReportMetadata::getId);
    if (paging.isDesc()) {
      comparator = comparator.reversed();
    }
    Predicate<ReportMetadata> searchPredicate = m -> searchCriteria.getReportName() == null || m.getReportName().equals(searchCriteria.getReportName());
    long count = metadata
      .stream()
      .filter(searchPredicate)
      .count();
    List<ReportMetadata> result = metadata
      .stream()
      .filter(searchPredicate)
      .limit(paging.getPageSize())
      .skip((long) paging.getPage() * paging.getPageSize())
      .sorted(comparator)
      .collect(Collectors.toList());
    return new PageImpl<>(result, paging.toPageRequest(), count);
  }

  public Optional<ReportMetadata> findOne(long id) {
    return metadata.stream().filter(m -> m.getId() == id).findAny();
  }

  public Optional<ReportMetadata> findByClass(Class clazz) {
    return metadata.stream().filter(m -> m.getReportClass().equals(clazz)).findAny();
  }

  public List<ReportParameter> listParameters(long reportId) {
    return metadata
      .stream()
      .filter(m -> m.getId() == reportId)
      .findFirst()
      .map(ReportMetadata::getParameters)
      .orElse(Collections.emptySet())
      .stream()
      .filter(p -> !p.isInternal())
      .sorted(Comparator.comparingInt(ReportParameter::getOrder))
      .collect(Collectors.toList());
  }

  private ReportMetadata createMetadata(Class<?> report, ReportComponent annotation) {
    return new ReportMetadata(annotation.id(), annotation.name(), annotation.description(), annotation.format(),
      annotation.header(), createParams(annotation.parameters(), annotation.parametersProvider()), annotation.archive(), annotation.upload(),
      (Class<? extends Report>) report);
  }

  private Set<ReportParameter> createParams(ReportComponent.Param[] parameters, Class<? extends ReportParameterProvider> parameterProviderClass) {
    Set<ReportParameter> params = new HashSet<>();
    if (ReportComponent.NullProvider.class.equals(parameterProviderClass)) {
      for (ReportComponent.Param parameter : parameters) {
        params.add(new ReportParameter(parameter.label(), parameter.name(), parameter.description(), parameter.type(),
          parameter.required(), parameter.internal(), parameter.defaultValue(), parameter.order(),
          parameter.enumClass() == Enum.class ? null : parameter.enumClass().getName()));
      }
    } else {
      final ReportParameterProvider parameterProvider = beanFactory.getBean(parameterProviderClass);
      params.addAll(parameterProvider.createParams());
    }
    return params;
  }
}
