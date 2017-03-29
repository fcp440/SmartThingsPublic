/**
 *  Fibaro KeyFob
 *
 *  Copyright 2017 Artur Draga
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
        
        (1..26).each{
        	if (it in (1..6)) {
                attribute "switch$it", "string"
                command "on$it"
                command "off$it"
            }
            command "button$it"
        }
        
        fingerprint deviceId: "0x1801" , inClusters: "0x5E,0x59,0x80,0x56,0x7A,0x73,0x98,0x22,0x85,0x5B,0x70,0x5A,0x72,0x8E,0x86,0x84,0x75"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles (scale: 2){
    	[1,2,3,7,13,8,14,9,15,19,20,21,4,5,6,10,16,11,17,12,18,22,23,24,25,26,27,28,29,30].each { n ->
			if (n in (1..6)) { //main large tiles
            	def String imgUrl = "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/fibaro-keyfob.src/b0#_icon.png"
                imgUrl = imgUrl.replaceAll("#", n as String)
                standardTile("button$n", "device.button", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
                    state "default", label: "", action:"button$n", icon: imgUrl
                }
            } else if (n in (7..12)) { //x2 tiles
            	standardTile("button$n", "device.button", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
					state "default", label:"", action:"button$n", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/fibaro-keyfob.src/x2.png"
				}
            } else if (n in (13..18)) { //x3 tiles
            	standardTile("button$n", "device.button", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
					state "default", label:"", action:"button$n", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/fibaro-keyfob.src/x3.png"
				}
            } else if (n in (19..24)) { //hold tiles
            	def i = n - 18
            	standardTile("switch$i", "device.switch$i",canChangeIcon: false, width: 2, height: 1, decoration: "flat") {
					state "on", label: "Release", action: "off$i",  backgroundColor: "#00A0DC", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/fibaro-keyfob.src/release_icon.png"
					state "off", label: "Hold", action: "on$i",  backgroundColor: "#ffffff", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/fibaro-keyfob.src/hold_icon.png"
    			}
            } else if (n in (25..30)) { //sequence tiles
            	def i = n - 24
            	standardTile("button$n", "device.button", inactiveLabel: false, decoration: "flat", width: 2, height: 1) {
					state "default", label:"Sequance $i", action:"button$n"
				}
            }
		}
        valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 4, height: 2) {
			state "default", label:'Battery: ${value}%'
		}
        standardTile("configure", "device.configure", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:"", action:"configure", icon:"st.secondary.configure"
		}
	}
    
    preferences {
    	input name: "unlockSeq", type: "number", title: "1. Unlocking Sequence\n\nSet unlocking sequence for the remote using button numbers 1 to 6 (for example: 1234) 2 to 5 buttons. Set to 0 to disable.\nDefault: 0", description: "Enter Sequence", required: false
    	input name: "lockTim", type: "number", title: "2.1. Time To Lock\n\nSet time in sec. The remote will lock after this time elapses from the last activity. Set to 0 to disable.\nDefault: 60", description: "Enter number", required: false
        input name: "lockBtn", type: "number", title: "2.2. Locking Button\n\nSet button number 1 to 6. The remote will lock if you press and hold this button. Set to 0 to disable.\nDefault: 0", description: "Enter Button nr", required: false
    	input name: "seq1", type: "number", title: "3. 1st Sequence\n\nSet button sequence using button numbers 1 to 6 (for example: 1234) 2 to 5 buttons. Set to 0 to disable.\nDefault: 0 ", description: "Enter Sequence", required: false
    	input name: "seq2", type: "number", title: "4. 2nd Sequence\nDefault: 0 ", description: "Enter Sequence", required: false
    	input name: "seq3", type: "number", title: "5. 3rd Sequence\nDefault: 0 ", description: "Enter Sequence", required: false
        input name: "seq4", type: "number", title: "6. 4th Sequence\nDefault: 0 ", description: "Enter Sequence", required: false
        input name: "seq5", type: "number", title: "7. 5th Sequence\nDefault: 0 ", description: "Enter Sequence", required: false
        input name: "seq6", type: "number", title: "8. 6th Sequence\nDefault: 0 ", description: "Enter Sequence", required: false
    	input name: "seqTim", type: "number", title: "9. Sequences - timeout\n\nTime that must elapse from the last click of the button to check if the sequence is valid\nDefault: 10 (1s) ", description: "Enter number", required: false
        input name: "btn1mode", type: "enum", title: "21. Button 1 Mode\nDefault: 1 click & hold ", options: ["1 Click", "2 Clicks", "3 Clicks", "Hold and Release", "1 & 2 Clicks", "1, 2 & 3 Clicks", "1 Click & Hold", "1, 2 Clicks & Hold", "1, 2, 3 Clicks & Hold"], description: "Select Mode", required: false
        input name: "btn2mode", type: "enum", title: "22. Button 2 Mode\nDefault: 1 click & hold ", options: ["1 Click", "2 Clicks", "3 Clicks", "Hold and Release", "1 & 2 Clicks", "1, 2 & 3 Clicks", "1 Click & Hold", "1, 2 Clicks & Hold", "1, 2, 3 Clicks & Hold"], description: "Select Mode", required: false
        input name: "btn3mode", type: "enum", title: "23. Button 3 Mode\nDefault: 1 click & hold ", options: ["1 Click", "2 Clicks", "3 Clicks", "Hold and Release", "1 & 2 Clicks", "1, 2 & 3 Clicks", "1 Click & Hold", "1, 2 Clicks & Hold", "1, 2, 3 Clicks & Hold"], description: "Select Mode", required: false
        input name: "btn4mode", type: "enum", title: "24. Button 4 Mode\nDefault: 1 click & hold ", options: ["1 Click", "2 Clicks", "3 Clicks", "Hold and Release", "1 & 2 Clicks", "1, 2 & 3 Clicks", "1 Click & Hold", "1, 2 Clicks & Hold", "1, 2, 3 Clicks & Hold"], description: "Select Mode", required: false
        input name: "btn5mode", type: "enum", title: "25. Button 5 Mode\nDefault: 1 click & hold ", options: ["1 Click", "2 Clicks", "3 Clicks", "Hold and Release", "1 & 2 Clicks", "1, 2 & 3 Clicks", "1 Click & Hold", "1, 2 Clicks & Hold", "1, 2, 3 Clicks & Hold"], description: "Select Mode", required: false
        input name: "btn6mode", type: "enum", title: "26. Button 6 Mode\nDefault: 1 click & hold ", options: ["1 Click", "2 Clicks", "3 Clicks", "Hold and Release", "1 & 2 Clicks", "1, 2 & 3 Clicks", "1 Click & Hold", "1, 2 Clicks & Hold", "1, 2, 3 Clicks & Hold"], description: "Select Mode", required: false
    }
}

def parse(String description) {
	def results = []
	if (description.startsWith("Err")) {
	    results = createEvent(descriptionText:description, displayed:true)
	} else {
		def cmd = zwave.parse(description, [0x5E: 2, 0x59: 1, 0x80: 1, 0x56: 1, 0x7A: 3, 0x73: 1, 0x98: 1, 0x22: 1, 0x85: 2, 0x5B: 3, 0x70: 1, 0x8E: 2, 0x86: 2, 0x84: 2, 0x75: 2])
		if(cmd) results += zwaveEvent(cmd)
		if(!results) results = [ descriptionText: cmd, displayed: false ]
	}
	return results
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand([0x5E: 2, 0x59: 1, 0x80: 1, 0x56: 1, 0x7A: 3, 0x73: 1, 0x98: 1, 0x22: 1, 0x85: 2, 0x5B: 1, 0x70: 1, 0x8E: 2, 0x86: 2, 0x84: 2, 0x75: 2, 0x72: 2]) 
	if (encapsulatedCommand) {
		return zwaveEvent(encapsulatedCommand)
	} else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
		createEvent(descriptionText: cmd.toString())
	}
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
	def results = [createEvent(descriptionText: "$device.displayName woke up", isStateChange: false)]
	//def prevBattery = device.currentState("battery")
	//if (!prevBattery || (new Date().time - prevBattery.date.time)/60000 >= 60 * 53) {
		results << response(zwave.batteryV1.batteryGet().format())
	//}
	results << response(zwave.wakeUpV1.wakeUpNoMoreInformation().format())
	return results
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
    log.debug "Battery!: $map"
	sendEvent(name: "battery", value: cmd.batteryLevel)
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
	log.debug "mappedButton!: $mappedButton action: $action"
    buttonEvent( mappedButton, action )
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
def button1() { buttonEvent(1,"pushed") }
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

def buttonEvent(button, action) {
	button = button as Integer
    def switchNr = button - 18 as Integer
	if (action == "pushed") {
		sendEvent(name: "button", value: "pushed", data: [buttonNumber: button], descriptionText: "$device.displayName button $button was pushed", isStateChange: true)
	} else if (action == "on"){
    	sendEvent(name: "button", value: "pushed", data: [buttonNumber: button], descriptionText: "$device.displayName button $button was pushed", isStateChange: true)
		sendEvent(name: "switch$switchNr", value: "on", data: [buttonNumber: button], descriptionText: "$device.displayName button $button was held", isStateChange: true)
	} else if  (action == "off") {
		sendEvent(name: "switch$switchNr", value: "off", data: [buttonNumber: button], descriptionText: "$device.displayName button $button was released", isStateChange: true)
    }
}

def seqToValue(sequance) { 
	sequance = sequance as String
    def Integer size = sequance.length()
    def Integer result = 0
    if (size < 2) {
    	log.debug "Sequence too short!"
        return null
    } else {
        if (size > 5) { size = 5; log.debug "Sequence too long, will be trimmed." }
        (0..size-1).each{ n ->
            result = result + ((sequance[n] as Integer) * (8**n))
        }
        return result
    }
}

def modToValue(mode) {
	def Integer result
    log.debug "mod: $mode"
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

def installed() {
	initialize()
}

def updated() {
	initialize()
    configure()
}

def initialize() {
	sendEvent(name: "numberOfButtons", value: 30)
}

def seqConfig(value,paramNr) {
	def Integer tempValue
    def short valPart1
    def short valPart2
    
	tempValue = seqToValue(value)
    if (tempValue) {
       	valPart1 = tempValue & 0xFF
        valPart2 = (tempValue >> 8) & 0xFF
        //log.debug parameterMap()["seq1"]
        return zwave.configurationV1.configurationSet(configurationValue: [valPart2, valPart1], parameterNumber: paramNr as Integer, size: 2)
    } else {
    	return null
    }
}

def configure() {
	def cmds = []
    
    if ( unlockSeq ) {
    	cmds << seqConfig(unlockSeq, parameterMap()["unlockSeq"][0] as Integer)
    	cmds << zwave.configurationV1.configurationGet(parameterNumber: parameterMap()["unlockSeq"][0] as Integer)
    }
    if ( lockBtn || lockTim ) {
    	def Integer timeVal
    	def Integer buttonVal
        def Integer tempValue
    	def short valPart1
    	def short valPart2
        if (lockBtn) { buttonVal = (lockBtn as Integer)*256 } else { buttonVal = 0 }
        if (lockTim) { timeVal = lockTim } else { timeVal = 0 }
        
        if (timeVal > 255) { timeVal = 255 }
        if (timeVal < 5 && timeVal != 0) { timeVal = 5 }
        if (buttonVal > 1536) { buttonVal = 1536 }
        
        tempValue = buttonVal+timeVal
       	valPart1 = tempValue & 0xFF
        valPart2 = (tempValue >> 8) & 0xFF
        cmds << zwave.configurationV1.configurationSet(configurationValue: [valPart2, valPart1], parameterNumber: 2 , size: 2)
    	cmds << zwave.configurationV1.configurationGet(parameterNumber: 2)
    }
    if (seq1) {
        cmds << seqConfig(seq1, parameterMap()["seq1"][0] as Integer)
    	cmds << zwave.configurationV1.configurationGet(parameterNumber: parameterMap()["seq1"][0] as Integer)
    }
    if (seq2) {
        cmds << seqConfig(seq2, parameterMap()["seq2"][0] as Integer)
    	cmds << zwave.configurationV1.configurationGet(parameterNumber: parameterMap()["seq2"][0] as Integer)
    }
    if (seq3) {
        cmds << seqConfig(seq3, parameterMap()["seq3"][0] as Integer)
    	cmds << zwave.configurationV1.configurationGet(parameterNumber: parameterMap()["seq3"][0] as Integer)
    }
    if (seq4) {
        cmds << seqConfig(seq4, parameterMap()["seq4"][0] as Integer)
    	cmds << zwave.configurationV1.configurationGet(parameterNumber: parameterMap()["seq4"][0] as Integer)
    }
    if (seq5) {
        cmds << seqConfig(seq5, parameterMap()["seq5"][0] as Integer)
    	cmds << zwave.configurationV1.configurationGet(parameterNumber: parameterMap()["seq5"][0] as Integer)
    }
    if (seq6) {
        cmds << seqConfig(seq6, parameterMap()["seq6"][0] as Integer)
    	cmds << zwave.configurationV1.configurationGet(parameterNumber: parameterMap()["seq6"][0] as Integer)
    }
    if (seqTim && seqTim <= 255) {
    	cmds << zwave.configurationV1.configurationSet(configurationValue: [seqTim as Integer], parameterNumber: parameterMap()["seqTim"][0] as Integer , size: 1)
    	cmds << zwave.configurationV1.configurationGet(parameterNumber: parameterMap()["seqTim"][0] as Integer)
    }
    if (btn1mode) {
    	cmds << zwave.configurationV1.configurationSet(configurationValue: [modToValue(btn1mode)], parameterNumber: parameterMap()["btn1mode"][0] as Integer  , size: 1)
    	cmds << zwave.configurationV1.configurationGet(parameterNumber: parameterMap()["btn1mode"][0]  as Integer)
    }
    if (btn2mode) {
    	cmds << zwave.configurationV1.configurationSet(configurationValue: [modToValue(btn2mode)], parameterNumber: parameterMap()["btn2mode"][0] as Integer , size: 1)
    	cmds << zwave.configurationV1.configurationGet(parameterNumber: parameterMap()["btn2mode"][0] as Integer)
    }
    if (btn3mode) {
    	cmds << zwave.configurationV1.configurationSet(configurationValue: [modToValue(btn3mode)], parameterNumber: parameterMap()["btn3mode"][0] as Integer , size: 1)
    	cmds << zwave.configurationV1.configurationGet(parameterNumber: parameterMap()["btn3mode"][0] as Integer)
    }
    if (btn4mode) {
    	cmds << zwave.configurationV1.configurationSet(configurationValue: [modToValue(btn4mode)], parameterNumber: parameterMap()["btn4mode"][0] as Integer , size: 1)
    	cmds << zwave.configurationV1.configurationGet(parameterNumber: parameterMap()["btn4mode"][0] as Integer)
    }
    if (btn5mode) {
    	cmds << zwave.configurationV1.configurationSet(configurationValue: [modToValue(btn5mode)], parameterNumber: parameterMap()["btn5mode"][0] as Integer , size: 1)
    	cmds << zwave.configurationV1.configurationGet(parameterNumber: parameterMap()["btn5mode"][0] as Integer)
    }
    if (btn6mode) {
    	cmds << zwave.configurationV1.configurationSet(configurationValue: [modToValue(btn6mode)], parameterNumber: parameterMap()["btn6mode"][0] as Integer , size: 1)
    	cmds << zwave.configurationV1.configurationGet(parameterNumber: parameterMap()["btn6mode"][0] as Integer)
    }
    
    /*def cmds = []
    cmds << zwave.configurationV1.configurationSet(configurationValue: [15], parameterNumber: 21, size: 1)
    cmds << zwave.configurationV1.configurationSet(configurationValue: [15], parameterNumber: 22, size: 1)
    cmds << zwave.configurationV1.configurationSet(configurationValue: [15], parameterNumber: 23, size: 1)
    cmds << zwave.configurationV1.configurationSet(configurationValue: [15], parameterNumber: 24, size: 1)
    cmds << zwave.configurationV1.configurationSet(configurationValue: [15], parameterNumber: 25, size: 1)
    cmds << zwave.configurationV1.configurationSet(configurationValue: [15], parameterNumber: 26, size: 1)
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 3)
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 22)
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 23)
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 24)
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 25)
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 26)
   	//return zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()*/
    delayBetween(cmds.collect{zwave.securityV1.securityMessageEncapsulation().encapsulate(it).format()}, 200)   
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {	 
	log.debug "${cmd}"	
}


def parameterMap() { [ unlockSeq:[1,2], lockBtnTim:[2,2], seq1:[3,2], seq2:[4,2], seq3:[5,2], seq4:[6,2], seq5:[7,2], seq6:[8,2], seqTim:[9,1], btn1mode:[21,1], btn2mode:[22,1], btn3mode:[23,1], btn4mode:[24,1], btn5mode:[25,1], btn6mode:[26,1] ] } // btn1mode:[parNumber,size]
/* Parameters as follows:
1 - Lock Mode - unlocking sequence
2 - Lock Mode - time to lock and locking button
3 to 8 - Sequences
9 - Sequences - timeout
21 to 26 - Scene activation modes (enabling disabling double, tripple clicks etc)
*/

/*Command Classes

COMMAND_CLASS_ZWAVEPLUS_INFO v2
0x5E: 2

COMMAND_CLASS_ASSOCIATION_GRP_INFO 
0x59: 1

COMMAND_CLASS_BATTERY
0x80: 1

COMMAND_CLASS_CRC_16_ENCAP
0x56: 1

COMMAND_CLASS_FIRMWARE_UPDATE_MD v3
0x7A: 3

COMMAND_CLASS_POWERLEVEL
0x73: 1

COMMAND_CLASS_SECURITY
0x98: 1

COMMAND_CLASS_APPLICATION_STATUS
0x22: 1

SEC:
COMMAND_CLASS_ASSOCIATION v2
0x85: 2

COMMAND_CLASS_CENTRAL_SCENE v3
0x5B: 1  //?????

COMMAND_CLASS_CONFIGURATION
0x70: 1

COMMAND_CLASS_DEVICE_RESET_LOCALLY
0x5A: 1

COMMAND_CLASS_MANUFACTURER_SPECIFIC v2
0x72: 2

COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION v2
0x8E: 2

COMMAND_CLASS_VERSION v2
0x86: 2

COMMAND_CLASS_WAKE_UP v2
0x84: 2

COMMAND_CLASS_PROTECTION v2
0x75: 2
*/