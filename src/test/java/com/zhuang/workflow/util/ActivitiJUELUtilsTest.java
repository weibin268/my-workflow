package com.zhuang.workflow.util;

import com.zhuang.workflow.impl.activiti.util.ActivitiJUELUtils;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class ActivitiJUELUtilsTest {

    @Test
    public void evaluateBooleanResult() {
        Map<String, Object> map=new HashMap<String, Object>();
        map.put("count", 1000);
        System.out.println(ActivitiJUELUtils.evaluateBooleanResult("${count>1000 || 1==1}", map));
    }
}