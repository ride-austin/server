package com.rideaustin.rest;

import java.net.URI;
import java.net.URISyntaxException;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.rideaustin.CheckedTransactional;
import com.rideaustin.RiderEndpoint;
import com.rideaustin.model.BaseEntityPhoto;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.jpa.UserRepository;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.service.CurrentUserService;
import com.rideaustin.service.thirdparty.S3StorageService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;

@RestController
@CheckedTransactional
@RequestMapping("/rest/photos")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class UserPhotos {

  private final CurrentUserService cuSvc;
  private final S3StorageService s3Svc;
  private final UserRepository userRepo;

  @RiderEndpoint
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ApiOperation(value = "Upload new user profile photo", response = URI.class)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_CREATED, message = "Upload successful"),
    @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = "Image file is missing"),
    @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "Failed to upload file")
  })
  public ResponseEntity post(@RequestParam("file") MultipartFile file) throws RideAustinException {
      User user = cuSvc.getUser();
      if (file == null) {
        throw new BadRequestException("Photo is required");
      }
      String photoUrl = s3Svc.savePublicOrThrow("User photo", BaseEntityPhoto.USER_PHOTOS, file);
      user = userRepo.findOne(user.getId());
      user.setPhotoUrl(photoUrl);
      userRepo.saveAndFlush(user);
    try {
      return ResponseEntity.created(new URI(photoUrl)).build();
    } catch (URISyntaxException e) {
      throw new ServerError(e);
    }
  }
}
