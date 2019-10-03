package com.rideaustin.rest;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.rideaustin.CheckedTransactional;
import com.rideaustin.DriverEndpoint;
import com.rideaustin.MobileClientEndpoint;
import com.rideaustin.assemblers.CarPhotoDtoAssembler;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.enums.DocumentType;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.model.CarPhotoDto;
import com.rideaustin.service.user.CarPhotoService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rest/carphotos")
@CheckedTransactional
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CarPhotosCommon {

  private final CarPhotoService carPhotoService;
  private final CarPhotoDtoAssembler dtoAssembler;

  @MobileClientEndpoint
  @RolesAllowed({AvatarType.ROLE_ADMIN, AvatarType.ROLE_DRIVER})
  @ApiOperation("Upload a new car photo as a driver or an admin")
  @PostMapping(value = "/car/{carId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses({
    @ApiResponse(code = org.apache.http.HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_FORBIDDEN, message = "User is not allowed to upload photo"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_NOT_FOUND, message = "Car doesn't exist"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "Failed to upload file to S3")
  })
  public CarPhotoDto addNewPhoto(
    @ApiParam(value = "Car ID", example = "1") @PathVariable("carId") Long carId,
    @ApiParam(value = "Photo type", example = "CAR_PHOTO_FRONT,CAR_PHOTO_BACK,CAR_PHOTO_INSIDE,CAR_PHOTO_TRUNK") @RequestBody @RequestParam DocumentType carPhotoType,
    @ApiParam(value = "Image file") @RequestParam("photo") MultipartFile fileData)
    throws RideAustinException {
    return dtoAssembler.toDto(carPhotoService.saveNewCarPhoto(carId, carPhotoType, fileData));
  }

  @DriverEndpoint
  @DeleteMapping("/{carPhotoId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @RolesAllowed({AvatarType.ROLE_ADMIN, AvatarType.ROLE_DRIVER})
  @ApiOperation("Remove existing car photo as a driver or an admin")
  @ApiResponses({
    @ApiResponse(code = org.apache.http.HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_FORBIDDEN, message = "User is not allowed to upload photo"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_NOT_FOUND, message = "Car doesn't exist"),
  })
  public void delete(@ApiParam(value = "Photo ID", example = "1") @PathVariable("carPhotoId") Long carPhotoId)
    throws RideAustinException {
    carPhotoService.removeCarPhoto(carPhotoId);
  }

}
