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
        if (!AntiVPN.getInstance().getVpnConfig().webhookEnabled()) {
            return;
        }

        String webhookUrl = AntiVPN.getInstance().getVpnConfig().webhookUrl();
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
        }, dev.brighten.antivpn.api.VPNExecutor.threadExecutor);
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
            if (AntiVPN.getInstance().getVpnConfig().webhookUseAuth()) {
                String token = AntiVPN.getInstance().getVpnConfig().webhookAuthToken();
                if (token != null && !token.trim().isEmpty()) {
                    connection.setRequestProperty("Authorization", "Bearer " + token);
                }
            }
            
            connection.setDoOutput(true);
            connection.setConnectTimeout(AntiVPN.getInstance().getVpnConfig().webhookTimeout() * 1000);
            connection.setReadTimeout(AntiVPN.getInstance().getVpnConfig().webhookTimeout() * 1000);

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
