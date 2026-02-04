# Webhook Integration Guide

This document explains how to configure and use the webhook feature in AntiVPN to receive notifications when a player is detected using a VPN.

## Overview

When a player is detected using a VPN or connecting from a blocked country, AntiVPN can send an HTTP POST request to a configured webhook URL with detailed information about the detection.

## Configuration

Add the following configuration to your `config.yml`:

```yaml
webhooks:
  # Enable/disable webhook notifications
  enabled: false
  # The webhook URL to send POST requests to when a VPN is detected
  url: ''
  # Optional: Set to true to include authentication header (Authorization: Bearer <token>)
  useAuthentication: false
  # The authentication token to use when useAuthentication is true
  # Security Note: Token is stored in plaintext. Ensure proper file permissions on this file.
  authToken: ''
  # Timeout in seconds for webhook requests (default: 5)
  timeout: 5
```

### Configuration Options

- **enabled**: Set to `true` to enable webhook notifications
- **url**: The complete URL where webhook POST requests will be sent
- **useAuthentication**: Set to `true` to include an `Authorization: Bearer <token>` header
- **authToken**: The authentication token to use (only used when `useAuthentication` is true)
- **timeout**: Connection and read timeout in seconds (default: 5)

## Webhook Payload

When a VPN is detected, AntiVPN sends a JSON payload with the following structure:

```json
{
  "event": "vpn_detected",
  "timestamp": 1707022000000,
  "resultType": "DENIED_PROXY",
  "player": {
    "uuid": "550e8400-e29b-41d4-a716-446655440000",
    "name": "ExamplePlayer",
    "ip": "192.0.2.1"
  },
  "detection": {
    "isProxy": true,
    "countryCode": "US",
    "countryName": "United States",
    "city": "New York",
    "isp": "Example ISP",
    "asn": "AS12345",
    "method": "Blacklist"
  }
}
```

### Payload Fields

- **event**: Always set to `"vpn_detected"`
- **timestamp**: Unix timestamp in milliseconds when the detection occurred
- **resultType**: The type of detection result. Possible values:
  - `"DENIED_PROXY"`: Player is using a VPN/proxy
  - `"DENIED_COUNTRY"`: Player is connecting from a blocked country
- **player**: Player information
  - **uuid**: Player's Minecraft UUID
  - **name**: Player's username
  - **ip**: Player's IP address
- **detection**: VPN detection details
  - **isProxy**: Boolean indicating if a VPN/proxy was detected
  - **countryCode**: ISO country code (e.g., "US", "GB")
  - **countryName**: Full country name
  - **city**: City name
  - **isp**: Internet Service Provider name
  - **asn**: Autonomous System Number
  - **method**: Detection method used (e.g., "Blacklist", "Datacenter")

## Example Configurations

### Discord Webhook

```yaml
webhooks:
  enabled: true
  url: 'https://discord.com/api/webhooks/YOUR_WEBHOOK_ID/YOUR_WEBHOOK_TOKEN'
  useAuthentication: false
  authToken: ''
  timeout: 5
```

Note: Discord webhooks require a specific format. You may need to use a proxy service to convert the AntiVPN payload format to Discord's webhook format.

### Custom Backend with Authentication

```yaml
webhooks:
  enabled: true
  url: 'https://your-server.com/api/vpn-alerts'
  useAuthentication: true
  authToken: 'your-secret-token-here'
  timeout: 10
```

### Local Development Server

```yaml
webhooks:
  enabled: true
  url: 'http://localhost:8080/webhooks/vpn'
  useAuthentication: false
  authToken: ''
  timeout: 5
```

## Testing Your Webhook

### Using the Test Server

A simple Python test server is available to verify webhook functionality:

```python
#!/usr/bin/env python3
import json
from http.server import HTTPServer, BaseHTTPRequestHandler
from datetime import datetime

class WebhookHandler(BaseHTTPRequestHandler):
    def do_POST(self):
        content_length = int(self.headers.get('Content-Length', 0))
        body = self.rfile.read(content_length).decode('utf-8')
        
        print(f"\nWebhook received at {datetime.now().isoformat()}")
        print("Payload:", json.dumps(json.loads(body), indent=2))
        
        self.send_response(200)
        self.send_header('Content-Type', 'application/json')
        self.end_headers()
        self.wfile.write(b'{"status": "received"}')

if __name__ == '__main__':
    server = HTTPServer(('localhost', 8080), WebhookHandler)
    print("Test server running on http://localhost:8080")
    server.serve_forever()
```

1. Save the above script as `test_server.py`
2. Run it: `python3 test_server.py`
3. Configure AntiVPN to use `http://localhost:8080` as the webhook URL
4. The server will display incoming webhooks in the console

## Security Considerations

1. **HTTPS**: Always use HTTPS URLs in production to encrypt webhook data in transit
2. **Authentication**: Enable authentication to prevent unauthorized webhook receivers from impersonating your endpoint
3. **Token Storage**: The authentication token is stored in plaintext in `config.yml`. Ensure proper file system permissions (e.g., `chmod 600 config.yml`)
4. **IP Whitelisting**: Consider implementing IP whitelisting on your webhook endpoint to only accept requests from your Minecraft server
5. **Rate Limiting**: Implement rate limiting on your webhook endpoint to prevent abuse

## Troubleshooting

### Webhook Not Firing

- Verify `enabled: true` in configuration
- Check that `url` is properly set and accessible
- Look for error messages in server logs
- Verify network connectivity from your Minecraft server to the webhook URL

### Authentication Failures

- Verify `useAuthentication` and `authToken` are properly configured
- Check that your webhook endpoint expects the `Authorization: Bearer <token>` header format
- Review webhook endpoint logs for authentication errors

### Timeout Errors

- Increase the `timeout` value in configuration
- Verify your webhook endpoint responds quickly (within timeout period)
- Check network latency between Minecraft server and webhook endpoint

## Common Use Cases

### Log Aggregation
Send webhook data to a log aggregation service like Splunk, ELK, or Datadog for analysis and alerting.

### Discord/Slack Notifications
Use a webhook proxy service to format and forward alerts to Discord or Slack channels.

### Automated Banning
Integrate with your server management system to automatically apply additional penalties to detected players.

### Analytics
Store webhook data in a database for analytics on VPN usage patterns, geographic distribution, and ISP trends.

### Compliance Logging
Maintain an audit trail of VPN detections for compliance and security purposes.
