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

import lombok.Data;
import io.lindb.lingo.common.model.AggregateType;

/**
 * SelectItem
 */
@Data
public class SelectItem {
	public enum Type {
		Normal,
		Sum,
		Min,
		Max,
	}

	private String name;
	private Type type;

	public SelectItem() {
	}

	public SelectItem(String name, Type type) {
		this.name = name;
		this.type = type;
	}

	public AggregateType getAggregateType() {
		switch (this.type) {
			case Sum:
				return AggregateType.Sum;
			case Min:
				return AggregateType.Min;
			case Max:
				return AggregateType.Max;
			default:
				return AggregateType.Last;
		}
	}
}
