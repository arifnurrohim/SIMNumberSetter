# SIM Number Setter

> **Fork:** `app.arifnurrohim.simsetter` (SIM Setter). This fork adds dual SIM support: a SIM slot
> picker lets you read and write the subscriber number of either SIM, instead of only the default
> phone. Original project: [KieronQuinn/SIMNumberSetter](https://github.com/KieronQuinn/SIMNumberSetter).

SIM Number Setter is a small Xposed module that invokes normally unused Android System code to set 
the "subscriber number" on the device's SIM card. This is the number displayed in the system 
settings, and used in apps such as Google Messages, as well as being available to third party apps 
with sufficient permissions.

The number is not always set by carriers (leaving "Unknown"), or can be lost or invalid after 
porting a number to a different SIM. 

This small app allows you to fix it, permanently, using a rooted device with Xposed.

**If you don't have a rooted device, or don't want to root your main device, you may wish to 
consider rooting an old device, and temporarily putting your SIM into that device, using this app,
and then putting it back in your main device - the fixed number will travel with it**

### Important Notice

Changing the SIM number is semi-permanent: the number change will survive reboots, uninstalling this
app, even switching the SIM to a different device, but can be changed again at any time using this 
app. 

SIM Number Setter does NOT:
- Change your actual phone number, no matter what you enter. You must contact your carrier if you wish to port your number or switch network.
- Unblock a network blocked SIM.
- Give you free data or calls.
- Change your IMEI.
  
SIM Number Setter uses the built-in Android methods to write data to the SIM. 
The app nor the developer are not responsible for issues with the process, including any damage to 
the SIM or network issues.

### Screenshots

[![Screenshots](https://i.imgur.com/UNKADmrl.png)](https://i.imgur.com/UNKADmr.png)

### Download

[Download from releases page](https://github.com/KieronQuinn/SIMNumberSetter/releases)

## Building and releasing

This fork builds with Gradle (Gradle 7.3.3, AGP 7.1.1, JDK 17, compile SDK 33).

- `.github/workflows/build.yml` builds a debug APK on every push/PR and uploads it as the
  `SIMSetter-dev` artifact.
- `.github/workflows/release.yml` runs when a `v*` tag is pushed, builds a signed release APK and
  publishes it to GitHub Releases.

### Release secrets

Add these repository secrets (Settings → Secrets and variables → Actions):

| Secret | Required | Description |
| --- | --- | --- |
| `KEYSTORE_BASE64` | yes | Base64 of the release keystore |
| `KEYSTORE_PASSWORD` | yes | Keystore password |
| `KEY_ALIAS` | no | Key alias (defaults to `simsetter`) |
| `KEY_PASSWORD` | no | Key password (defaults to `KEYSTORE_PASSWORD`) |

Create the Base64 value with:

```bash
base64 -w0 release.keystore > release.keystore.b64
```

A local `release.keystore`/`release.keystore.b64` is generated in the project root and is ignored by
Git. Then publish a release with:

```bash
git tag v1.2.3
git push origin v1.2.3
```

