Payment Push Listener

-----------------------------------------------------------------------------------------------------------------

Payment Push Listener is an Android application that captures notifications from selected banking apps and sends transaction information to an HTTP server in encrypted JSON format.

-----------------------------------------------------------------------------------------------------------------

Overview

The app listens for push notifications from chosen banking apps.

Each notification is converted into a JSON object with the following fields:

amount — transaction amount in cents

timestamp — notification timestamp in milliseconds

The JSON is encrypted using AES and sent to a specified server over HTTPS.

-----------------------------------------------------------------------------------------------------------------

Features

Supports multiple banks simultaneously (Sberbank, Tinkoff, Alfa Bank, VTB).

Transaction filtering by minimum and maximum amount.

Configurable server URL, AES encryption key, enabled banks, and amount thresholds.

Unsafe HTTPS client used for testing (connection is encrypted, but certificate validation is disabled).

-----------------------------------------------------------------------------------------------------------------

Installation and Setup

Install the app on your Android device.

Grant notification access to the app.

In the app settings:

Enter the server URL and a 16-character AES key.

Enable the banks you want to track using checkboxes.

Set the minimum and maximum transaction amounts to filter notifications.

Save the settings using the Save button (bank checkboxes are applied immediately).

-----------------------------------------------------------------------------------------------------------------

JSON Example
{
  "amount": 12000,
  "timestamp": 1772652292619
}

In this example, amount is 120₽ (12000 cents), and timestamp is the Unix time in milliseconds.

-----------------------------------------------------------------------------------------------------------------

Security

The connection is encrypted via TLS.

JSON data is additionally encrypted with the AES key provided by the user.

For production, it is recommended to use a trusted SSL certificate on the server.

-----------------------------------------------------------------------------------------------------------------

Troubleshooting & Tips

Ensure notification access is granted; otherwise, the app cannot capture transaction notifications.

If push notifications are not received, verify that the correct package names for the banking apps are enabled.

Ensure the AES key is exactly 16 characters, or encryption will fail.

For testing, the app uses an unsafe client; in production, consider proper certificate validation.
