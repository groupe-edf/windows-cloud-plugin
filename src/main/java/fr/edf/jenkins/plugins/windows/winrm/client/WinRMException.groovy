/*
 * Copyright 2020, EDF Group and its affiliates.
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
 */

package fr.edf.jenkins.plugins.windows.winrm.client

/**
 * Exception throws were an error occured in {@link fr.edf.jenkins.plugins.windows.winrm}
 * @author Mathieu Delrocq
 *
 */
class WinRMException extends Exception {
    
    public static final String FORMATTED_MESSAGE = "Error during %s command, protocol : %s, code : %s, description : %"
    
    private WinRMException() {
        // Hide default constructor
    }

    public WinRMException(String message) {
        super(message)
    }
    public WinRMException(String message, Throwable t) {
        super(message,t)
    }
}
