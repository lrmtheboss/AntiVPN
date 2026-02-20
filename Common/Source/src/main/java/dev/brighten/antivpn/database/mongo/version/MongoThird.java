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
import dev.brighten.antivpn.AntiVPN;
import dev.brighten.antivpn.database.DatabaseException;
import dev.brighten.antivpn.database.mongo.MongoVPN;
import dev.brighten.antivpn.database.version.Version;
import dev.brighten.antivpn.utils.CIDRUtils;
import dev.brighten.antivpn.utils.MiscUtils;
import org.bson.Document;
import org.bson.types.Decimal128;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;

public class MongoThird  implements Version<MongoVPN> {
    @Override
    public void update(MongoVPN database) throws DatabaseException {
        List<CIDRUtils> ipRanges = new ArrayList<>();
        List<CIDRUtils> rangesToInsert = new ArrayList<>();
        List<BigInteger[]> rangesToRemove = new ArrayList<>();
        database.settingsDocument.find(Filters.and(Filters.eq("setting", "whitelist"), Filters.exists("cidr_string")))
                .forEach((Consumer<? super Document>) doc -> {
                    BigInteger start = doc.get("ip_start", Decimal128.class).bigDecimalValue().toBigInteger();
                    BigInteger end = doc.get("ip_end", Decimal128.class).bigDecimalValue().toBigInteger();

                    try {
                        var range = MiscUtils.rangeToCidrs(start, end);

                        if(range.size() > 1) {
                            rangesToRemove.add(new BigInteger[]{start, end});
                            rangesToInsert.addAll(range);
                            AntiVPN.getInstance().getExecutor().log(Level.WARNING, "Found multiple CIDR ranges for whitelist range for %s, %s!", start, end);
                        } else ipRanges.addAll(range);
                    } catch (UnknownHostException e) {
                        AntiVPN.getInstance().getExecutor().logException(
                                String.format("Could not convert ip range to CIDR! %s, %s", start, end), e);
                    }
                });

        if(!rangesToInsert.isEmpty()) {
            AntiVPN.getInstance().getExecutor().log("Inserting %s new ranges into database...", rangesToInsert.size());
            var documentsToInsert = rangesToInsert.stream().map(cidr -> {
                Document doc = new Document("setting", "whitelist");
                doc.append("ip_start", new Decimal128(new BigDecimal(cidr.getStartIpInt())));
                doc.append("ip_end", new Decimal128(new BigDecimal(cidr.getEndIpInt())));
                doc.append("cidr_string", cidr.getCidr());

                return doc;
            }).toList();

            database.settingsDocument.insertMany(documentsToInsert);
        }
        if(!rangesToRemove.isEmpty()) {
            AntiVPN.getInstance().getExecutor().log("Removing %s old ranges from database...", rangesToRemove.size());
            rangesToRemove.forEach(range -> database.settingsDocument
                    .deleteMany(Filters.and(
                            Filters.gte("ip_start", new Decimal128(new BigDecimal(range[0]))),
                            Filters.lte("ip_end", new Decimal128(new BigDecimal(range[1]))))));
        }

        if(!ipRanges.isEmpty()) {
            AntiVPN.getInstance().getExecutor().log("Updating %s CIDRs in database with proper notation...", ipRanges.size());

            ipRanges.forEach(cidr -> database.settingsDocument
                    .updateMany(Filters.and(Filters.eq("setting", "whitelist"),
                                    Filters.eq("ip_start", new Decimal128(new BigDecimal(cidr.getStartIpInt()))),
                                    Filters.eq("ip_end", new Decimal128(new BigDecimal(cidr.getEndIpInt())))),
                            new Document("$set", new Document("cidr_string", cidr.getCidr()))));
        }

        var versionCollect = database.antivpnDatabase.getCollection("version");
        versionCollect.insertOne(new Document("version", versionNumber()));
    }

    @Override
    public int versionNumber() {
        return 2;
    }

    @Override
    public boolean needsUpdate(MongoVPN database) {
        var versionCollect = database.antivpnDatabase.getCollection("version");

        return versionCollect.find(Filters.eq("version", versionNumber())).first() == null;
    }
}
