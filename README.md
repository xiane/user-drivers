Android Things user land drivers
================================

Simple example peripheral drivers for Android Things.

How to use this drivers
=======================

The drivers are uploaded to Maven repository. So you can easly use it 
by adding simple text to build.gradle.

You should know groupId, driver name and version. Initialy the driver name is 
Folder name of this project.

For example, you want to use Lcd driver, you should do like this on your build.gradle
file in the target apps.

```
dependencies {
    implementation 'com.hardkernel:driver-Lcd:0.5'
}
```

And you also need to add repository to top level's build.gradle.

```
allprojects {
    repositories {
        google()
        mavenCentral()
        jcenter()
    }
}
```

Current User land Drivers
-------------------------

Driver | Target | Usage | Note |
:---:|:---: | --- | --- |
[driver-BarcodeScanner](BarcodeScanner) | Barcode Scanner Driver | `implementation 'com.hardkernel:driver-BarcodeScanner:0.5'` | [sample](https://github.com/xiane/thingsGpioExample/tree/examples/BarcodeScanner)
[driver-Eeprom](Eeprom) | AT24C EEPROM Driver | `implementation 'com.hardkernel:driver-Eeprom:0.6'` | [sample](https://github.com/xiane/thingsGpioExample/tree/examples/Lcd)
[driver-Lcd](Lcd) | Lcd Character Display | `implementation 'com.hardkernel:driver-Lcd:0.5'` | [sample](https://github.com/xiane/thingsGpioExample/tree/examples/Lcd)
[driver-Led](Led) | Led Control over GPIO | `implementation 'com.hardkernel:driver-Led:0.6'` | [sample](https://github.com/xiane/thingsGpioExample/tree/examples/Led)
[driver-Mcp2515](Mcp2515) | CAN Bus Driver | `implementation 'com.hardkernel:driver-Mcp2515:0.5.3'` | [sample](https://github.com/xiane/thingsGpioExample/tree/examples/Mcp2515)
[driver-Mcp300x](Mcp300x) | ADC Convertor Driver| `implementation 'com.hardkernel:driver-Mcp300x:0.5.1'` | [sample](https://github.com/xiane/thingsGpioExample/tree/examples/Joystick)
[driver-RotaryEncoder](RotaryEncoder) | Rotary Encoder Driver | `implementation 'com.hardkernel:driver-RotaryEncoder:0.6'` | [sample](https://github.com/xiane/thingsGpioExample/tree/examples/RotaryEncoderNServoMotor)
[driver-Si1132](Si1132) | Si1132 Driver | `implementation 'com.hardkernel:driver-Si1132:0.5.1'` | [sample](https://github.com/xiane/thingsGpioExample/tree/examples/WeatherBoard)
[driver-Ssd1306](Ssd1306) | Ssd1306 Driver | `implementation 'com.hardkernel:driver-Ssd1306:0.6.2'` | [sample](https://github.com/xiane/thingsGpioExample/tree/examples/OledNRtc)
[driver-WeatherBoard](WeatherBoard) | Weather Board Meta Driver using Bme280 & Si1132 | `implementation 'com.hardkernel:driver-WeatherBoard:0.6.3'` | [sample](https://github.com/xiane/thingsGpioExample/tree/examples/WeatherBoard)

Deprecated Drivers
------------------

Driver | Target | Reason |
:---:|:---:| --- |
driver-Bme280 | Bme 280 Driver | Replaced driver-bmx280 from android things [driver-bmx280](https://github.com/androidthings/contrib-drivers/tree/master/bmx280)

