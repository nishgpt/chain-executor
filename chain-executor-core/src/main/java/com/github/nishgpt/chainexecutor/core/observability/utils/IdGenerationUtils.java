/*
 * Copyright(c) 2023 Nishant Gupta (nishant141077@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.nishgpt.chainexecutor.core.observability.utils;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import java.security.SecureRandom;

public class IdGenerationUtils {

  public static final String OBSERVATION_ID_PREFIX = "OB";
  public static final String OBSERVATION_GROUP_ID_PREFIX = "OG";
  private static final char[] ALLOWED_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
  private static final int SIZE = 10;
  private static final SecureRandom RANDOM = new SecureRandom();

  private IdGenerationUtils() {
  }

  public static String getId(final String prefix) {
    return String.format("%s%s", prefix, NanoIdUtils.randomNanoId(RANDOM, ALLOWED_CHARS, SIZE));
  }
}
