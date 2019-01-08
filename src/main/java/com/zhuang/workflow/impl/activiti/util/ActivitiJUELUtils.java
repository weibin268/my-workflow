package com.zhuang.workflow.impl.activiti.util;

import java.util.Map;
import java.util.Map.Entry;

import org.activiti.engine.impl.javax.el.ExpressionFactory;
import org.activiti.engine.impl.javax.el.ValueExpression;
import org.activiti.engine.impl.juel.ExpressionFactoryImpl;
import org.activiti.engine.impl.juel.SimpleContext;

public class ActivitiJUELUtils {

	public static boolean evaluateBooleanResult(String expression, Map<String, Object> params) {
		ExpressionFactory factory = new ExpressionFactoryImpl();
		SimpleContext context = new SimpleContext();

		for (Entry<String, Object> entry : params.entrySet()) {
			if (expression.contains(entry.getKey())) {
				context.setVariable(entry.getKey(),
						factory.createValueExpression(entry.getValue(), entry.getValue().getClass()));
			}
		}
		
		ValueExpression e = factory.createValueExpression(context, expression, boolean.class);
		
		return (Boolean) e.getValue(context);
	}
}
