# FinTrack - SMS Twilio

This document describes how to enable SMS alerts with Twilio when a user tries to add an expense above the remaining budget.

## Configuration

Edit `src/main/resources/twilio.properties` and set your Twilio credentials:

- `twilio.accountSid`
- `twilio.authToken`
- `twilio.fromNumber`
- `twilio.toNumber`
- `twilio.cooldownMinutes` (default 5)

## Behavior

When a user tries to add an expense greater than the remaining budget, the app:

1. Sends an SMS to the fixed `twilio.toNumber`.
2. Logs the attempt in the `sms_notification` table.
3. Enforces a cooldown (default 5 minutes) to avoid spam.

## Manual test

You can run a small demo to validate the Twilio setup:

```powershell
mvn -q -DskipTests compile
java -cp "target/classes;target/dependency/*" Test.SmsSenderDemo
```

## Notes

- Make sure MySQL is running and the `fintrack` database exists.
- The SMS table is created automatically if it does not exist.

