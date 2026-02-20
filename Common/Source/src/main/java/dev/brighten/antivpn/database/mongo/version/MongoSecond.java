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
import dev.brighten.antivpn.utils.CIDRUtils;
import org.bson.Document;
import org.bson.types.Decimal128;

import java.math.BigDecimal;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MongoSecond implements Version<MongoVPN> {
    @Override
    public void update(MongoVPN database) throws DatabaseException {
        List<Document> backup = new ArrayList<>();
        database.settingsDocument.find(Filters.and(Filters.eq("setting", "whitelist"),
                        Filters.exists("ip")))
                .forEach((Consumer<? super Document>) doc -> {
                    backup.add(new Document(doc));

                    String ip = doc.getString("ip");

                    try {
                        var cidr = new CIDRUtils(ip + "/32");

                        doc.append("ip_start", new Decimal128(new BigDecimal(cidr.getStartIpInt())));
                        doc.append("ip_end", new Decimal128(new BigDecimal(cidr.getEndIpInt())));
                        doc.append("cidr_string", cidr.toString());
                        doc.remove("ip");

                        database.settingsDocument.replaceOne(Filters.eq("_id", doc.getObjectId("_id")), doc);
                    } catch (UnknownHostException e) {
                        rollback(backup, database);
                        throw new RuntimeException(e);
                    }
                });

        database.settingsDocument.createIndex(Indexes.compoundIndex(Indexes.ascending("ip_start"), Indexes.ascending("ip_end")));
        database.settingsDocument.createIndex(Indexes.ascending("cidr_string"));
        var versionCollect = database.antivpnDatabase.getCollection("version");
        versionCollect.insertOne(new Document("version", versionNumber()));
    }

    private void rollback(List<Document> toRollback, MongoVPN database) {
        AntiVPN.getInstance().getExecutor().log("Rolling back to version 0...");
        toRollback.forEach(doc -> database.settingsDocument.replaceOne(Filters.eq("_id", doc.getObjectId("_id")), doc));
        toRollback.clear();
    }

    @Override
    public int versionNumber() {
        return 1;
    }

    @Override
    public boolean needsUpdate(MongoVPN database) {
        var versionCollect = database.antivpnDatabase.getCollection("version");

        return versionCollect.find(Filters.eq("version", versionNumber())).first() == null;
    }
}
