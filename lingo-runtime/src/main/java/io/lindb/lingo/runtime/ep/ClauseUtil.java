/**
 * Licensed to LinDB under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. LinDB licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.lindb.lingo.runtime.ep;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

import com.espertech.esper.common.client.soda.CountProjectionExpression;
import com.espertech.esper.common.client.soda.EPStatementObjectModel;
import com.espertech.esper.common.client.soda.Expression;
import com.espertech.esper.common.client.soda.ExpressionPrecedenceEnum;
import com.espertech.esper.common.client.soda.MaxProjectionExpression;
import com.espertech.esper.common.client.soda.MinProjectionExpression;
import com.espertech.esper.common.client.soda.SelectClause;
import com.espertech.esper.common.client.soda.SelectClauseElement;
import com.espertech.esper.common.client.soda.SelectClauseExpression;
import com.espertech.esper.common.client.soda.SumProjectionExpression;

/**
 * ClauseUtil
 */
public class ClauseUtil {
	private ClauseUtil() {
	}

	public static Map<String, SelectItem> buildSelectCluase(EPStatementObjectModel model) {
		Map<String, SelectItem> result = new HashMap<>();
		SelectClause selectClause = model.getSelectClause();
		List<SelectClauseElement> selectList = selectClause.getSelectList();

		for (SelectClauseElement selectItem : selectList) {
			if (selectItem instanceof SelectClauseExpression) {
				SelectClauseExpression selectClauseExpr = (SelectClauseExpression) selectItem;

				String name = selectClauseExpr.getAsName();
				Expression expr = selectClauseExpr.getExpression();
				if (StringUtils.isEmpty(name)) {
					name = buildExpression(expr);
				}
				if (expr instanceof CountProjectionExpression) {
					result.put(name, new SelectItem(name, SelectItem.Type.Sum));
				} else if (expr instanceof SumProjectionExpression) {
					result.put(name, new SelectItem(name, SelectItem.Type.Sum));
				} else if (expr instanceof MinProjectionExpression) {
					result.put(name, new SelectItem(name, SelectItem.Type.Min));
				} else if (expr instanceof MaxProjectionExpression) {
					result.put(name, new SelectItem(name, SelectItem.Type.Max));
				} else {
					result.put(name, new SelectItem(name, SelectItem.Type.Normal));
				}
			}
		}
		return result;
	}

	public static void buildGroupByClause(EPStatementObjectModel model) {

	}

	private static String buildExpression(Expression expression) {
		StringWriter writer = new StringWriter();
		expression.toEPL(writer, ExpressionPrecedenceEnum.MINIMUM);
		return writer.toString();
	}
}
