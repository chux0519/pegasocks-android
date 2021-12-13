# pegasocks-android

A simple proxy app using `pegasocks` and `tun2socks`. Based on [mokhtarabadi/pegasocks-android](https://github.com/mokhtarabadi/pegasocks-android), I wrote the app in kotlin and add a simple UI to interact with the pegas thread for switching server and testing speed, also fixed some upstream pegas bugs.

It's a poor man's proxy app, there still are many features to be implemented and bugs to be fixed.

Also I'm new to Android, so the code may not follow the best practice, any help would be really appreciate.

## usage

Copy your pegas config(like `~/.config/.pegasrc`), and paste to the config page, save it.

It will automaticly override necessary fields for Android, see: [ConfigFragment.kt#L65-L75](https://github.com/chux0519/pegasocks-android/blob/master/app/src/main/java/com/hexyoungs/pegasocks/ConfigFragment.kt#L65-L75)

Once the VPN is running, you can interact via the `Servers` page.

<img src="https://i.imgur.com/owU4C80.jpg" height=500 align="left" />
<img src="https://i.imgur.com/MvosWEs.jpg" height=500 align="left" />
<img src="https://i.imgur.com/zia0Qh9.jpg" height=500 />


## TODO

- [ ] Logs Page
- [ ] About Page
- [ ] ACL file import/export
