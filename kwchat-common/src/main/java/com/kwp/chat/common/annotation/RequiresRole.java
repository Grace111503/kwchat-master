package com.kwp.chat.common.annotation;

import java.lang.annotation.*;

/**
 * 角色注解
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresRole {

    /**
     * 角色编码
     */
    String[] value();

    /**
     * 逻辑关系（AND：同时拥有，OR：拥有其一即可）
     */
    Logical logical() default Logical.OR;

    enum Logical {
        AND, OR
    }
}