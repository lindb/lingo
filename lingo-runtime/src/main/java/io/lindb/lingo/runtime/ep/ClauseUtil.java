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
