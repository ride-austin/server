package com.rideaustin.service.user;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.rideaustin.model.CarDocument;
import com.rideaustin.model.Document;
import com.rideaustin.model.enums.DocumentType;
import com.rideaustin.model.ride.Car;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.CarDocumentDslRepository;
import com.rideaustin.repo.dsl.CarDslRepository;
import com.rideaustin.repo.dsl.DocumentDslRepository;
import com.rideaustin.rest.exception.ForbiddenException;
import com.rideaustin.rest.exception.NotFoundException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.service.CurrentUserService;
import com.rideaustin.service.DocumentService;
import com.rideaustin.service.thirdparty.S3StorageService;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CarPhotoService {

  private final DocumentDslRepository documentDslRepository;
  private final CarDocumentDslRepository carDocumentDslRepository;
  private final DocumentService documentService;
  private final CarDslRepository carDslRepository;
  private final S3StorageService s3StorageService;
  private final CurrentUserService currentUserService;

  public List<Document> getCarPhotos(Long carId) {
    return carDocumentDslRepository.findCarPhotos(carId);
  }

  public Document saveNewCarPhoto(@Nonnull Long carId, @Nonnull DocumentType carPhotoType,
    @Nonnull MultipartFile photoData) throws RideAustinException {

    User user = currentUserService.getUser();
    Car car = carDslRepository.findOne(carId);
    if (car == null) {
      throw new NotFoundException("Car not found");
    }
    if (!user.isAdmin() && user.getId() != car.getDriver().getUser().getId()) {
      throw new ForbiddenException("User is not allowed to change other driver's car photo");
    }

    List<Document> existingPhotos = carDocumentDslRepository.findCarPhotos(carId);
    Optional<Document> existing = existingPhotos.stream().filter(d -> d.getDocumentType().equals(carPhotoType)).findFirst();
    if (existing.isPresent()) {
      existing.get().setRemoved(true);
      documentDslRepository.save(existing.get());
    }
    Document carPhoto = new Document();
    carPhoto.setDocumentType(carPhotoType);
    carPhoto.setName(carPhotoType.getDefaultName());
    carPhoto.setDocumentUrl(s3StorageService.savePublicOrThrow("car photo - ".concat(carPhotoType.name()), DocumentType.CAR_PHOTOS, photoData));

    documentDslRepository.save(carPhoto);
    documentService.saveCarDocument(car, carPhoto);
    carDslRepository.save(car);
    return carPhoto;
  }

  public void removeCarPhoto(Long carPhotoId) throws RideAustinException {
    CarDocument carPhoto = carDocumentDslRepository.findByDocumentId(carPhotoId);
    if (carPhoto == null) {
      throw new NotFoundException("Car photo not found");
    }
    User user = currentUserService.getUser();
    if (!user.isAdmin() && user.getId() != carPhoto.getCar().getDriver().getUser().getId()) {
      throw new ForbiddenException("User is not allowed to remove other driver's car photo");
    }

    carPhoto.getDocument().setRemoved(true);
    carDocumentDslRepository.saveAny(carPhoto);
  }

}
