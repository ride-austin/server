package com.rideaustin.rest;

import java.util.Date;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.Valid;

import org.apache.http.HttpStatus;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.rideaustin.CheckedTransactional;
import com.rideaustin.DriverEndpoint;
import com.rideaustin.WebClientEndpoint;
import com.rideaustin.assemblers.DocumentDtoEnricher;
import com.rideaustin.model.Document;
import com.rideaustin.model.DocumentDto;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.enums.DocumentType;
import com.rideaustin.model.ride.Car;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.CarDslRepository;
import com.rideaustin.repo.dsl.DriverDslRepository;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.ForbiddenException;
import com.rideaustin.rest.exception.NotFoundException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.model.ListAvatarDocumentsParams;
import com.rideaustin.rest.model.ListCarDocumentsParams;
import com.rideaustin.rest.model.MobileDriverDriverDto;
import com.rideaustin.service.CurrentUserService;
import com.rideaustin.service.DocumentService;
import com.rideaustin.service.DriverService;
import com.rideaustin.service.city.CityValidationService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;

@RestController
@CheckedTransactional
@RequestMapping("/rest/driversDocuments")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DriversDocuments {

  private final DriverService driverService;
  private final CurrentUserService currentUserService;
  private final DocumentService documentService;
  private final DocumentDtoEnricher documentDtoAssembler;
  private final CityValidationService cityValidationService;
  private final CarDslRepository carDslRepository;
  private final DriverDslRepository driverDslRepository;

  @DriverEndpoint
  @WebClientEndpoint
  @ApiOperation("List all available documents for a driver")
  @RolesAllowed({AvatarType.ROLE_ADMIN, AvatarType.ROLE_DRIVER})
  @GetMapping(path = "/{driverId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_FORBIDDEN, message = "Requesting user is not the same driver as requested one")
  })
  public List<DocumentDto> listDriverDocuments(
    @ApiParam(value = "Driver ID", example = "1") @PathVariable long driverId,
    @ApiParam ListAvatarDocumentsParams listAvatarDocumentsParams
  ) throws RideAustinException {
    restrictOtherDrivers(driverId);
    listAvatarDocumentsParams.setAvatarId(driverId);
    return documentDtoAssembler.toDto(documentService.listAvatarDocuments(listAvatarDocumentsParams));
  }

  @DriverEndpoint
  @WebClientEndpoint
  @ApiOperation("List all available documents for a car")
  @RolesAllowed({AvatarType.ROLE_ADMIN, AvatarType.ROLE_DRIVER})
  @GetMapping(path = "/{driverId}/cars/{carId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_FORBIDDEN, message = "Requesting user is not the same driver as requested one")
  })
  public List<DocumentDto> listCarDocuments(
    @ApiParam(value = "Driver ID", example = "1") @PathVariable long driverId,
    @ApiParam(value = "Car ID", example = "1") @PathVariable long carId,
    @ApiParam ListCarDocumentsParams listCarDocumentsParams
  ) throws RideAustinException {
    restrictOtherDrivers(driverId);
    listCarDocumentsParams.setDriverId(driverId);
    listCarDocumentsParams.setCarId(carId);

    List<DocumentDto> carDocuments = documentService.listCarDocuments(listCarDocumentsParams);

    return documentDtoAssembler.toDto(carDocuments);
  }

  @DriverEndpoint
  @WebClientEndpoint
  @ApiOperation("Update existing document")
  @RolesAllowed({AvatarType.ROLE_ADMIN, AvatarType.ROLE_DRIVER})
  @PutMapping(value = "/{documentId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = "Document not found")
  })
  public Document updateDocument(
    @ApiParam(value = "Document ID", example = "1") @PathVariable Long documentId,
    @ApiParam @Valid @RequestBody Document document
  ) throws NotFoundException {
    return documentService.updateDocument(documentId, document);
  }

  @DriverEndpoint
  @WebClientEndpoint
  @ApiOperation("Add a new document")
  @RolesAllowed({AvatarType.ROLE_ADMIN, AvatarType.ROLE_DRIVER})
  @PostMapping(path = "/{driverId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = "File is not provided or parameters are invalid"),
    @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = "Driver not found"),
    @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "Failed to upload image file")
  })
  public MobileDriverDriverDto addDocument(
    @ApiParam(value = "Driver ID", example = "1") @PathVariable long driverId,
    @ApiParam(value = "Document type", required = true) @RequestParam(name = "driverPhotoType") DocumentType documentType,
    @ApiParam(value = "Image file", required = true) @RequestParam(name = "fileData") MultipartFile file,
    @ApiParam(value = "Car ID", example = "1") @RequestParam(required = false) Long carId,
    @ApiParam(value = "City ID", example = "1") @RequestParam(required = false) Long cityId,
    @ApiParam(value = "Validity date", example = "2019-12-31") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date validityDate
  ) throws RideAustinException {

    if (file.isEmpty()) {
      throw new BadRequestException("Empty file provided");
    }

    Driver driver = driverService.findDriver(driverId, currentUserService.getUser());
    cityValidationService.validateCity(documentType, cityId);

    if (DocumentType.CAR_DOCUMENTS.contains(documentType)) {
      Car car = validateCar(driver, carId);
      documentService.uploadCarDocument(file, documentType, validityDate, cityId, driver, car);
    } else {
      documentService.uploadAvatarDocument(file, documentType, validityDate, driver, cityId);
    }

    driver = driverService.findDriver(driverId, currentUserService.getUser());

    return driverDslRepository.getCurrentDriver(driver.getUser());
  }

  private void restrictOtherDrivers(long driverId) throws ForbiddenException {
    User user = currentUserService.getUser();
    if (!user.isAdmin() && user.isDriver() && driverId != user.getAvatar(Driver.class).getId()) {
      throw new ForbiddenException();
    }
  }

  private Car validateCar(@Nonnull Driver driver, Long carId) throws BadRequestException {
    if (carId == null) {
      throw new BadRequestException("No car ID provided");
    }
    Car car = carDslRepository.findOne(carId);
    if (car == null) {
      throw new BadRequestException("Wrong car ID provided");
    }
    if (car.getDriver().getId() != driver.getId()) {
      throw new BadRequestException("Car is assigned to other driver");
    }
    return car;
  }
}
