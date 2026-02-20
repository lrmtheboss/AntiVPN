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

package dev.brighten.antivpn.utils;

import dev.brighten.antivpn.AntiVPN;
import dev.brighten.antivpn.utils.json.JSONException;
import dev.brighten.antivpn.utils.json.JSONObject;
import dev.brighten.antivpn.utils.json.JsonReader;

import java.io.*;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadFactory;
import java.util.regex.Pattern;

public class MiscUtils {

    private static final Pattern ipv4 = Pattern.compile("[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}");

    public static void close(Closeable... closeables) {
        try {
            for (Closeable closeable : closeables) if (closeable != null) closeable.close();
        } catch (Exception e) {
            AntiVPN.getInstance().getExecutor().logException(e);
        }
    }

    public static void close(AutoCloseable... closeables) {
        try {
            for (AutoCloseable closeable : closeables) if (closeable != null) closeable.close();
        } catch (Exception e) {
            AntiVPN.getInstance().getExecutor().logException(e);
        }
    }

    public static void copy(InputStream in, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            int lenght;
            byte[] buf = new byte[1024];

            while ((lenght = in.read(buf)) > 0)
            {
                out.write(buf, 0, lenght);
            }

            out.close();
            in.close();
        } catch (Exception e) {
            AntiVPN.getInstance().getExecutor().logException(e);
        }
    }

    public static ThreadFactory createThreadFactory(String threadName) {
        return r -> {
            Thread thread = new Thread(r);
            thread.setName(threadName);
            return thread;
        };
    }

    public static List<CIDRUtils> rangeToCidrs(BigInteger start, BigInteger end) throws UnknownHostException {
        List<CIDRUtils> cidrs = new ArrayList<>();

        while (start.compareTo(end) <= 0) {
            // Find the number of trailing zero bits — this determines max block size alignment
            int trailingZeros = start.equals(BigInteger.ZERO)
                    ? 128  // handle the edge case
                    : start.getLowestSetBit();

            // Find the largest block that fits
            BigInteger remaining = end.subtract(start).add(BigInteger.ONE);
            int maxBits = remaining.bitLength() - 1;

            int blockBits = Math.min(trailingZeros, maxBits);
            int prefixLen = 32 - blockBits; // use 128 for IPv6

            // Build the CIDR string
            byte[] addrBytes = toFixedLengthBytes(start, 4); // use 16 for IPv6
            String cidr = InetAddress.getByAddress(addrBytes).getHostAddress() + "/" + prefixLen;
            cidrs.add(new CIDRUtils(cidr));

            // Advance past this block
            start = start.add(BigInteger.ONE.shiftLeft(blockBits));
        }

        return cidrs;
    }

    private static byte[] toFixedLengthBytes(BigInteger value, int length) {
        byte[] raw = value.toByteArray();
        byte[] result = new byte[length];
        int srcPos = Math.max(0, raw.length - length);
        int destPos = Math.max(0, length - raw.length);
        System.arraycopy(raw, srcPos, result, destPos, Math.min(raw.length, length));
        return result;
    }

    public static UUID lookupUUID(String playername) {
        try {
            JSONObject object = JsonReader
                    .readJsonFromUrl("https://funkemunky.cc/mojang/uuid?name=" + playername);

            if(object.has("uuid")) {
                return UUID.fromString(object.getString("uuid"));
            }
        } catch (IOException | JSONException e) {
            AntiVPN.getInstance().getExecutor().logException("Error while looking up UUID for " + playername + "! Falling back to Mojang API", e);
            return lookupMojangUuid(playername);
        }

        return null;
    }

    private static UUID lookupMojangUuid(String playerName) {
        try {
            JSONObject object = JsonReader.readJsonFromUrl("https://api.mojang.com/users/profiles/minecraft/" + playerName);

            if(object.has("id")) {
                return UUID.fromString(object.getString("id"));
            }
        } catch (IOException | JSONException e) {
            AntiVPN.getInstance().getExecutor().logException("Error while looking up UUID for " + playerName + " from Mojang!:", e);
        }

        return null;
    }
    public static boolean isIpv4(String ip)
    {
        return ipv4.matcher(ip).matches();
    }
}
