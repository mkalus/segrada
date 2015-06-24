package org.segrada.model.util;

import java.io.Serializable;

/**
 * Copyright 2015 Maximilian Kalus [segrada@auxnet.de]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Tuple implementation to hold model/string pairs
 */
public class IdModelTuple implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * referenced id
	 */
	public final String id;

	/**
	 * model name
	 */
	public final String model;

	/**
	 * Constructor
	 */
	public IdModelTuple(String id, String model) {
		this.id = id;
		this.model = model;
	}
}
