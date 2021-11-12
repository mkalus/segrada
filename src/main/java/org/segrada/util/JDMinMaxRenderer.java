package org.segrada.util;

/**
 * Copyright 2015-2021 Maximilian Kalus [segrada@auxnet.de]
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
 * Helper class to output min/max values of jd
 */
public final class JDMinMaxRenderer {
    private JDMinMaxRenderer() throws InstantiationException {
        throw new InstantiationException("The class is not created for instantiation");
    }

    public static long min(long julianDate) {
        return julianDate <= 0 ? 0 : julianDate;
    }

    public static long max(long julianDate) {
        return julianDate >= 10000000 ? 10000000 : julianDate;
    }
}
