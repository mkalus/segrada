package org.segrada.model.savedquery;

/**
 * Copyright 2016 Maximilian Kalus [segrada@auxnet.de]
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
 * Trivial Graph data model
 */
public class GraphData {
	public final int x;
	public final int y;

	public final boolean physics;

	/**
	 * Constructor
	 * @param x coordinate
	 * @param y coordinate
	 * @param physics physics of node works?
	 */
	public GraphData(int x, int y, boolean physics) {
		this.x = x;
		this.y = y;
		this.physics = physics;
	}

	@Override
	public String toString() {
		return "GraphData[" + x + ":" + y + (physics ? "": ";no physics") + "]";
	}
}
