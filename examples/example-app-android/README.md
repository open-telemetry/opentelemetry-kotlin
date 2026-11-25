# example-app-android

The Android launcher for [example-app](../example-app/README.md). It hosts the shared Compose UI in
`MainActivity`. Telemetry is configured in `example-app`.

## Running

Start [local-collector](../local-collector/README.md) on your machine, then install the app on an
emulator and launch it:

```sh
./gradlew :examples:local-collector:run
./gradlew :examples:example-app:runAndroidExampleApp
```

The app exports to `http://10.0.2.2:4318`, which is the emulator's address for the host machine.
Telemetry is batched and flushed when the app goes to the background, so press Home to send it
immediately.

## Physical devices

A physical device can't reach `10.0.2.2`. To use one:

1. Set `url` in `example-app`'s `AppConfig.kt` to `http://<your machine's IP>:4318`.
2. Add that IP to `src/main/res/xml/network_security_config.xml`. The app only allows cleartext
   HTTP to the hosts listed there.
