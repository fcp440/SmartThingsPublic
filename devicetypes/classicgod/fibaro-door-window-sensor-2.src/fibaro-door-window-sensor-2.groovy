/**
 *  Fibaro Door/Window Sensor 2
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
	definition (name: "Fibaro Door/Window Sensor 2", namespace: "ClassicGOD", author: "Artur Draga") {	
		capability "Contact Sensor"
		capability "Tamper Alert"
		capability "Temperature Measurement"
		capability "Configuration"
		capability "Battery"
		capability "Sensor"
		capability "Health Check"
	
		attribute "temperatureAlarm", "string"
	
		fingerprint mfr: "010F", prod: "0702", model: "1000"
		fingerprint deviceId: "0x0701", inClusters:"0x5E,0x59,0x22,0x80,0x56,0x7A,0x73,0x98,0x31,0x85,0x70,0x5A,0x72,0x8E,0x71,0x86,0x84"
		fingerprint deviceId: "0x0701", inClusters:"0x5E,0x59,0x22,0x80,0x56,0x7A,0x73,0x31,0x85,0x70,0x5A,0x72,0x8E,0x71,0x86,0x84"
	}

	tiles (scale: 2) {
		multiAttributeTile(name:"FGDW", type:"lighting", width:6, height:4) {
			tileAttribute("device.contact", key:"PRIMARY_CONTROL") {
				attributeState("open", label:"open", icon:"st.contact.contact.open", backgroundColor:"#00a0dc")
				attributeState("closed", label:"closed", icon:"st.contact.contact.closed", backgroundColor:"#ffffff")
			}
			tileAttribute("device.multiStatus", key:"SECONDARY_CONTROL") {
				attributeState("multiStatus", label:'${currentValue}')
			}
		}
		
		valueTile("tamper", "device.tamper", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
			state "tamper", label:'Tamper:\n ${currentValue}'
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
		
		valueTile("battery", "device.battery", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
			state "battery", label:'${currentValue}%\n battery ', unit:"%"
		}	
		
		standardTile("temperatureAlarm", "device.temperatureAlarm", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
			state "default", label: "", backgroundColor:"#ffffff"
			state "clear", label:'', backgroundColor:"#ffffff", icon: "st.alarm.temperature.normal"
			state "underheat", label:'underheat', backgroundColor:"#1e9cbb", icon: "st.alarm.temperature.freeze"
			state "overheat", label:'overheat', backgroundColor:"#d04e00", icon: "st.alarm.temperature.overheat"
		}	

		main "FGDW"
		details(["FGDW","tamper","temperature","battery","temperatureAlarm"])
	}
		
	preferences {
		
		input (
			title: "Wake up interval",
			description: "How ofthen should your device automatically sync with the HUB.\n0 or 3600-64800 (in seconds (1-18h), 3600s (1h) step)",
			type: "paragraph",
			element: "paragraph"
		)
		
		input ( 
			name: "wakeUpInterval", 
			title: null, 
			type: "number", 
			range: "0..64800", 
			defaultValue: 21600, 
			required: false 
		)
		
		parameterMap().each {
			input (
				title: "${it.num}. ${it.title}",
				description: it.descr,
				type: "paragraph",
				element: "paragraph"
			)
			
			input (
				name: it.key,
				title: null,
				description: "Default: $it.def" ,
				type: it.type,
				options: it.options,
				range: (it.min != null && it.max != null) ? "${it.min}..${it.max}" : null,
				defaultValue: it.def,
				required: false
			)
		}
		
		input ( name: "logging", title: "Logging", type: "boolean", required: false )
	}
}

// Parameter configuration, synchronization and verification
def updated() {
	logging("${device.displayName} - Executing updated()","info")
	if ( state.lastUpdated && (now() - state.lastUpdated) < 500 ) return
	def syncRequired = 0

	if ( settings.temperatureAlarm as Integer == 0 ) { 
		sendEvent(name: "temperatureAlarm", value: null, displayed: false) 
	} else if ( settings.temperatureAlarm != null ) {
		sendEvent(name: "temperatureAlarm", value: "clear", displayed: false) 
	}
	
	syncStart()
	state.lastUpdated = now()
}

def configure() {
	def cmds = []
	cmds << zwave.batteryV1.batteryGet()
	cmds << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 1)
	encapSequence(cmds,1000)
}

private syncStart() {
	boolean syncNeeded = false
	parameterMap().each {
		if(settings."$it.key" != null) {
			if (state."$it.key" == null) { state."$it.key" = [value: null, state: "synced"] }
			if (state."$it.key".value != settings."$it.key" as Integer || state."$it.key".state != "synced" ) {
				state."$it.key".value = settings."$it.key" as Integer
				state."$it.key".state = "notSynced"
				syncNeeded = true
			}
		}
	}
	
	if(settings.wakeUpInterval != null) {
		if (state.wakeUpInterval == null) { state.wakeUpInterval = [value: null, state: "synced"] }
		if (state.wakeUpInterval.value != settings.wakeUpInterval as Integer) {
			sendEvent(name: "checkInterval", value: (settings.wakeUpInterval as Integer) * 4 + 120, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
			state.wakeUpInterval.value = settings.wakeUpInterval as Integer
			state.wakeUpInterval.state = "notSynced"
			syncNeeded = true
		}
	} else {
		sendEvent(name: "checkInterval", value: 21600 * 4 + 120, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	}
	
	if ( syncNeeded ) { 
		logging("${device.displayName} - sync needed.", "info")
		multiStatusEvent("Sync pending. Please wake up the device by pressing the tamper button.", true)
	}
}

private syncNext() {
	logging("${device.displayName} - Executing syncNext()","info")
	def cmds = []
	for ( param in parameterMap() ) {
		if ( state."$param.key"?.value != null && state."$param.key"?.state in ["notSynced","inProgress"] ) {
			multiStatusEvent("Sync in progress. (param: ${param.num})", true)
			state."$param.key"?.state = "inProgress"
			cmds << response(encap(zwave.configurationV2.configurationSet(configurationValue: intToParam(state."$param.key".value, param.size), parameterNumber: param.num, size: param.size)))
			cmds << response(encap(zwave.configurationV2.configurationGet(parameterNumber: param.num)))
			break
		} 
	}
	if (cmds) { 
		runIn(10, "syncCheck")
		sendHubCommand(cmds,1000)
	} else {
		runIn(1, "syncCheck")
	}
}

private syncCheck() {
	logging("${device.displayName} - Executing syncCheck()","info")
	def failed = []
	def incorrect = []
	def notSynced = []
	parameterMap().each {
		if (state."$it.key"?.state == "incorrect" ) {
			incorrect << it
		} else if ( state."$it.key"?.state == "failed" ) {
			failed << it
		} else if ( state."$it.key"?.state in ["inProgress","notSynced"] ) {
			notSynced << it
		}
	}
	if (failed) {
		multiStatusEvent("Sync failed! Verify parameter: ${failed[0].num}", true, true)
	} else if (incorrect) {
		multiStatusEvent("Sync mismatch! Verify parameter: ${incorrect[0].num}", true, true)
	} else if (notSynced) {
		multiStatusEvent("Sync incomplete! Wake up the device again by pressing the tamper button.", true, true)
	} else {
		sendHubCommand(response(encap(zwave.wakeUpV1.wakeUpNoMoreInformation())))
		if (device.currentValue("multiStatus")?.contains("Sync")) { multiStatusEvent("Sync OK.", true, true) }
	}
}

private multiStatusEvent(String statusValue, boolean force = false, boolean display = false) {
	if (!device.currentValue("multiStatus")?.contains("Sync") || device.currentValue("multiStatus") == "Sync OK." || force) {
		sendEvent(name: "multiStatus", value: statusValue, descriptionText: statusValue, displayed: display)
	}
}

//synchronization related event handlers
def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
	logging("${device.displayName} woke up", "info")
	def cmds = []
	sendEvent(descriptionText: "$device.displayName woke up", isStateChange: true)
	if ( state.wakeUpInterval?.state == "notSynced" && state.wakeUpInterval?.value != null ) {
		cmds << zwave.wakeUpV2.wakeUpIntervalSet(seconds: state.wakeUpInterval.value as Integer, nodeid: zwaveHubNodeId)
		state.wakeUpInterval.state = "synced"
	}
	cmds << zwave.batteryV1.batteryGet()
	cmds << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 1)
	runIn(1,"syncNext")
	[response(encapSequence(cmds,1000))]
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	def paramKey = parameterMap().find( {it.num == cmd.parameterNumber } ).key
	logging("${device.displayName} - Parameter ${paramKey} value is ${cmd.scaledConfigurationValue} expected " + state."$paramKey".value, "info")
	state."$paramKey".state = (state."$paramKey".value == cmd.scaledConfigurationValue) ? "synced" : "incorrect"
	syncNext()
}

def zwaveEvent(physicalgraph.zwave.commands.applicationstatusv1.ApplicationRejectedRequest cmd) {
	logging("${device.displayName} - rejected request!","warn")
	for ( param in parameterMap() ) {
		if ( state."$param.key"?.state == "inProgress" ) {
			state."$param.key"?.state = "failed"
			break
		} 
	}
}

//event handlers
def zwaveEvent(physicalgraph.zwave.commands.alarmv2.AlarmReport cmd) {
	logging("${device.displayName} - AlarmReport received, zwaveAlarmType: ${cmd.zwaveAlarmType}, zwaveAlarmEvent: ${cmd.zwaveAlarmEvent}", "info")
	def lastTime = new Date().format("yyyy MMM dd EEE HH:mm:ss", location.timeZone)
	switch (cmd.zwaveAlarmType) {
		case 6: 
			sendEvent(name: "contact", value: (cmd.zwaveAlarmEvent == 22)? "open":"closed"); 
			if (cmd.zwaveAlarmEvent == 22) { multiStatusEvent("Contact Open - $lastTime") }
			break;
		case 7: 
			sendEvent(name: "tamper", value: (cmd.zwaveAlarmEvent == 3)? "detected":"clear"); 
			if (cmd.zwaveAlarmEvent == 3) { multiStatusEvent("Tamper - $lastTime") }
			break;
		case 4:
			switch (cmd.zwaveAlarmEvent) {
				case 0: sendEvent(name: "temperatureAlarm", value: "clear"); break;
				case 2: sendEvent(name: "temperatureAlarm", value: "overheat"); break;
				case 6: sendEvent(name: "temperatureAlarm", value: "underheat"); break;
			}; 
			break;
		default: logging("${device.displayName} - Unknown zwaveAlarmType: ${cmd.zwaveAlarmType}","warn");
	}
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	logging("${device.displayName} - SensorMultilevelReport received, sensorType: ${cmd.sensorType}, scaledSensorValue: ${cmd.scaledSensorValue}", "info")
	switch (cmd.sensorType) {
		case 1: sendEvent(name: "temperature", value: cmd.scaledSensorValue); break;
		default: logging("${device.displayName} - Unknown sensorType: ${cmd.sensorType}","warn");
	}
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	logging("${device.displayName} - BatteryReport received, value: ${cmd.batteryLevel}", "info")
	sendEvent(name: "battery", value: cmd.batteryLevel.toString(), unit: "%", displayed: true)
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
			displayed: true,
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
		log.warn "Could not extract MultiChannel command from $cmd"
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
		zwave.crc16EncapV1.crc16Encap().encapsulate(cmd).format() 
}

private multiEncap(physicalgraph.zwave.Command cmd, Integer ep) {
	logging("${device.displayName} - encapsulating command using Multi Channel Encapsulation, ep: $ep command: $cmd","info")
	zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint:ep).encapsulate(cmd)
}

private encap(physicalgraph.zwave.Command cmd) {
	if (zwaveInfo.zw.contains("s")) { 
		secEncap(cmd)
	} else if (zwaveInfo.cc.contains("56")){ 
		crcEncap(cmd)
	} else {
		logging("${device.displayName} - no encapsulation supported for command: $cmd","info")
		cmd.format()
	}
}

private encap(physicalgraph.zwave.Command cmd, Integer ep) {
	encap(multiEncap(cmd, ep))
}

private encap(List encapList) {
	encap(encapList[0], encapList[1])
}

private encap(Map encapMap) {
	encap(encapMap.cmd, encapMap.ep)
}

private encapSequence(cmds, Integer delay=250) {
	delayBetween(cmds.collect{ encap(it) }, delay)
}

private encapSequence(cmds, Integer delay, Integer ep) {
	delayBetween(cmds.collect{ encap(it, ep) }, delay)
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
	//[0x5E: 2, 0x59: 1, 0x80: 1, 0x56: 1, 0x7A: 3, 0x73: 1, 0x98: 1, 0x22: 1, 0x85: 2, 0x5B: 1, 0x70: 2, 0x8E: 2, 0x86: 2, 0x84: 2, 0x75: 2, 0x72: 2] //Fibaro KeyFob
	//[0x5E: 2, 0x86: 1, 0x72: 2, 0x59: 1, 0x80: 1, 0x73: 1, 0x56: 1, 0x22: 1, 0x31: 5, 0x98: 1, 0x7A: 3, 0x20: 1, 0x5A: 1, 0x85: 2, 0x84: 2, 0x71: 3, 0x8E: 2, 0x70: 2, 0x30: 1, 0x9C: 1] //Fibaro Motion Sensor ZW5
	//[0x5E: 2, 0x86: 1, 0x72: 1, 0x59: 1, 0x73: 1, 0x22: 1, 0x56: 1, 0x32: 3, 0x71: 1, 0x98: 1, 0x7A: 1, 0x25: 1, 0x5A: 1, 0x85: 2, 0x70: 2, 0x8E: 2, 0x60: 3, 0x75: 1, 0x5B: 1] //Fibaro Double Switch 2
	//[0x5E: 2, 0x22: 1, 0x59: 1, 0x56: 1, 0x7A: 3, 0x32: 3, 0x71: 1, 0x73: 1, 0x98: 1, 0x31: 5, 0x85: 2, 0x70: 2, 0x72: 2, 0x5A: 1, 0x8E: 2, 0x25: 1, 0x86: 2] //Fibaro Wall Plug ZW5
	  [0x5E: 2, 0x59: 1, 0x22: 1, 0x80: 1, 0x56: 1, 0x7A: 3, 0x73: 1, 0x98: 1, 0x31: 5, 0x85: 2, 0x70: 2, 0x5A: 1, 0x72: 2, 0x8E: 2, 0x71: 2, 0x86: 1, 0x84: 2] //Fibaro Door/Window Sensor 2
}

private parameterMap() {[
	[key: "doorState", num: 1, size: 1, type: "enum", options: [0: "0 - closed when magnet near", 1: "1 - opened when magnet near"], def: "0", title: "Door/window state", 
		descr: "Defines the state of door/window depending on the magnet position."],
	[key: "ledIndications", num: 2, size: 1, type: "enum", options: [
		//0: "0 - indicator disabled", 
		1: "1 - indication of opening/closing",
		2: "2 - indication of wake up",
		//3: "3 - indication of opening/closin & wake up",
		4: "4 - indication of device tampering",
		//5: "5 - indication of opening/closin & tampering",
		6: "6 - indication of wake up & tampering",
		//7: "7 - opening/closin, wake up & tampering"
		], 
		def: "6", title: "Visual LED indications", 
		descr: "Defines events indicated by the visual LED indicator. Disabling events might extend battery life."],
	[key: "tamperDelay", num: 30, size: 2, type: "number", def: 5, min: 0, max: 32400, title: "Tamper - alarm cancellation delay", 
		descr: "Time period after which a tamper alarm will be cancelled.\n0-32400 - time in seconds"], 
	[key: "tamperCancelation", num: 31, size: 1, type: "enum", options: [0: "0 - do not send tamper cancellation report", 1: "1 - send tamper cancellation report"], def: "1", title: "Door/window state", 
		descr: "Reporting cancellation of tamper alarm to the controller and 3rd association group."],
	[key: "temperatureMeasurement", num: 50, size: 2, type: "number", def: 300, min: 0, max: 32400, title: "Interval of temperature measurements", 
		descr: "This parameter defines how often the temperature will be measured (specific time).\n0 - temperature measurements disabled\n5-32400 - time in seconds"], 
	[key: "temperatureThreshold", num: 51, size: 2, type: "number", def: 10, min: 0, max: 300, title: "Temperature reports threshold", 
		descr: "Change of temperature resulting in temperature report being sent to the HUB.\n0 - threshold based reports disabled\n1-300 - threshold (0.1-30°C, 0.1°C step, 10 = 1°C)"], 
	[key: "temperatureInterval", num: 52, size: 2, type: "number", def: 300, min: 0, max: 32400, title: "Interval of temperature reports", 
		descr: "How often the temperature reports will be sent to the main controller (regardless of parameters 50 and 51).\n0 - periodic temperature reports disabled\n300-32400 - time in seconds"], 
	[key: "temperatureOffset", num: 53, size: 2, type: "number", def: 0, min: -1000, max: 1000, title: "Temperature offset", 
		descr: "The value to be added to the actual temperature, measured by the sensor.\n-1000–1000 (-100–100°C, 0.1°C step, 10 = 1°C)"], 
	[key: "temperatureAlarm", num: 54, size: 1, type: "enum", options: [
		0: "0 - temperature alarms disabled", 
		1: "1 - high temperature alarm",
		2: "2 - low temperature alarm",
		3: "3 - high and low temperature alarms"], 
		def: "0", title: "Temperature alarm reports", 
		descr: "Temperature alarms reported to the Z-Wave controller."],
	[key: "temperatureHigh", num: 55, size: 2, type: "number", def: 350, min: 1, max: 600, title: "High temperature alarm threshold", 
		descr: "If temperature is higher than set value, overheat high temperature alarm will be triggered.\n1-600 (0.1-60°C, 0.1°C step, 10 = 1°C)"], 
	[key: "temperatureLow", num: 56, size: 2, type: "number", def: 100, min: 0, max: 599, title: "Low temperature alarm threshold", 
		descr: "If temperature is lower than set value, low temperature alarm will be triggered.\n0-599 (0-59.9°C, 0.1°C step, 10 = 1°C)"]
	]
}
