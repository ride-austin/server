package com.rideaustin.report.render;

import com.rideaustin.report.Report;
import com.rideaustin.report.model.ReportFormat;
import com.rideaustin.report.model.ReportMetadata;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.service.email.EmailAttachment;
import com.rideaustin.service.email.XLSXEmailAttachment;
import com.rideaustin.service.thirdparty.S3StorageService;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class XLSXReportRenderer<T, P> extends BaseReportRenderer<Report<T, P>> {

  public XLSXReportRenderer(S3StorageService s3StorageService) {
    super(s3StorageService);
  }

  @Override
  protected Collection<EmailAttachment> doCreateAttachments(Report<T, P> report) throws ServerError {
    try (XSSFWorkbook wb = new XSSFWorkbook()) {
      String name = Optional.ofNullable(report.getMetadata())
        .map(ReportMetadata::getReportName)
        .orElseGet(() -> getDefaultReportName(report));

      CreationHelper helper = wb.getCreationHelper();
      XSSFSheet sheet = wb.createSheet(name);

      int r = 0;
      final String[] headers = report.getAdapter().getHeaders();
      XSSFRow headerRow = sheet.createRow((short) r++);
      for (int i = 0; i < headers.length; i++) {
        headerRow.createCell(i)
          .setCellValue(helper.createRichTextString(headers[i]));
      }
      final List<String[]> contents = report.getResultsStream()
        .map(report.getAdapter().getRowMapper())
        .collect(Collectors.toList());
      if (contents.isEmpty()) {
        return Collections.emptySet();
      } else {
        for (String[] content : contents) {
          XSSFRow row = sheet.createRow((short) r++);
          for (int i = 0; i < content.length; i++) {
            row.createCell(i)
              .setCellValue(helper.createRichTextString(content[i]));
          }
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        wb.write(os);
        return Collections.singleton(new XLSXEmailAttachment(name, os.toByteArray()));
      }
    } catch (IOException e) {
      throw new ServerError(e);
    }
  }

  @Override
  protected boolean canRepresentFormat(ReportFormat reportFormat) {
    return ReportFormat.XLSX.equals(reportFormat);
  }

  @Override
  public boolean canRepresent(Report report) {
    return !report.isComposite() && canRepresentFormat(report.getMetadata().getReportFormat());
  }
}
