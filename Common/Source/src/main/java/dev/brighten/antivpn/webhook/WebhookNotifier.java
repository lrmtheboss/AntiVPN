package dev.brighten.antivpn.webhook;

import dev.brighten.antivpn.AntiVPN;
import dev.brighten.antivpn.api.APIPlayer;
import dev.brighten.antivpn.api.CheckResult;
import dev.brighten.antivpn.utils.json.JSONException;
import dev.brighten.antivpn.utils.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * Handles sending webhook notifications when a VPN is detected.
 */
public class WebhookNotifier {

    /**
     * Sends a webhook notification asynchronously when a player is detected using a VPN.
     *
     * @param player The player that was detected
     * @param result The check result containing VPN information
     */
    public static void sendWebhookNotification(APIPlayer player, CheckResult result) {
        if (!AntiVPN.getInstance().getVpnConfig().isWebhookEnabled()) {
            return;
        }

        String webhookUrl = AntiVPN.getInstance().getVpnConfig().getWebhookUrl();
        if (webhookUrl == null || webhookUrl.trim().isEmpty()) {
            AntiVPN.getInstance().getExecutor().log(Level.WARNING, 
                "Webhook is enabled but no URL is configured. Please set webhooks.url in config.yml");
            return;
        }

        // Send webhook asynchronously to avoid blocking
        CompletableFuture.runAsync(() -> {
            try {
                sendWebhook(webhookUrl, player, result);
            } catch (Exception e) {
                AntiVPN.getInstance().getExecutor().logException("Failed to send webhook notification", e);
            }
        }, AntiVPN.getInstance().getExecutor().getThreadExecutor());
    }

    /**
     * Actually sends the HTTP POST request to the webhook URL.
     *
     * @param webhookUrl The URL to send the webhook to
     * @param player The player information
     * @param result The check result
     * @throws IOException If there's an error sending the request
     * @throws JSONException If there's an error creating the JSON payload
     */
    private static void sendWebhook(String webhookUrl, APIPlayer player, CheckResult result) 
            throws IOException, JSONException {
        URL url = new URL(webhookUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        try {
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("User-Agent", "AntiVPN-Webhook/1.0");
            
            // Add authentication header if configured
            if (AntiVPN.getInstance().getVpnConfig().isWebhookUseAuth()) {
                String token = AntiVPN.getInstance().getVpnConfig().getWebhookAuthToken();
                if (token != null && !token.trim().isEmpty()) {
                    connection.setRequestProperty("Authorization", "Bearer " + token);
                }
            }
            
            connection.setDoOutput(true);
            connection.setConnectTimeout(AntiVPN.getInstance().getVpnConfig().getWebhookTimeout() * 1000);
            connection.setReadTimeout(AntiVPN.getInstance().getVpnConfig().getWebhookTimeout() * 1000);

            // Create JSON payload
            JSONObject payload = createPayload(player, result);
            byte[] payloadBytes = payload.toString().getBytes(StandardCharsets.UTF_8);

            // Send request
            try (OutputStream os = connection.getOutputStream()) {
                os.write(payloadBytes);
                os.flush();
            }

            // Check response
            int responseCode = connection.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                AntiVPN.getInstance().getExecutor().log(Level.INFO, 
                    "Successfully sent webhook notification for player %s (response: %d)", 
                    player.getName(), responseCode);
            } else {
                AntiVPN.getInstance().getExecutor().log(Level.WARNING, 
                    "Webhook notification returned non-success status code %d for player %s", 
                    responseCode, player.getName());
            }
        } finally {
            connection.disconnect();
        }
    }

    /**
     * Creates the JSON payload for the webhook notification.
     *
     * @param player The player information
     * @param result The check result
     * @return JSONObject containing the webhook payload
     * @throws JSONException If there's an error creating the JSON
     */
    private static JSONObject createPayload(APIPlayer player, CheckResult result) throws JSONException {
        String format = AntiVPN.getInstance().getVpnConfig().getWebhookFormat().toLowerCase();
        
        switch (format) {
            case "discord":
                return createDiscordPayload(player, result);
            case "slack":
                return createSlackPayload(player, result);
            default:
                return createGenericPayload(player, result);
        }
    }

    /**
     * Creates a Discord-formatted webhook payload with rich embeds.
     *
     * @param player The player information
     * @param result The check result
     * @return JSONObject containing the Discord-formatted payload
     * @throws JSONException If there's an error creating the JSON
     */
    private static JSONObject createDiscordPayload(APIPlayer player, CheckResult result) throws JSONException {
        JSONObject payload = new JSONObject();
        
        // Create embed
        JSONObject embed = new JSONObject();
        
        // Set title and color based on result type
        if (result.resultType().name().equals("DENIED_PROXY")) {
            embed.put("title", "🚫 VPN/Proxy Detection");
            embed.put("color", 15158332); // Red color
        } else if (result.resultType().name().equals("DENIED_COUNTRY")) {
            embed.put("title", "🌍 Country Blocked");
            embed.put("color", 15105570); // Orange color
        }
        
        // Add description
        embed.put("description", "A player attempted to join using a VPN/proxy or from a blocked country.");
        
        // Add fields with player and detection information
        JSONObject[] fields = new JSONObject[0];
        if (result.response() != null) {
            fields = new JSONObject[] {
                createDiscordField("Player", player.getName(), true),
                createDiscordField("UUID", player.getUuid().toString(), true),
                createDiscordField("IP Address", player.getIp().getHostAddress(), true),
                createDiscordField("Country", result.response().getCountryName() + " (" + result.response().getCountryCode() + ")", true),
                createDiscordField("City", result.response().getCity(), true),
                createDiscordField("ISP", result.response().getIsp(), true),
                createDiscordField("ASN", result.response().getAsn(), true),
                createDiscordField("Detection Method", result.response().getMethod() != null ? result.response().getMethod() : "N/A", true),
                createDiscordField("Proxy Status", result.response().isProxy() ? "✓ Detected" : "✗ Not Detected", true)
            };
        } else {
            fields = new JSONObject[] {
                createDiscordField("Player", player.getName(), true),
                createDiscordField("UUID", player.getUuid().toString(), true),
                createDiscordField("IP Address", player.getIp().getHostAddress(), true)
            };
        }
        
        embed.put("fields", fields);
        
        // Add timestamp in ISO 8601 format
        java.time.Instant instant = java.time.Instant.ofEpochMilli(System.currentTimeMillis());
        embed.put("timestamp", instant.toString());
        
        // Add footer
        JSONObject footer = new JSONObject();
        footer.put("text", "AntiVPN Detection System");
        embed.put("footer", footer);
        
        // Add embed to payload
        payload.put("embeds", new JSONObject[] { embed });
        
        return payload;
    }

    /**
     * Helper method to create a Discord embed field.
     *
     * @param name Field name
     * @param value Field value
     * @param inline Whether the field should be inline
     * @return JSONObject containing the field
     * @throws JSONException If there's an error creating the JSON
     */
    private static JSONObject createDiscordField(String name, String value, boolean inline) throws JSONException {
        JSONObject field = new JSONObject();
        field.put("name", name);
        field.put("value", value != null ? value : "N/A");
        field.put("inline", inline);
        return field;
    }

    /**
     * Creates a Slack-formatted webhook payload.
     *
     * @param player The player information
     * @param result The check result
     * @return JSONObject containing the Slack-formatted payload
     * @throws JSONException If there's an error creating the JSON
     */
    private static JSONObject createSlackPayload(APIPlayer player, CheckResult result) throws JSONException {
        JSONObject payload = new JSONObject();
        
        // Build text message
        StringBuilder text = new StringBuilder();
        text.append("*VPN/Proxy Detection Alert*\n");
        text.append("Player: ").append(player.getName()).append("\n");
        text.append("IP: ").append(player.getIp().getHostAddress()).append("\n");
        
        if (result.response() != null) {
            text.append("Country: ").append(result.response().getCountryName())
                .append(" (").append(result.response().getCountryCode()).append(")\n");
            text.append("City: ").append(result.response().getCity()).append("\n");
            text.append("ISP: ").append(result.response().getIsp()).append("\n");
            if (result.response().getMethod() != null) {
                text.append("Method: ").append(result.response().getMethod()).append("\n");
            }
        }
        
        payload.put("text", text.toString());
        
        return payload;
    }

    /**
     * Creates a generic JSON payload (original format).
     *
     * @param player The player information
     * @param result The check result
     * @return JSONObject containing the generic payload
     * @throws JSONException If there's an error creating the JSON
     */
    private static JSONObject createGenericPayload(APIPlayer player, CheckResult result) throws JSONException {
        JSONObject payload = new JSONObject();
        
        // Basic event information
        payload.put("event", "vpn_detected");
        payload.put("timestamp", System.currentTimeMillis());
        payload.put("resultType", result.resultType().name());
        
        // Player information
        JSONObject playerInfo = new JSONObject();
        playerInfo.put("uuid", player.getUuid().toString());
        playerInfo.put("name", player.getName());
        playerInfo.put("ip", player.getIp().getHostAddress());
        payload.put("player", playerInfo);
        
        // VPN detection information
        if (result.response() != null) {
            JSONObject detectionInfo = new JSONObject();
            detectionInfo.put("isProxy", result.response().isProxy());
            detectionInfo.put("countryCode", result.response().getCountryCode());
            detectionInfo.put("countryName", result.response().getCountryName());
            detectionInfo.put("city", result.response().getCity());
            detectionInfo.put("isp", result.response().getIsp());
            detectionInfo.put("asn", result.response().getAsn());
            
            if (result.response().getMethod() != null) {
                detectionInfo.put("method", result.response().getMethod());
            }
            
            payload.put("detection", detectionInfo);
        }
        
        return payload;
    }
}
