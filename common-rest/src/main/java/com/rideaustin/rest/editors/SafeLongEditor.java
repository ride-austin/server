package com.rideaustin.rest.editors;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.propertyeditors.CustomNumberEditor;

/**
 * This class is intended to mute binding errors that may arise from passing string values for filling numeric fields.
 * If passed value is not numeric, it will be superseded by default value provided as a constructor parameter.
 */
public class SafeLongEditor extends CustomNumberEditor {

  private Long defaultValue;

  public SafeLongEditor() {
    this(Long.MIN_VALUE);
  }

  public SafeLongEditor(Long defaultValue) {
    this(Long.class, false, defaultValue);
  }

  private SafeLongEditor(Class<? extends Number> numberClass, boolean allowEmpty, Long defaultValue) {
    super(numberClass, allowEmpty);
    this.defaultValue = defaultValue;
  }

  @Override
  public void setAsText(String text) {
    if (NumberUtils.isNumber(text)) {
      super.setAsText(text);
    } else {
      setValue(defaultValue);
    }
  }
}