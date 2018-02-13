/**
 *  Fibaro Universal Binary Sensor
 *
 *  Copyright 2017 Artur Draga
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
	definition (name: "Fibaro Universal Binary Sensor", namespace: "ClassicGOD", author: "Artur Draga") {
		capability "Actuator"
		capability "Button"
		capability "Switch"
		capability "Temperature Measurement"
		
		attribute "switch2", "string"
		
		fingerprint mfr: "010F", prod: "0501"
	}

	tiles (scale: 2) {
		multiAttributeTile(name:"FGDW", type:"lighting", width:6, height:4) {
			tileAttribute("device.switch", key:"PRIMARY_CONTROL") {
				attributeState("off", label:"In1: off", icon:"https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/fibaro-universal-binary-sensor.src/images/ubs.png", backgroundColor:"#ffffff")
				attributeState("on", label:"In1: on", icon:"https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/fibaro-universal-binary-sensor.src/images/ubs.png", backgroundColor:"#00a0dc")
			}
			tileAttribute("device.multiStatus", key:"SECONDARY_CONTROL") {
				attributeState("multiStatus", label:'${currentValue}')
			}
		}
		valueTile("temperature1", "device.temperature", inactiveLabel: false, width: 2, height: 2) {
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
		valueTile("temperature2", "device.temperature2", inactiveLabel: false, width: 2, height: 2) {
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
		standardTile("in2", "device.switch2", decoration: "flat", width: 2, height: 2) {
			state "off", label:'In2: Off', backgroundColor: "#cccccc"
			state "on", label:'In2: On', backgroundColor: "#00a0dc"
		}
		valueTile("temperature3", "device.temperature3", inactiveLabel: false, width: 2, height: 2) {
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
		valueTile("temperature4", "device.temperature4", inactiveLabel: false, width: 2, height: 2) {
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
	}

	preferences {
	
		input (
			title: "Fibaro Universal Binary Sensor manual",
			description: "Tap to view the manual.",
			image: "https://manuals.fibaro.com/wp-content/uploads/2017/02/ubs_icon.jpg",
			url: "https://manuals.fibaro.com/content/manuals/en/FGBS-321/FGBS-001-EN-A-v1.1.pdf",
			type: "href",
			element: "href"
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

//Configuration and synchronization
def updated() {
	logging("Executing updated()","info")
	if ( state.lastUpdated && (now() - state.lastUpdated) < 500 ) return
	def cmds = []
	logging("${device.displayName} - Executing updated()","info")
	
	runIn(5,"syncStart")
	state.lastUpdated = now()
	configure()
	
}

def configure() {
	logging("Executing configure()","info")
	def cmds = []
	cmds << response(encap(zwave.associationV2.associationGet(groupingIdentifier: 3))) //verify if group 3 association is correct
	sendHubCommand(cmds,3000)
}

private syncStart() {
	boolean syncNeeded = false
	Integer settingValue = null
	parameterMap().each {
		if(settings."$it.key" != null || it.num == 2) {
			settingValue = settings."$it.key" as Integer
			if (state."$it.key" == null) { state."$it.key" = [value: null, state: "synced"] } 
			if (state."$it.key".value != settingValue || state."$it.key".state != "synced" ) {
				state."$it.key".value = settingValue
				state."$it.key".state = "notSynced"
				syncNeeded = true
			}
		}
	}
	if ( syncNeeded ) { 
		logging("sync needed.", "info")
		syncNext()
	}
}

private syncNext() {
	logging("Executing syncNext()","info")
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
		sendHubCommand(cmds,3000)
	} else {
		runIn(1, "syncCheck")
	}
}

private syncCheck() {
	logging("Executing syncCheck()","info")
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
		multiStatusEvent("Sync incomplete! Open settings and tap Done to try again.", true, true)
	} else {
		if (device.currentValue("multiStatus")?.contains("Sync")) { multiStatusEvent("Sync OK.", true, true) }
	}
	runIn(5, "updateSensor")
}

private multiStatusEvent(String statusValue, boolean force = false, boolean display = false) {
	if (!device.currentValue("multiStatus")?.contains("Sync") || device.currentValue("multiStatus") == "Sync OK." || force) {
		sendEvent(name: "multiStatus", value: statusValue, descriptionText: statusValue, displayed: display)
	}
}

//event handlers related to configuration and sync
def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	def paramKey = parameterMap().find( {it.num == cmd.parameterNumber } ).key
	logging("Parameter ${paramKey} value is ${cmd.scaledConfigurationValue} expected " + state."$paramKey".value, "info")
	state."$paramKey".state = (state."$paramKey".value == cmd.scaledConfigurationValue) ? "synced" : "incorrect"
	syncNext()
}

def zwaveEvent(physicalgraph.zwave.commands.applicationstatusv1.ApplicationRejectedRequest cmd) {
	logging("rejected request!","warn")
	for ( param in parameterMap() ) {
		if ( state."$param.key"?.state == "inProgress" ) {
			state."$param.key"?.state = "failed"
			break
		} 
	}
}

def zwaveEvent(physicalgraph.zwave.commands.associationv2.AssociationReport cmd) {
	def cmds = []
	if (cmd.groupingIdentifier == 3) {
		if (cmd.nodeId != [zwaveHubNodeId]) {
			log.debug "${device.displayName} - incorrect MultiChannel Association for Group 3! nodeId: ${cmd.nodeId} will be changed to [0, ${zwaveHubNodeId}, 1]"
			cmds << zwave.associationV2.associationRemove(groupingIdentifier: 1)
			cmds << zwave.associationV2.associationRemove(groupingIdentifier: 3)
			cmds << zwave.associationV2.associationSet(groupingIdentifier: 3, nodeId: [zwaveHubNodeId])
		} else {
			logging("${device.displayName} - MultiChannel Association for Group 1 correct.","info")
		}
	}
	if (cmds) { [response(encapSequence(cmds, 1000))] }
}

//event handlers
def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd, ep = null) {
	logging("${device.displayName} - BasicSet received: ${cmd} - $ep", "info")
	sendEvent(name: (ep == 1) ? "switch" : "switch2", value: (cmd.value == 255) ? "on" : "off", displayed: true)
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd, ep = null) {
	logging("${device.displayName} - SensorMultilevelReport received, sensorType: ${cmd.sensorType}, scaledSensorValue: ${cmd.scaledSensorValue} ep: ${ep}", "info")
	if (cmd.sensorType == 1) {
		def cmdScale = cmd.scale == 1 ? "F" : "C"
		sendEvent(name: (ep == 3) ? "temperature" : "temperature${ep-2}", unit: getTemperatureScale(), value: convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale, cmd.precision), displayed: true)
		if (ep == 3) multiStatusEvent("${convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale, cmd.precision)}°")
		
	}
}

def zwaveEvent(physicalgraph.zwave.commands.sceneactivationv1.SceneActivationSet cmd) {
	logging("${device.displayName} - SceneActivationSet received, ${cmd}", "info")
	switch (cmd.sceneId) {
		case 10: buttonEvent(1,"pushed"); break;
		case 11: break;
		case 12: buttonEvent(1,"held"); break;
		case 13: buttonEvent(1,"released"); break;
		case 14: buttonEvent(3,"pushed"); break;
		case 15: buttonEvent(5,"pushed"); break;
		case 20: buttonEvent(2,"pushed"); break;
		case 21: break;
		case 22: buttonEvent(2,"held"); break;
		case 23: buttonEvent(2,"released"); break;
		case 24: buttonEvent(4,"pushed"); break;
		case 25: buttonEvent(6,"pushed"); break;
	}
}

def buttonEvent(button, action) {
	sendEvent(name: "button", value: action, data: [buttonNumber: button], descriptionText: "$device.displayName button $button was $action", isStateChange: true, displayed: false)
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
		log.warn "Unable to extract Secure command from $cmd"
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
		log.warn "Unable to extract CRC16 command from $cmd"
	}
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand(cmdVersions())
	if (encapsulatedCommand) {
		logging("${device.displayName} - Parsed MultiChannelCmdEncap ${encapsulatedCommand}")
		zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint as Integer)
	} else {
		log.warn "Unable to extract MultiChannel command from $cmd"
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
	logging("${device.displayName} - encapsulating command using MultiChannel Encapsulation, ep: $ep command: $cmd","info")
	zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint:ep).encapsulate(cmd)
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
	[0x30: 2, 0x60: 3, 0x85: 2, 0x8E: 2, 0x72: 2, 0x70: 2, 0x86: 1, 0x7A: 2, 0x2B: 1, 0x9C: 1, 0x31: 5] // Fibaro Universal Binary Sensor
}

private parameterMap() {[
	[key: "IN1cancelationDelay", num: 1, size: 2, type: "number", def: 0, min: 0, max: 65535 , title: "Input I alarm cancellation delay", 
		descr: "Additional delay after an alarm from input IN1 has ceased."],
	[key: "IN2cancelationDelay", num: 2, size: 2, type: "number", def: 0, min: 0, max: 65535 , title: "Input II alarm cancellation delay", 
		descr: "Additional delay after an alarm from input IN2 has ceased."],
	[key: "IN1type", num: 3, size: 1, type: "enum", options: [
		0: "INPUT_NO (Normal Open)",
		1: "INPUT_NC (Normal Close)",
		2: "INPUT_MONOSTABLE",
		3: "INPUT_BISTABLE"
		], def: "1", min: 0, max: 255 , title: "Type of input no. 1", descr: null],
	[key: "IN2type", num: 4, size: 1, type: "enum", options: [
		0: "INPUT_NO (Normal Open)",
		1: "INPUT_NC (Normal Close)",
		2: "INPUT_MONOSTABLE",
		3: "INPUT_BISTABLE"
		], def: "1", min: 0, max: 255 , title: "Type of input no. 2", descr: null],
	[key: "tempReadInterval", num: 10, size: 2, type: "number", def: 20, min: 0, max: 255 , title: "Temperature reading interval", 
		descr: "Interval between successive readings of temperature from all sensors connected to the device."],
	[key: "sceneFunctionality", num: 14, size: 1, type: "enum", options: [
		0: "scenes inactive",
		1: "scenes active"
		], def: "0", min: 0, max: 1 , title: "Scene activation functionality", descr: null],
]}
