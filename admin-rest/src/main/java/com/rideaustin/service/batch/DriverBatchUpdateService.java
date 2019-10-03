package com.rideaustin.service.batch;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.inject.Inject;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableList;
import com.rideaustin.Constants;
import com.rideaustin.model.Document;
import com.rideaustin.model.enums.DocumentStatus;
import com.rideaustin.model.enums.DocumentType;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.DocumentDslRepository;
import com.rideaustin.repo.dsl.DriverDslRepository;
import com.rideaustin.service.model.DriverBatchUpdateError;
import com.rideaustin.service.user.DriverTypeUtils;

import au.com.bytecode.opencsv.CSVReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DriverBatchUpdateService {

  private final DriverDslRepository driverDslRepository;
  private final DocumentDslRepository documentDslRepository;
  private final DriverBatchUpdateConverter converter;

  public String[] generateSample() {
    return Arrays.stream(DriverBatchUpdateDto.class.getDeclaredFields()).map(Field::getName)
      .map(f -> CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, f))
      .toArray(String[]::new);
  }

  public List<DriverBatchUpdateError> batchUpdateDrivers(MultipartFile driversDataCsv) {
    List<DriverBatchUpdateError> uploadErrors = new LinkedList<>();
    try (CSVReader reader = new CSVReader(new StringReader(new String(driversDataCsv.getBytes())))) {
      LinkedList<String[]> rows = new LinkedList<>(reader.readAll());
      List<Driver> updatedDrivers = new LinkedList<>();
      List<Document> updatedDocuments = new LinkedList<>();
      String[] header = rows.removeFirst();
      for (int i = 0; i < rows.size(); i++) {
        Pair<DriverBatchUpdateDto, List<DriverBatchUpdateError>> convertResult = converter.convert(header, i, rows.get(i));
        if (!convertResult.getRight().isEmpty()) {
          final int rowNumber = i;
          convertResult.getRight().forEach(error -> error.setRowNumber(rowNumber));
          uploadErrors.addAll(convertResult.getRight());
          break;
        }
        Driver updatedDriver = updateDriver(convertResult.getLeft());
        if (updatedDriver != null) {
          updatedDrivers.add(updatedDriver);
          updatedDocuments.addAll(updateDocuments(updatedDriver, convertResult.getLeft()));
        }
      }
      if (uploadErrors.isEmpty()) {
        driverDslRepository.saveMany(updatedDrivers);
        documentDslRepository.saveMany(updatedDocuments);
      }
    } catch (IOException e) {
      log.error("Error while reading input file", e);
      uploadErrors.add(new DriverBatchUpdateError("Input file can not be read"));
    }
    return uploadErrors;
  }

  private List<Document> updateDocuments(Driver updatedDriver, DriverBatchUpdateDto dto) {
    Document photo = documentDslRepository.findByAvatarAndType(updatedDriver, DocumentType.DRIVER_PHOTO);
    safeUpdateDocument(photo, dto.getProfilePhotosStatus());
    Document license = documentDslRepository.findByAvatarAndType(updatedDriver, DocumentType.LICENSE);
    safeUpdateDocument(license, dto.getDriverLicenseStatus(), dto.getLicenseExpiryDate());
    return ImmutableList.of(photo, license);
  }

  private void safeUpdateDocument(Document document, DocumentStatus status, Date expiryDate) {
    safeUpdateDocument(document, status);
    safeUpdate(document::setValidityDate, expiryDate, document::getValidityDate);
  }

  private void safeUpdateDocument(Document document, DocumentStatus status) {
    safeUpdate(document::setDocumentStatus, status, document::getDocumentStatus);
  }

  private Driver updateDriver(DriverBatchUpdateDto dto) {
    Driver driver = driverDslRepository.findById(dto.getId());
    if (driver == null) {
      return null;
    }
    User user = driver.getUser();
    safeUpdate(user::setFirstname, dto.getFirstName(), user::getFirstname);
    safeUpdate(user::setMiddleName, dto.getMiddleName(), user::getMiddleName);
    safeUpdate(user::setLastname, dto.getLastName(), user::getLastname);
    safeUpdate(user::setPhoneNumber, dto.getPhoneNumber(), user::getPhoneNumber);
    safeUpdate(user::setEmail, dto.getEmail(), user::getEmail);
    safeUpdate(user::setDateOfBirth, dto.getDateOfBirth(), user::getDateOfBirth);
    safeUpdate(user::setUserEnabled, dto.getEnabled(), user::getUserEnabled);
    safeUpdate(user::setGender, dto.getGender(), user::getGender);
    safeUpdate(driver::setSsn, dto.getSsn(), driver::getSsn);
    safeUpdate(driver::setActive, dto.getActive(), driver::isActive);
    safeUpdate(driver::setGrantedDriverTypesBitmask, DriverTypeUtils.toBitMask(dto.getDriverTypes()), driver::getGrantedDriverTypesBitmask);
    safeUpdate(driver::setRating, dto.getRating(), driver::getRating);
    safeUpdate(driver::setPayoneerStatus, dto.getPayoneerStatus(), driver::getPayoneerStatus);
    safeUpdate(driver::setCityApprovalStatus, dto.getCityApprovalStatus(), driver::getCityApprovalStatus);
    safeUpdate(driver::setActivationStatus, dto.getActivationStatus(), driver::getActivationStatus);
    safeUpdate(driver::setActivationNotes, dto.getActivationNotes(), driver::getActivationNotes);
    safeUpdate(driver::setLicenseNumber, dto.getLicenseNumber(), driver::getLicenseNumber);
    safeUpdate(driver::setLicenseState, dto.getLicenseState(), driver::getLicenseState);
    safeUpdate(driver::setCityId, Optional.ofNullable(dto.getCity()).map(Constants.City::getId).orElse(null), driver::getCityId);
    return driver;
  }

  private <T> void safeUpdate(Consumer<T> setter, T value, Supplier<T> defaultValue) {
    setter.accept(Optional.ofNullable(value).orElse(defaultValue.get()));
  }
}
