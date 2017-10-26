# iot-framework

## Overview
This project utilizes the Raspberry Pi and big data technologies to create a framework for geotagging any sensor data, performing 
reliable stream analytics and batching of that data in memory utilizing Apache Kafka and Storm projects, and inserting it to
a MongoDB NoSQL data store for batch analytics and enablement of a lambda big data architecture.  As framework enhancements are 
made and configurations necessary, these should also be stored in MongoDB.  The big data back end can scan scale to support virtually any number of data producers (hence, you could have tens, hundreds, or thousands of the raspberry pi or other devices sending data to the framework).  Necessary configurations are stored in iot.config in the SD card holding the raspbian image; these are accessible to raspberry pi at runtime within the /boot directory.

The Raspberry Pi is plug and play once configured; turn it on, and it will attempt to connect to the internet via ethernet or tethering via bluetooth to a phone or other bluetooth hotspot.  Just configure the mac address in iot.config and attach a bluetooth adapter to the raspberry pi.  As soon as it has an internet connection, it will start enqueuing messages to a Kafka topic hosted on AWS (you will need to set up and configure your own infrastructure).

The raspberry pi leverages python and the awscli to connect to a remote aws instance and port.  Then, it enqueues sensor data every configurable number of seconds via a class that implements the check_sensor method. The check_sensor method must return valid json. This class name can be configured in the /boot/iot.config file on the pi, but must be in the sensorinterface.py module.  It will attempt to reconnect if it drops connection; it does not currently persist messages that fail enqueue or try to reflow them; however, the Kafka producer class will try to reflow three times (iirc).

A simple Storm topology leveraging a kafka-storm spout and mongo-storm bolt are leveraged to dequeue and insert the data to mongodb.  This is where stream analytics should be performed; for instance, a mechanism to send out an SMTP message if a certain value is out of bounds.  These types of things should be wholly configurable and reusable within MongoDB once implemented once.  A piece of functionality should be implemented in a bolt and all variables included in that bolt constructor.  In this fashion, configuration documents can be stored in MongoDB that store parameters for different bolts and optionally add these bolts to the toplogy if they are used by a particular discriminator.

MongoDB is used as a backend for web applications and batch processing.  A lambda data architecture using MongoDB and Storm are theoretically possible; One bolt would insert append-only messages to MongoDB, and another would perform batching and MongoDB updates to the real-time layer.  These should maybe be separate instances.  There are many other options for databases that could be plugged into the framework (see storm-hdfs, storm-hbase, storm-cassandra, etc etc etc).

//TO-DO 
Node.js API to serve up a lambda reports to web apps.

### Hardware and RPI configuration
**Hardware**
- Raspberry Pi B (w/ sd card, power cable, running raspbian)
- GlobalSat BU-353-S4 USB GPS Receiver (Black)
- Single-pin dallas DS18B20 temperature sensor temperature sensor
- M/F Jumper wires
- 4.7k ohm resistor
-(Optional) Bluetooth adapter

**Configs**
Detailed thermometer setup, pi configurations, bluetooth setup, etc. to come.  For now you just get my work notes:
DISCRIMINATOR should be set in the iot.config and uniquely identify whatever sensor data you're sending.  This is the name of the Kafka queue, as well as the name of the data collection in mongodb.

#### Installations
Install and configure necessary python libs and the aws cli (details to come)

#### Thermometer setup
Kudos to this tutorial: http://www.modmypi.com/blog/ds18b20-one-wire-digital-temperature-sensor-and-the-raspberry-pi
Added this to /boot/config.txt for w1 gpio sensing:
dtoverlay=w1-gpio
sudo modprobe w1-gpio
sudo modprobe w1-therm

#### Optional bluetooth setup
Kudos to this tutorial: http://www.wolfteck.com/projects/raspi/iphone/
sudo apt-get update
sudo apt-get upgrade
sudo aptitude install bluetooth bluez-utils bluez-compat
hcitool scan
sudo bluez-simple-agent hci0 <iphone mac address>
sudo bluez-test-device trusted <iphone mac address> yes
sudo pand -c <iphone mac address>  -role PANU --persist 30

#### Storm/Kafka
Storm is configured using iot.storm.config file.  It includes the full connection string for mongodb and the discriminator (must match the discriminator propery in /boot/iot.config in the pi).  Automatic queue creation seems to not work for the kafka-storm spout.  However, it works for the pi producer.  So, unless the pi is started first iot_storm.jar will fail to execute.  

Alternatively, if storm/kafka are on the same box you can let rc.local create the queue before storm is started.  Another option if storm is remote is to have a client kafka producer from the storm box produce a "test message" to Kafka just so the queue is automatically created.

You will need your rc.local to start zookeeper, kafka, and storm in that order.  Storm can be installed and executed as an executable jar, taking in arguments for the config file location and a unique identifier like so.  Be warned, spout behavior seems to be affected if this name isn't changed every time storm is started.  I append it with a timestamp:

java -jar iot_storm.jar /opt/iot_storm/config/iot.storm.config storm12345

Thank you for looking!
