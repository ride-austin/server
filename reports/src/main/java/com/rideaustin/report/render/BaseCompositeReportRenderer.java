package com.rideaustin.report.render;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;
import com.rideaustin.report.BaseCompositeReport;
import com.rideaustin.report.Report;
import com.rideaustin.report.model.ReportFormat;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.service.email.EmailAttachment;
import com.rideaustin.service.thirdparty.S3StorageService;

public class BaseCompositeReportRenderer<P> extends BaseReportRenderer<BaseCompositeReport<P>> {

  private final BaseReportRenderer renderer;

  public BaseCompositeReportRenderer(BaseReportRenderer renderer, S3StorageService s3StorageService) {
    super(s3StorageService);
    this.renderer = renderer;
  }

  @Override
  public Collection<EmailAttachment> doCreateAttachments(BaseCompositeReport<P> report) throws ServerError {
    List<EmailAttachment> attachments = Lists.newArrayList();
    for (Report containedReport : report.getReports()) {
      attachments.addAll(renderer.doCreateAttachments(containedReport));
    }
    return attachments;
  }

  @Override
  public boolean canRepresent(Report report) {
    return report.isComposite() && canRepresentFormat(report.getMetadata().getReportFormat());
  }

  @Override
  protected boolean canRepresentFormat(ReportFormat reportFormat) {
    return renderer.canRepresentFormat(reportFormat);
  }

}
