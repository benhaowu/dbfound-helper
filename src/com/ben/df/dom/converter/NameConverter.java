package com.ben.df.dom.converter;

import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.ResolvingConverter;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

/**
 * @author : wubenhao
 * @date : create in 2022/8/31
 */
public class NameConverter extends ResolvingConverter<String> {

    @Nullable
    @Override
    public String fromString(@Nullable String s, ConvertContext convertContext) {
            if(StringUtils.isNotEmpty(s)){
                return s;
            }
            return "";
    }

    @Nullable
    @Override
    public String toString(@Nullable String s, ConvertContext convertContext) {
        if(s == null){
            return "";
        }
        return s;
    }

    @NotNull
    @Override
    public Collection<? extends String> getVariants(ConvertContext convertContext) {
        return Collections.emptyList();
    }
}
