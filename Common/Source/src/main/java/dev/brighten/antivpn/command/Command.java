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

package dev.brighten.antivpn.command;

import java.util.List;

public abstract class Command {

    public abstract String permission();

    public abstract String name();

    public abstract String[] aliases();

    public abstract String description();

    public abstract String usage();

    public abstract String parent();

    public abstract Command[] children();
    
    public abstract String execute(CommandExecutor executor, String[] args);

    public abstract List<String> tabComplete(CommandExecutor executor, String alias, String[] args);
}
