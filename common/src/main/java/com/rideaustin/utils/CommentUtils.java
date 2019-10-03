package com.rideaustin.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ImmutableSet;
import com.optimaize.langdetect.DetectedLanguage;
import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import com.rideaustin.rest.exception.BadRequestException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommentUtils {

  private static final List<LanguageProfile> PROFILES = new ArrayList<>();
  private static final Set<String> FORBIDDEN_LANGUAGES = ImmutableSet.of(
    "ar", "be", "bg", "fa", "he", "ja", "km", "kn", "ko", "ru", "sr", "ta",
    "te", "th", "uk", "ur", "vi", "yi", "zh-cn", "zh-tw"
  );
  private static LanguageDetector languageDetector;

  static {
    try {
      PROFILES.addAll(new LanguageProfileReader().readAllBuiltIn());
      languageDetector = LanguageDetectorBuilder.create(NgramExtractors.standard())
        .withProfiles(PROFILES)
        .build();
    } catch (IOException e) {
      log.error("Failed to read language profiles", e);
    }
  }

  private CommentUtils() {}

  /**
   * Fallback to true if failed to determine readability
   * @param comment comment text
   * @return is readable
   */
  public static boolean isReadable(String comment) {
    if (CollectionUtils.isEmpty(PROFILES) || StringUtils.isBlank(comment)) {
      return true;
    }

    List<DetectedLanguage> probabilities = languageDetector.getProbabilities(comment);
    if (probabilities.isEmpty()) {
      return true;
    }
    Set<String> probableLanguages = probabilities
      .stream()
      .map(lang -> lang.getLocale().getLanguage())
      .collect(Collectors.toSet());
    return CollectionUtils.intersection(probableLanguages, FORBIDDEN_LANGUAGES).isEmpty();
  }

  public static void validateComment(String comment) throws BadRequestException {
    boolean readable = CommentUtils.isReadable(comment);
    if (!readable) {
      throw new BadRequestException("Your comment seems to be unreadable or uninformative to driver");
    }
  }
}
