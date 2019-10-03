package com.rideaustin.rest;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rideaustin.CheckedTransactional;
import com.rideaustin.WebClientEndpoint;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.rest.exception.NotFoundException;
import com.rideaustin.service.DocumentService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;

@RestController
@CheckedTransactional
@RolesAllowed(AvatarType.ROLE_ADMIN)
@RequestMapping("/rest/driversDocuments")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DriversDocumentsAdministration {

  private final DocumentService documentService;

  @WebClientEndpoint
  @ApiOperation("Remove document")
  @DeleteMapping(path = "/{documentId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = "Document not found")
  })
  public void removeDocument(
    @ApiParam(value = "Document ID", example = "1") @PathVariable long documentId
  ) throws NotFoundException {
    documentService.removeDocument(documentId);
  }
}
