# iot-framework

##Overview
This project utilizes the Raspberry Pi and big data technologies to create a framework for geotagging any sensor data, performing 
reliable stream analytics and batching of that data utilizing Apache Kafka and Storm projects, and inserting it to
a MongoDB NoSQL data store for batch analytics and enablement of a lambda big data architecture.  As framework enhancements are 
made and configurations necessary, these should also be stored in MongoDB.

###Hardware and RPI configuration
**Hardware**
- Raspberry Pi B (w/ sd card, power cable, running raspbian)
- GlobalSat BU-353-S4 USB GPS Receiver (Black)
- Single-pin dallas DS18B20 temperature sensor temperature sensor
- M/F Jumper wires
- 4.7k ohm resistor
-(Optional) Bluetooth adapter

**Configs**
Detailed thermometer setup, pi configurations, bluetooth setup, etc. to come.  For now you just get my shitty work notes:
DISCRIMINATOR should uniquely identify whatever temperature data you're sending.  This is the name of the Kafka queue, as well as the name
of the data collection in mongodb.

####Installations
Install and configure necessary python libs and the aws cli (details to come)

####Thermometer setup
Added this to /boot/config.txt for w1 gpio sensing:
Kudos to this tutorial: http://www.modmypi.com/blog/ds18b20-one-wire-digital-temperature-sensor-and-the-raspberry-pi
dtoverlay=w1-gpio
sudo modprobe w1-gpio
sudo modprobe w1-therm
Change your temperature sensor file directory to here, I forgot to make this a configuration:

####Optional bluetooth setup
Kudos to this tutorial: http://www.wolfteck.com/projects/raspi/iphone/
sudo apt-get update
sudo apt-get upgrade
sudo aptitude install bluetooth bluez-utils bluez-compat
hcitool scan
sudo bluez-simple-agent hci0 <iphone mac address>
sudo bluez-test-device trusted <iphone mac address> yes
sudo pand -c <iphone mac address>  -role PANU --persist 30



####

