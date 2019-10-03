DROP TABLE IF EXISTS `active_drivers`;
CREATE TABLE `active_drivers` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_date` datetime NOT NULL,
  `updated_date` datetime NOT NULL,
  `inactive_on` datetime DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `driver_id` bigint(20) DEFAULT NULL,
  `car_id` bigint(20) DEFAULT NULL,
  `city_id` bigint(20) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  KEY `idx_active_drivers_diver_id` (`driver_id`),
  KEY `fk_active_drivers_city` (`city_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `administrators`;
CREATE TABLE `administrators` (
  `id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `airports`;
CREATE TABLE `airports` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_date` datetime NOT NULL,
  `updated_date` datetime NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `pickup_fee` decimal(19,2) DEFAULT NULL,
  `dropoff_fee` decimal(19,2) DEFAULT NULL,
  `enabled` bit(1) DEFAULT NULL,
  `city_id` bigint(20) NOT NULL,
  `area_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `api_clients`;
CREATE TABLE `api_clients` (
  `id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `app_infos`;
CREATE TABLE `app_infos` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_date` datetime NOT NULL,
  `updated_date` datetime NOT NULL,
  `avatar_type` varchar(32) NOT NULL,
  `platform_type` varchar(32) NOT NULL,
  `version` varchar(32) NOT NULL,
  `build` int(11) DEFAULT NULL,
  `mandatory_upgrade` bit(1) NOT NULL,
  `user_agent_header` varchar(255) NOT NULL,
  `download_url` varchar(1024) DEFAULT NULL,
  `city_id` bigint(20) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `area`;
CREATE TABLE `area` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_date` datetime NOT NULL,
  `updated_date` datetime NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `icon_url` varchar(255) DEFAULT NULL,
  `enabled` bit(1) DEFAULT NULL,
  `city_id` bigint(20) NOT NULL DEFAULT '1',
  `key_name` varchar(7500) DEFAULT NULL,
  `visible_to_drivers` bit(1) NOT NULL DEFAULT b'1',
  `area_geometry_id` bigint(20) DEFAULT NULL,
  `parent_area_id` bigint(20) DEFAULT NULL,
  `map_icon_url` varchar(64) DEFAULT NULL,
  `map_icon_coords` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `area_exclusions`;
CREATE TABLE `area_exclusions` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_date` datetime NOT NULL,
  `updated_date` datetime NOT NULL,
  `area_id` bigint(20) NOT NULL,
  `exclusion_geometry_id` bigint(20) NOT NULL,
  `leave_area_on_enter` bit(1) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `excl_area_id_idx` (`area_id`),
  KEY `excl_geom_id_idx` (`exclusion_geometry_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `area_geometries`;
CREATE TABLE `area_geometries` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_date` datetime NOT NULL,
  `updated_date` datetime NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `top_left_corner_location_lat` double DEFAULT NULL,
  `top_left_corner_location_lng` double DEFAULT NULL,
  `bottom_right_corner_location_lat` double DEFAULT NULL,
  `bottom_right_corner_location_lng` double DEFAULT NULL,
  `center_point_lat` double DEFAULT NULL,
  `center_point_lng` double DEFAULT NULL,
  `csv_geometry` varchar(7500) DEFAULT NULL,
  `label_lat` double DEFAULT NULL,
  `label_lng` double DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `index_zip_code_geometry_top_left_corner_location_lat` (`top_left_corner_location_lat`),
  KEY `index_zip_code_geometry_top_left_corner_location_lng` (`top_left_corner_location_lng`),
  KEY `index_zip_code_geometry_bottom_right_corner_location_lat` (`bottom_right_corner_location_lat`),
  KEY `index_zip_code_geometry_bottom_right_corner_location_lng` (`bottom_right_corner_location_lng`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

insert into area_geometries VALUE('1', now(), now(), NULL, '0', '0', '0', '0', '30.267153', '-97.743061', '-97.41895,30.58591 -97.41895,30.0758 -98.08044,30.0758 -98.08044,30.58591', '30.267153', '-97.743061');

DROP TABLE IF EXISTS `area_queue`;
CREATE TABLE `area_queue` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_date` datetime NOT NULL,
  `updated_date` datetime NOT NULL,
  `area_id` bigint(20) NOT NULL,
  `active_driver_id` bigint(20) NOT NULL,
  `enabled` bit(1) DEFAULT NULL,
  `car_category` varchar(255) DEFAULT NULL,
  `last_present_in_queue` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_active_drivers_diver_id` (`active_driver_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `avatar_documents`;
CREATE TABLE `avatar_documents` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `avatar_id` bigint(20) DEFAULT NULL,
  `document_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_avatar_documents_document_id` (`document_id`),
  KEY `idx_avatar_documents_avatar_id` (`avatar_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `avatar_email_notifications`;
CREATE TABLE `avatar_email_notifications` (
  `avatar_id` bigint(20) NOT NULL,
  `notification_date` datetime DEFAULT NULL,
  `notification_type` varchar(255) DEFAULT NULL,
  KEY `idx_avatars_avatar_id` (`avatar_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `avatars`;
CREATE TABLE `avatars` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_date` datetime NOT NULL,
  `updated_date` datetime NOT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `active` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`),
  KEY `idx_avatars_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `blocked_devices`;
CREATE TABLE `blocked_devices` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_date` datetime NOT NULL,
  `updated_date` datetime NOT NULL,
  `rider_id` bigint(20) NOT NULL,
  `device_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `campaign_areas`;
CREATE TABLE `campaign_areas` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `campaign_id` bigint(20) NOT NULL,
  `area_id` bigint(20) NOT NULL,
  `type` varchar(8) NOT NULL,
  `subtype` varchar(8) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `campaign_providers`;
CREATE TABLE `campaign_providers` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_date` datetime NOT NULL,
  `updated_date` datetime NOT NULL,
  `name` varchar(45) NOT NULL,
  `enabled` bit(1) NOT NULL DEFAULT b'1',
  `menu_icon` varchar(128) NOT NULL,
  `city_id` bigint(20) NOT NULL,
  `shown_in_menu` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `campaign_riders`;
CREATE TABLE `campaign_riders` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `rider_id` bigint(20) NOT NULL,
  `campaign_id` bigint(20) NOT NULL,
  `enabled` bit(1) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `campaign_riders_campaign_idx` (`campaign_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `campaign_rides`;
CREATE TABLE `campaign_rides` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `campaign_id` bigint(20) NOT NULL,
  `ride_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_campaign_rides_ride_id` (`ride_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `campaigns`;
CREATE TABLE `campaigns` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_date` datetime NOT NULL,
  `updated_date` datetime NOT NULL,
  `name` varchar(45) NOT NULL,
  `description` varchar(4096) NOT NULL,
  `enabled` bit(1) NOT NULL DEFAULT b'1',
  `capped_amount` decimal(19,2) NOT NULL DEFAULT '0.00',
  `maximum_capped_amount` decimal(19,2) NOT NULL DEFAULT '0.00',
  `coverage_type` varchar(8) NOT NULL,
  `accessibility_config` varchar(1024) DEFAULT NULL,
  `eligible_car_categories` int(11) NOT NULL,
  `active_on_days` int(11) NOT NULL,
  `active_from_hour` int(11) NOT NULL,
  `active_to_hour` int(11) NOT NULL,
  `banner_icon` varchar(128) DEFAULT NULL,
  `receipt_image` varchar(128) DEFAULT NULL,
  `receipt_title` varchar(128) DEFAULT NULL,
  `header_icon` varchar(128) DEFAULT NULL,
  `description_body` text,
  `footer_text` varchar(1024) DEFAULT NULL,
  `provider_id` bigint(20) DEFAULT NULL,
  `tipping_allowed` bit(1) DEFAULT NULL,
  `description_trip_history` varchar(64) DEFAULT NULL,
  `eligibility_strategy` varchar(128) DEFAULT NULL,
  `show_map` bit(1) NOT NULL DEFAULT b'1',
  `show_details` bit(1) NOT NULL DEFAULT b'1',
  `maximum_distance` decimal(19,2) DEFAULT NULL,
  `is_user_bound` bit(1) NOT NULL DEFAULT b'0',
  `validate_trackers` bit(1) NOT NULL DEFAULT b'1',
  `trackers_validation_threshold` decimal(19,2) DEFAULT NULL,
  `report_recipients` varchar(1024) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `car_documents`;
CREATE TABLE `car_documents` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `car_id` bigint(20) DEFAULT NULL,
  `document_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_avatar_documents_document_id` (`document_id`),
  KEY `idx_avatar_documents_car_id` (`car_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `car_types`;
CREATE TABLE `car_types` (
  `car_category` varchar(255) NOT NULL,
  `title` varchar(255) DEFAULT NULL,
  `description` varchar(4096) DEFAULT NULL,
  `icon_url` varchar(255) DEFAULT NULL,
  `plain_icon_url` varchar(255) DEFAULT NULL,
  `map_icon_url` varchar(255) DEFAULT NULL,
  `full_icon_url` varchar(255) DEFAULT NULL,
  `selected_icon_url` varchar(255) DEFAULT NULL,
  `unselected_icon_url` varchar(255) DEFAULT NULL,
  `selected_female_icon_url` varchar(255) DEFAULT NULL,
  `max_persons` int(11) NOT NULL DEFAULT '0',
  `order` int(11) DEFAULT NULL,
  `active` bit(1) DEFAULT NULL,
  `bitmask` int(11) DEFAULT NULL,
  `configuration` varchar(4096) DEFAULT NULL,
  PRIMARY KEY (`car_category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

LOCK TABLES `car_types` WRITE;
/*!40000 ALTER TABLE `car_types` DISABLE KEYS */;
INSERT INTO `car_types` VALUES ('PREMIUM','PREMIUM',NULL,NULL,NULL,'','','','','',0,3,1,4,'{"skipRideAuthorization": false}'),
                               ('REGULAR','STANDARD',NULL,'','','','','','','',0,1,1,1,'{"skipRideAuthorization": false}'),
                               ('SUV','SUV',NULL,'','','','','','','',0,2,1,2,'{"skipRideAuthorization": false}');
/*!40000 ALTER TABLE `car_types` ENABLE KEYS */;
UNLOCK TABLES;

DROP TABLE IF EXISTS `cars`;
CREATE TABLE `cars` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_date` datetime NOT NULL,
  `updated_date` datetime NOT NULL,
  `color` varchar(255) NOT NULL,
  `license` varchar(255) NOT NULL,
  `make` varchar(255) NOT NULL,
  `model` varchar(255) NOT NULL,
  `year` varchar(255) NOT NULL,
  `driver_id` bigint(20) DEFAULT NULL,
  `car_categories_bitmask` int(11) NOT NULL DEFAULT '1',
  `inspection_status` varchar(32) NOT NULL DEFAULT 'NOT_INSPECTED',
  `inspection_notes` text,
  `selected` bit(1) NOT NULL DEFAULT b'0',
  `removed` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`),
  KEY `idx_cars_driver_id` (`driver_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `charities`;
CREATE TABLE `charities` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `description` longtext,
  `image_url` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `city_bitmask` int(11) NOT NULL DEFAULT '1',
  `order` int(11) NOT NULL DEFAULT '1',
  `enabled` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `cities`;
CREATE TABLE `cities` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_date` datetime NOT NULL,
  `updated_date` datetime NOT NULL,
  `name` varchar(255) NOT NULL,
  `enabled` bit(1) DEFAULT NULL,
  `bitmask` int(11) NOT NULL,
  `logo_url` varchar(100) DEFAULT NULL,
  `area_geometry_id` bigint(20) DEFAULT NULL,
  `office` varchar(255) DEFAULT NULL,
  `page_url` varchar(255) DEFAULT NULL,
  `contact_email` varchar(255) DEFAULT NULL,
  `support_email` varchar(255) DEFAULT NULL,
  `play_store_link` varchar(255) DEFAULT NULL,
  `app_store_link` varchar(255) DEFAULT NULL,
  `app_name` varchar(255) DEFAULT NULL,
  `logo_url_dark` varchar(255) DEFAULT NULL,
  `documents_email` varchar(255) DEFAULT NULL,
  `drivers_email` varchar(255) DEFAULT NULL,
  `onboarding_email` varchar(255) DEFAULT NULL,
  `update_preferences_link` varchar(255) DEFAULT NULL,
  `unsubscribe_link` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `citys_name_unique` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

LOCK TABLES `cities` WRITE;
/*!40000 ALTER TABLE `cities` DISABLE KEYS */;
INSERT INTO `cities` VALUES (1,now(),now(),'AUSTIN',1,1,NULL,1,NULL,NULL,'','',NULL,NULL,NULL,NULL,'','','',NULL,NULL);
/*!40000 ALTER TABLE `cities` ENABLE KEYS */;
UNLOCK TABLES;

DROP TABLE IF EXISTS `city_car_types`;
CREATE TABLE `city_car_types` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_date` datetime NOT NULL,
  `updated_date` datetime NOT NULL,
  `city_id` bigint(20) NOT NULL,
  `car_category` varchar(255) NOT NULL,
  `enabled` bit(1) NOT NULL,
  `configuration` varchar(2550) DEFAULT NULL,
  `minimum_fare` decimal(19,2) NOT NULL DEFAULT '0.00',
  `base_fare` decimal(19,2) NOT NULL DEFAULT '0.00',
  `booking_fee` decimal(19,2) NOT NULL DEFAULT '0.00',
  `ra_fee_factor` decimal(19,2) NOT NULL DEFAULT '0.00',
  `rate_per_mile` decimal(19,2) NOT NULL DEFAULT '0.00',
  `rate_per_minute` decimal(19,2) NOT NULL DEFAULT '0.00',
  `cancellation_fee` decimal(19,2) NOT NULL DEFAULT '0.00',
  `city_fee_rate` decimal(19,2) NOT NULL DEFAULT '0.00',
  `processing_fee_rate` decimal(19,2) NOT NULL DEFAULT '0.00',
  `processing_fee_text` varchar(255) DEFAULT NULL,
  `processing_fee_fixed_part` decimal(19,2) NOT NULL DEFAULT '0.00',
  `processing_fee_minimum` decimal(19,2) NOT NULL DEFAULT '0.00',
  `fixed_ra_fee` decimal(19,2) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_city_car_types_city` (`city_id`),
  KEY `fk_city_car_types_car_type` (`car_category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `city_driver_types`;
CREATE TABLE `city_driver_types` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_date` datetime NOT NULL,
  `updated_date` datetime NOT NULL,
  `city_id` bigint(20) NOT NULL,
  `driver_type` varchar(255) NOT NULL,
  `enabled` bit(1) NOT NULL,
  `configuration` varchar(2550) DEFAULT NULL,
  `car_categories_bitmask` int(11) NOT NULL DEFAULT '1',
  `bitmask` int(11) NOT NULL,
  `configuration_class` varchar(255) DEFAULT 'com.rideaustin.model.ride.CityDriverType$DefaultDriverTypeConfiguration',
  PRIMARY KEY (`id`),
  KEY `fk_city_driver_types_city` (`city_id`),
  KEY `fk_city_driver_types_driver_type` (`driver_type`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;

LOCK TABLES `city_driver_types` WRITE;
/*!40000 ALTER TABLE `city_driver_types` DISABLE KEYS */;
INSERT INTO `city_driver_types` VALUES (1,now(),now(),1,'WOMEN_ONLY',1,'{"penalizeDeclinedRides": true,"displayTitle":"Female Driver Mode","displaySubtitle":"You can enable booking Ride Austin Female Drivers","ineligibleGenderAlert":{"cancelTitle":"Not now","message":"Female driver mode is intended for passengers identifying as female. If you made a mistake in selecting your gender, please change it in settings.","enabled": true,"actionTitle":"Go to settings"},"unknownGenderAlert":{"cancelTitle":"Not now","message":"Female driver mode is intended for female passengers. To determine your eligibility for this mode, please confirm your gender identification.","enabled": true,"actionTitle":"Confirm gender"},"eligibleCategories": ["REGULAR","SUV","PREMIUM","LUXURY"],"eligibleGenders": ["FEMALE"],"cityValidationRequired":true}',31,1,'com.rideaustin.model.ride.CityDriverType$WomanOnlyConfiguration'),
                                       (2,now(),now(),1,'DIRECT_CONNECT',1,'{"searchHandlerClass":"com.rideaustin.service.DirectConnectSearchDriverHandler","cityValidationRequired":false,"shouldResetOnRedispatch":true}',31,2,'com.rideaustin.model.ride.CityDriverType$DefaultDriverTypeConfiguration'),
                                       (3,now(),now(),1,'FINGERPRINTED',1,'{"searchHandlerClass":"com.rideaustin.service.DefaultSearchDriverHandler", "menuTitle":"Fingerprinted Drivers Mode", "menuIconUrl":"", "iconUrl":"", "title":"Fingerprinted Drivers", "subtitle":"By enabling the Fingerprinted Drivers mode, you\'ll get only Fingerprinted Drivers. When this mode is not enabled, you can get both Fingerprinted and non Fingerprinted Drivers","exclusive":false}',31,4,'com.rideaustin.model.ride.CityDriverType$FingerprintedDriverTypeConfiguration');
/*!40000 ALTER TABLE `city_driver_types` ENABLE KEYS */;
UNLOCK TABLES;

DROP TABLE IF EXISTS `configuration_items`;
CREATE TABLE `configuration_items` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `city_id` bigint(20) DEFAULT NULL,
  `client_type` varchar(15) DEFAULT NULL,
  `configuration_key` varchar(255) DEFAULT NULL,
  `configuration_value` varchar(4096) DEFAULT NULL,
  `is_default` bit(1) NOT NULL DEFAULT b'1',
  `environment` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=92 DEFAULT CHARSET=utf8;

LOCK TABLES `configuration_items` WRITE;
/*!40000 ALTER TABLE `configuration_items` DISABLE KEYS */;
INSERT INTO `configuration_items` VALUES 
                                         (1,1,'DRIVER','referFriend','{"title":"Refer a Friend","smsEnabled":false,"emailEnabled":false,"header":"Receive a $125 bonus","body":"Refer a driver, and you\'ll each receive a $125 bonus after your friend completes 20 trips with"}',1,NULL),
                                         (3,NULL,'DRIVER','womanOnly','{"enabled":true,"ineligibleGenderAlert":{"cancelTitle":"Not now","message":"Female driver mode is intended for passengers identifying as female. If you made a mistake in selecting your gender, please change it in settings.","enabled":true,"actionTitle":"Go to settings"},"displayTitle":"Female Driver Mode","unknownGenderAlert":{"cancelTitle":"Not now","message":"Female driver mode is intended for female passengers. To determine your eligibility for this mode, please confirm your gender identification.","enabled":true,"actionTitle":"Confirm gender"},"eligibleCategories":["REGULAR","SUV","PREMIUM"],"eligibleGenders":["FEMALE"],"displaySubtitle":"You can enable booking Ride Austin Female Drivers"}',1,NULL),
                                         (4,NULL,'DRIVER','tipping','{"enabled": true}',1,NULL),
                                         (5,NULL,'DRIVER','rideCancellation','{"rideCancellationPeriod":300}',1,NULL),
                                         (6,NULL,'DRIVER','smsMaskingEnabled','true',1,NULL),
                                         (7,NULL,'RIDER','womanOnly','{"enabled": true,"ineligibleGenderAlert":{"cancelTitle":"Not now","message":"Female driver mode is intended for passengers identifying as female. If you made a mistake in selecting your gender, please change it in settings.","enabled":true,"actionTitle":"Go to settings"},"displayTitle":"Female Driver Mode","unknownGenderAlert":{"cancelTitle":"Not now","message":"Female driver mode is intended for female passengers. To determine your eligibility for this mode, please confirm your gender identification.","enabled":true,"actionTitle":"Confirm gender"},"eligibleCategories": ["REGULAR","SUV","PREMIUM"],"eligibleGenders":["FEMALE"],"displaySubtitle":"You can enable booking Ride Austin Female Drivers"}',1,NULL),
                                         (8,NULL,'RIDER','smsMaskingEnabled','true',1,NULL),
                                         (9,NULL,'RIDER','rideCancellation','{"enabled": false}',1,NULL),
                                         (10,NULL,'RIDER','tipping','{"rideTipLimit":300,"ridePaymentDelay":86400}',1,NULL),
                                         (11,1,'RIDER','riderReferFriend','{"detailtexttemplate":" Every time a new RideAustin user signs up with your invite code, they\'ll receive $5 off their first fare. Once they take their first ride, you\'ll also get $5 off the fare on your next ride. Discounts expire after 7 days, and you can accumulate up to $500 of discounts in your account.","smsbodytemplate":"You should try RideAustin! Get <codeValue> in ride credit using my code <codeLiteral> Download the app at: <downloadUrl>","emailbodytemplate":"<p>You should try RideAustin! Get <codeValue> in ride credit using my code <b><codeLiteral><b>. Download the app at: <downloadUrl></p>","downloadUrl":"","enabled":true}',1,NULL),
                                         (12,1,'RIDER','generalInformation','{"applicationName":"Ride Austin","applicationNamePipe":"Ride|Austin","splashUrl":"","iconUrl":"","logoUrl":"","legalRider":"","legalDriver":"","privacyStatement":"","supportEmail":"","companyDomain":"","companyWebsite":"","facebookUrl":"","facebookUrlSchemeiOS":"","appstoreLink":"","playStoreLink":"","playStoreWeb":"","logoBlackUrl":""}',1,NULL),
                                         (13,1,'DRIVER','generalInformation','{"applicationName":"Ride Austin","applicationNamePipe":"Ride|Austin","splashUrl":"","iconUrl":"","logoUrl":"","supportEmail":"","companyDomain":"","companyWebsite":"","legalRider":"","legalDriver":"","privacyStatement":"","facebookUrl":"","facebookUrlSchemeiOS":"","appstoreLink":"","playStoreLink":"","playStoreWeb":"","logoBlackUrl":""}',1,NULL),
                                         (16,1,'RIDER','driverRegistration','{"description":"Thank you for choosing to drive with RideAustin. To qualify, drivers are required to be 21 years of age and have vehicles that are:","enabled":true,"driverRegistrationTerms":"", "supportEmail":"", "logoWhiteUrl":"", "requirements":["2001 or Newer","4 Door","Not salvaged or Re-built Vehicles"], "minCarYear":2001, "inspection_sticker":{"enabled":true,"sticker_required_year":3000,"header":"Registration Sticker","title1":"Take a photo of your Registration Sticker","text1":"Please make sure that we can easily read all the details."},"tnc_card":{"enabled":true,"backPhoto":true,"header":"Chauffeur\'s Permit","title1":"Chauffeur\'s Permit","action1":"If you have this, upload a picture","text1":"You’ll need a permit from the City Of Austin Ground Transportation Department. If you have this, upload a picture here:","title2":"Don\'t have one?","text2":"View guidelines","title1_back":"Chauffeur\'s Permit Backside","text1_back":"Please take a photo of the back of your permit"},"newCarSuccessMessage":"After completing the next steps, check your email for further instructions."}',0,NULL),
                                         (18,1,'RIDER','servicedZipCodes','{"servicedZipCodes":[]}',0,NULL),
                                         (20,1,'RIDER','geocodingConfiguration','{"pickupHints":[{"designatedPickup":{"lng":-97.667177,"lat":30.202779},"designatedPickups":[{"name":"Main Terminal Pillar B","driverCoord":{"lng":-97.667991,"lat":30.202772}},{"name":"Main Terminal Pillar D","driverCoord":{"lng":-97.667715,"lat":30.202777}},{"name":"Main Terminal Pillar F","driverCoord":{"lng":-97.66734,"lat":30.202791}},{"name":"Main Terminal Pillar G","driverCoord":{"lng":-97.66697499999999,"lat":30.202796}},{"name":"Main Terminal Pillar H","driverCoord":{"lng":-97.666578,"lat":30.202814}},{"name":"Main Terminal Pillar K","driverCoord":{"lng":-97.666218,"lat":30.202809}},{"name":"Main Terminal Pillar M","driverCoord":{"lng":-97.665929,"lat":30.202809}}],"name":"Austin Airport","areaPolygon":[{"lng":-97.661287,"lat":30.198735},{"lng":-97.661287,"lat":30.202642},{"lng":-97.67264,"lat":30.202642},{"lng":-97.67264,"lat":30.198735},{"lng":-97.661287,"lat":30.198735}]}]}',1,NULL),
                                         (22,1,'RIDER','accessibility','{"title":"Please contact Austin Cab Company","enabled":true,"phoneNumber":"+15124782222"}',0,NULL),
                                         (24,NULL,'RIDER','rides','{"distantPickUpNotificationThreshold":200,"rideSummaryDescription":"","rideSummaryDescriptionFreeCreditCharged":"Eligible credits have been applied."}',1,NULL),
                                         (25,NULL,'UNKNOWN','subscriptionTopics','[{"id":1,"name":"Austin Riders","description":"Austin Riders Subscription","arn":"","subscriptionPolicyClassName":"austinRidersPolicy"}, {"id":2,"name":"Austin Drivers","description":"Austin Drivers Subscription","arn":"","subscriptionPolicyClassName":"austinDriversPolicy"}]',0,'DEV'),
                                         (26,NULL,'UNKNOWN','snsApplications','[{"id":1,"arn":"","applicationPolicyClassName":"austinRiderIosApplicationPolicy"}, {"id":2,"arn":"","applicationPolicyClassName":"austinDriverIosApplicationPolicy"}]',0,'DEV'),
                                         (27,1,'DRIVER','driverEligibility','{"driverEligibility":["com.rideaustin.service.eligibility.checks.DriverTypeEligibilityCheck","com.rideaustin.service.eligibility.checks.CarIsSelectedEligibilityCheck","com.rideaustin.service.eligibility.checks.DriverExistsEligibilityCheck","com.rideaustin.service.eligibility.checks.ActiveDriverIsAvailableEligibilityCheck","com.rideaustin.service.eligibility.checks.CarCategoryEligibilityCheck","com.rideaustin.service.eligibility.checks.DriverIsActiveEligibilityCheck","com.rideaustin.service.eligibility.checks.DriverHasCarsEligibilityCheck"]}',0,NULL),
                                         (32,1,'DRIVER','driverMessages','{"iOSLocationSettingsAlwaysPrompt":"When online, please allow location access \'Always\' to avoid unexpected issues"}',1,NULL),
                                         (34,NULL,'DRIVER','commonMessages','{"networkTimeoutMessage":"You appear to have a poor connection. Keep calm and try again later." }',1,NULL),
                                         (35,NULL,'RIDER','commonMessages','{"networkTimeoutMessage":"You appear to have a poor connection. Keep calm and try again later." }',1,NULL),
                                         (36,1,'RIDER','riderLiveLocation','{"enabled":true,"requiredAccuracy":50,"expirationTime":5}',1,NULL),
                                         (38,1,'DRIVER','riderLiveLocation','{"enabled":true,"requiredAccuracy":50,"expirationTime":5}',1,NULL),
                                         (40,NULL,'RIDER','ratingConfiguration','{"defaultRating":5.0,"minimumRatingThreshold": 5,"limit":50}',1,NULL),
                                         (41,NULL,'DRIVER','ratingConfiguration','{"defaultRating":5.0,"minimumRatingThreshold": 5,"limit":50}',1,NULL),
                                         (42,1,'CONSOLE','surgeConfig','{"surgeMode":"MANUAL","utilizationThreshold":0.6,"defaultAreaMonitoringPeriod":1800000,"maxAuthorizedLimitedAutoValue":3.0,"surgeEquationMapping":{ "[-1000..99]":1,"[100..174]":1.25,"[175..199]":1.5,"[200..299]":1.75,"[300..349]":2.0,"[350..399]":2.25,"[400..449]":2.5,"[450..499]":2.75,"[500..599]":3.0,"[600..699]":3.5,"[700..799]":4.0,"[800..899]":4.5,"[900..2147483647]":5.0 },"surgeProvider":"STATS" }',1,NULL),
                                         (44,1,'CONSOLE','redispatchOnCancel','{"enabled":"false"}',1,NULL),
                                         (46,1,'DRIVER','offline','{"TERMS_NOT_ACCEPTED":"In order to go online you should read and accept new Driver terms and conditions","DRIVER_INACTIVE":"You have been marked as offline due to inactivity. Please go online again for additional requests.","MISSED_RIDES":"You have been marked as offline due to excessive consecutive missed ride requests.Please go online again for additional requests.","CAR_TYPES_DEACTIVATE":"Your previous selection was deactivated."}',1,NULL),
                                         (48,1,'DRIVER','rideUpgrade','{"buttonTitle":"Ask rider to upgrade to SUV","variants":[{"carCategory":"REGULAR","validUpgrades":["SUV"]},{"carCategory":"SUV","validUpgrades":[]},{"carCategory":"PREMIUM","validUpgrades":[]}]}',1,NULL),
                                         (50,1,'DRIVER','rideAcceptance','{"acceptancePeriod":10,"allowancePeriod":5,"totalWaitTime":90,"decisionThreshold":0,"latencyCoverage":3}',1,NULL),
                                         (52,1,'DRIVER','locationUpdateIntervals','{"whenOnTrip":2,"whenOnlineAndMoving":7,"whenOnlineAndNotMoving":7,"movementSpeed":7}',1,NULL),
                                         (54,1,'RIDER','riderEligibility','{"riderEligibility":["com.rideaustin.service.eligibility.checks.RiderGenderEligibilityCheck,com.rideaustin.service.eligibility.checks.RiderCarCategoryEligibilityCheck"]}',0,NULL),
                                         (57,1,'RIDER','unpaidBalance','{"enabled":true,"title":"Unpaid balance","subtitle":"You need to pay your balance to be able to take another trip","iconSmallURL":"","iconLargeURL":"","warningMessage":"You have unpaid rides"}',1,NULL),
                                         (58,1,'RIDER','promoCredits','{"showTotal": true,"showDetail": true,"description":"Terms & conditions apply. See original promotion for expiration term, and discounts applicable per trip.","title":"Credits Balance","subtitle":"Credits Available"}',1,NULL),
                                         (59,1,'DRIVER','geocodingConfiguration','{"pickupHints":[{"designatedPickup":{"lng":-97.667177,"lat":30.202779},"designatedPickups":[{"name":"Main Terminal Pillar B","driverCoord":{"lng":-97.667991,"lat":30.202772}},{"name":"Main Terminal Pillar D","driverCoord":{"lng":-97.667715,"lat":30.202777}},{"name":"Main Terminal Pillar F","driverCoord":{"lng":-97.66734,"lat":30.202791}},{"name":"Main Terminal Pillar G","driverCoord":{"lng":-97.66697499999999,"lat":30.202796}},{"name":"Main Terminal Pillar H","driverCoord":{"lng":-97.666578,"lat":30.202814}},{"name":"Main Terminal Pillar K","driverCoord":{"lng":-97.666218,"lat":30.202809}},{"name":"Main Terminal Pillar M","driverCoord":{"lng":-97.665929,"lat":30.202809}}],"name":"Austin Airport","areaPolygon":[{"lng":-97.661287,"lat":30.198735},{"lng":-97.661287,"lat":30.202642},{"lng":-97.67264,"lat":30.202642},{"lng":-97.67264,"lat":30.198735},{"lng":-97.661287,"lat":30.198735}]}]}',1,NULL),
                                         (62,1,'CONSOLE','rideMessaging','{"enabled":true}',1,NULL),
                                         (63,1,'DRIVER','driverStats','{"enabled":true}',1,NULL),
                                         (65,1,'RIDER','directConnect','{"enabled":true,"title":"Direct Connect","description":"Direct Connect enables you to pair with your driver by simply entering their Driver ID.","placeholder":"Enter Driver ID","actionTitle":"Request Driver"}',0,NULL),
                                         (67,1,'DRIVER','directConnect','{"enabled":true,"title":"Direct Connect","description":"To connect with a Rider directly, please have the rider to input this ID","requiresChauffeur":true}',0,NULL),
                                         (69,1,'CONSOLE','driverStats','{"enabled":true}',1,NULL),
                                         (70,1,'CONSOLE','stackedRides','{"enabled":true,"endRideTimeThreshold":150,"dropoffExpectationTime":60,"forceRedispatchEnabled":true}',1,NULL),
                                         (72,1,'DRIVER','autoGoOffline','{"enabled":true, "backgroundWarningPeriod":10800,"backgroundMaximumPeriod":10830,"warningMessage":"You\'ve been away for a while. Tap to STAY ONLINE", "offlineBackgroundMessage":"You are now offline"}',1,NULL),
                                         (74,1,'CONSOLE','cancellationReasons','{"RIDER":{"CHANGE_BOOKING":"I needed to change booking (time/place)","CHANGE_MIND":"I changed my mind","ANOTHER_RIDE":"I found another ride","MISTAKE":"I booked by mistake","TOO_LONG":"It was too long to wait","OTHER":"Other"},"DRIVER":{"NO_SHOW":"Rider did not show up","WRONG_GPS":"Wrong GPS location","TOO_MANY_RIDERS":"Too many riders for car type","OTHER":"Other"}}',1,NULL),
                                         (76,1,'CONSOLE','queue','{"inactiveBeforeLeave":10,"outOfAreaBeforeLeave":2,"inExclusionBeforeLeave":10,"maxDeclines":2,"penaltyTimeoutSeconds":900,"penaltyEnabled":true}',1,NULL),
                                         (78,1,'RIDER','cancellationFeedback','{"enabled":true,"cancellationThreshold":60}',1,NULL),
                                         (79,1,'DRIVER','cancellationFeedback','{"enabled":true}',1,NULL),
                                         (82,1,'CONSOLE','campaigns','{"trackerThreshold":0.6}',1,NULL),
                                         (84,1,'CONSOLE','stripe','{"cardPreauth":100,"applePayPreauth":3000}',1,NULL),
                                         (86,1,'CONSOLE','ridePayment','{"cancellationChargeFreePeriod":300,"tipLimit":300,"upfrontEnabled":false,"upfrontTimeout":180,"upfrontThreshold":5,"asyncPreauthEnabled":true}',1,NULL),
                                         (88,1,'DRIVER','driverActions','{"autoArriveDistanceToPickup":10,"autoEndDistanceToDestination":20,"allowArriveDistanceToPickup":500,"remindToArriveDistanceFromPickup":10}',1,NULL),
                                         (90,1,'CONSOLE','destinationUpdate','{"enabled":false,"limit":3}',1,NULL);
/*!40000 ALTER TABLE `configuration_items` ENABLE KEYS */;
UNLOCK TABLES;

DROP TABLE IF EXISTS `custom_payment`;
CREATE TABLE `custom_payment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_date` datetime NOT NULL,
  `updated_date` datetime NOT NULL,
  `administrator_id` bigint(20) NOT NULL,
  `driver_id` bigint(20) NOT NULL,
  `value` decimal(19,2) NOT NULL,
  `category` varchar(255) DEFAULT NULL,
  `description` varchar(511) DEFAULT NULL,
  `payment_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `documents`;
CREATE TABLE `documents` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_date` datetime NOT NULL,
  `updated_date` datetime NOT NULL,
  `document_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `document_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `notes` varchar(4096) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `city_id` bigint(20) DEFAULT NULL,
  `document_status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `removed` bit(1) DEFAULT b'0',
  `validity_date` date DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `driver_email_history`;
CREATE TABLE `driver_email_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_date` datetime NOT NULL,
  `updated_date` datetime NOT NULL,
  `actor` varchar(255) NOT NULL,
  `communication_type_id` bigint(20) NOT NULL,
  `driver_id` bigint(20) NOT NULL,
  `content` text,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `driver_email_reminders`;
CREATE TABLE `driver_email_reminders` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `subject` varchar(255) NOT NULL,
  `email_template` varchar(255) NOT NULL,
  `store_content` bit(1) NOT NULL DEFAULT b'0',
  `email_type` varchar(32) DEFAULT NULL,
  `city_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8;

LOCK TABLES `driver_email_reminders` WRITE;
/*!40000 ALTER TABLE `driver_email_reminders` DISABLE KEYS */;
INSERT INTO `driver_email_reminders` VALUES
                                            (3,'Custom','','custom_email.ftl',1,'SUPPORT',NULL),
                                            (4,'Payoneer sign up','Payoneer sign up','payoneer_sign_up.ftl',0,'ONBOARDING',NULL),
                                            (5,'Out of State Driving History','Out of State Driving History','out_of_state_driving_history.ftl',0,'DOCUMENTS',NULL),
                                            (6,'Activation email','Driver activation email','activation_email_austin.ftl',0,'DRIVERS',1),
                                            (8,'Rejection Notice','Rejection Notice','rejected_application.ftl',1,'ONBOARDING',NULL),
                                            (9,'Missing Documents','Missing Documents','missing_documents.ftl',0,'DOCUMENTS',NULL),
                                            (10,'Driving History','Driving History','missing_driving_history.ftl',0,'DOCUMENTS',NULL),
                                            (11,'Account Suspension','Account Suspension','account_suspension.ftl',1,'ONBOARDING',NULL),
                                            (12,'Account Deactivation','Account Deactivation','account_deactivation.ftl',0,'ONBOARDING',NULL);
/*!40000 ALTER TABLE `driver_email_reminders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `driver_statistics`
--

DROP TABLE IF EXISTS `driver_statistics`;
CREATE TABLE `driver_statistics` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `driver_id` bigint(20) NOT NULL,
  `created_date` datetime NOT NULL,
  `updated_date` datetime NOT NULL,
  `version` int(11) DEFAULT NULL,
  `accepted_count` int(11) NOT NULL,
  `last_accepted_count` int(11) NOT NULL DEFAULT '0',
  `last_accepted_over` int(11) NOT NULL DEFAULT '0',
  `cancelled_count` int(11) NOT NULL,
  `last_cancelled_count` int(11) NOT NULL DEFAULT '0',
  `last_cancelled_over` int(11) NOT NULL DEFAULT '0',
  `completed_count` int(11) NOT NULL,
  `dispatch_count` int(11) NOT NULL DEFAULT '0',
  `declined_count` int(11) NOT NULL DEFAULT '0',
  `received_count` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `driver_statistics_driver_id_UNIQUE` (`driver_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `driver_types`;
CREATE TABLE `driver_types` (
  `name` varchar(255) NOT NULL,
  `created_date` datetime NOT NULL,
  `updated_date` datetime NOT NULL,
  `description` varchar(255) NOT NULL,
  `car_categories_bitmask` int(11) NOT NULL DEFAULT '1',
  `bitmask` int(11) NOT NULL,
  `enabled` bit(1) NOT NULL DEFAULT b'1',
  `configuration` varchar(512) DEFAULT NULL,
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `driver_types` (`name`,`created_date`,`updated_date`,`description`,`car_categories_bitmask`,`bitmask`,
                            `enabled`,`configuration`)
                            VALUES ('DIRECT_CONNECT',now(),now(),'Direct Connect enables you to pair with your driver by simply entering their Driver ID.',31,2,1,NULL),
                                   ('FINGERPRINTED',now(),now(),'You can choose to book rides with only Fingerprinted drivers',31,4,1,NULL),
                                   ('WOMEN_ONLY',now(),now(),'WOMEN ONLY',31,1,1,'{"penalizeDeclinedRides":true}');

DROP TABLE IF EXISTS `drivers`;
CREATE TABLE `drivers` (
  `agreement_date` datetime NOT NULL,
  `insurance_expiry_date` date DEFAULT NULL,
  `insurance_picture_url` varchar(255) DEFAULT NULL,
  `payoneer_id` varchar(255) DEFAULT NULL,
  `payoneer_status` varchar(255) DEFAULT NULL,
  `rating` double DEFAULT NULL,
  `ssn` varchar(255) NOT NULL,
  `id` bigint(20) NOT NULL,
  `license_number` varchar(255) DEFAULT NULL,
  `license_state` varchar(32) DEFAULT NULL,
  `activation_date` date DEFAULT NULL,
  `inspection_status` varchar(32) NOT NULL DEFAULT 'NOT_INSPECTED',
  `inspection_notes` text,
  `activation_status` varchar(32) DEFAULT NULL,
  `activation_notes` text,
  `granted_driver_types_bitmask` int(11) DEFAULT NULL,
  `city_id` bigint(20) NOT NULL DEFAULT '1',
  `city_approval_status` varchar(32) NOT NULL DEFAULT 'PENDING',
  `onboarding_status` varchar(32) NOT NULL DEFAULT 'PENDING',
  `onboarding_pending_since` datetime DEFAULT NULL,
  `direct_connect_id` varchar(32) DEFAULT NULL,
  `special_flags` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_dcid` (`direct_connect_id`),
  KEY `fk_drivers_city` (`city_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `drivers_aud`;
CREATE TABLE `drivers_aud` (
  `revision` bigint(20) NOT NULL AUTO_INCREMENT,
  `id` bigint(20) NOT NULL,
  `revision_date` datetime DEFAULT NULL,
  `username` varchar(255) CHARACTER SET utf8 DEFAULT NULL,
  `agreement_date` datetime DEFAULT NULL,
  `ssn` varchar(255) CHARACTER SET utf8 DEFAULT NULL,
  `license_number` varchar(255) CHARACTER SET utf8 DEFAULT NULL,
  `license_state` varchar(32) CHARACTER SET utf8 DEFAULT NULL,
  `rating` double DEFAULT NULL,
  `payoneer_id` varchar(255) CHARACTER SET utf8 DEFAULT NULL,
  `payoneer_status` varchar(255) CHARACTER SET utf8 DEFAULT NULL,
  `payoneer_signup_url` varchar(255) CHARACTER SET utf8 DEFAULT NULL,
  `activation_date` date DEFAULT NULL,
  `agreed_to_legal_terms` tinyint(1) DEFAULT NULL,
  `city_approval_status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `activation_status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `activation_notes` text COLLATE utf8mb4_unicode_ci,
  `onboarding_status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `onboarding_pending_since` datetime DEFAULT NULL,
  `granted_driver_types_bitmask` int(11) DEFAULT NULL,
  `enabled_request_types_bitmask` int(11) DEFAULT NULL,
  `city_id` bigint(20) DEFAULT NULL,
  `direct_connect_id` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`revision`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS `events`;
CREATE TABLE `events` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `avatar_id` bigint(20) DEFAULT NULL,
  `avatar_type` varchar(255) DEFAULT NULL,
  `created_on` datetime DEFAULT NULL,
  `event_type` varchar(255) DEFAULT NULL,
  `expires_on` datetime DEFAULT NULL,
  `ride_id` bigint(20) DEFAULT NULL,
  `message` varchar(255) DEFAULT NULL,
  `parameters` varchar(61400) CHARACTER SET ascii DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_avatar_id` (`avatar_id`),
  KEY `idx_avatartype` (`avatar_type`),
  KEY `fk_events_rides` (`ride_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `fare_payments`;
CREATE TABLE `fare_payments` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_date` datetime NOT NULL,
  `updated_date` datetime NOT NULL,
  `split_status` varchar(255) DEFAULT NULL,
  `main_rider` bit(1) DEFAULT b'0',
  `rider_id` bigint(20) DEFAULT NULL,
  `ride_id` bigint(20) DEFAULT NULL,
  `charge_id` varchar(255) DEFAULT NULL,
  `free_credit_used` decimal(19,2) DEFAULT NULL,
  `stripe_credit_charged` decimal(19,2) DEFAULT NULL,
  `card_id` bigint(20) DEFAULT NULL,
  `provider` varchar(32) DEFAULT NULL,
  `charge_scheduled` datetime DEFAULT NULL,
  `payment_status` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_fare_payments_ride` (`ride_id`),
  KEY `fk_fare_payments_rider` (`rider_id`),
  KEY `fk_fare_payments_rider_card` (`card_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `geo_log`;
CREATE TABLE `geo_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_date` datetime NOT NULL,
  `updated_date` datetime NOT NULL,
  `location_lat` double NOT NULL,
  `location_lng` double NOT NULL,
  `rider_id` bigint(20) NOT NULL,
  `event` varchar(255) NOT NULL,
  `car_category` varchar(255) DEFAULT 'REGULAR',
  PRIMARY KEY (`id`),
  KEY `idx_geo_log_rider_id` (`rider_id`),
  KEY `geo_log_created_date_index` (`created_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `lost_and_found_requests`;
CREATE TABLE `lost_and_found_requests` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_date` datetime NOT NULL,
  `updated_date` datetime NOT NULL,
  `type` varchar(8) DEFAULT NULL,
  `requested_by` bigint(20) DEFAULT NULL,
  `content` varchar(2048) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_requested_by` (`requested_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `password_verification_tokens`;
CREATE TABLE `password_verification_tokens` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_date` datetime NOT NULL,
  `updated_date` datetime NOT NULL,
  `token` varchar(64) NOT NULL,
  `expires_on` datetime NOT NULL,
  `email` varchar(64) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `pwv_tokens_token_idx` (`token`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `phone_verification_items`;
CREATE TABLE `phone_verification_items` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_date` datetime NOT NULL,
  `updated_date` datetime NOT NULL,
  `phone_number` varchar(20) NOT NULL,
  `auth_token` varchar(40) NOT NULL,
  `verification_code` varchar(10) NOT NULL,
  `verified_on` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `promocode_redemptions`;
CREATE TABLE `promocode_redemptions` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_date` datetime NOT NULL,
  `updated_date` datetime NOT NULL,
  `promocode_id` bigint(20) DEFAULT NULL,
  `rider_id` bigint(20) DEFAULT NULL,
  `applied_to_owner` tinyint(1) NOT NULL DEFAULT '0',
  `active` bit(1) NOT NULL DEFAULT b'1',
  `original_value` decimal(19,2) NOT NULL DEFAULT '0.00',
  `remaining_value` decimal(19,2) NOT NULL DEFAULT '0.00',
  `valid_until` datetime DEFAULT NULL,
  `number_of_times_used` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_promocodes_redemptions_promocodes` (`promocode_id`),
  KEY `fk_promocodes_redemptions_riders` (`rider_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `promocode_usage_report`;
/*!50001 DROP VIEW IF EXISTS `promocode_usage_report`*/;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
/*!50001 CREATE VIEW `promocode_usage_report` AS SELECT
 1 AS `redemption_id`,
 1 AS `rider_id`,
 1 AS `ride_id`,
 1 AS `first_name`,
 1 AS `last_name`,
 1 AS `email`,
 1 AS `completed_on`,
 1 AS `code_literal`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary table structure for view `promocode_usages_count`
--

DROP TABLE IF EXISTS `promocode_usages_count`;
/*!50001 DROP VIEW IF EXISTS `promocode_usages_count`*/;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
/*!50001 CREATE VIEW `promocode_usages_count` AS SELECT
 1 AS `id`,
 1 AS `usages_count`*/;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `promocodes`
--

DROP TABLE IF EXISTS `promocodes`;
CREATE TABLE `promocodes` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_date` datetime NOT NULL,
  `updated_date` datetime NOT NULL,
  `code_literal` varchar(255) NOT NULL,
  `code_value` decimal(19,2) NOT NULL,
  `current_redemption` bigint(20) DEFAULT NULL,
  `ends_on` datetime DEFAULT NULL,
  `maximum_redemption` bigint(20) DEFAULT NULL,
  `new_riders_only` bit(1) NOT NULL,
  `promocode_type` varchar(255) NOT NULL,
  `starts_on` datetime DEFAULT NULL,
  `owner_id` bigint(20) DEFAULT NULL,
  `driver_id` bigint(20) DEFAULT NULL,
  `city_bitmask` int(11) DEFAULT NULL,
  `car_type_bitmask` int(11) DEFAULT NULL,
  `valid_for_number_of_rides` int(11) DEFAULT NULL,
  `valid_for_number_of_days` int(11) DEFAULT NULL,
  `use_end_date` datetime DEFAULT NULL,
  `next_trip_only` bit(1) NOT NULL DEFAULT b'1',
  `maximum_uses_per_account` int(11) DEFAULT NULL,
  `title` varchar(50) DEFAULT NULL,
  `applicable_to_fees` bit(1) NOT NULL DEFAULT b'0',
  `capped_amount_per_use` decimal(19,2) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_promocodes_code_literal` (`code_literal`),
  UNIQUE KEY `uq_promocodes_owner_id` (`owner_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `queued_rides`;
CREATE TABLE `queued_rides` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_date` datetime NOT NULL,
  `updated_date` datetime NOT NULL,
  `ride_id` bigint(20) NOT NULL,
  `token` varchar(60) NOT NULL,
  `expires_on` datetime DEFAULT NULL,
  `expired` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `queued_rides_token_uq_idx` (`token`),
  KEY `queued_rides_ride_id_idx` (`ride_id`),
  KEY `queued_rides_expires_on_idx` (`expires_on`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `rating_updates`;
CREATE TABLE `rating_updates` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_date` datetime NOT NULL,
  `updated_date` datetime NOT NULL,
  `rated_avatar_id` bigint(20) NOT NULL,
  `rated_by_avatar_id` bigint(20) NOT NULL,
  `ride_id` bigint(20) NOT NULL,
  `rating` double DEFAULT NULL,
  `comment` varchar(2000) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_rating_update_ride` (`ride_id`),
  KEY `fk_rating_update_avatar` (`rated_avatar_id`),
  KEY `fk_rating_update_avatar_by` (`rated_by_avatar_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `ride_calls`;
CREATE TABLE `ride_calls` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_date` datetime NOT NULL,
  `updated_date` datetime NOT NULL,
  `from` varchar(20) NOT NULL,
  `to` varchar(20) NOT NULL,
  `call_sid` varchar(160) NOT NULL,
  `type` varchar(10) NOT NULL,
  `ride_id` bigint(20) DEFAULT NULL,
  `processed` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`),
  KEY `idx_ride_calls_sid` (`call_sid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `ride_cancellation_feedback`;
CREATE TABLE `ride_cancellation_feedback` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_date` datetime NOT NULL,
  `updated_date` datetime NOT NULL,
  `ride_id` bigint(20) NOT NULL,
  `submitted_by` bigint(20) DEFAULT NULL,
  `reason` varchar(64) NOT NULL,
  `comment` varchar(4096) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `cancellation_feedback_ride_idx` (`ride_id`),
  KEY `cancellation_feedback_ride_user_idx` (`ride_id`,`submitted_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `ride_driver_dispatches`;
CREATE TABLE `ride_driver_dispatches` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_date` datetime NOT NULL,
  `updated_date` datetime NOT NULL,
  `dispatched_on` datetime DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `active_driver_id` bigint(20) DEFAULT NULL,
  `ride_id` bigint(20) DEFAULT NULL,
  `driving_time_to_rider` bigint(20) DEFAULT NULL,
  `dispatch_location_lat` double DEFAULT NULL,
  `dispatch_location_long` double DEFAULT NULL,
  `driving_distance_to_rider` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_driver_dispatches` (`ride_id`,`active_driver_id`),
  KEY `idx_ride_driver_dispatches_active_driver_id` (`active_driver_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `ride_trackers`;
CREATE TABLE `ride_trackers` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `distance_travelled` decimal(19,2) DEFAULT NULL,
  `latitude` double DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  `sequence` bigint(20) DEFAULT NULL,
  `speed` double DEFAULT NULL,
  `heading` double DEFAULT NULL,
  `course` double DEFAULT NULL,
  `tracked_on` datetime DEFAULT NULL,
  `ride_id` bigint(20) DEFAULT NULL,
  `valid` bit(1) DEFAULT b'1',
  PRIMARY KEY (`id`),
  KEY `idx_ride_trackers_ride_id` (`ride_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `ride_upgrade_requests`;
CREATE TABLE `ride_upgrade_requests` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_date` datetime NOT NULL,
  `updated_date` datetime NOT NULL,
  `requested_by` bigint(20) NOT NULL,
  `requested_from` bigint(20) NOT NULL,
  `ride_id` bigint(20) NOT NULL,
  `status` varchar(16) NOT NULL,
  `source` varchar(32) NOT NULL,
  `target` varchar(32) NOT NULL,
  `expires_on` datetime NOT NULL,
  `surge_factor` decimal(19,2) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `requested_by_idx` (`requested_by`),
  KEY `requested_from_idx` (`requested_from`),
  KEY `expires_on_idx` (`expires_on`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `rider_card_locks`;

CREATE TABLE `rider_card_locks` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_date` datetime NOT NULL,
  `updated_date` datetime NOT NULL,
  `card_fingerprint` varchar(255) NOT NULL,
  `ride_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_rider_card_locks_rides` (`ride_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `rider_cards`;
CREATE TABLE `rider_cards` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_date` datetime NOT NULL,
  `updated_date` datetime NOT NULL,
  `card_brand` varchar(255) NOT NULL,
  `card_expired` bit(1) NOT NULL,
  `card_number` varchar(4) NOT NULL,
  `stripe_card_id` varchar(255) NOT NULL,
  `rider_id` bigint(20) NOT NULL,
  `fingerprint` varchar(255) DEFAULT NULL,
  `removed` bit(1) NOT NULL DEFAULT b'0',
  `sync_date` datetime DEFAULT NULL,
  `last_failure_date` datetime DEFAULT NULL,
  `failed_charge_attempts` int(11) NOT NULL DEFAULT '0',
  `expiration_month` varchar(2) DEFAULT NULL,
  `expiration_year` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_rider_cards_rider_id` (`rider_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `rider_overrides`;
CREATE TABLE `rider_overrides` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_date` datetime NOT NULL,
  `updated_date` datetime NOT NULL,
  `first_name` varchar(64) DEFAULT NULL,
  `last_name` varchar(64) DEFAULT NULL,
  `phone_number` varchar(24) DEFAULT NULL,
  `overridden_id` bigint(20) NOT NULL,
  `ride_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `rider_overrides_ride_id_idx` (`ride_id`),
  KEY `rider_overrides_overridden_id_idx` (`overridden_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `riders`;
CREATE TABLE `riders` (
  `rating` double DEFAULT NULL,
  `round_up_fare` bit(1) DEFAULT NULL,
  `stripe_id` varchar(255) DEFAULT NULL,
  `id` bigint(20) NOT NULL,
  `primary_card_id` bigint(20) DEFAULT NULL,
  `charity_id` bigint(20) DEFAULT NULL,
  `payment_status` varchar(20) DEFAULT NULL,
  `city_id` bigint(20) NOT NULL DEFAULT '1',
  `dispatcher_account` bit(1) DEFAULT b'0',
  PRIMARY KEY (`id`),
  KEY `idx_riders_primary_card_id` (`primary_card_id`),
  KEY `fk_riders_charities` (`charity_id`),
  KEY `fk_riders_city` (`city_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `rides`;
CREATE TABLE `rides` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_date` datetime NOT NULL,
  `updated_date` datetime NOT NULL,
  `requested_on` datetime DEFAULT NULL,
  `base_fare` decimal(19,2) DEFAULT NULL,
  `booking_fee` decimal(19,2) DEFAULT NULL,
  `charge_id` varchar(255) DEFAULT NULL,
  `city_fee` decimal(19,2) DEFAULT NULL,
  `sub_total` decimal(19,2) DEFAULT NULL,
  `completed_on` datetime DEFAULT NULL,
  `distance_fare` decimal(19,2) DEFAULT NULL,
  `distance_travelled` decimal(19,2) DEFAULT NULL,
  `driver_rating` double DEFAULT NULL,
  `driver_reached_on` datetime DEFAULT NULL,
  `end_location_lat` double DEFAULT NULL,
  `end_location_long` double DEFAULT NULL,
  `estimated_fare` decimal(19,2) DEFAULT NULL,
  `minimum_fare` decimal(19,2) DEFAULT NULL,
  `pre_charge_id` varchar(255) DEFAULT NULL,
  `rate_per_mile` decimal(19,2) DEFAULT NULL,
  `rate_per_minute` decimal(19,2) DEFAULT NULL,
  `ride_map` varchar(255) DEFAULT NULL,
  `ride_map_minimized` varchar(255) DEFAULT NULL,
  `rider_rating` double DEFAULT NULL,
  `round_up_amount` decimal(19,2) DEFAULT NULL,
  `start_location_lat` double DEFAULT NULL,
  `start_location_long` double DEFAULT NULL,
  `started_on` datetime DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `time_fare` decimal(19,2) DEFAULT NULL,
  `total_fare` decimal(19,2) DEFAULT NULL,
  `active_driver_id` bigint(20) DEFAULT NULL,
  `rider_id` bigint(20) DEFAULT NULL,
  `driver_accepted_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `start_address` varchar(255) DEFAULT NULL,
  `end_address` varchar(255) DEFAULT NULL,
  `start_zip_code` varchar(32) DEFAULT NULL,
  `end_zip_code` varchar(32) DEFAULT NULL,
  `charity_id` bigint(20) DEFAULT NULL,
  `driver_payment` decimal(19,2) DEFAULT NULL,
  `requested_car_category` varchar(255) NOT NULL DEFAULT 'REGULAR',
  `free_credit_used` decimal(19,2) DEFAULT NULL,
  `stripe_credit_charged` decimal(19,2) DEFAULT NULL,
  `cancellation_fee` decimal(19,2) DEFAULT NULL,
  `driver_session_id` bigint(20) DEFAULT NULL,
  `rider_session_id` bigint(20) DEFAULT NULL,
  `surge_factor` decimal(19,2) DEFAULT '0.00',
  `surge_fare` decimal(19,2) DEFAULT NULL,
  `normal_fare` decimal(19,2) DEFAULT NULL,
  `cancelled_on` datetime DEFAULT NULL,
  `tipped_on` datetime DEFAULT NULL,
  `tip` decimal(19,2) DEFAULT NULL,
  `ra_payment` decimal(19,2) NOT NULL DEFAULT '0.00',
  `payment_status` varchar(20) DEFAULT NULL,
  `tracking_share_token` varchar(255) DEFAULT NULL,
  `requested_driver_type_bitmask` int(11) DEFAULT NULL,
  `city_id` bigint(20) NOT NULL DEFAULT '1',
  `start_area_id` bigint(20) DEFAULT NULL,
  `promocode_redemption_id` bigint(20) DEFAULT NULL,
  `processing_fee` decimal(19,2) DEFAULT NULL,
  `airport_id` bigint(20) DEFAULT NULL,
  `airport_fee` decimal(19,2) DEFAULT NULL,
  `apple_pay_token` varchar(255) DEFAULT NULL,
  `comment` text,
  PRIMARY KEY (`id`),
  KEY `idx_rides_active_driver_id` (`active_driver_id`),
  KEY `idx_rides_rider_id` (`rider_id`),
  KEY `fk_rides_charities` (`charity_id`),
  KEY `fk_rides_city` (`city_id`),
  KEY `idx_completed_on` (`completed_on`),
  KEY `idx_cancelled_on` (`cancelled_on`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `sessions`;
CREATE TABLE `sessions` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_date` datetime NOT NULL,
  `updated_date` datetime NOT NULL,
  `deleted` bit(1) DEFAULT NULL,
  `token_uuid` varchar(255) DEFAULT NULL,
  `user_agent` varchar(255) DEFAULT NULL,
  `user_platform` varchar(255) DEFAULT NULL,
  `user_device` varchar(255) DEFAULT NULL,
  `user_device_id` varchar(255) DEFAULT NULL,
  `user_device_other` varchar(255) DEFAULT NULL,
  `version` varchar(255) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `auth_token` varchar(255) DEFAULT NULL,
  `expires_on` datetime DEFAULT NULL,
  `api_client_app_type` int(11) DEFAULT NULL,
  `session_closing_reason` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `session_unique_auth_token` (`auth_token`),
  KEY `idx_sessions_user_id` (`user_id`),
  KEY `session_deleted_index` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `support_topic_forms`;
CREATE TABLE `support_topic_forms` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `topic_id` bigint(20) NOT NULL,
  `body` text,
  `field_content` text,
  `title` varchar(128) DEFAULT NULL,
  `header_text` varchar(128) DEFAULT NULL,
  `action_title` varchar(128) DEFAULT NULL,
  `action_type` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_topic_id` (`topic_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;

LOCK TABLES `support_topic_forms` WRITE;
/*!40000 ALTER TABLE `support_topic_forms` DISABLE KEYS */;
INSERT INTO `support_topic_forms` VALUES (1,51,'The best way to retrieve an item you may have left in a vehicle is to call the driver. Here\'s how:1. Scroll down and enter the phone number you would like to be contacted at. Tap submit.If you lost your personal phone, enter a friend\'s phone number instead.2. We\'ll call the number you enter to connect you directly with your driver\'s mobile number.If your driver picks up and confirms that your item has been found, coordinate a mutually convenient time and place to meet for its return to you.Please be considerate that your driver\'s personal schedule will be affected by taking time out to return your item to you.Drivers are independent contractors. Neither RideAustin nor drivers are responsible for the items left in a vehicle after a trip ends. We\'re here to help, but cannot guarantee that a driver has your item or can immediately deliver it to you.','[{"fieldTitle":"Phone number","fieldPlaceholder":"Enter the best phone number to reach you","fieldType":"phone","isMandatory":true,"variable":"phone"}]','Contact my driver about a lost item','Tell us more','Submit','lostandfound/contact'),
                                         (2,52,'Calling your driver to connect is the best way to retrieve an item you may have left in a vehicle. If you have not tried contacting your driver directly, head back and select "Contact my driver about a lost item".If more than 24 hours have passed since your trip ended and you\'re still unable to connect with your driver, we\'ll step in to help. Please share some details below.Drivers are independent contractors. Neither RideAustin nor drivers are responsible for the items left in a vehicle after a trip ends. We\'re here to help, but cannot guarantee that a driver has your item or can immediately deliver it to you.','[{"fieldTitle":"Item description","fieldPlaceholder":"Item description","fieldType":"text","isMandatory":true,"variable":"description"},{"fieldTitle":"Share details","fieldPlaceholder":"Where might your item be?","fieldType":"text","isMandatory":true,"variable":"details"},{"fieldTitle":"Can we share your number with the driver?","fieldPlaceholder":"","fieldType":"bool","isMandatory":true,"variable":"can_share_number"},{"fieldTitle":"Enter the best phone number to reach you","fieldPlaceholder":"(201) 555-5555","fieldType":"phone","isMandatory":true,"variable":"phone"}]','I couldn\'t reach my driver about a lost item','Tell us more','Submit','lostandfound/lost'),
                                         (3,3,'If you notice an item left behind, please let us know by sharing details and a photo here.We\'ll help you connect with the rider so that the two of you can arrange a mutually convenient time and place for a return. In the next 48 hours, the rider may reach out to you directly to recover the lost item.In the meantime, please keep the item safe.In the future, it\'s helpful to remind riders to take all their belongings as they exit your vehicle.','[{"fieldTitle":"When did you find this item?","fieldPlaceholder":"","fieldType":"date","isMandatory":true,"variable":"foundOn"},{"fieldTitle":"Do you know which ride this item belongs to?","fieldPlaceholder":"","fieldType":"text","isMandatory":true,"variable":"rideDescription"},{"fieldTitle":"Photo of lost item","fieldPlaceholder":"","fieldType":"photo","isMandatory":true,"variable":"image"},{"fieldTitle":"Can we share your number with the rider?","fieldPlaceholder":"Yes or No","fieldType":"bool","isMandatory":true,"variable":"sharingContactsAllowed"},{"fieldTitle":"Share details","fieldPlaceholder":"What item did you find? Where was it left in your vehicle","fieldType":"text","isMandatory":true,"variable":"details"}]','I found an item','Tell us more','Submit','lostandfound/found');
/*!40000 ALTER TABLE `support_topic_forms` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `support_topics`
--

DROP TABLE IF EXISTS `support_topics`;
CREATE TABLE `support_topics` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `description` varchar(2000) NOT NULL,
  `avatar_type` varchar(32) DEFAULT NULL,
  `parent_topic_id` bigint(20) DEFAULT NULL,
  `active` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`),
  KEY `fk_support_topics_parent` (`parent_topic_id`)
) ENGINE=InnoDB AUTO_INCREMENT=53 DEFAULT CHARSET=utf8;

LOCK TABLES `support_topics` WRITE;
/*!40000 ALTER TABLE `support_topics` DISABLE KEYS */;
INSERT INTO `support_topics` VALUES (1,'Trip fare related issues','DRIVER',NULL,1),
                                    (2,'Rider related issues','DRIVER',NULL,1),
                                    (3,'Lost item found','DRIVER',NULL,1),
                                    (4,'Vehicle issue or problems during the trip','DRIVER',NULL,1),
                                    (5,'Ride request problems','DRIVER',NULL,1),
                                    (6,'Application problem report','DRIVER',NULL,1),
                                    (7,'General issues','DRIVER',NULL,1),
                                    (8,'Please review my charge','RIDER',NULL,1),
                                    (9,'My driver was unprofessional','RIDER',NULL,1),
                                    (10,'I was in an accident','RIDER',NULL,1),
                                    (11,'I had issues with this trip','RIDER',NULL,1),
                                    (12,'I\'d like to request an adjustment','DRIVER',1,1),
                                    (13,'I have a question about the fare calculation','DRIVER',1,1),
                                    (14,'I\'d like to dispute a fare adjustment','DRIVER',1,1),
                                    (15,'I have a question about my payout','DRIVER',1,1),
                                    (16,'I\'d like to refund the rider','DRIVER',1,1),
                                    (17,'Other','DRIVER',1,1),
                                    (18,'Too many riders in my car','DRIVER',2,1),
                                    (19,'Rider behavior issue','DRIVER',2,1),
                                    (20,'The wrong rider took this trip','DRIVER',2,1),
                                    (21,'Other','DRIVER',2,1),
                                    (22,'I need to report an accident','DRIVER',4,1),
                                    (23,'Vehicle problem report','DRIVER',4,1),
                                    (24,'Cleaning fee request','DRIVER',4,1),
                                    (25,'I made multiple stops on this trip','DRIVER',4,1),
                                    (26,'Other','DRIVER',4,1),
                                    (27,'I am not receiving ride requests','DRIVER',5,1),
                                    (28,'Requests for difference vehicle options','DRIVER',5,1),
                                    (29,'Issues with Woman Only driver','DRIVER',5,1),
                                    (30,'Receiving ride requests during my trip','DRIVER',5,1),
                                    (31,'Other','DRIVER',5,1),
                                    (32,'I was charged a cancellation fee','RIDER',8,1),
                                    (33,'My promo code didn\'t apply','RIDER',8,1),
                                    (34,'I was charged a cleaning Fee','RIDER',8,1),
                                    (35,'I have a question about Priority Fare','RIDER',8,1),
                                    (36,'I have a different concern about my charge','RIDER',8,1),
                                    (37,'I can\'t view the trip\'s fare','RIDER',8,1),
                                    (38,'I was overcharged','RIDER',8,1),
                                    (39,'I lost an item','RIDER',NULL,1),
                                    (42,'My driver drove dangerously','RIDER',9,1),
                                    (43,'My driver asked for cash','RIDER',9,1),
                                    (44,'My driver was rude/had a poor attitude','RIDER',9,1),
                                    (45,'The wrong driver picked me up','RIDER',9,1),
                                    (46,'My driver was in the wrong vehicle','RIDER',9,1),
                                    (47,'My driver said/did something inapppropriate','RIDER',9,1),
                                    (48,'My driver took a poor route','RIDER',11,1),
                                    (49,'My driver didn\'t start or end my trip on time','RIDER',11,1),
                                    (50,'I did not take this trip','RIDER',11,1),
                                    (51,'Contact my driver about a lost item','RIDER',39,1),
                                    (52,'I couldn\'t reach my driver about a lost item','RIDER',39,1);
/*!40000 ALTER TABLE `support_topics` ENABLE KEYS */;
UNLOCK TABLES;

DROP TABLE IF EXISTS `support_topics_follow_up`;
CREATE TABLE `support_topics_follow_up` (
  `id` bigint(20) NOT NULL,
  `follow_up_type` varchar(32) NOT NULL,
  PRIMARY KEY (`id`,`follow_up_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

LOCK TABLES `support_topics_follow_up` WRITE;
/*!40000 ALTER TABLE `support_topics_follow_up` DISABLE KEYS */;
INSERT INTO `support_topics_follow_up` VALUES (1,'EMAIL'),
                                              (2,'EMAIL'),
                                              (3,'EMAIL'),
                                              (4,'EMAIL'),
                                              (5,'EMAIL'),
                                              (6,'EMAIL'),
                                              (7,'EMAIL'),
                                              (8,'EMAIL'),
                                              (9,'EMAIL'),
                                              (10,'EMAIL'),
                                              (11,'EMAIL'),
                                              (12,'EMAIL'),
                                              (13,'EMAIL'),
                                              (14,'EMAIL'),
                                              (15,'EMAIL'),
                                              (16,'EMAIL'),
                                              (17,'EMAIL'),
                                              (18,'EMAIL'),
                                              (19,'EMAIL'),
                                              (20,'EMAIL'),
                                              (21,'EMAIL'),
                                              (22,'EMAIL'),
                                              (23,'EMAIL'),
                                              (24,'EMAIL'),
                                              (25,'EMAIL'),
                                              (26,'EMAIL'),
                                              (27,'EMAIL'),
                                              (28,'EMAIL'),
                                              (29,'EMAIL'),
                                              (30,'EMAIL'),
                                              (31,'EMAIL'),
                                              (32,'EMAIL'),
                                              (33,'EMAIL'),
                                              (34,'EMAIL'),
                                              (35,'EMAIL'),
                                              (36,'EMAIL'),
                                              (37,'EMAIL'),
                                              (38,'EMAIL'),
                                              (39,'EMAIL'),
                                              (42,'EMAIL'),
                                              (43,'EMAIL'),
                                              (44,'EMAIL'),
                                              (45,'EMAIL'),
                                              (46,'EMAIL'),
                                              (47,'EMAIL'),
                                              (48,'EMAIL'),
                                              (49,'EMAIL'),
                                              (50,'EMAIL');
/*!40000 ALTER TABLE `support_topics_follow_up` ENABLE KEYS */;
UNLOCK TABLES;

DROP TABLE IF EXISTS `surge_areas`;
CREATE TABLE `surge_areas` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_date` datetime NOT NULL,
  `updated_date` datetime NOT NULL,
  `car_categories_bitmask` int(11) NOT NULL DEFAULT '7',
  `mandatory` bit(1) DEFAULT b'0',
  `automated` bit(1) DEFAULT b'0',
  `area_geometry_id` bigint(20) NOT NULL,
  `is_active` bit(1) DEFAULT b'1',
  `city_id` bigint(20) NOT NULL DEFAULT '1',
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `surge_areas_name_UNIQUE` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `surge_areas_history`;
CREATE TABLE `surge_areas_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_date` datetime NOT NULL,
  `updated_date` datetime NOT NULL,
  `zip_code` varchar(32) DEFAULT NULL,
  `surge_factor` decimal(19,2) DEFAULT '0.00',
  `recommended_surge_factor` decimal(19,2) DEFAULT '0.00',
  `number_of_requested_rides` int(11) DEFAULT '0',
  `number_of_completed_rides` int(11) DEFAULT '0',
  `number_of_eyeballs` int(11) DEFAULT '0',
  `number_of_cars` int(11) DEFAULT '0',
  `number_of_available_cars` int(11) DEFAULT '0',
  `car_categories_bitmask` int(11) NOT NULL DEFAULT '7',
  `automated` bit(1) DEFAULT b'0',
  `name` varchar(255) DEFAULT NULL,
  `top_left_corner_location_lat` double DEFAULT NULL,
  `top_left_corner_location_lng` double DEFAULT NULL,
  `bottom_right_corner_location_lat` double DEFAULT NULL,
  `bottom_right_corner_location_lng` double DEFAULT NULL,
  `center_point_lat` double DEFAULT NULL,
  `center_point_lng` double DEFAULT NULL,
  `csv_geometry` varchar(7500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `surge_area_id` bigint(20) DEFAULT NULL,
  `surge_factor_car_category` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_car_category_surge_id` (`surge_area_id`,`surge_factor_car_category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `surge_factors`;
CREATE TABLE `surge_factors` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_date` datetime NOT NULL,
  `updated_date` datetime NOT NULL,
  `surge_area_id` bigint(20) DEFAULT NULL,
  `car_type` varchar(255) NOT NULL,
  `value` decimal(19,2) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `surge_area_id` (`surge_area_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `terms`;

CREATE TABLE `terms` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_date` datetime NOT NULL,
  `updated_date` datetime NOT NULL,
  `publication_date` datetime NOT NULL,
  `city_id` bigint(20) NOT NULL,
  `current` bit(1) NOT NULL,
  `mandatory` bit(1) NOT NULL,
  `url` varchar(255) NOT NULL,
  `version` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

LOCK TABLES `terms` WRITE;
/*!40000 ALTER TABLE `terms` DISABLE KEYS */;
INSERT INTO `terms` VALUES (1,'2017-05-24 00:00:00','2017-05-24 00:00:00','2017-05-24 00:00:00',1,1,1,'','1.0');
/*!40000 ALTER TABLE `terms` ENABLE KEYS */;
UNLOCK TABLES;


DROP TABLE IF EXISTS `terms_acceptance`;
CREATE TABLE `terms_acceptance` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_date` datetime NOT NULL,
  `updated_date` datetime NOT NULL,
  `driver_id` bigint(20) DEFAULT NULL,
  `terms_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_driver_id_and_terms_id` (`driver_id`,`terms_id`),
  KEY `idx_acceptance_diver_id` (`driver_id`),
  KEY `idx_acceptance_term_id` (`terms_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `tokens`;
CREATE TABLE `tokens` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_date` datetime NOT NULL,
  `updated_date` datetime NOT NULL,
  `device_id` varchar(255) DEFAULT NULL,
  `environment` int(11) DEFAULT NULL,
  `type` int(11) DEFAULT NULL,
  `value` varchar(255) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `arn` varchar(255) DEFAULT NULL,
  `avatar_type` varchar(15) NOT NULL DEFAULT 'RIDER',
  `agent_city` varchar(20) NOT NULL DEFAULT 'AUSTIN',
  `application_id` bigint(20) NOT NULL DEFAULT '1',
  `topic_subscriptions` text,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_tokens_user_id_value` (`user_id`,`value`),
  KEY `idx_tokens_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `user_tracks`;
CREATE TABLE `user_tracks` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_date` datetime NOT NULL,
  `updated_date` datetime NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `utm_source` varchar(255) DEFAULT NULL,
  `utm_medium` varchar(255) DEFAULT NULL,
  `utm_campaign` varchar(255) DEFAULT NULL,
  `promo_code` varchar(255) DEFAULT NULL,
  `marketing_title` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_date` datetime NOT NULL,
  `updated_date` datetime NOT NULL,
  `photo_url` varchar(255) DEFAULT NULL,
  `avatar_types_bitmask` int(11) DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `enabled` bit(1) DEFAULT NULL,
  `facebook_id` varchar(255) DEFAULT NULL,
  `first_name` varchar(255) NOT NULL,
  `last_name` varchar(255) NOT NULL,
  `password` varchar(255) DEFAULT NULL,
  `phone_number` varchar(255) NOT NULL,
  `address` longtext,
  `zip_code` varchar(32) DEFAULT NULL,
  `date_of_birth` date DEFAULT NULL,
  `middle_name` varchar(255) DEFAULT NULL,
  `nick_name` varchar(255) DEFAULT NULL,
  `phone_number_verified` bit(1) NOT NULL DEFAULT b'1',
  `gender` varchar(8) NOT NULL DEFAULT 'UNKNOWN',
  `email_verified` bit(1) NOT NULL DEFAULT b'1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_users_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `city_restricted_areas`
(
    `id`           BIGINT   NOT NULL,
    `created_date` DATETIME NOT NULL,
    `updated_date` DATETIME NOT NULL,
    `city_id`      BIGINT   NOT NULL,
    `zone_id`      BIGINT   NOT NULL,
    `enabled`      BIT(1)   NOT NULL DEFAULT b'1',
    PRIMARY KEY (`id`),
    INDEX `city_id_idx` (`city_id` ASC)
);

ALTER TABLE `active_drivers` ADD CONSTRAINT `fk_active_drivers_city` FOREIGN KEY (`city_id`) REFERENCES `cities` (`id`);
ALTER TABLE `active_drivers` ADD CONSTRAINT `fk_active_drivers_drivers` FOREIGN KEY (`driver_id`) REFERENCES `drivers` (`id`);
ALTER TABLE `administrators` ADD CONSTRAINT `fk_administrators_avatars` FOREIGN KEY (`id`) REFERENCES `avatars` (`id`);
ALTER TABLE `avatar_documents` ADD CONSTRAINT `fk_avatar_documents_avatars` FOREIGN KEY (`avatar_id`) REFERENCES `avatars` (`id`);
ALTER TABLE `avatar_documents` ADD CONSTRAINT `fk_avatar_documents_documents` FOREIGN KEY (`document_id`) REFERENCES `documents` (`id`);
ALTER TABLE `avatar_email_notifications` ADD CONSTRAINT `fk_avatar_email_notifications_avatars` FOREIGN KEY (`avatar_id`) REFERENCES `avatars` (`id`);
ALTER TABLE `avatars` ADD CONSTRAINT `fk_avatars_user_users` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);
ALTER TABLE `car_documents` ADD CONSTRAINT `fk_car_documents_acars` FOREIGN KEY (`car_id`) REFERENCES `cars` (`id`);
ALTER TABLE `car_documents` ADD CONSTRAINT `fk_car_documents_documents` FOREIGN KEY (`document_id`) REFERENCES `documents` (`id`);
ALTER TABLE `cars` ADD CONSTRAINT `fk_cars_driver_drivers` FOREIGN KEY (`driver_id`) REFERENCES `drivers` (`id`);
ALTER TABLE `city_car_types` ADD CONSTRAINT `fk_city_car_types_car_type` FOREIGN KEY (`car_category`) REFERENCES `car_types` (`car_category`);
ALTER TABLE `city_car_types` ADD CONSTRAINT `fk_city_car_types_city` FOREIGN KEY (`city_id`) REFERENCES `cities` (`id`);
ALTER TABLE `city_driver_types` ADD CONSTRAINT `fk_city_driver_types_city` FOREIGN KEY (`city_id`) REFERENCES `cities` (`id`);
ALTER TABLE `drivers` ADD CONSTRAINT `fk_drivers_avatars` FOREIGN KEY (`id`) REFERENCES `avatars` (`id`);
ALTER TABLE `drivers` ADD CONSTRAINT `fk_drivers_city` FOREIGN KEY (`city_id`) REFERENCES `cities` (`id`);
ALTER TABLE `events` ADD CONSTRAINT `fk_events_rides` FOREIGN KEY (`ride_id`) REFERENCES `rides` (`id`);
ALTER TABLE `fare_payments` ADD CONSTRAINT `fk_fare_payments_ride` FOREIGN KEY (`ride_id`) REFERENCES `rides` (`id`);
ALTER TABLE `fare_payments` ADD CONSTRAINT `fk_fare_payments_rider` FOREIGN KEY (`rider_id`) REFERENCES `riders` (`id`);
ALTER TABLE `fare_payments` ADD CONSTRAINT `fk_fare_payments_rider_card` FOREIGN KEY (`card_id`) REFERENCES `rider_cards` (`id`);
ALTER TABLE `promocode_redemptions` ADD CONSTRAINT `idx_promocodes_redemptions_promocode_id` FOREIGN KEY (`promocode_id`) REFERENCES `promocodes` (`id`);
ALTER TABLE `promocode_redemptions` ADD CONSTRAINT `idx_promocodes_redemptions_rider_id` FOREIGN KEY (`rider_id`) REFERENCES `riders` (`id`);
ALTER TABLE `promocodes` ADD CONSTRAINT `idx_promocodes_owner_id` FOREIGN KEY (`owner_id`) REFERENCES `riders` (`id`);
ALTER TABLE `rating_updates` ADD CONSTRAINT `fk_rating_update_avatar` FOREIGN KEY (`rated_avatar_id`) REFERENCES `avatars` (`id`);
ALTER TABLE `rating_updates` ADD CONSTRAINT `fk_rating_update_avatar_by` FOREIGN KEY (`rated_by_avatar_id`) REFERENCES `avatars` (`id`);
ALTER TABLE `rating_updates` ADD CONSTRAINT `fk_rating_update_ride` FOREIGN KEY (`ride_id`) REFERENCES `rides` (`id`);
ALTER TABLE `ride_driver_dispatches` ADD CONSTRAINT `fk_ride_driver_dispatches_active_drivers` FOREIGN KEY (`active_driver_id`) REFERENCES `active_drivers` (`id`);
ALTER TABLE `ride_driver_dispatches` ADD CONSTRAINT `fk_ride_driver_dispatches_rides` FOREIGN KEY (`ride_id`) REFERENCES `rides` (`id`);
ALTER TABLE `ride_trackers` ADD CONSTRAINT `fk_ride_trackers_rides` FOREIGN KEY (`ride_id`) REFERENCES `rides` (`id`);
ALTER TABLE `rider_card_locks` ADD CONSTRAINT `idx_rider_card_locks_ride_id` FOREIGN KEY (`ride_id`) REFERENCES `rides` (`id`);
ALTER TABLE `rider_cards` ADD CONSTRAINT `fk_rider_cards_riders` FOREIGN KEY (`rider_id`) REFERENCES `riders` (`id`);
ALTER TABLE `riders` ADD CONSTRAINT `fk_riders_avatars` FOREIGN KEY (`id`) REFERENCES `avatars` (`id`);
ALTER TABLE `riders` ADD CONSTRAINT `fk_riders_charities` FOREIGN KEY (`charity_id`) REFERENCES `charities` (`id`);
ALTER TABLE `riders` ADD CONSTRAINT `fk_riders_city` FOREIGN KEY (`city_id`) REFERENCES `cities` (`id`);
ALTER TABLE `riders` ADD CONSTRAINT `fk_riders_rider_cards` FOREIGN KEY (`primary_card_id`) REFERENCES `rider_cards` (`id`);
ALTER TABLE `rides` ADD CONSTRAINT `fk_rides_active_drivers` FOREIGN KEY (`active_driver_id`) REFERENCES `active_drivers` (`id`);
ALTER TABLE `rides` ADD CONSTRAINT `fk_rides_charities` FOREIGN KEY (`charity_id`) REFERENCES `charities` (`id`);
ALTER TABLE `rides` ADD CONSTRAINT `fk_rides_city` FOREIGN KEY (`city_id`) REFERENCES `cities` (`id`);
ALTER TABLE `rides` ADD CONSTRAINT `fk_rides_riders` FOREIGN KEY (`rider_id`) REFERENCES `riders` (`id`);
ALTER TABLE `sessions` ADD CONSTRAINT `fk_sessions_users` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);
ALTER TABLE `support_topics` ADD CONSTRAINT `fk_support_topics_parent` FOREIGN KEY (`parent_topic_id`) REFERENCES `support_topics` (`id`);
ALTER TABLE `support_topics_follow_up` ADD CONSTRAINT `fk_support_topics_follow_up` FOREIGN KEY (`id`) REFERENCES `support_topics` (`id`);
ALTER TABLE `surge_factors` ADD CONSTRAINT `fk_surge_area_id` FOREIGN KEY (`surge_area_id`) REFERENCES `surge_areas` (`id`);
ALTER TABLE `tokens` ADD CONSTRAINT `fk_tokens_users` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

/*!50003 DROP FUNCTION IF EXISTS `calculate_rating_average` */;
DELIMITER ;;
CREATE  FUNCTION `calculate_rating_average`(`default_rating` NUMERIC(3,2), `minimum_rating_threshold` INT, `limit` INT, `avatar_id` LONG) RETURNS decimal(3,2)
BEGIN
	DECLARE `rating_average` NUMERIC(3,2) ;
    DECLARE `total` NUMERIC(10);
    SET `total` =
		(   SELECT count(*)
			  FROM rating_updates
			 WHERE rated_avatar_id = `avatar_id`
		);

    IF(`total` < `minimum_rating_threshold`) THEN SET `rating_average` = `default_rating`;
    ELSE
		SET `rating_average` =
			( SELECT AVG(rating) as `rating_average`
				FROM (
						SELECT `rating`
						  FROM `rating_updates`
						 WHERE `rated_avatar_id` = `avatar_id`
					  ORDER BY `created_date` DESC, `id` DESC
						 LIMIT `limit`
					 ) AS `inner_ratings`
			);
	END IF;
	RETURN `rating_average`;
END ;;
DELIMITER ;

--
-- Final view structure for view `promocode_usage_report`
--

/*!50001 DROP VIEW IF EXISTS `promocode_usage_report`*/;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013  SQL SECURITY DEFINER */
/*!50001 VIEW `promocode_usage_report` AS (select `promocode_redemptions`.`id` AS `redemption_id`,`riders`.`id` AS `rider_id`,`rides`.`id` AS `ride_id`,`users`.`first_name` AS `first_name`,`users`.`last_name` AS `last_name`,`users`.`email` AS `email`,`rides`.`completed_on` AS `completed_on`,`promocodes`.`code_literal` AS `code_literal` from (((((`riders` join `avatars` on((`riders`.`id` = `avatars`.`id`))) join `users` on((`avatars`.`user_id` = `users`.`id`))) join `promocode_redemptions` on((`promocode_redemptions`.`rider_id` = `riders`.`id`))) join `promocodes` on((`promocode_redemptions`.`promocode_id` = `promocodes`.`id`))) left join `rides` on((`rides`.`rider_id` = `riders`.`id`))) where ((`rides`.`status` = 'COMPLETED') or isnull(`rides`.`id`))) */;

--
-- Final view structure for view `promocode_usages_count`
--

/*!50001 DROP VIEW IF EXISTS `promocode_usages_count`*/;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013  SQL SECURITY DEFINER */
/*!50001 VIEW `promocode_usages_count` AS select `promocode`.`id` AS `id`,ifnull(count(`redemption`.`promocode_id`),0) AS `usages_count` from ((`promocodes` `promocode` left join `promocode_redemptions` `redemption` on((`redemption`.`promocode_id` = `promocode`.`id`))) left join `rides` `ride` on(((`ride`.`rider_id` = `redemption`.`rider_id`) and (`ride`.`status` = 'COMPLETED')))) group by `promocode`.`id` */;

DROP TABLE IF EXISTS qrtz_fired_triggers;
DROP TABLE IF EXISTS qrtz_paused_trigger_grps;
DROP TABLE IF EXISTS qrtz_scheduler_state;
DROP TABLE IF EXISTS qrtz_locks;
DROP TABLE IF EXISTS qrtz_simple_triggers;
DROP TABLE IF EXISTS qrtz_simprop_triggers;
DROP TABLE IF EXISTS qrtz_cron_triggers;
DROP TABLE IF EXISTS qrtz_blob_triggers;
DROP TABLE IF EXISTS qrtz_triggers;
DROP TABLE IF EXISTS qrtz_job_details;
DROP TABLE IF EXISTS qrtz_calendars;
CREATE TABLE qrtz_job_details (
                                  sched_name        VARCHAR(120) NOT NULL,
                                  job_name          VARCHAR(200) NOT NULL,
                                  job_group         VARCHAR(200) NOT NULL,
                                  description       VARCHAR(250) NULL,
                                  job_class_name    VARCHAR(250) NOT NULL,
                                  is_durable        VARCHAR(1)   NOT NULL,
                                  is_nonconcurrent  VARCHAR(1)   NOT NULL,
                                  is_update_data    VARCHAR(1)   NOT NULL,
                                  requests_recovery VARCHAR(1)   NOT NULL,
                                  job_data          BLOB         NULL,
                                  PRIMARY KEY (sched_name, job_name, job_group)
)
    ENGINE = InnoDB;
CREATE TABLE qrtz_triggers (
                               sched_name     VARCHAR(120) NOT NULL,
                               trigger_name   VARCHAR(200) NOT NULL,
                               trigger_group  VARCHAR(200) NOT NULL,
                               job_name       VARCHAR(200) NOT NULL,
                               job_group      VARCHAR(200) NOT NULL,
                               description    VARCHAR(250) NULL,
                               next_fire_time BIGINT(13)   NULL,
                               prev_fire_time BIGINT(13)   NULL,
                               priority       INTEGER      NULL,
                               trigger_state  VARCHAR(16)  NOT NULL,
                               trigger_type   VARCHAR(8)   NOT NULL,
                               start_time     BIGINT(13)   NOT NULL,
                               end_time       BIGINT(13)   NULL,
                               calendar_name  VARCHAR(200) NULL,
                               misfire_instr  SMALLINT(2)  NULL,
                               job_data       BLOB         NULL,
                               PRIMARY KEY (sched_name, trigger_name, trigger_group),
                               FOREIGN KEY (sched_name, job_name, job_group)
                                   REFERENCES qrtz_job_details (sched_name, job_name, job_group)
)
    ENGINE = InnoDB;
CREATE TABLE qrtz_simple_triggers (
                                      sched_name      VARCHAR(120) NOT NULL,
                                      trigger_name    VARCHAR(200) NOT NULL,
                                      trigger_group   VARCHAR(200) NOT NULL,
                                      repeat_count    BIGINT(7)    NOT NULL,
                                      repeat_interval BIGINT(12)   NOT NULL,
                                      times_triggered BIGINT(10)   NOT NULL,
                                      PRIMARY KEY (sched_name, trigger_name, trigger_group),
                                      FOREIGN KEY (sched_name, trigger_name, trigger_group)
                                          REFERENCES qrtz_triggers (sched_name, trigger_name, trigger_group)
)
    ENGINE = InnoDB;
CREATE TABLE qrtz_cron_triggers (
                                    sched_name      VARCHAR(120) NOT NULL,
                                    trigger_name    VARCHAR(200) NOT NULL,
                                    trigger_group   VARCHAR(200) NOT NULL,
                                    cron_expression VARCHAR(120) NOT NULL,
                                    time_zone_id    VARCHAR(80),
                                    PRIMARY KEY (sched_name, trigger_name, trigger_group),
                                    FOREIGN KEY (sched_name, trigger_name, trigger_group)
                                        REFERENCES qrtz_triggers (sched_name, trigger_name, trigger_group)
)
    ENGINE = InnoDB;
CREATE TABLE qrtz_simprop_triggers
(
    sched_name    VARCHAR(120)   NOT NULL,
    trigger_name  VARCHAR(200)   NOT NULL,
    trigger_group VARCHAR(200)   NOT NULL,
    str_prop_1    VARCHAR(512)   NULL,
    str_prop_2    VARCHAR(512)   NULL,
    str_prop_3    VARCHAR(512)   NULL,
    int_prop_1    INT            NULL,
    int_prop_2    INT            NULL,
    long_prop_1   BIGINT         NULL,
    long_prop_2   BIGINT         NULL,
    dec_prop_1    NUMERIC(13, 4) NULL,
    dec_prop_2    NUMERIC(13, 4) NULL,
    bool_prop_1   VARCHAR(1)     NULL,
    bool_prop_2   VARCHAR(1)     NULL,
    PRIMARY KEY (sched_name, trigger_name, trigger_group),
    FOREIGN KEY (sched_name, trigger_name, trigger_group)
        REFERENCES qrtz_triggers (sched_name, trigger_name, trigger_group)
)
    ENGINE = InnoDB;
CREATE TABLE qrtz_blob_triggers (
                                    sched_name    VARCHAR(120) NOT NULL,
                                    trigger_name  VARCHAR(200) NOT NULL,
                                    trigger_group VARCHAR(200) NOT NULL,
                                    blob_data     BLOB         NULL,
                                    PRIMARY KEY (sched_name, trigger_name, trigger_group),
                                    INDEX (sched_name, trigger_name, trigger_group),
                                    FOREIGN KEY (sched_name, trigger_name, trigger_group)
                                        REFERENCES qrtz_triggers (sched_name, trigger_name, trigger_group)
)
    ENGINE = InnoDB;
CREATE TABLE qrtz_calendars (
                                sched_name    VARCHAR(120) NOT NULL,
                                calendar_name VARCHAR(200) NOT NULL,
                                calendar      BLOB         NOT NULL,
                                PRIMARY KEY (sched_name, calendar_name)
)
    ENGINE = InnoDB;
CREATE TABLE qrtz_paused_trigger_grps (
                                          sched_name    VARCHAR(120) NOT NULL,
                                          trigger_group VARCHAR(200) NOT NULL,
                                          PRIMARY KEY (sched_name, trigger_group)
)
    ENGINE = InnoDB;
CREATE TABLE qrtz_fired_triggers (
                                     sched_name        VARCHAR(120) NOT NULL,
                                     entry_id          VARCHAR(95)  NOT NULL,
                                     trigger_name      VARCHAR(200) NOT NULL,
                                     trigger_group     VARCHAR(200) NOT NULL,
                                     instance_name     VARCHAR(200) NOT NULL,
                                     fired_time        BIGINT(13)   NOT NULL,
                                     sched_time        BIGINT(13)   NOT NULL,
                                     priority          INTEGER      NOT NULL,
                                     state             VARCHAR(16)  NOT NULL,
                                     job_name          VARCHAR(200) NULL,
                                     job_group         VARCHAR(200) NULL,
                                     is_nonconcurrent  VARCHAR(1)   NULL,
                                     requests_recovery VARCHAR(1)   NULL,
                                     PRIMARY KEY (sched_name, entry_id)
)
    ENGINE = InnoDB;
CREATE TABLE qrtz_scheduler_state (
                                      sched_name        VARCHAR(120) NOT NULL,
                                      instance_name     VARCHAR(200) NOT NULL,
                                      last_checkin_time BIGINT(13)   NOT NULL,
                                      checkin_interval  BIGINT(13)   NOT NULL,
                                      PRIMARY KEY (sched_name, instance_name)
)
    ENGINE = InnoDB;
CREATE TABLE qrtz_locks (
                            sched_name VARCHAR(120) NOT NULL,
                            lock_name  VARCHAR(40)  NOT NULL,
                            PRIMARY KEY (sched_name, lock_name)
)
    ENGINE = InnoDB;
CREATE INDEX idx_qrtz_j_req_recovery ON qrtz_job_details (sched_name, requests_recovery);
CREATE INDEX idx_qrtz_j_grp ON qrtz_job_details (sched_name, job_group);
CREATE INDEX idx_qrtz_t_j ON qrtz_triggers (sched_name, job_name, job_group);
CREATE INDEX idx_qrtz_t_jg ON qrtz_triggers (sched_name, job_group);
CREATE INDEX idx_qrtz_t_c ON qrtz_triggers (sched_name, calendar_name);
CREATE INDEX idx_qrtz_t_g ON qrtz_triggers (sched_name, trigger_group);
CREATE INDEX idx_qrtz_t_state ON qrtz_triggers (sched_name, trigger_state);
CREATE INDEX idx_qrtz_t_n_state ON qrtz_triggers (sched_name, trigger_name, trigger_group, trigger_state);
CREATE INDEX idx_qrtz_t_n_g_state ON qrtz_triggers (sched_name, trigger_group, trigger_state);
CREATE INDEX idx_qrtz_t_next_fire_time ON qrtz_triggers (sched_name, next_fire_time);
CREATE INDEX idx_qrtz_t_nft_st ON qrtz_triggers (sched_name, trigger_state, next_fire_time);
CREATE INDEX idx_qrtz_t_nft_misfire ON qrtz_triggers (sched_name, misfire_instr, next_fire_time);
CREATE INDEX idx_qrtz_t_nft_st_misfire ON qrtz_triggers (sched_name, misfire_instr, next_fire_time, trigger_state);
CREATE INDEX idx_qrtz_t_nft_st_misfire_grp ON qrtz_triggers (sched_name, misfire_instr, next_fire_time, trigger_group, trigger_state);
CREATE INDEX idx_qrtz_ft_trig_inst_name ON qrtz_fired_triggers (sched_name, instance_name);
CREATE INDEX idx_qrtz_ft_inst_job_req_rcvry ON qrtz_fired_triggers (sched_name, instance_name, requests_recovery);
CREATE INDEX idx_qrtz_ft_j_g ON qrtz_fired_triggers (sched_name, job_name, job_group);
CREATE INDEX idx_qrtz_ft_jg ON qrtz_fired_triggers (sched_name, job_group);
CREATE INDEX idx_qrtz_ft_t_g ON qrtz_fired_triggers (sched_name, trigger_name, trigger_group);
CREATE INDEX idx_qrtz_ft_tg ON qrtz_fired_triggers (sched_name, trigger_group);
