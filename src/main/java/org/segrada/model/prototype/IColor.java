package org.segrada.model.prototype;

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
 * Color model interface
 */
public interface IColor extends SegradaEntity {
	void setTitle(String title);

	Integer getColor();
	void setColor(Integer color);

	/**
	 * @return something along the lines of #112233
	 */
	String getColorCode();

	/**
	 * set color along the lines of #abcdef
	 * @param colorCode string #abcdef or #ABCDEF or something similar - must contain 6 hex characters!
	 */
	void setColorCode(String colorCode);

	/**
	 * @return red part or 0, if color not set
	 */
	int getR();
	/**
	 * @return green part or 0, if color not set
	 */
	int getG();
	/**
	 * @return blue part or 0, if color not set
	 */
	int getB();
}
