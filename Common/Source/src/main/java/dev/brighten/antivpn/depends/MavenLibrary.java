/*
 * Copyright 2026 Dawson Hessler
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

package dev.brighten.antivpn.depends;

import java.lang.annotation.*;

/**
 * Annotation to indicate a required library for a class.
 */
@Documented
@Repeatable(MavenLibraries.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MavenLibrary {

    /**
     * The group id of the library
     *
     * @return the group id of the library
     */
    String groupId();

    /**
     * The artifact id of the library
     *
     * @return the artifact id of the library
     */
    String artifactId();

    /**
     * The version of the library
     *
     * @return the version of the library
     */
    String version();

    /**
     * The repo where the library can be obtained from
     *
     * @return the repo where the library can be obtained from
     */
    Repository repo() default @Repository(url = "https://repo1.maven.org/maven2");

    Relocate[] relocations() default {}; // Add this line

}
