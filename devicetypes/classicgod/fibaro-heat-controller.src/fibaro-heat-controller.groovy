/**
 *  Fibaro Heat Controller
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
	definition (name: "Fibaro Heat Controller", namespace: "ClassicGOD", author: "Artur Draga") {
		
        capability "Thermostat"


		command "test"
        command "tempUp"
        command "tempDown"
        command "test2"
        command "refreshBattery"
	}

	tiles (scale: 2) {
		multiAttributeTile(name:"thermostatFull", type:"thermostat", width:6, height:4) {
            tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
                attributeState("temp", label:'${currentValue}', unit:"dF", defaultState: true)
            }
            tileAttribute("device.control", key: "VALUE_CONTROL") {
                attributeState("VALUE_UP", action: "tempUp")
                attributeState("VALUE_DOWN", action: "tempDown")
            }
            tileAttribute("device.temperatureSensor", key: "SECONDARY_CONTROL") {
                attributeState("temperatureSensor", label:'${currentValue}°', defaultState: true)
            }
            tileAttribute("device.thermostatOperatingState", key: "OPERATING_STATE") {
                attributeState("off", backgroundColor:"#ffffff")
                attributeState("heat", backgroundColor:"#e86d13")
                attributeState("auto", backgroundColor:"#00a0dc")
            }
        }
		valueTile("battery", "device.battery", decoration: "flat", width: 2, height: 2) {
			state "battery", label:'${currentValue}%\nbattery', action:"refreshBattery"
        }
	}

	preferences {
	
		input (
			title: "Fibaro Wall Plug manual",
			description: "Tap to view the manual.",
			image: "http://manuals.fibaro.com/wp-content/uploads/2017/02/wp_icon.png",
			url: "http://manuals.fibaro.com/content/manuals/en/FGWPEF-102/FGWPEF-102-EN-A-v2.0.pdf",
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

def test() {
	log.debug "test"
    def cmds = []
    //cmds << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 1)
    cmds << zwave.configurationV2.configurationGet(parameterNumber: 3)
encapSequence(cmds)
}

def test2() {
	log.debug "test2"

}

def refreshBattery() {
	def cmds = []
	cmds << [zwave.batteryV1.batteryGet(), 1]
	cmds << [zwave.batteryV1.batteryGet(), 2]
    encapSequence(cmds,3000)
}

def tempUp() {
	log.debug "tempUp"
    Integer currentTemp = device.currentValue("temperature")
    def currentState = device.currentValue("thermostatOperatingState")
    if (currentTemp == null) {
    	if (currentState == 'heat') {
        	sendEvent([name: "thermostatOperatingState", value: "off"])
        } else {
    		sendEvent([name: "temperature", value: 10])
        }
    } else if (currentTemp >= 30) {
    	sendEvent([name: "temperature", value: null])
        sendEvent([name: "thermostatOperatingState", value: "heat"])
    } else {
    	sendEvent([name: "temperature", value: (currentTemp as Integer) + 1])
        sendEvent([name: "thermostatOperatingState", value: "auto"])
    }
    runIn(7,"setTemp")
}

def tempDown() {
	log.debug "tempDown"
    Integer currentTemp = device.currentValue("temperature")
    def currentState = device.currentValue("thermostatOperatingState")
    if (currentTemp == null) {
    	if (currentState == 'off') {
        	sendEvent([name: "thermostatOperatingState", value: "heat"])
        } else {
    		sendEvent([name: "temperature", value: 30])
        }
    } else if (currentTemp <= 10) {
    	sendEvent([name: "temperature", value: null])
        sendEvent([name: "thermostatOperatingState", value: "off"])
    } else {
    	sendEvent([name: "temperature", value: (currentTemp as Integer) - 1])
        sendEvent([name: "thermostatOperatingState", value: "auto"])
    }
    runIn(7,"setTemp")
	
}

def setTemp() {
    log.debug "setTemp"
    def cmds = []
	Integer currentTemp = device.currentValue("temperature")
    if (currentTemp == null) {
    	switch (device.currentValue("thermostatOperatingState")) {
        	case 'off': cmds << response(encap(zwave.thermostatModeV2.thermostatModeSet(mode: 0))); break;
            case 'heat': cmds << response(encap(zwave.thermostatModeV2.thermostatModeSet(mode: 31))); break;
        }
    } else {
    	cmds << response(encap(zwave.thermostatModeV2.thermostatModeSet(mode: 1)))
		cmds << response(encap(zwave.thermostatSetpointV2.thermostatSetpointSet(precision: 1, reserved01: 0, scale: 0, scaledValue: currentTemp, setpointType: 1, size: 2)))
	}
    
    sendHubCommand(cmds,3000)
}

//Configuration and synchronization
def updated() {
	if ( state.lastUpdated && (now() - state.lastUpdated) < 500 ) return
	def cmds = []
	logging("${device.displayName} - Executing updated()","info")
	
	//cmds << zwave.multiChannelAssociationV2.multiChannelAssociationGet(groupingIdentifier: 1) //verify if group 1 association is correct
	
	runIn(3,"syncStart")
	state.lastUpdated = now()
	//response(encapSequence(cmds,1000))
}

def configure() {
	encap(zwave.basicV1.basicSet(value: 0))
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
	log.debug cmd
	//def paramKey = parameterMap().find( {it.num == cmd.parameterNumber } ).key
	//logging("Parameter ${paramKey} value is ${cmd.scaledConfigurationValue} expected " + state."$paramKey".value, "info")
	//state."$paramKey".state = (state."$paramKey".value == cmd.scaledConfigurationValue) ? "synced" : "incorrect"
	//syncNext()
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

/*def zwaveEvent(physicalgraph.zwave.commands.multichannelassociationv2.MultiChannelAssociationReport cmd) {
	def cmds = []
	if (cmd.groupingIdentifier == 1) {
		if (cmd.nodeId != [zwaveHubNodeId]) {
			log.debug "${device.displayName} - incorrect MultiChannel Association for Group 1! nodeId: ${cmd.nodeId} will be changed to [0, ${zwaveHubNodeId}, 1]"
			cmds << zwave.multiChannelAssociationV2.multiChannelAssociationRemove(groupingIdentifier: 1)
			cmds << zwave.associationV2.associationSet(groupingIdentifier: 1, nodeId: zwaveHubNodeId)
		} else {
			logging("${device.displayName} - MultiChannel Association for Group 1 correct.","info")
		}
	}  
	if (cmds) { [response(encapSequence(cmds, 1000))] }
}*/

//event handlers
def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	//ignore
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport cmd) {
	switch (cmd.mode) {
    	case 0: sendEvent([name: "thermostatOperatingState", value: "off"]); break;
        case 1: sendEvent([name: "thermostatOperatingState", value: "auto"]); break;
        case 31: sendEvent([name: "thermostatOperatingState", value: "heat"]); break;
    }
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatsetpointv2.ThermostatSetpointReport cmd) {
	def cmdScale = cmd.scale == 1 ? "F" : "C"
	sendEvent(name: "temperature", unit: getTemperatureScale(), value: convertTemperatureIfNeeded(cmd.scaledValue, cmdScale, cmd.precision).toFloat() as Integer, displayed: true)
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	logging("${device.displayName} - SwitchBinaryReport received, value: ${cmd.value}","info")
	sendEvent([name: "switch", value: (cmd.value == 0 ) ? "off": "on"])
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd, ep = null) {
	logging("${device.displayName} - BatteryReport received, value: ${cmd.batteryLevel} - $ep", "info")
	if (ep == 1 || ep == null) {
    	sendEvent(name: "battery", value: cmd.batteryLevel.toString(), unit: "%", displayed: true)
    } else {
    	sendEvent(name: "sensorBattery", value: cmd.batteryLevel.toString(), unit: "%", displayed: true)
    }
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	logging("${device.displayName} - SensorMultilevelReport received, value: ${cmd.scaledSensorValue} scale: ${cmd.scale}","info")
	if (cmd.sensorType == 4) { 
		sendEvent([name: "power", value: cmd.scaledSensorValue, unit: "W"]) 
		multiStatusEvent("${device.currentValue("power")} W / ${device.currentValue("energy")} kWh")
	}
}

def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd) {
	logging("${device.displayName} - MeterReport received, value: ${cmd.scaledMeterValue} scale: ${cmd.scale}","info")
	switch (cmd.scale) {
		case 0: sendEvent([name: "energy", value: cmd.scaledMeterValue, unit: "kWh"]); break;
		case 2: sendEvent([name: "power", value: cmd.scaledMeterValue, unit: "W"]); break;
	}
	multiStatusEvent("${device.currentValue("power")} W / ${device.currentValue("energy")} kWh")
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
	[0x5E: 2, 0x22: 1, 0x59: 1, 0x56: 1, 0x7A: 1, 0x32: 3, 0x71: 1, 0x73: 1, 0x98: 1, 0x31: 5, 0x85: 2, 0x70: 2, 0x72: 2, 0x5A: 1, 0x8E: 2, 0x25: 1, 0x86: 2] //Fibaro Wall Plug ZW5
}

private parameterMap() {[
	
]}
