package com.magic.microspider.util.annotation;

import java.lang.annotation.*;

/**
 * Created by liunn on 2018/1/11.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ParamValidator {

    /**
     * 是否必填
     * @return
     */
    boolean required() default false;

    /**
     * 是否数字,包含整数和小数
     * @return
     */
    boolean isNumber() default false;

    /**
     * 正整数(包含0)
     * @return
     */
    boolean isUnsignInt() default false;

    /**
     * 字段名称
     * @return
     */
    String name() default "";

    /**
     * 正则表达式
     * @return
     */
    String regex() default "";


    /**
     * 日期验证
     * @return
     */
    String dateFormat() default "";

    /**
     * 最小长度
     * @return
     */
    int minLength() default 0;


    /**
     * 最大长度
     * @return
     */
    int maxLength() default 0;
}
