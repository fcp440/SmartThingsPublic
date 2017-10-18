/**
 *  Fibaro Single Switch 2
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
	definition (name: "Fibaro Single Switch 2", namespace: "ClassicGOD", author: "Artur Draga") {
		capability "Switch"
		capability "Energy Meter"
		capability "Power Meter"
		capability "Button"
		capability "Configuration"
		capability "Health Check"

		command "reset"
		command "refresh"
		fingerprint mfr: "010F", prod: "0403"
		fingerprint deviceId: "0x1001", inClusters:"0x5E,0x86,0x72,0x59,0x73,0x22,0x56,0x32,0x71,0x98,0x7A,0x25,0x5A,0x85,0x70,0x8E,0x60,0x75,0x5B"
		fingerprint deviceId: "0x1001", inClusters:"0x5E,0x86,0x72,0x59,0x73,0x22,0x56,0x32,0x71,0x7A,0x25,0x5A,0x85,0x70,0x8E,0x60,0x75,0x5B"
	}

	tiles (scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 3, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "off", label: 'Off', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState:"turningOn"
				attributeState "on", label: 'On', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00a0dc", nextState:"turningOff"
				attributeState "turningOn", label:'Turning On', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#00a0dc", nextState:"turningOff"
				attributeState "turningOff", label:'Turning Off', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
			}
			tileAttribute("device.multiStatus", key:"SECONDARY_CONTROL") {
				attributeState("multiStatus", label:'${currentValue}')
			} 
		}
		valueTile("power", "device.power", decoration: "flat", width: 2, height: 2) {
			state "power", label:'${currentValue}\nW', action:"refresh"
		}
		valueTile("energy", "device.energy", decoration: "flat", width: 2, height: 2) {
			state "energy", label:'${currentValue}\nkWh', action:"refresh"
		}
		valueTile("reset", "device.energy", decoration: "flat", width: 2, height: 2) {
			state "reset", label:'reset\nkWh', action:"reset"
		}
	}

	preferences {
    	input (
			title: "Fibaro Single Switch 2",
			description: "Tap to view the manual.",
			image: "http://manuals.fibaro.com/wp-content/uploads/2016/08/switch2_icon.jpg",
			url: "http://manuals.fibaro.com/content/manuals/en/FGS-2x3/FGS-2x3-EN-T-v1.2.pdf",
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

//UI and tile functions
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

def on() {
	encap(zwave.basicV1.basicSet(value: 255))
}

def off() {
	encap(zwave.basicV1.basicSet(value: 0))
}

def reset() {
	def cmds = []
	cmds << zwave.meterV3.meterReset()
	cmds << zwave.meterV3.meterGet(scale: 0)
	cmds << zwave.meterV3.meterGet(scale: 2)
	encapSequence(cmds,1000)
}

def refresh() {
	def cmds = []
	cmds << zwave.meterV3.meterGet(scale: 0)
	cmds << zwave.meterV3.meterGet(scale: 2)
	encapSequence(cmds,1000)
}

//Configuration and synchronization
def updated() {
	if ( state.lastUpdated && (now() - state.lastUpdated) < 500 ) return
	def cmds = []
	logging("Executing updated()","info")

	if (device.currentValue("numberOfButtons") != 6) { sendEvent(name: "numberOfButtons", value: 6) }
	
    state.lastUpdated = now()
    syncStart()
}

private syncStart() {
	boolean syncNeeded = false
    Integer settingValue = null
	parameterMap().each {
		if(settings."$it.key" != null) {
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
		sendHubCommand(cmds,1000)
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

//event handlers
def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	//ignore
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	logging("SwitchBinaryReport received, value: ${cmd.value} ","info")
	sendEvent([name: "switch", value: (cmd.value == 0 ) ? "off": "on"])
}

def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd) {
	logging("MeterReport received, value: ${cmd.scaledMeterValue} scale: ${cmd.scale} ","info")	
	switch (cmd.scale) {
		case 0: sendEvent([name: "energy", value: cmd.scaledMeterValue, unit: "kWh"]); break;
		case 2: sendEvent([name: "power", value: cmd.scaledMeterValue, unit: "W"]); break;
	}
    multiStatusEvent("${device.currentValue("power")} W / ${device.currentValue("energy")} kWh")
}

def zwaveEvent(physicalgraph.zwave.commands.centralscenev1.CentralSceneNotification cmd) {
	logging("CentralSceneNotification received, sceneNumber: ${cmd.sceneNumber} keyAttributes: ${cmd.keyAttributes}","info")
	def String action
	def Integer button
	switch (cmd.keyAttributes as Integer) {
		case 0: action = "pushed"; button = cmd.sceneNumber; break
		case 1: action = "released"; button = cmd.sceneNumber; break
		case 2: action = "held"; button = cmd.sceneNumber; break
		case 3: action = "pushed"; button = 2+(cmd.sceneNumber as Integer); break
		case 4: action = "pushed"; button = 4+(cmd.sceneNumber as Integer); break
	}
	sendEvent(name: "button", value: action, data: [buttonNumber: button], isStateChange: true)
}

/*
####################
## Z-Wave Toolkit ##
####################
*/
def parse(String description) {	  
	def result = []
	logging("Parsing: ${description}")
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
			logging("Parsed: ${cmd}")
			zwaveEvent(cmd)
		}
	}
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand(cmdVersions()) 
	if (encapsulatedCommand) {
		logging("Parsed SecurityMessageEncapsulation into: ${encapsulatedCommand}")
		zwaveEvent(encapsulatedCommand)
	} else {
		logging("Unable to extract Secure command from $cmd","warn")
	}
}

def zwaveEvent(physicalgraph.zwave.commands.crc16encapv1.Crc16Encap cmd) {
	def version = cmdVersions()[cmd.commandClass as Integer]
	def ccObj = version ? zwave.commandClass(cmd.commandClass, version) : zwave.commandClass(cmd.commandClass)
	def encapsulatedCommand = ccObj?.command(cmd.command)?.parse(cmd.data)
	if (encapsulatedCommand) {
		logging("Parsed Crc16Encap into: ${encapsulatedCommand}")
		zwaveEvent(encapsulatedCommand)
	} else {
		logging("Unable to extract CRC16 command from $cmd","warn")
	}
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand(cmdVersions())
	if (encapsulatedCommand) {
		logging("Parsed MultiChannelCmdEncap ${encapsulatedCommand}")
		zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint as Integer)
	} else {
		logging("Unable to extract MultiChannel command from $cmd","warn")
	}
}

private logging(text, type = "debug") {
	if (settings.logging == "true" || type == "warn") {
		log."$type" "${device.displayName} - $text"
	}
}

private secEncap(physicalgraph.zwave.Command cmd) {
	logging("encapsulating command using Secure Encapsulation, command: $cmd","info")
	zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
}

private crcEncap(physicalgraph.zwave.Command cmd) {
	logging("encapsulating command using CRC16 Encapsulation, command: $cmd","info")
	zwave.crc16EncapV1.crc16Encap().encapsulate(cmd).format() 
}

private multiEncap(physicalgraph.zwave.Command cmd, Integer ep) {
	logging("encapsulating command using MultiChannel Encapsulation, ep: $ep command: $cmd","info")
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
		logging("no encapsulation supported for command: $cmd","info")
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
	[0x5E: 1, 0x86: 1, 0x72: 1, 0x59: 1, 0x73: 1, 0x22: 1, 0x56: 1, 0x32: 3, 0x71: 1, 0x98: 1, 0x7A: 1, 0x25: 1, 0x5A: 1, 0x85: 2, 0x70: 2, 0x8E: 2, 0x60: 3, 0x75: 1, 0x5B: 1] //Fibaro Single Switch 2
}

private parameterMap() {[
	[key: "restoreState", num: 9, size: 1, type: "enum", options: [0: "0 - power off after power failure", 1: "1 - restore state"], def: "1", title: "Restore state after power failure", 
    	descr: "Determines if the device will return to state prior tothe power failure after power is restored"],
	[key: "ch1operatingMode", num: 10, size: 1, type: "enum", options: [
			0: "0 - standard operation", 
			1: "1 - delay ON", 
			2: "2 - delay OFF", 
			3: "3 - auto ON", 
			4: "4 - auto OFF", 
			5: "5 - flashing mode"
		], def: "0", title: "Operating mode", descr: "Allows to choose operating mode for the 1st channel controlled by the S1 switch."],
	[key: "ch1reactionToSwitch", num: 11, size: 1, type: "enum", options: [0: "0 - cancel and set target state", 1: "1 - no reaction", 2: "2 - reset timer"], def: "0", title: "Restore state after power failure", 
    	descr: "Determines how the device in timed mode reacts to pushing the switch connected to the S1 terminal."],
	[key: "ch1timeParameter", num: 12, size: 2, type: "number", def: 50, min: 0, max: 32000, title: "Time parameter for delay/auto ON/OFF modes", 
    	descr: "Allows to set time parameter used in timed modes."],
	[key: "ch1pulseTime", num: 13, size: 2, type: "number", def: 5, min: 1, max: 32000, title: "Allows to set time of switching to opposite state in flashing mode.", 
    	descr: null],
	//[key: "ch2operatingMode", num: 15, size: 1, type: "enum", options: [
	//		0: "0 - standard operation", 
	//		1: "1 - delay ON", 
	//		2: "2 - delay OFF", 
	//		3: "3 - auto ON", 
	//		4: "4 - auto OFF", 
	//		5: "5 - flashing mode"
	//	], def: "0", title: "Operating mode", descr: null],
	//[key: "ch2reactionToSwitch", num: 16, size: 1, type: "enum", options: [0: "0 - cancel and set target state", 1: "1 - no reaction", 2: "2 - reset timer"], def: "0", title: "Restore state after power failure", descr: null],
	//[key: "ch2timeParameter", num: 17, size: 2, type: "number", def: 50, min: 0, max: 32000, title: "Time parameter for delay/auto ON/OFF modes", descr: null],
	//[key: "ch2pulseTime", num: 18, size: 2, type: "number", def: 5, min: 1, max: 32000, title: "Pulse time for flashing mode", descr: null],
	[key: "switchType", num: 20, size: 1, type: "enum", options: [0: "0 - momentary switch", 1: "1 - toggle switch (contact closed - ON, contact opened - OFF)", 2: "2 - toggle switch (device changes status when switch changes status)"], def: "2", title: "Switch type", descr: null],
	[key: "flashingReports", num: 21, size: 1, type: "enum", options: [0: "0 - do not send reports", 1: "1 - sends reports"], def: "0", title: "Flashing mode - reports", descr: null],
	[key: "s1scenesSent", num: 28, size: 1, type: "enum", options: [
			0: "0 - do not send scenes", 
			1: "1 - key pressed 1 time", 
			2: "2 - key pressed 2 times", 
			3: "3 - key pressed 1 & 2 times", 
			4: "4 - key pressed 3 times", 
			5: "5 - key pressed 1 & 3 times", 
			6: "6 - key pressed 2 & 3 times", 
			7: "7 - key pressed 1, 2 & 3 times", 
			8: "8 - key held & released", 
			9: "9 - key Pressed 1 time & held", 
			10: "10 - key pressed 2 times & held", 
			11: "11 - key pressed 1, 2 times & held", 
			12: "12 - key pressed 3 times & held", 
			13: "13 - key pressed 1, 3 times & held", 
			14: "14 - key pressed 2, 3 times & held", 
			15: "15 - key pressed 1, 2, 3 times & held"
		], def: "0", title: "Switch 1 - scenes sent", descr: null],
	[key: "s2scenesSent", num: 29, size: 1, type: "enum", options: [
			0: "0 - do not send scenes", 
			1: "1 - key pressed 1 time", 
			2: "2 - key pressed 2 times", 
			3: "3 - key pressed 1 & 2 times", 
			4: "4 - key pressed 3 times", 
			5: "5 - key pressed 1 & 3 times", 
			6: "6 - key pressed 2 & 3 times", 
			7: "7 - key pressed 1, 2 & 3 times", 
			8: "8 - key held & released", 
			9: "9 - key Pressed 1 time & held", 
			10: "10 - key pressed 2 times & held", 
			11: "11 - key pressed 1, 2 times & held", 
			12: "12 - key pressed 3 times & held", 
			13: "13 - key pressed 1, 3 times & held", 
			14: "14 - key pressed 2, 3 times & held", 
			15: "15 - key pressed 1, 2, 3 times & held"
		], def: "0", title: "Switch 2 - scenes sent", descr: null],
	[key: "ch1powerReports", num: 50, size: 1, type: "number", def: 20, min: 0, max: 100, title: "Power reports", descr: null], 
	[key: "ch1minimalTime", num: 51, size: 1, type: "number", def: 10, min: 0, max: 120, title: "Minimal time between power reports", descr: null], 
	[key: "ch1energyReports", num: 53, size: 2, type: "number", def: 100, min: 0, max: 32000, title: "Energy reports", descr: null], 
	//[key: "ch2powerReports", num: 54, size: 1, type: "number", def: 20, min: 0, max: 100, title: "Power reports", descr: null], 
	//[key: "ch2minimalTime", num: 55, size: 1, type: "number", def: 10, min: 0, max: 120, title: "Minimal time between power reports", descr: null], 
	//[key: "ch2energyReports", num: 57, size: 2, type: "number", def: 100, min: 0, max: 32000, title: "Energy reports", descr: null], 
	[key: "periodicPowerReports", num: 58, size: 2, type: "number", def: 3600, min: 0, max: 32000, title: "Periodic power reports", descr: null], 
	[key: "periodicEnergyReports", num: 59, size: 2, type: "number", def: 3600, min: 0, max: 32000, title: "Periodic energy reports", descr: null], 
	[key: "deviceEnergyConsumed", num: 60, size: 1, type: "enum", options: [0: "0 - don't measure", 1: "1 - measure"], def: "0", title: "Energy consumed by the device itself", descr: null]
]}
