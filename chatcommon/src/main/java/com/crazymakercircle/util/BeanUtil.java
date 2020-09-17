package com.crazymakercircle.util;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.cglib.beans.BeanGenerator;
import org.springframework.cglib.beans.BeanMap;

import java.beans.PropertyDescriptor;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BeanUtil
{


    public  static   <T> List<T> asList(T... nodeA)
    {
        return Arrays.asList(nodeA);
    }

}
