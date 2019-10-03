package com.rideaustin.rest;

import java.io.IOException;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.rideaustin.CheckedTransactional;
import com.rideaustin.WebClientEndpoint;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.service.batch.DriverBatchUpdateService;
import com.rideaustin.service.model.DriverBatchUpdateError;

import au.com.bytecode.opencsv.CSVWriter;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@CheckedTransactional
@RolesAllowed(AvatarType.ROLE_ADMIN)
@RequestMapping("/rest/drivers/data")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DriversData {

  private final DriverBatchUpdateService driverBatchUpdateService;

  @WebClientEndpoint
  @ApiOperation("CSV bulk upload for driver's data")
  @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ApiResponses({
    @ApiResponse(code = org.apache.http.HttpStatus.SC_NO_CONTENT, message = "Upload success"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_BAD_REQUEST, message = "Provided data is invalid")
  })
  public ResponseEntity upload(@RequestPart MultipartFile driversDataCsv) {
    List<DriverBatchUpdateError> errors = driverBatchUpdateService.batchUpdateDrivers(driversDataCsv);
    if (errors.isEmpty()) {
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.badRequest().body(errors);
  }

  @WebClientEndpoint
  @ResponseStatus(HttpStatus.OK)
  @ApiOperation("Download a sample CSV to fill in for further uploading")
  @GetMapping(value = "/sample", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @ApiResponses({
    @ApiResponse(code = org.apache.http.HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "Failed to generate sample file")
  })
  public void sample(HttpServletResponse response) throws ServerError {
    try (CSVWriter writer = new CSVWriter(response.getWriter())) {
      writer.writeNext(driverBatchUpdateService.generateSample());
      response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=drivers_batch_upload.csv");
      response.setHeader(HttpHeaders.CONTENT_TYPE, "text/csv");
    } catch (IOException e) {
      log.error("Failed to generate sample file", e);
      throw new ServerError("Unable to generate sample file");
    }
  }
}
