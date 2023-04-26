# MCP2515 on the Android Things

This project contains app for Android Things working on ODROID-N2/C4 and ODROID-M1.

The ODROID Board is connected to the CAN BUS Controller Module (MCP2515_CAN).
The CAN bus speed is 1Mbps and.
The CAN controller works on 16 MHz and all parameters are configured for this frequency.

This repository is based on the SunnyWolf/SM2019Dashboard.

You can test it with other board connected to the another MCP2515.

You can make test environment with the ODROID-N2 and ODROID-C4.

Before starting this example, you should enable the spi on the both of the board.

Here are command lins for this test on the ODROID-N2 with Ubuntu 20.04.

```bash
$ ip link set can0 type can bitrate 1000000 triple-sampling on
$ ifconfig can0 up
```

You can get can message and send can message.

```bash
$ candump can0
$ cansend can0 042#01.02.03.04.05
```


You also test with test example [Apps](https://github.com/xiane/thingsGpioExample/tree/examples/Mcp2515) from android.
