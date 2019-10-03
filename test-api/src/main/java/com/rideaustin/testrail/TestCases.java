package com.rideaustin.testrail;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate @Test methods to link with TC by ID
 * Created on 15/02/2018
 *
 * @author sdelaysam
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface TestCases {
    String[] value();
}
