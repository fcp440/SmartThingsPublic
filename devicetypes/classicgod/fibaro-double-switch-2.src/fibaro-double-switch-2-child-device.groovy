/**
 *  Fibaro Double Switch 2 Child Device
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
	definition (name: "Fibaro Double Switch 2 Child Device", namespace: "ClassicGOD", author: "Artur Draga") {
		capability "Switch"
		capability "Actuator"
		capability "Sensor"
		capability "Energy Meter"
		capability "Power Meter"
		
		command "reset"
		command "refresh"
	}
	
	tiles {
		multiAttributeTile(name:"switch", type: "lighting", width: 3, height: 4){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "off", label: '', action: "switch.on", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/fibaro-double-switch-2.src/images/switch_2.png", backgroundColor: "#ffffff"
				attributeState "on", label: '', action: "switch.off", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/fibaro-double-switch-2.src/images/switch_1.png", backgroundColor: "#00a0dc"
			}
			tileAttribute("device.combinedMeter", key:"SECONDARY_CONTROL") {
				attributeState("combinedMeter", label:'${currentValue}')
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
		standardTile("main", "device.switch", decoration: "flat", canChangeIcon: true) {
			state "off", label: 'off', action: "switch.on", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/fibaro-double-switch-2.src/images/switch_2.png", backgroundColor: "#ffffff"
			state "on", label: 'on', action: "switch.off", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/fibaro-double-switch-2.src/images/switch_1.png", backgroundColor: "#00a0dc"
		}
		main "main"
		details(["switch","power","energy","reset"])
	}
	
	preferences {
		input ( name: "logging", title: "Logging", type: "boolean", required: false )
		input ( type: "paragraph", element: "paragraph", title: null, description: "This is a child device. If you're looking for parameters to set you'll find them in main component of this device." )
	}
}

def on() {
	parent.childOn()
}

def off() {
	parent.childOff()
}

def reset() {
	parent.childReset()
}

def refresh() {
	parent.childRefresh()
}
