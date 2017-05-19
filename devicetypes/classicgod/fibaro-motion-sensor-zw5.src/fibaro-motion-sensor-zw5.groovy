/**
 *  Fibaro Motion Sensor ZW5
 *
 *  Copyright 2017 Artur Draga
 *
 *  Special thanks to Kevin LaFramboise (krlaframboise)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
	definition (name: "Fibaro Motion Sensor ZW5", namespace: "ClassicGOD", author: "Artur Draga") {
		capability "Battery"
		capability "Configuration"
		capability "Illuminance Measurement"
		capability "Motion Sensor"
		capability "Sensor"
		capability "Tamper Alert"
		capability "Temperature Measurement"
		capability "Health Check"
		capability "Three Axis"

		command "resetMotionTile"
		command "forceSync"
		
		fingerprint deviceId: "0x0701", inClusters: "0x5E, 0x20, 0x86, 0x72, 0x5A, 0x59, 0x85, 0x73, 0x84, 0x80, 0x71, 0x56, 0x70, 0x31, 0x8E, 0x22, 0x30, 0x9C, 0x98, 0x7A", outClusters: ""
		fingerprint deviceId: "0x0701", inClusters: "0x5E, 0x20, 0x86, 0x72, 0x5A, 0x59, 0x85, 0x73, 0x84, 0x80, 0x71, 0x56, 0x70, 0x31, 0x8E, 0x22, 0x30, 0x9C, 0x7A", outClusters: ""
	}
	
	tiles(scale: 2) {
		multiAttributeTile(name:"FGMS", type:"lighting", width:6, height:4) {
			tileAttribute("device.motion", key:"PRIMARY_CONTROL") {
				attributeState("inactive", label:"no motion", icon:"st.motion.motion.inactive", backgroundColor:"#ffffff")
				attributeState("active", label:"motion", icon:"st.motion.motion.active", backgroundColor:"#00a0dc")   
			}
			tileAttribute("device.lastEvent", key:"SECONDARY_CONTROL") {
				attributeState("val", label:'${currentValue}')
			}  
		}
		valueTile("tamper", "device.tamper", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
			state "val", label:'Tamper:\n${currentValue}'
		}
		valueTile("temperature", "device.temperature", inactiveLabel: false, width: 2, height: 2) {
			state "temperature", label:'${currentValue}°',
			backgroundColors:[
				[value: 31, color: "#153591"],
				[value: 44, color: "#1e9cbb"],
				[value: 59, color: "#90d2a7"],
				[value: 74, color: "#44b621"],
				[value: 84, color: "#f1d801"],
				[value: 95, color: "#d04e00"],
				[value: 96, color: "#bc2323"]
			]
		}
		valueTile("illuminance", "device.illuminance", inactiveLabel: false, width: 2, height: 2) {
			state "luminosity", label:'${currentValue}\nLux', unit:"lux"
		}
		valueTile("battery", "device.battery", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
			state "battery", label:'${currentValue}%\nbattery', unit:"%"
		}
		valueTile("motionTile", "device.motionText", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
			state "motionText", label:'${currentValue}', action:"resetMotionTile"
		}
		standardTile("syncStatus", "device.syncStatus", decoration: "flat", width: 2, height: 2) {
			state "synced", label:'OK', action:"forceSync", backgroundColor: "#00a0dc", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/fibaro-motion-sensor-zw5.src/images/sync_icon.png"
			state "pending", label:"Pending", action:"forceSync", backgroundColor: "#153591", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/fibaro-motion-sensor-zw5.src/images/sync_icon.png"
			state "inProgress", label:"Syncing", action:"forceSync", backgroundColor: "#44b621", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/fibaro-motion-sensor-zw5.src/images/sync_icon.png"
			state "incomplete", label:"Incomplete", action:"forceSync", backgroundColor: "#f1d801", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/fibaro-motion-sensor-zw5.src/images/sync_icon.png"
			state "failed", label:"Failed", action:"forceSync", backgroundColor: "#bc2323", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/fibaro-motion-sensor-zw5.src/images/sync_icon.png"
			state "force", label:"Force", action:"forceSync", backgroundColor: "#e86d13", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/fibaro-motion-sensor-zw5.src/images/sync_icon.png"
		}
		main "FGMS"
		details(["FGMS","tamper","temperature","illuminance","motionTile","syncStatus","battery"])
	}
	
	preferences {
		input ( name: "logging", title: "Logging", type: "boolean", required: false )
		input ( name: "wakeUpInterval", title: "Wake Up interval", description: "How often should device wake up in seconds (default 7200)", type: "number", range: "1..65535", defaultValue: 7200, required: false )
		input ( type: "paragraph", element: "paragraph", title: null, description: "MOTION DETECTION SETTINGS:\nParameters 1 to 9 allow user to configure PIR motion detection settings" )
		getPrefsFor("motion")
		input ( type: "paragraph", element: "paragraph", title: null, description: "TAMPER ALARM SETTINGS:\nParameters 20 to 25 allow user to configure tamper alarm and accelerometer" )
		getPrefsFor("tamper")
		input ( type: "paragraph", element: "paragraph", title: null, description: "ILLUMINANCE REPORTING SETTINGS:\nParameters 40 and 42 allow user to configure Illuminance reporting" )
		getPrefsFor("illuminance")
		input ( type: "paragraph", element: "paragraph", title: null, description: "TEMPERATURE REPORTING SETTINGS:\nParameters 60 to 66 allow user to configure Temperature reporting" )
		getPrefsFor("temperature")
		input ( type: "paragraph", element: "paragraph", title: null, description: "LED SETTINGS:\nParameters 80 to 89 allow user to configure LED indicator behaviour")
		getPrefsFor("led")
	}
}

// UI - supporting functions
private getPrefsFor(String name) {
	parameterMap().findAll( {it.key.contains(name)} ).each {
		input (
			name: it.key,
			title: "${it.num}. ${it.title}",
			description: it.descr,
			type: it.type,
			options: it.options,
			range: (it.min != null && it.max != null) ? "${it.min}..${it.max}" : null,
			defaultValue: it.def,
			required: false
		)
	}
}

def forceSync() {
	logging("${device.displayName} - Executing forceSync()", "info")
	if (device.currentValue("syncStatus") != "force") {
		state.prevSyncState = device.currentValue("syncStatus")
		sendEvent(name: "syncStatus", value: "force")
	} else {
		if (state.prevSyncState != null) {
			sendEvent(name: "syncStatus", value: state.prevSyncState)
		} else {
			sendEvent(name: "syncStatus", value: "synced")
		}
	}
}

def resetMotionTile() {
	logging("${device.displayName} - Executing resetMotionTile()", "info")
	sendEvent(name: "motionText", value: "--", displayed: false)
}

// Event handlers and supporting functions
def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	logging("${device.displayName} - NotificationReport received for ${cmd.event}, parameter value: ${cmd.eventParameter[0]}", "info")
	def lastTime = new Date().format("yyyy MMM dd EEE HH:mm:ss", location.timeZone)
	if (cmd.notificationType == 7) {
		if (cmd.event == 0) {
			sendEvent(name: (cmd.eventParameter[0] == 3) ? "tamper" : "motion", value: (cmd.eventParameter[0] == 3) ? "clear" :"inactive")
		} else {
			sendEvent(name: (cmd.event == 3) ? "tamper" : "motion", value: (cmd.event == 3) ? "detected" : "active")
			sendEvent(name: "lastEvent", value: (cmd.event == 3) ? "Tamper - $lastTime" : "Motion - $lastTime", displayed: false)
		}
	}
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	logging("${device.displayName} - BatteryReport received, value: ${cmd.batteryLevel}", "info")
	sendEvent(name: "battery", value: cmd.batteryLevel.toString(), unit: "%", displayed: true)
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	logging("${device.displayName} - SensorMultilevelReport received, sensor: ${cmd.sensorType}, scaledValue: ${cmd.scaledSensorValue}", "info")
	switch (cmd.sensorType as Integer) {
		case 1:
			def cmdScale = cmd.scale == 1 ? "F" : "C"
			sendEvent(name: "temperature", unit: getTemperatureScale(), value: convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale, cmd.precision), displayed: true)
			break
		case 3:
			sendEvent(name: "illuminance", value: cmd.scaledSensorValue.toInteger().toString(), unit:"lux", displayed: true)
			break
		case [25,52,53,54]:
			motionEvent(cmd.sensorType, cmd.scaledSensorValue )
			break
	}
}

private motionEvent(Integer sensorType, value) {
	logging("${device.displayName} - Executing motionEvent() with parameters: ${sensorType}, ${value}", "info")
	def axisMap = [52: "yAxis", 53: "zAxis", 54: "xAxis"]
	switch (sensorType) {
		case 25:
			sendEvent(name: "motionText", value: "Vibration:\n${value}", displayed: false)
			break
		case 52..54:
			sendEvent(name: axisMap[sensorType], value: value , displayed: false)
			runIn(2,"axisEvent")
			break
	}
	
}

private axisEvent() {
	logging("${device.displayName} - Executing axisEvent() values are: ${device.currentValue("xAxis")}, ${device.currentValue("yAxis")}, ${device.currentValue("zAxis")}", "info")
	def xAxis = Math.round((device.currentValue("xAxis") as Float) * 100)
	def yAxis = Math.round((device.currentValue("yAxis") as Float) * 100) // * 100 becaouse from what I can tell apps expect data in cm/s2
	def zAxis = Math.round((device.currentValue("zAxis") as Float) * 100)
	sendEvent(name: "motionText", value: "X: ${device.currentValue("xAxis")}\nY: ${device.currentValue("yAxis")}\nZ: ${device.currentValue("zAxis")}", displayed: false)
	sendEvent(name: "threeAxis", value: "${xAxis},${yAxis},${zAxis}", isStateChange: true, displayed: false)
}

// Parameter configuration, synchronization and verification
def updated() {
	logging("${device.displayName} - Executing updated()","info")
	if ( state.lastUpdated && (now() - state.lastUpdated) < 500 ) return
	def syncRequired = 0
	parameterMap().each {
		if(settings."$it.key" != null) {
				if (state."$it.key" == null) { state."$it.key" = [value: null, state: "synced"] }
				if (state."$it.key".value != settings."$it.key" as Integer) {
					syncRequired = 1
					state."$it.key".value = settings."$it.key" as Integer
					state."$it.key".state = "notSynced"
				}
		}
	}
	if(settings.wakeUpInterval != null) {
		if (state.wakeUpInterval == null) { state.wakeUpInterval = [value: null, state: "synced"] }
		if (state.wakeUpInterval.value != settings.wakeUpInterval as Integer) {
			syncRequired = 1
			sendEvent(name: "checkInterval", value: (settings.wakeUpInterval as Integer) * 4 + 120, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
			state.wakeUpInterval.value = settings.wakeUpInterval as Integer
			state.wakeUpInterval.state = "notSynced"
		}
	}
	if ( syncRequired !=0 ) { sendEvent(name: "syncStatus", value: "pending") }
	state.lastUpdated = now()
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
	logging("${device.displayName} woke up", "info")
	def cmdsSet = []
	def cmdsGet = []
	def cmds = []
	def Integer cmdCount = 0
	def results = [createEvent(descriptionText: "$device.displayName woke up", isStateChange: true)]
	cmdsGet << zwave.batteryV1.batteryGet()
	if (device.currentValue("syncStatus") != "synced") {
		parameterMap().each {
			if (device.currentValue("syncStatus") == "force") { state."$it.key".state = "notSynced" }
			if (state."$it.key".value != null && state."$it.key".state == "notSynced") {
				cmdsSet << zwave.configurationV2.configurationSet(configurationValue: intToParam(state."$it.key".value, it.size), parameterNumber: it.num, size: it.size)
				cmdsGet << zwave.configurationV2.configurationGet(parameterNumber: it.num)
				cmdCount = cmdCount + 1
			}
		}
		if (device.currentValue("syncStatus") == "force") { state.wakeUpInterval.state = "notSynced" }
		if (state.wakeUpInterval.value != null && state.wakeUpInterval.state == "notSynced") {
			cmdsSet << zwave.wakeUpV2.wakeUpIntervalSet(seconds: state.wakeUpInterval.value as Integer, nodeid: zwaveHubNodeId)
			//cmdsGet << zwave.wakeUpV2.wakeUpIntervalGet() //not roking becaouse SmartThings... ;D
			cmdCount = cmdCount + 1
		}
		logging("${device.displayName} - Not synced, syncing ${cmdCount} parameters", "info")
		sendEvent(name: "syncStatus", value: "inProgress")
		runIn((5+cmdCount*1.5), syncCheck)
	}
	if (cmdsSet) { 
		cmds = encapSequence(cmdsSet,500)
		cmds << "delay 500" 
	}
	cmds = cmds + encapSequence(cmdsGet,1000)
	cmds << "delay "+(5000+cmdCount*1500)
	cmds << encap(zwave.wakeUpV1.wakeUpNoMoreInformation())
	results = results + response(cmds)
	
	return results
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	def paramKey = parameterMap().find( {it.num == cmd.parameterNumber } ).key
	logging("${device.displayName} - Parameter ${paramKey} value is ${cmd.scaledConfigurationValue} expected " + state."$paramKey".value, "info")
	if (state."$paramKey".value == cmd.scaledConfigurationValue) {
		state."$paramKey".state = "synced"
	}
}

def zwaveEvent(physicalgraph.zwave.commands.applicationstatusv1.ApplicationRejectedRequest cmd) {
	logging("${device.displayName} - rejected request!","warn")
	if (device.currentValue("syncStatus") == "inProgress") { sendEvent(name: "syncStatus", value:"failed") }
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpIntervalReport  cmd) { 
	log.debug "interval! " + cmd
}

def syncCheck() {
	logging("${device.displayName} - Executing syncCheck()","info")
	def Integer count = 0
	state.wakeUpInterval.state  = "synced"
	if (device.currentValue("syncStatus") != "synced") {
		parameterMap().each {
			if (state."$it.key".state == "notSynced" ) {
				count = count + 1
			} 
		}
	}
	if (count == 0) {
		logging("${device.displayName} - Sync Complete","info")
		sendEvent(name: "syncStatus", value: "synced")
	} else {
		logging("${device.displayName} Sync Incomplete","info")
		if (device.currentValue("syncStatus") != "failed") {
			sendEvent(name: "syncStatus", value: "incomplete")
		}
	}
}

// Copied from Fibaro official code.
def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) { 
	log.debug "manufacturerId:   ${cmd.manufacturerId}"
	log.debug "manufacturerName: ${cmd.manufacturerName}"
	log.debug "productId:		${cmd.productId}"
	log.debug "productTypeId:	${cmd.productTypeId}"
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.DeviceSpecificReport cmd) { 
	log.debug "deviceIdData:				${cmd.deviceIdData}"
	log.debug "deviceIdDataFormat:		  ${cmd.deviceIdDataFormat}"
	log.debug "deviceIdDataLengthIndicator: ${cmd.deviceIdDataLengthIndicator}"
	log.debug "deviceIdType:				${cmd.deviceIdType}"
	
	if (cmd.deviceIdType == 1 && cmd.deviceIdDataFormat == 1) {//serial number in binary format
		String serialNumber = "h'"
		cmd.deviceIdData.each{ data ->
			serialNumber += "${String.format("%02X", data)}"
		}
		updateDataValue("serialNumber", serialNumber)
		log.debug "${device.displayName} - serial number: ${serialNumber}"
	}
}

def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {	
	updateDataValue("version", "${cmd.applicationVersion}.${cmd.applicationSubVersion}")
	log.debug "applicationVersion:	  ${cmd.applicationVersion}"
	log.debug "applicationSubVersion:   ${cmd.applicationSubVersion}"
	log.debug "zWaveLibraryType:		${cmd.zWaveLibraryType}"
	log.debug "zWaveProtocolVersion:	${cmd.zWaveProtocolVersion}"
	log.debug "zWaveProtocolSubVersion: ${cmd.zWaveProtocolSubVersion}"
}

def zwaveEvent(physicalgraph.zwave.commands.deviceresetlocallyv1.DeviceResetLocallyNotification cmd) {
	log.warn "${device.displayName} - received command: $cmd - device has reset itself"
}

def configure() {
	log.debug "Executing 'configure'"
	// Device-Watch simply pings if no device events received for 8 hrs & 2 minutes
	sendEvent(name: "checkInterval", value: 8 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])

	def cmds = []
	
	cmds += zwave.wakeUpV2.wakeUpIntervalSet(seconds: 7200, nodeid: zwaveHubNodeId)//FGMS' default wake up interval
	cmds += zwave.manufacturerSpecificV2.manufacturerSpecificGet()
	cmds += zwave.manufacturerSpecificV2.deviceSpecificGet()
	cmds += zwave.versionV1.versionGet()
	cmds += zwave.associationV2.associationSet(groupingIdentifier:1, nodeId:[zwaveHubNodeId])
	cmds += zwave.batteryV1.batteryGet()
	cmds += zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 1, scale: 0)
	cmds += zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 3, scale: 1)
	cmds += zwave.wakeUpV2.wakeUpNoMoreInformation()
	
	encapSequence(cmds, 500)
}

/*
####################
## Z-Wave Toolkit ##
####################
*/
def parse(String description) {	  
	def result = []
	logging("${device.displayName} - Parsing: ${description}")
	if (description.startsWith("Err 106")) {
		result = createEvent(
			descriptionText: "Failed to complete the network security key exchange. If you are unable to receive data from it, you must remove it from your network and add it again.",
			eventType: "ALERT",
			name: "secureInclusion",
			value: "failed",
			displayed: true
		)
	} else if (description == "updated") {
		return null
	} else {
		def cmd = zwave.parse(description, cmdVersions()) 
		if (cmd) {
			logging("${device.displayName} - Parsed: ${cmd}")
			zwaveEvent(cmd)
		}
	}
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand(cmdVersions()) 
	if (encapsulatedCommand) {
		logging("${device.displayName} - Parsed SecurityMessageEncapsulation into: ${encapsulatedCommand}")
		zwaveEvent(encapsulatedCommand)
	} else {
		log.warn "Unable to extract secure cmd from $cmd"
	}
}

def zwaveEvent(physicalgraph.zwave.commands.crc16encapv1.Crc16Encap cmd) {
	def version = cmdVersions()[cmd.commandClass as Integer]
	def ccObj = version ? zwave.commandClass(cmd.commandClass, version) : zwave.commandClass(cmd.commandClass)
	def encapsulatedCommand = ccObj?.command(cmd.command)?.parse(cmd.data)
	if (encapsulatedCommand) {
		logging("${device.displayName} - Parsed Crc16Encap into: ${encapsulatedCommand}")
		zwaveEvent(encapsulatedCommand)
	} else {
		log.warn "Could not extract crc16 command from $cmd"
	}
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand(cmdVersions())
	if (encapsulatedCommand) {
   		logging("${device.displayName} - Parsed MultiChannelCmdEncap ${encapsulatedCommand}")
		zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint as Integer)
	} else {
		log.warn "Could not extract multi channel command from $cmd"
	}
}

private logging(text, type = "debug") {
	if (settings.logging == "true") {
		log."$type" text
	}
}

private secEncap(physicalgraph.zwave.Command cmd) {
	logging("${device.displayName} - encapsulating command using Secure Encapsulation, command: $cmd","info")
	zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
}

private crcEncap(physicalgraph.zwave.Command cmd) {
		logging("${device.displayName} - encapsulating command using CRC16 Encapsulation, command: $cmd","info")
		zwave.crc16EncapV1.crc16Encap().encapsulate(cmd).format() // doesn't work righ now because SmartThings...
		//"5601${cmd.format()}0000"
}

private encap(physicalgraph.zwave.Command cmd) {
	if (zwaveInfo.zw.contains("s") && zwaveInfo.sec.contains(Integer.toHexString(cmd.commandClassId).toUpperCase())) { 
		// if device is included securly and the command is on list of commands dupported with secure encapsulation
		secEncap(cmd)
	} else if (zwaveInfo.cc.contains("56")){ 
		// if device supports crc16
		crcEncap(cmd)
	} else { // if all else fails send plain command 
		logging("${device.displayName} - no encapsulation supported for command: $cmd","info")
		cmd.format()
	}
}

private encapSequence(cmds, delay=250) {
	delayBetween(cmds.collect{ encap(it) }, delay)
}

private List intToParam(Long value, Integer size = 1) {
	def result = []
	size.times { 
		result = result.plus(0, (value & 0xFF) as Short)
		value = (value >> 8)
	}
	return result
}
/*
##########################
## Device Configuration ##
##########################
*/
private Map cmdVersions() {
	//[0x5E: 2, 0x59: 1, 0x80: 1, 0x56: 1, 0x7A: 3, 0x73: 1, 0x98: 1, 0x22: 1, 0x85: 2, 0x5B: 1, 0x70: 1, 0x8E: 2, 0x86: 2, 0x84: 2, 0x75: 2, 0x72: 2] //Fibaro KeyFob
	[0x5E: 1, 0x86: 1, 0x72: 2, 0x59: 1, 0x80: 1, 0x73: 1, 0x56: 1, 0x22: 1, 0x31: 5, 0x98: 1, 0x7A: 3, 0x20: 1, 0x5A: 1, 0x85: 2, 0x84: 2, 0x71: 3, 0x8E: 1, 0x70: 2, 0x30: 1, 0x9C: 1] //Fibaro Motion Sensor ZW5
	//[0x5E: 1, 0x86: 1, 0x72: 1, 0x59: 1, 0x73: 1, 0x22: 1, 0x56: 1, 0x32: 3, 0x71: 1, 0x98: 1, 0x7A: 1, 0x25: 1, 0x5A: 1, 0x85: 2, 0x70: 2, 0x8E: 1, 0x60: 3, 0x75: 1, 0x5B: 1] //Fibaro Double Switch 2 (FGS-223) & FIBARO Single Switch 2 (FGS-213)
}

private parameterMap() {[
	[key: "motionSensitivity", num: 1, size: 2, type: "number", def: 15, min: 8, max: 255, title: "Sensitivity", descr: null], 
	[key: "motionBlindTime", num: 2, size: 1, type: "number", def: 15, min: 0, max: 15,  title: "Blind time", descr: null], 
	//[key: "motionPulseCounter", num: 3, size: 1, type: "enum", options: [0: "1 Pulse", 1: "2 Pulses (default)", 2: "3 Pulses", 3: "4 Pulses"], def: 1, title: "Pulse counter", descr: null], 
	[key: "motionWindowTime", num: 4, size: 1, type: "enum", options: [0: "0 - 4 sec", 1: "1 - 8 sec", 2: "2 - 12 sec (default)", 3: "3 - 16 sec"], def: "2", title: "Time Window", descr: null], 
	[key: "motionCancelationDelay", num: 6, size: 2, type: "number", def: 30, min: 1, max: 32767, title: "Alarm cancellation delay", descr: null], 
	[key: "motionOperatingMode", num: 8, size: 1, type: "enum", options: [0: "0 - Always Active (default)", 1: "1 - Active During Day", 2: "2 - Active During Night"], def: "0", title: "Operating mode", descr: null], 
	[key: "motionNightDay", num: 9, size: 2, type: "number", def: 200, min: 1, max: 32767, title: "Night/day", descr: null], 
	[key: "tamperSensitivity", num: 20, size: 1, type: "number", def: 20, min: 0, max: 121, title: "Sensitivity", descr: null], 
	[key: "tamperCancelationDelay", num: 22, size: 2, type: "number", def: 30, min: 1, max: 32767, title: "Alarm cancellation delay", descr: null], 
	[key: "tamperOperatingMode", num: 24, size: 1, type: "enum", options: [0: "0 - Tamper only (default)", 1: "1 - Tamper and Earthquake Detector", 2: "2 - Tamper and Orientation"], def: "0", title: "Operating mode", descr: null], 
	[key: "tamperAlarmCancelation", num: 25, size: 1, type: "enum", options: [0: "0 - Do not send tamper cancellation report", 1: "1 - Send tamper cancellation report (default)"], def: "1", title: "Alarm cancellation", descr: null], 
	[key: "illuminanceThreshold", num: 40, size: 2, type: "number", def: 200, min: 0, max: 32767, title: "Reporting threshold", descr: null], 
	[key: "illuminanceInterval", num: 42, size: 2, type: "number", def: 3600, min: 0, max: 32767, title: "Reporting interval", descr: null], 
	[key: "temperatureThreshold", num: 60, size: 2, type: "number", def: 10, min: 0, max: 255, title: "Reporting threshold", descr: null], 
	[key: "temperatureMeasuringInterval", num: 62, size: 2, type: "number", def: 900, min: 0, max: 32767, title: "Measuring interval", descr: null],
	[key: "temperatureReportInterval", num: 64, size: 2, type: "number", def: 0, min: 0, max: 32767, title: "Reporting Interval", descr: null], 
	[key: "temperatureOffset", num: 66, size: 2, type: "number", def: 0, min: -1000, max: 1000, title: "Temperature offset", descr: null], 
	[key: "ledMode", num: 80, size: 1, type: "enum", options: [
			0: "0 - LED inactive", 
			1: "1 - temp Dependent (1 long blink)", 
			2: "2 - flashlight Mode (1 long blink)", 
			3: "3 - white (1 long blink)", 
			4: "4 - red (1 long blink)", 
			5: "5 - green (1 long blink)", 
			6: "6 - blue (1 long blink)", 
			7: "7 - yellow (1 long blink)", 
			8: "8 - cyan (1 long blink)", 
			9: "9 - magenta (1 long blink)", 
			10: "10 - temp dependent (1 long 1 short blink) (default)", 
			11: "11 - flashlight Mode (1 long 1 short blink)", 
			12: "12 - white (1 long 1 short blink)", 
			13: "13 - red (1 long 1 short blink)", 
			14: "14 - green (1 long 1 short blink)", 
			15: "15 - blue (1 long 1 short blink)", 
			16: "16 - yellow (1 long 1 short blink)", 
			17: "17 - cyan (1 long 1 short blink)", 
			18: "18 - magenta (1 long 1 short blink)",
			19: "19 - temp Dependent (1 long 2 short blink)", 
			20: "20 - white (1 long 2 short blinks)", 
			21: "21 - red (1 long 2 short blinks)", 
			22: "22 - green (1 long 2 short blinks)", 
			23: "23 - blue (1 long 2 short blinks)", 
			24: "24 - yellow (1 long 2 short blinks)", 
			25: "25 - cyan (1 long 2 short blinks)", 
			26: "26 - magenta (1 long 2 short blinks)"
		], def: "10", title: "Signalling mode", descr: null], 
	[key: "ledBrightness", num: 81, size: 1, type: "number", def: 50, min: 0, max: 100, title: "Brightness", descr: null], 
	[key: "ledLowBrightness", num: 82, size: 2, type: "number", def: 100, min: 0, max: 32767, title: "Illuminance for low indicator brightness", descr: null], 
	[key: "ledHighBrightness", num: 83, size: 2, type: "number", def: 1000, min: 0, max: 32767, title: "Illuminance for high indicator brightness", descr: null], 
	[key: "ledTemperatureBlue", num: 86, size: 2, type: "number", def: 18, min: 0, max: 255, title: "Temperature for blue colour", descr: null], 
	[key: "ledTemperatureRed", num: 87, size: 2, type: "number", def: 28, min: 0, max: 255, title: "Temperature for red colour", descr: null], 
	[key: "ledTamperAlarm", num: 89, size: 1, type: "enum", options: [0: "0 - tamper alarm is not indicated", 1: "1 - tamper alarm is indicated (default)"], def: "1", title: "Tamper alarm", descr: null]
]}
