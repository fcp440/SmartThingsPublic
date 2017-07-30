/**
 *  Aeotec Home Energy Meter Gen5 Child Device
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
	definition (name: "Aeotec HEM Gen5 Child Device", namespace: "ClassicGOD", author: "Artur Draga") {
		capability "Energy Meter"
		capability "Power Meter"
		capability "Voltage Measurement"
		capability "Configuration"
		capability "Refresh"
		capability "Sensor"

		command "reset"
		command "resetMeter"

		attribute "current", "number"
		attribute "combinedMeter", "string"
	}

	tiles (scale: 2) {
		multiAttributeTile(name:"multiTile", type: "generic", width: 3, height: 4){
			tileAttribute ("device.power", key: "PRIMARY_CONTROL") {
				attributeState "power", label: '${currentValue} W', icon: null, backgroundColors:[
					[value: 0, color: "#44b621"],
					[value: 666, color: "#f1d801"],
					[value: 1337, color: "#d04e00"],
					[value: 2000, color: "#bc2323"]
				]
			}
			tileAttribute("device.combinedMeter", key:"SECONDARY_CONTROL") {
				attributeState("combinedMeter", label:'${currentValue}')
			} 
		}
		
		valueTile("power", "device.power", decoration: "flat", width: 2, height: 2, canChangeIcon: true) {
			state "power", label:'${currentValue} W', unit: "W", icon: "st.Home.home2", backgroundColors:[
				[value: 0, color: "#44b621"],
				[value: 666, color: "#f1d801"],
				[value: 1337, color: "#d04e00"],
				[value: 2000, color: "#bc2323"]
			]
		}
		
		valueTile("energy", "device.energy", decoration: "flat", width: 2, height: 2) {
			state "energy", label:'${currentValue}\n kWh ', unit: "kWh"
		}
		
		valueTile("voltage", "device.voltage", decoration: "flat", width: 2, height: 2) {
			state "voltage", label:'${currentValue}\n V ', unit: "V"
		}
		
		valueTile("current", "device.current", decoration: "flat", width: 2, height: 2) {
			state "current", label:'${currentValue}\n A ', unit: "A"
		}
		
		standardTile("reset", "device.reset", decoration: "flat", width: 2, height: 2) {
			state "default", label:'Reset kWh', action:"reset", icon: "st.Kids.kids4"
		}
		
		standardTile("refresh", "device.refresh", decoration: "flat", width: 2, height: 2) {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		
		main "power"
		details("multiTile","energy","voltage","current","reset","refresh")
	}
		
	preferences {
		input ( 
			type: "paragraph", 
			element: "paragraph", 
			title: "Hello there!", 
			description: "This is a child device. If you're looking for parameters to set you'll find them in the main component of this device.",
			image: "http://aeotec.com/images/products/220/z-wave-home-energy-measure@2x.jpg"
		)
	}
}

def reset() {
	if ( state.lastReset && (now() - state.lastReset) < 2000 ) {
		state.lastReset = now() - 2000
		resetMeter()
	} else {
		state.lastReset = now()
	}
}

def resetMeter() {
	parent.childReset(device.deviceNetworkId)
}

def refresh() {
	parent.childRefresh(device.deviceNetworkId)
}
