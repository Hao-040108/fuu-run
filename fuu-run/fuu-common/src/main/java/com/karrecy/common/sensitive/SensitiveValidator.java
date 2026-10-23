package com.karrecy.common.sensitive;

import com.github.houbb.sensitive.word.core.SensitiveWordHelper;
import com.karrecy.common.utils.StringUtils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * 自定义敏感词校验注解实现
 */
public class SensitiveValidator implements ConstraintValidator<SensitiveWord, String> {

    /**
     * 平台核心业务词：不作为敏感词拦截（代购/代拿/跑腿是任务类型）
     */
    private static final String[] ALLOWED_BUSINESS_WORDS = {"代购", "代拿", "跑腿"};

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringUtils.isBlank(value)) {
            return true;
        }
        String checkValue = value;
        for (String word : ALLOWED_BUSINESS_WORDS) {
            checkValue = checkValue.replace(word, "");
        }
        if (SensitiveWordHelper.contains(checkValue)) {
            String first = SensitiveWordHelper.findFirst(checkValue);
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("内容包含敏感词-"+first)
                    .addConstraintViolation();
            return false;
        }
        return true;
    }

}
