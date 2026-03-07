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
package com.github.nishgpt.chainexecutor.models.observability;

//TODO:: better description for each enum value
public enum ObservationVerbosity {
  //Will capture basic details only
  BASIC {
    @Override
    public <T> T accept(Visitor<T> visitor) {
      return visitor.visitBasic();
    }
  },
  //Will capture additional context of request, response etc.
  VERBOSE {
    @Override
    public <T> T accept(Visitor<T> visitor) {
      return visitor.visitVerbose();
    }
  },
  ;

  public abstract <T> T accept(final Visitor<T> visitor);

  public interface Visitor<T> {

    T visitBasic();

    T visitVerbose();
  }
}
