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

package dev.brighten.antivpn.database.mongo.version;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import dev.brighten.antivpn.AntiVPN;
import dev.brighten.antivpn.database.DatabaseException;
import dev.brighten.antivpn.database.mongo.MongoVPN;
import dev.brighten.antivpn.database.version.Version;
import org.bson.Document;

public class MongoFirst implements Version<MongoVPN> {

    @Override
    public void update(MongoVPN database) throws DatabaseException {
        if(database.settingsDocument.listIndexes().first() == null) {
            AntiVPN.getInstance().getExecutor().log("Created index for settings collection!");
            database.settingsDocument.createIndex(Indexes.ascending("ip"));
            database.settingsDocument.createIndex(Indexes.ascending("setting"));
        }
        var versionCollect = database.antivpnDatabase.getCollection("version");

        versionCollect.insertOne(new Document("version", versionNumber()));
    }

    @Override
    public int versionNumber() {
        return 0;
    }

    @Override
    public boolean needsUpdate(MongoVPN database) {
        var versionCollect = database.antivpnDatabase.getCollection("version");

        return versionCollect.find(Filters.eq("version", versionNumber())).first() == null;
    }
}
