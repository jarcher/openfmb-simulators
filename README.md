![Logo of the Project](https://github.com/openfmb/dtech-demo-2016/blob/master/img/openfmb-tm-black_reduced_100.png)

# Description

This repository provides the simulators for ESS, Solar and the Recloser.  When building this repository a single jar is produced with differen entry points to start the different simulators.  Each simulator has a properties files to define the OpenFMB ID information and how the simulator operates. In addition the Island Balancer application can be initiated by the same jar with an entry point.   The Island Balancer application subscribes to the loadpublisher, solar, recloser and ESS to determine the state of the microgrid and the appropriate discharge of the ESS during an microgrid island state.

# Installing / Getting started

NOTE: This project is used in the Dtech Demo.  Refer to [Wiki](https://github.com/openfmb/dtech-demo-2016/wiki) for information on DTech Demo. 

To start the battery simulator
```shell
java -cp openfmb-simulators-0.0.5-SNAPSHOT-jar-with-dependencies.jar  com.greenenergycorp.openfmb.simulator.battery.BatterySimulator
```
To start the solar simulator
```shell
java -cp openfmb-simulators-0.0.5-SNAPSHOT-jar-with-dependencies.jar com.greenenergycorp.openfmb.simulator.solar.SolarSimulator
```
To start the recloser simulator
```shell
java -cp openfmb-simulators-0.0.5-SNAPSHOT-jar-with-dependencies.jar com.greenenergycorp.openfmb.simulator.recloser.RecloserSimulator
```
To start the balancer application
```shell
java -cp openfmb-simulators-0.0.5-SNAPSHOT-jar-with-dependencies.jar com.greenenergycorp.openfmb.simulator.balance.IslandBalancer
```

To run another simulator you will need to modify the simulator properties file.  A modified properties file for solar has been provided below. This can be copy and pasted in to a file called solarsim2.properities.

New properites file for solar simulator
```
device.logicalDeviceID=DEMO.MGRID.SOLAR.2
device.mRID=DEMO.MGRID.SOLAR.2
device.name=Solar
device.description=Solar

value.scale=0.01
value.offset=0.0
value.jitterChance=0.2
value.jitterPercent=0.05

topic.SolarReadingProfile=openfmb/solarmodule/SolarReadingProfile
topic.SolarEventProfile=openfmb/solarmodule/SolarEventProfile

data.file=data/load.tsv

config.intervalMs=1000
```

Note that the key items to change are the ID and MRID to avoid conflict.  In this case we incremented the suffix to **2**. 
To run the simulator with this property file you will need to be in the same directory as the jar to use the following command.

```
java -cp openfmb-simulators-0.0.5-SNAPSHOT-jar-with-dependencies.jar -Dconfig.sim.path=solarsim2.properties  com.greenenergycorp.openfmb.simulator.solar.SolarSimulator
```
The HMI will now show a new solar device on the web page.

## Building

In order to build this project you must first build the [openfmb-adapters](https://github.com/openfmb/openfmb-adapters) project to create the XML to MQTT bindings. 

```shell
git clone https://github.com/openfmb/openfmb-simulators.git
cd openfmb-simulators
mvn clean install -Pslf4j-simple
```

The build jar is put in the target directory and needs to be moved to the main directory where the properities files are located. 


## Configuration and Description

The following is a description of the simulators and the associated property files.   These properties can be modified to reflect changes in the simulation.  As a rule the ID and MRID are always the same for a device.  The other properties are how the simulator operates and capacities of devices where appropriate. In addition the topics being used are defined and can be examined with MQTT Spy.

The configuration of each simulator and the application resides in the property files.

### Energy Storage Simulator Description and Properties

This process simulates a battery that can be in standby mode, setpoint-driven mode, and either islanded or grid connected. It subscribes to the BatteryControlProfile for setpoints.   

The ESS simulator parameters are found in the batterysim.properties file.  Configuration for the battery simulator resides [here](https://github.com/openfmb/openfmb-simulators/blob/master/batterysim.properties).

### PV Simulator Description and Properties

This process reads a file of 24 hours of one-hour output data, interpolating values to produce updates at a configurable interval.  

The PV simulator parameters are found in the SolarSim.properties file.  Configuration for the solar simulator resides [here](https://github.com/openfmb/openfmb-simulators/blob/master/solarsim.properties).

### Recloser Description and Properties

This process simulates an open/closed breaker, handling RecloserControlProfiles to execute the flips. It also subscribes to all battery, solar, and resource power values to calculate a flow across the PCC.

The Recloser Simulator parameters are found in the RecloserSim.properties file. Configuration for the recloser simulator resides [here](https://github.com/openfmb/openfmb-simulators/blob/master/reclosersim.properties).

### Island Balancer Description

This process subscribes to the recloser and detects islanding and grid connected modes. In Island mode it will set the battery to Island mode and issue setpoints to the battery to balance between microgrid generation and load. Configuration for the balancer application resider [here](https://github.com/openfmb/openfmb-simulators/blob/master/balancer.properties).

Each properties files defines the following.
```
device.logicalDeviceID
device.mRID
device.name
device.description

# topics to subscribe or publisher too
 
topic.<profile>=
 
# parameters for the simulator including publish rate if appropriate
 
```

# Contributing

Green Energy Corp, Daniel Evans

If you'd like to contribute, please fork the repository and use a feature
branch. Pull requests are warmly welcome.

Please review the [CONTRIBUTING](https://github.com/openfmb/openfmb-simulators/blob/master/CONTRIBUTING.md) file. 

# License

See the [APACHE_FILE_HEADER](https://github.com/openfmb/openfmb-simulators/blob/master/APACHE_FILE_HEADER) file for more info.
