package com.gold.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by user on 2017/11/3.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public  @interface XStreamCDATA {
}
