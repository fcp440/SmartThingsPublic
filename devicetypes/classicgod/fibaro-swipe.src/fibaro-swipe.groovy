/**
 *  Fibaro Swipe
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
def buttons = ["up","down","clockwise","left","right","counterClockwise","up2","down2","left2","right2","sequence1","sequence2","sequence3","sequence4","sequence5","sequence6","program1","program2","program3","program4","program5","program6"]

metadata {
	definition (name: "Fibaro Swipe", namespace: "ClassicGOD", author: "Artur Draga") {
		capability "Actuator"
		capability "Battery"
		capability "Button"
		capability "Switch"
		capability "Configuration"
		
		attribute "switch1", "string"
		attribute "switch2", "string"
		
		command "gestureMain"
		buttons.each { button ->
			if ( button in ["clockwise","counterClockwise"] ) {
				command "${button}Start"
				command "${button}Stop"
			} else {
				command button
			}
		}

		fingerprint mfr: "010F", prod: "0D01"
		fingerprint deviceId: "0x1801", inClusters:"0x5E,0x59,0x80,0x56,0x7A,0x72,0x73,0x98,0x86,0x85,0x5B,0x70,0x5A,0x8E,0x84"
		fingerprint deviceId: "0x1801", inClusters:"0x5E,0x59,0x80,0x56,0x7A,0x72,0x73,0x86,0x85,0x5B,0x70,0x5A,0x8E,0x84"
	}
	
	tiles (scale: 2) {
		standardTile("mainButton", "device.button", inactiveLabel: false, width: 2, height: 2, decoration: "flat", canChangeIcon: true) {
			state "default", label:"SWIPE", action: "mainButton", backgroundColor: "#00A0DC", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/fibaro-swipe.src/images/swipe.png"
			state "pushed", label:"SWIPE", action: "mainButton", backgroundColor: "#FFFFFF", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/fibaro-swipe.src/images/swipe.png"
		}
	
		buttons.each { button ->
			if (button in ["clockwise","counterClockwise"]) {
				standardTile(button, "device.switch" + ((button == "clockwise") ? 1:2), inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
					state "off", label:"", action: "${button}Start", backgroundColor: "#FFFFFF", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/fibaro-swipe.src/images/${button}.png"
					state "on", label:"", action: "${button}Stop", backgroundColor: "#00A0DC", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/fibaro-swipe.src/images/${button}I.png"
				}	
			} else {
				standardTile(button, "device.button", inactiveLabel: false, width: 2, height: (button.contains("program")) ? 1:2, decoration: "flat") {
					state "button", label:"", action: button, icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/fibaro-swipe.src/images/${button}.png"
				}
			}
		}
		
		valueTile("battery", "device.battery", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
			state "battery", label:'${currentValue}%\nbattery', unit:"%"
		}	
		
		standardTile("syncStatus", "device.syncStatus", decoration: "flat", width: 2, height: 2) {
			state "synced", label:'OK', action:"forceSync", backgroundColor: "#00a0dc", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/fibaro-swipe.src/images/sync_icon.png"
			state "pending", label:"Pending", action:"forceSync", backgroundColor: "#153591", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/fibaro-swipe.src/images/sync_icon.png"
			state "inProgress", label:"Syncing", action:"forceSync", backgroundColor: "#44b621", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/fibaro-swipe.src/images/sync_icon.png"
			state "incomplete", label:"Incomplete", action:"forceSync", backgroundColor: "#f1d801", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/fibaro-swipe.src/images/sync_icon.png"
			state "failed", label:"Failed", action:"forceSync", backgroundColor: "#bc2323", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/fibaro-swipe.src/images/sync_icon.png"
			state "force", label:"Force", action:"forceSync", backgroundColor: "#e86d13", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/fibaro-swipe.src/images/sync_icon.png"
		}

		main "mainButton" 
		details(buttons.plus(8,"syncStatus").plus(11,"battery"))
	}
		
	preferences {
		input (
			title: "Fibaro Swipe manual",
			description: "Tap to view the manual.",
			image: "http://manuals.fibaro.com/wp-content/uploads/2017/02/swipe_icon.png",
			url: "http://manuals.fibaro.com/content/manuals/en/FGGC-001/FGGC-001-EN-T-v1.0.pdf",
			type: "href",
			element: "href"
		)
		
		input (
			title: "Wake up interval",
			description: "Fibaro Swipe will wake up after each defined time interval and always try to connect with the main controller.\n0 or 3600-64800 (in seconds (1-18h), 3600s (1h) step)",
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
		
		parameterMap().findAll( {!it.key.contains('param') && !it.key.contains('sequence')} ).each {
			getPrefsFor(it)
		}
		
		input (
			title: "Scenes sent to the controller",
			description: "Defines which actions result in sending scenes to the HUB. Enabling double gestures will introduce delay when performing single gestures.",
			type: "paragraph",
			element: "paragraph"
		)
		
		input ( name: "flickUp", title: "UP gesture" ,type: "boolean", defaultValue: 1, required: false )
		input ( name: "flickDown", title: "DOWN gesture" ,type: "boolean", defaultValue: 1, required: false )
		input ( name: "flickLeft", title: "LEFT gesture" ,type: "boolean", defaultValue: 1, required: false )
		input ( name: "flickRight", title: "RIGHT gesture" ,type: "boolean", defaultValue: 1, required: false )
		input ( name: "clockwise", title: "Clockwise gesture" ,type: "boolean", defaultValue: 0, required: false )
		input ( name: "counterClockwise", title: "Counter-clockwise gesture" ,type: "boolean", defaultValue: 0, required: false )
		input ( name: "flickUp2", title: "Double UP gesture" ,type: "boolean", defaultValue: 0, required: false )
		input ( name: "flickDown2", title: "Double DOWN gesture" ,type: "boolean", defaultValue: 0, required: false )
		input ( name: "flickLeft2", title: "Double LEFT gesture" ,type: "boolean", defaultValue: 0, required: false )
		input ( name: "flickRight2", title: "Double RIGHT gesture" ,type: "boolean", defaultValue: 0, required: false )
		
		input (
			title: "Sequences",
			description: "User can create sequences of two or three gestures.\nValues for gestures:\n\t1 - UP\n\t2 - DOWN\n\t3 - LEFT\n\t4 - RIGHT",
			type: "paragraph",
			element: "paragraph"
		)
		
		parameterMap().findAll( {it.key.contains('sequence')} ).each {
			input (name: it.key, title: "${it.num}. ${it.title}", type: it.type, range: "${it.min}..${it.max}", defaultValue: 0, required: false)
		}

		input ( name: "mainButton", type: "number", title: "Button to be activated from main menu:", required: false, defaultValue: 1)
		input ( name: "logging", title: "Logging", type: "boolean", required: false )
	}
}

//UI Support functions
def getPrefsFor(parameter) {
	input (
		title: "${parameter.num}. ${parameter.title}",
		description: parameter.descr,
		type: "paragraph",
		element: "paragraph"
	)
	input (
		name: parameter.key,
		title: null,
		type: parameter.type,
		options: parameter.options,
		range: (parameter.min != null && parameter.max != null) ? "${parameter.min}..${parameter.max}" : null,
		defaultValue: parameter.def,
		required: false
	)
}

def mainButton() { 
	if (!settings.mainButton) {
		buttonEvent(1)
	} else {
		buttonEvent(settings.mainButton)
	}
}
def up() { buttonEvent(1) }
def down() { buttonEvent(2) }
def left() { buttonEvent(3) }
def right() { buttonEvent(4) }
def clockwiseStart() { buttonEvent(5); switchEvent(1,"on"); }
def clockwiseStop() { switchEvent(1,"off") }
def counterClockwiseStart() { buttonEvent(6); switchEvent(2,"on"); }
def counterClockwiseStop() { switchEvent(2,"off") }
def up2() { buttonEvent(7) }
def down2() { buttonEvent(8) }
def left2() { buttonEvent(9) }
def right2() { buttonEvent(10) }
def sequence1() { buttonEvent(11) }
def sequence2() { buttonEvent(12) }
def sequence3() { buttonEvent(13) }
def sequence4() { buttonEvent(14) }
def sequence5() { buttonEvent(15) }
def sequence6() { buttonEvent(16) }
def program1() { programSequence(1) }
def program2() { programSequence(2) }
def program3() { programSequence(3) }
def program4() { programSequence(4) }
def program5() { programSequence(5) }
def program6() { programSequence(6) }

def buttonEvent(Integer number) {
	def descriptionList = [
		"UP gesture",
		"DOWN gesture",
		"LEFT gesture",
		"RIGHT gesture", 
		"Clockwise gesture", 
		"Counter-clockwise gesture", 
		"Double UP gesture", 
		"Double DOWN gesture", 
		"Double LEFT gesture", 
		"Double RIGH gesture", 
		"Sequence 1", 
		"Sequence 2", 
		"Sequence 3", 
		"Sequence 4", 
		"Sequence 5", 
		"Sequence 6" ]
	logging("${device.displayName} - Sending buttonEvent $number","info")
	sendEvent(name: "button", value: "pushed", data: [buttonNumber: number], descriptionText: "${descriptionList[number-1]} detected", isStateChange: true)
}

def switchEvent(Integer number, String value) {
	logging("${device.displayName} - Sending switchEvent $number $value","info")
	sendEvent(name: "switch$number", value: value, data: [switchNumber: number], isStateChange: true, displayed: false)
}

// Parameter configuration, synchronization and verification
def programSequence(Integer seqNum) {
	if (state.param30 == null) { 
		state.param30 = [value: seqNum, state: "notSynced"] 
	} else {
		state.param30.value = seqNum
		state.param30.state = "notSynced"
	}
	sendEvent(name: "syncStatus", value: "pending") 
}

def updated() {
	if ( state.lastUpdated && (now() - state.lastUpdated) < 500 ) return
	logging("${device.displayName} - Executing updated()","info")
	sendEvent(name: "numberOfButtons", value: 16)
	def syncRequired = 0
	def value
	parameterMap().each {
		if(settings."$it.key" != null || it.key in ["param10","param12"] ) {
			if (it.key == "param10") { value = calcParam10() }
			else if (it.key == "param12") { value = calcParam12() } 
			else if (it.key.contains("sequence")) { value = calcSequence(settings."$it.key") }
			else (value = settings."$it.key")
			
			if (state."$it.key" == null) { state."$it.key" = [value: null, state: "synced"] }
			if (state."$it.key".value != value as Integer) {
				syncRequired = 1
				state."$it.key".value = value as Integer
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

def Integer calcParam10() {
	Integer result = 0
	result += ((settings.flickUp == "true")? 1:0)
	result += ((settings.flickDown == "true")? 2:0)
	result += ((settings.flickLeft == "true")? 4:0)
	result += ((settings.flickRight == "true")? 8:0)
	result += ((settings.clockwise == "true")? 16:0)
	result += ((settings.counterClockwise == "true")? 32:0)
	return result
}

def Integer calcParam12() {
	Integer result = 0
	result += ((settings.flickUp2 == "true")? 0:1)
	result += ((settings.flickDown2 == "true")? 0:2)
	result += ((settings.flickLeft2 == "true")? 0:4)
	result += ((settings.flickRight2 == "true")? 0:8)
	return result
}

def Integer calcSequence(sequence) {
	sequence = sequence as String
	def Integer size = sequence.length()
	def Integer result = 0
	if (size > 3) { size = 3 }
	(0..size-1).each{ n ->
		result += (sequence[n] as Integer) * 2**(4*(2-n))
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
	logging("${device.displayName} woke up", "info")
	def cmdsSet = []
	def cmdsGet = []
	def cmds = []
	def Integer cmdCount = 0
	def results = [createEvent(descriptionText: "$device.displayName woke up", isStateChange: true)]
	cmdsGet << zwave.batteryV1.batteryGet()
	cmdsGet << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 1)
	if (device.currentValue("syncStatus") != "synced") {
		parameterMap().each {
			if (device.currentValue("syncStatus") == "force") { state."$it.key".state = "notSynced" }
			if (state."$it.key"?.value != null && state."$it.key"?.state == "notSynced") {
				cmdsSet << zwave.configurationV2.configurationSet(configurationValue: intToParam(state."$it.key".value, it.size), parameterNumber: it.num, size: it.size)
				cmdsGet << zwave.configurationV2.configurationGet(parameterNumber: it.num)
				cmdCount = cmdCount + 1
			}
		}
		if (device.currentValue("syncStatus") == "force") { state.wakeUpInterval.state = "notSynced" }
		if (state.wakeUpInterval?.value != null && state.wakeUpInterval?.state == "notSynced") {
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
	def paramKey
	paramKey = parameterMap().find( {it.num == cmd.parameterNumber as Integer} ).key 
	if (paramKey == "param30") {
		if (cmd.scaledConfigurationValue == 0) {
			log.info("${device.displayName} - exited sequence learning mode.")
			state.param30.value = 0
		} else {
			log.info("${device.displayName} - entered into sequence learning mode for sequence ${cmd.scaledConfigurationValue}.")
		}
		state."$paramKey".state = "synced"
	} else {
		logging("${device.displayName} - Parameter ${paramKey} value is ${cmd.scaledConfigurationValue} expected " + state."$paramKey"?.value, "info")
		if (state."$paramKey".value == cmd.scaledConfigurationValue) {
			state."$paramKey".state = "synced"
		} 
	}
}

def zwaveEvent(physicalgraph.zwave.commands.applicationstatusv1.ApplicationRejectedRequest cmd) {
	logging("${device.displayName} - rejected request!","warn")
	if (device.currentValue("syncStatus") == "inProgress") { sendEvent(name: "syncStatus", value:"failed") }
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpIntervalReport cmd) { 
	log.debug "interval! " + cmd
}

def syncCheck() {
	logging("${device.displayName} - Executing syncCheck()","info")
	def Integer count = 0
	state.wakeUpInterval?.state = "synced"
	if (device.currentValue("syncStatus") != "synced") {
		parameterMap().each {
			if (state."$it.key"?.state == "notSynced" ) {
				log.debug "sunc $it.key " + state."$it.key"
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

//event handlers
def zwaveEvent(physicalgraph.zwave.commands.centralscenev1.CentralSceneNotification cmd) {
	if ((cmd.sceneNumber as Integer) in (1..4)) {
		buttonEvent((cmd.sceneNumber as Integer) + ((cmd.keyAttributes == 0)? 0:6))
	} else if ((cmd.sceneNumber as Integer) in (5..6)) {
		switchEvent((cmd.sceneNumber as Integer)-4, (cmd.keyAttributes == 2)? "on":"off")
		if (cmd.keyAttributes == 2) { buttonEvent((cmd.sceneNumber as Integer) ) }
	} else {
		buttonEvent((cmd.sceneNumber as Integer) + 4)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	log.info "$device.displayName battery is $cmd.batteryLevel ($cmd)"
	sendEvent(name: "battery", value: cmd.batteryLevel)
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
	//[0x5E: 2, 0x59: 1, 0x22: 1, 0x80: 1, 0x56: 1, 0x7A: 3, 0x73: 1, 0x98: 1, 0x31: 5, 0x85: 2, 0x70: 2, 0x5A: 1, 0x72: 2, 0x8E: 2, 0x71: 2, 0x86: 1, 0x84: 2] //Fibaro Door/Window Sensor 2
	  [0x5E: 2, 0x59: 1, 0x80: 1, 0x56: 1, 0x7A: 3, 0x72: 2, 0x73: 1, 0x98: 1, 0x86: 1, 0x85: 2, 0x5B: 1, 0x70: 2, 0x5A: 1, 0x8E: 2, 0x84: 2] //Fibaro Swipe
}

private parameterMap() {[
	[key: "orientation", num: 1, size: 1, type: "enum", options: [
		0: "0 - default orientation", 
		1: "1 - 180° rotation",
		2: "2 - 90° clockwise rotation",
		3: "3 - 90° counter-clockwise rotation"],
		def: "0", title: "Device orientation", 
		descr: "Determines orientation of the Swipe in relation to its default position."],
	[key: "buzzerSignaling", num: 2, size: 1, type: "enum", options: [0: "0 - gestures detection is not signalled", 1: "1 - gestures detection is signalled"], def: "1", title: "Buzzer - acoustic signal settings", 
		descr: "Acoustic signalling of gestures detection."],
	[key: "ledIndicator", num: 3, size: 1, type: "enum", options: [0: "0 - gestures detection is not signalled", 1: "1 - gestures detection is signalled"], def: "0", title: "Visual indication of gestures detection.", 
		descr: "Acoustic signalling of gestures detection."],
	[key: "buzzerMode", num: 4, size: 1, type: "enum", options: [
		1: "1 - only successful recognition is signalled",
		2: "2 - only failed recognition is signalled",
		3: "3 - successful and failed recognition is signalled"],
		def: "3", title: "Buzzer - signalling result of gesture recognition", 
		descr: "Acoustic signalling of gesture recognition result."],
	[key: "powerInterval", num: 5, size: 2, type: "number", def: 4, min: 0, max: 1080, title: "Powering mode - interval", 
		descr: "How often the device checks if the USB power supply is connected.\n0 - powering mode is not updated\n1-1080 (in minutes) - time interval"], 
	[key: "powerMode", num: 6, size: 1, type: "enum", options: [
		0: "0 - Standby Mode",
		1: "1 - Simple Mode",
		2: "2 - the Swipe does not enter power saving mode"],
		def: "0", title: "Power saving mode (battery mode)", 
		descr: "Determines operation of gesture detection when battery powered.\nStandby Mode - hold gesture must be performed to exit power saving mode.\nSimple Mode - gesture recognition is always active, but only slowly performed gestures will be recognized properly"],
	[key: "holdGesture", num: 7, size: 1, type: "enum", options: [
		0: "0 - Hold gesture enabled",
		1: "1 - Hold gesture disabled"],
		def: "0", title: "Hold gesture to enter the menu", 
		descr: "This parameter allows to choose if the menu can be entered using the Hold gesture."],
	[key: "param10", num: 10, size: 1, type: "number", def: 15, min: 0, max: 63, title: null, descr: null], 
	[key: "param12", num: 12, size: 1, type: "number", def: 15, min: 0, max: 63, title: null, descr: null],
	[key: "param30", num: 30, size: 1, type: "number", def: 15, min: 0, max: 63, title: null, descr: null], 
	[key: "sequence1", num: 31, size: 2, type: "number", def: 0, min: 0, max: 444, title: "1st sequence of gestures", descr: null],
	[key: "sequence2", num: 32, size: 2, type: "number", def: 0, min: 0, max: 444, title: "2nd sequence of gestures", descr: null],
	[key: "sequence3", num: 33, size: 2, type: "number", def: 0, min: 0, max: 444, title: "3rd sequence of gestures", descr: null],
	[key: "sequence4", num: 34, size: 2, type: "number", def: 0, min: 0, max: 444, title: "4th sequence of gestures", descr: null],
	[key: "sequence5", num: 35, size: 2, type: "number", def: 0, min: 0, max: 444, title: "5th sequence of gestures", descr: null],
	[key: "sequence6", num: 36, size: 2, type: "number", def: 0, min: 0, max: 444, title: "6th sequence of gestures", descr: null],
]}
