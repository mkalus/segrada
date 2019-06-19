package org.segrada.service.util;

/**
 * Copyright 2015-2019 Maximilian Kalus [segrada@auxnet.de]
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
 * Lazy loaded object - used in orient db
 */
public abstract class AbstractLazyLoadedObject implements java.lang.reflect.InvocationHandler {
	/**
	 * Target object
	 */
	private Object target;

	/**
	 * Invoke object
	 * @param proxy
	 * @param method
	 * @param args
	 * @return
	 * @throws Throwable
	 */
	public Object invoke(Object proxy, java.lang.reflect.Method method, Object[] args) throws Throwable {
		if (target == null) {
			try {
				target = loadObject();
			} catch (Exception e) {
				return null;
			}
		}
		return method.invoke(target, args);
	}

	/**
	 * load object
	 * @return
	 */
	protected abstract Object loadObject();
}
