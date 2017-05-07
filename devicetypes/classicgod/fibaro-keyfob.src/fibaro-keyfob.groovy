/**
 *  Fibaro KeyFob
 *
 *  Copyright 2017 Artur Draga
 *	
 *	Special thanks to Eric "erocm123" Maycock for help with the code.
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
	definition (name: "Fibaro KeyFob", namespace: "ClassicGOD", author: "Artur Draga") {
		capability "Actuator"
		capability "Battery"
		capability "Button"
		capability "Switch"
		capability "Configuration"

		(1..30).each{ n ->
			if (n in (1..6)) { 
				attribute "switch$n", "string"
				command "on$n"
				command "off$n"
			}
			command "button$n"
		}
		attribute "syncStatus", "string"
		attribute "batteryStatus", "string"
		command "forceSync"
		command "menuButton"

		fingerprint deviceId: "0x1801" , inClusters: "0x5E,0x59,0x80,0x56,0x7A,0x73,0x98,0x22,0x85,0x5B,0x70,0x5A,0x72,0x8E,0x86,0x84,0x75"
		fingerprint deviceId: "0x1801" , inClusters: "0x5E,0x85,0x59,0x80,0x5B,0x70,0x56,0x5A,0x7A,0x72,0x8E,0x73,0x86,0x84,0x75,0x22"
	}

	tiles (scale: 2){
		def detailList = []
		standardTile("menuButton", "device.button", inactiveLabel: false, decoration: "flat", width: 2, height: 2, canChangeIcon: true) {
			state "default", label:"PUSH",  action:"menuButton", backgroundColor: "#00A0DC", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/fibaro-keyfob.src/main_icon.png"
			state "pushed", label:"PUSH",  action:"menuButton", backgroundColor: "#FFFFFF", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/fibaro-keyfob.src/main_icon.png"
		}
		[1,2,3,7,13,8,14,9,15,19,20,21,4,5,6,10,16,11,17,12,18,22,23,24,25,26,27,28,29,30].each { n ->
			if (n in (1..6)) { //main large tiles
				def String imgUrl = "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/fibaro-keyfob.src/b0#_icon.png"
				imgUrl = imgUrl.replaceAll("#", n as String)
				detailList << "button$n"
				standardTile("button$n", "device.button", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
					state "default", label: "", action:"button$n", icon: imgUrl
				}
			} else if (n in (7..12)) { //x2 tiles
				detailList << "button$n"
				standardTile("button$n", "device.button", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
					state "default", label:"", action:"button$n", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/fibaro-keyfob.src/x2.png"
				}
			} else if (n in (13..18)) { //x3 tiles
				detailList << "button$n"
				standardTile("button$n", "device.button", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
					state "default", label:"", action:"button$n", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/fibaro-keyfob.src/x3.png"
				}
			} else if (n in (19..24)) { //hold tiles
				def i = n - 18
				detailList << "switch$i"
				standardTile("switch$i", "device.switch$i",canChangeIcon: false, width: 2, height: 1, decoration: "flat") {
					state "on", label: "", action: "off$i",  backgroundColor: "#00A0DC", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/fibaro-keyfob.src/release_icon.png"
					state "off", label: "", action: "on$i",  backgroundColor: "#ffffff", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/fibaro-keyfob.src/hold_icon.png"
				}
			} else if (n in (25..30)) { //sequence tiles
				def i = n - 24
				detailList << "button$n"
				valueTile("button$n", "device.button", inactiveLabel: false, decoration: "flat", width: 2, height: 1) {
					state "default", label:"SEQENCE $i", action:"button$n"
				}
			}
		}
		valueTile("batteryStatus", "device.batteryStatus", inactiveLabel: false, decoration: "flat", width: 4, height: 2) {
			state "val", label:'${currentValue}'
		}
		standardTile("syncStatus", "device.syncStatus", decoration: "flat", width: 2, height: 2) {
			state "synced", label:'OK', action:"forceSync", backgroundColor: "#00a0dc", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/fibaro-motion-sensor-zw5.src/images/sync_icon.png"
			state "pending", label:"Pending", action:"forceSync", backgroundColor: "#153591", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/fibaro-motion-sensor-zw5.src/images/sync_icon.png"
			state "inProgress", label:"Syncing", action:"forceSync", backgroundColor: "#44b621", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/fibaro-motion-sensor-zw5.src/images/sync_icon.png"
			state "incomplete", label:"Incomplete", action:"forceSync", backgroundColor: "#f1d801", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/fibaro-motion-sensor-zw5.src/images/sync_icon.png"
			state "failed", label:"Failed", action:"forceSync", backgroundColor: "#bc2323", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/fibaro-motion-sensor-zw5.src/images/sync_icon.png"
			state "force", label:"Force", action:"forceSync", backgroundColor: "#e86d13", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/fibaro-motion-sensor-zw5.src/images/sync_icon.png"
		}
		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2, canChangeIcon: true) {
			state "val", label:'Battery: ${currentValue}%', icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/fibaro-keyfob.src/main_icon.png"
		}
		
		detailList << "batteryStatus"
		detailList << "syncStatus"
		
		main "menuButton"
		details(detailList)
	}

	preferences {
		input (
			type: "paragraph",
			element: "paragraph",
			title: "Lock Mode:",
			description: "The KeyFob can be protected with a sequence of 2 to 5 button clicks. It can be locked by being inactive for time set or pressing and holding selected button"
		)
		input name: "protection", title: "Protection State", type: "enum", options: [0: "Unprotected", 1: "Protection by sequence"], required: false
		input name: "unlockSeq", type: "number", title: "Unlocking Sequence:", required: false
		input name: "lockTim", type: "number", title: "Time To Lock", required: false
		input name: "lockBtn", type: "number", title: "Locking Button", required: false
		input (
			type: "paragraph",
			element: "paragraph",
			title: "Sequences:",
			description: "User can create sequences of two to five button to expand numberof possible actions. \n\nSet button sequence using button numbers 1 to 6 (for example: 1234)"
		)
		parameterMap().findAll( {it.key.contains('seq')} ).each {
			input (
				name: it.key,
				title: it.descr,
				type: "number",
				required: false
			)
		}
		input (
			type: "paragraph",
			element: "paragraph",
			title: "Button Modes:",
			description: "Select button modes.\nActivating a double click will introduce delay to a single click reaction and activating a triple click will introduce delay to a double click reaction."
		)
		parameterMap().findAll( {it.key.contains('btn')} ).each {
			input (
				name: it.key,
				title: it.descr,
				type: "enum",
				options: [
					1: "1 Click", 
					2: "2 Clicks", 
					3: "1 & 2 Clicks", 
					4: "3 Clicks", 
					5: "1 & 3 Clicks", 
					6: "2 & 3 Clicks", 
					7: "1, 2 & 3 Clicks", 
					8: "Hold and Release", 
					9: "1 Click & Hold (Default)", 
					10: "2 Clicks & Hold",
					11: "1, 2 Clicks & Hold",
					12: "3 Clicks & Hold",
					13: "1, 3 Clicks & Hold",
					14: "2, 3 Clicks & Hold",
					15: "1, 2, 3 Clicks & Hold"
				],
				required: false
			)
		}
		input name: "menuButton", type: "number", title: "Button number to be activated from main menu:", required: false
		input name: "logging", title: "Logging", type: "boolean", required: false 
	}
}

def installed() {
	initialize()
}

def initialize() {
	sendEvent(name: "numberOfButtons", value: 30)
	state.lastUpdated = now()
	parameterMap().each {
		state."$it.key" = [value: null, state: "synced"]
	}
	state.protection = [value: null, state: "synced"]
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
	log.info "$device.displayName woke up"
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
				cmdsSet << zwave.configurationV2.configurationSet(configurationValue: intToParam(state."$it.key".value, it.size), parameterNumber: it.num , size: it.size)
				cmdsGet << zwave.configurationV2.configurationGet(parameterNumber: it.num)
				cmdCount = cmdCount + 1
			}
		}
		if (device.currentValue("syncStatus") == "force")  { state.protection.state = "notSynced" }
		if ( state.protection.value != null && state.protection.state == "notSynced") {
			cmdsSet << zwave.protectionV2.protectionSet(localProtectionState: state.protection.value )
			cmdsGet << zwave.protectionV2.protectionGet()
			cmdCount = cmdCount + 1
		}
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
	
	//sendHubCommand(response(cmds))
	return results
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	def paramKey
	paramKey = parameterMap().find( {it.num == cmd.parameterNumber } ).key
	if (state."$paramKey".value == cmd.scaledConfigurationValue) {
		state."$paramKey".state = "synced"
	}
}

def zwaveEvent(physicalgraph.zwave.commands.protectionv2.ProtectionReport cmd) {
	if (state.protection.value == cmd.localProtectionState) {
		state.protection.state = "synced"
	}
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	log.info "$device.displayName battery is $cmd.batteryLevel ($cmd)"
	def timeDate = new Date().format("yyyy MMM dd EEE HH:mm:ss", location.timeZone)
	sendEvent(name: "batteryStatus", value: "Battery: $cmd.batteryLevel%\n($timeDate)")
	sendEvent(name: "battery", value: cmd.batteryLevel)
}

def zwaveEvent(physicalgraph.zwave.commands.applicationstatusv1.ApplicationRejectedRequest cmd) {
	log.warn "KeyFob rejected onfiguration!"
	sendEvent(name: "syncStatus", value:"failed")
}

def zwaveEvent(physicalgraph.zwave.commands.protectionv2.ProtectionSupportedReport cmd) {
	log.debug cmd
}

def zwaveEvent(physicalgraph.zwave.commands.centralscenev1.CentralSceneNotification cmd) {
	def realButton = cmd.sceneNumber as Integer //1-6 physical, 7-12 sequences
	def keyAttribute = cmd.keyAttributes as Integer
	def Integer mappedButton
	def String action
	/* buttons:
		1-6		Single Presses
		7-12	Double Presses
		13-18	Tripple Presses
		19-24	Held Buttons
		25-30	Sequences
	*/
	if (keyAttribute in [1,2]) {
		mappedButton = 18 + realButton
		if (keyAttribute == 1) {
			action = "off"
		} else {
			action = "on"
		}
	} else {
		if (keyAttribute == 0) {
			if (realButton > 6) {
				mappedButton = 18 + realButton
			} else {
				mappedButton = realButton
			}
		} else {
			mappedButton = 6*(keyAttribute-2)+realButton
		}
		action = "pushed"
	}
	buttonEvent( mappedButton, action )
}

def updated() {
	if ( state.lastUpdated && (now() - state.lastUpdated) < 500 ) return
	sendEvent(name: "numberOfButtons", value: 30)
	def Integer tempValue 
	def Integer syncRequired = 0
	parameterMap().each {
		if(settings."$it.key" != null || it.key == "lockBtnTim" ) {
			switch (it.type) {
				case "buttonTime": tempValue = btnTimToValue(); break
				case "sequence": tempValue = seqToValue(settings."$it.key"); break
				case "mode":  tempValue = settings."$it.key" as Integer; break
				case "number": tempValue = settings."$it.key"; break
				}
				if (state."$it.key" == null) { state."$it.key" = [value: null, state: "synced"] }
				if (state."$it.key".value != tempValue) {
					syncRequired = 1
					state."$it.key".value = tempValue
					state."$it.key".state = "notSynced"
				}
		}
	}
	if (state.protection == null) { state.protection = [value: null, state: "synced"] }
	if(state.protection != null) {
		tempValue = settings.protection as Integer
		if (state.protection.value != tempValue) {
			syncRequired = 1
			state.protection.value = tempValue
			state.protection.state = "notSynced"
		}
	}
	if ( syncRequired !=0 ) { sendEvent(name: "syncStatus", value: "pending") }
	state.lastUpdated = now()
}

def on1() { buttonEvent(19,"on") }
def on2() { buttonEvent(20,"on") }
def on3() { buttonEvent(21,"on") }
def on4() { buttonEvent(22,"on") }
def on5() { buttonEvent(23,"on") }
def on6() { buttonEvent(24,"on") }
def off1() { buttonEvent(19,"off") }
def off2() { buttonEvent(20,"off") }
def off3() { buttonEvent(21,"off") }
def off4() { buttonEvent(22,"off") }
def off5() { buttonEvent(23,"off") }
def off6() { buttonEvent(24,"off") }
def button1() { buttonEvent(1,"pushed")  }
def button2() { buttonEvent(2,"pushed") }
def button3() { buttonEvent(3,"pushed") }
def button4() { buttonEvent(4,"pushed") }
def button5() { buttonEvent(5,"pushed") }
def button6() { buttonEvent(6,"pushed") }
def button7() { buttonEvent(7,"pushed") }
def button8() { buttonEvent(8,"pushed") }
def button9() { buttonEvent(9,"pushed") }
def button10() { buttonEvent(10,"pushed") }
def button11() { buttonEvent(11,"pushed") }
def button12() { buttonEvent(12,"pushed") }
def button13() { buttonEvent(13,"pushed") }
def button14() { buttonEvent(14,"pushed") }
def button15() { buttonEvent(15,"pushed") }
def button16() { buttonEvent(16,"pushed") }
def button17() { buttonEvent(17,"pushed") }
def button18() { buttonEvent(18,"pushed") }
def button25() { buttonEvent(25,"pushed") }
def button26() { buttonEvent(26,"pushed") }
def button27() { buttonEvent(27,"pushed") }
def button28() { buttonEvent(28,"pushed") }
def button29() { buttonEvent(29,"pushed") }
def button30() { buttonEvent(30,"pushed") }

def menuButton() { 
	if (settings.menuButton == null ) {
		buttonEvent(1,"pushed") 
	} else {
		buttonEvent(settings.menuButton as Integer,"pushed") 
	}
}

def buttonEvent(button, action) {
	button = button as Integer
	def switchNr = button - 18 as Integer
	if (action == "pushed") {
		sendEvent(name: "button", value: "pushed", data: [buttonNumber: button], descriptionText: "$device.displayName button $button was pushed", isStateChange: true)
	} else if (action == "on"){
		sendEvent(name: "button", value: "pushed", data: [buttonNumber: button], descriptionText: "$device.displayName button $button was pushed", isStateChange: true)
		sendEvent(name: "switch$switchNr", value: "on", data: [switchNumber: switchNr], descriptionText: "$device.displayName switch $switchNr was turned on", isStateChange: true)
	} else if  (action == "off") {
		sendEvent(name: "switch$switchNr", value: "off", data: [switchNumber: switchNr], descriptionText: "$device.displayName switch $switchNr was turned off", isStateChange: true)
	}
}

def syncCheck() {
	def Integer count = 0
	if (device.currentValue("syncStatus") != "synced") {
		parameterMap().each {
			if (state."$it.key".state == "notSynced" ) {
				count = count + 1
			} 
		}
	}
	if (state.protection.state != "synced") { count = count + 1 }
	if (count == 0) {
		sendEvent(name: "syncStatus", value: "synced")
	} else {
		if (device.currentValue("syncStatus") != "failed") {
			sendEvent(name: "syncStatus", value: "incomplete")
		}
	}
}

def forceSync() {
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

def seqToValue(sequence) { 
	sequence = sequence as String
	def Integer size = sequence.length()
	def Integer result = 0
	if (size > 5) { size = 5; log.info "Sequence too long, will be trimmed." }
	(0..size-1).each{ n ->
			result = result + ((sequence[n] as Integer) * (8**n))
	}
	return result
}

def modeToValue(mode) {
	def Integer result
	switch (mode) {
		case "1 Click": result = 1; break;
		case "2 Clicks": result = 2; break;
		case "1 & 2 Clicks": result = 3; break;
		case "3 Clicks": result = 4; break;
		case "1 & 3 Clicks": result = 5; break;
		case "3 & 2 Clicks": result = 6; break;
		case "1, 2 & 3 Clicks": result = 7; break;
		case "Hold and Release": result = 8; break;
		case "1 Click & Hold": result = 9; break;
		case "2 Clicks & Hold": result = 10; break;
		case "1, 2 Clicks & Hold": result = 11; break;
		case "3 Clicks & Hold": result = 12; break;
		case "1, 3 Clicks & Hold": result = 13; break;
		case "2, 3 Clicks & Hold": result = 14; break;
		case "1, 2, 3 Clicks & Hold": result = 15; break;
	}
	return result
}

def btnTimToValue () {
		def Integer buttonVal
		def Integer timeVal
		def Integer tempValue
		if (lockBtn) { buttonVal = (lockBtn as Integer)*256 } else { buttonVal = 0 }
		if (lockTim) { timeVal = lockTim } else { timeVal = 0 }
		if (timeVal > 255) { timeVal = 255 }
		if (timeVal < 5 && timeVal != 0) { timeVal = 5 }
		if (buttonVal > 1536) { buttonVal = 1536 }
		return buttonVal+timeVal
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
	[0x5E: 2, 0x59: 1, 0x80: 1, 0x56: 1, 0x7A: 3, 0x73: 1, 0x98: 1, 0x22: 1, 0x85: 2, 0x5B: 1, 0x70: 2, 0x8E: 2, 0x86: 2, 0x84: 2, 0x75: 2, 0x72: 2] //Fibaro KeyFob
	//[0x5E: 1, 0x86: 1, 0x72: 2, 0x59: 1, 0x80: 1, 0x73: 1, 0x56: 1, 0x22: 1, 0x31: 5, 0x98: 1, 0x7A: 3, 0x20: 1, 0x5A: 1, 0x85: 2, 0x84: 2, 0x71: 3, 0x8E: 1, 0x70: 2, 0x30: 1, 0x9C: 1] //Fibaro Motion Sensor ZW5
	//[0x5E: 1, 0x86: 1, 0x72: 1, 0x59: 1, 0x73: 1, 0x22: 1, 0x56: 1, 0x32: 3, 0x71: 1, 0x98: 1, 0x7A: 1, 0x25: 1, 0x5A: 1, 0x85: 2, 0x70: 2, 0x8E: 1, 0x60: 3, 0x75: 1, 0x5B: 1] //Fibaro Double Switch 2 (FGS-223) & FIBARO Single Switch 2 (FGS-213)
}

def parameterMap() { return [ 
	[key: "unlockSeq", num: 1, size: 2, descr: "Unlocking Sequence", type: "sequence"], 
	[key: "lockBtnTim", num: 2, size: 2, descr: "Lock Time and Button", type: "buttonTime"], 
	[key: "seq1", num: 3, size: 2, descr: "First Sequence", type: "sequence"], 
	[key: "seq2", num: 4, size: 2, descr: "Second Sequence", type: "sequence"], 
	[key: "seq3", num: 5, size: 2, descr: "Third Sequence", type: "sequence"], 
	[key: "seq4", num: 6, size: 2, descr: "Fourth Sequence", type: "sequence"], 
	[key: "seq5", num: 7, size: 2, descr: "Fifth Sequence", type: "sequence"], 
	[key: "seq6", num: 8, size: 2, descr: "Sixth Sequence", type: "sequence"], 
	[key: "seqTim", num: 9, size: 1, descr: "Sequence Timeout", type: "number"], 
	[key: "btn1mode", num: 21, size: 1, descr: "Square (1) Button Mode", type: "mode"], 
	[key: "btn2mode", num: 22, size: 1, descr: "Circle (2) Button Mode", type: "mode"], 
	[key: "btn3mode", num: 23, size: 1, descr: "Saltire (3) Button Mode", type: "mode"], 
	[key: "btn4mode", num: 24, size: 1, descr: "Triangle (4) Button Mode", type: "mode"], 
	[key: "btn5mode", num: 25, size: 1, descr: "Minus (5) Button Mode", type: "mode"], 
	[key: "btn6mode", num: 26, size: 1, descr: "Plus (6) Button Mode", type: "mode"] 
] } 
