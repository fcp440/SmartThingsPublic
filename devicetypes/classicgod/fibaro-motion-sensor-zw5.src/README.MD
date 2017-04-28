This is an advanced DTH for Fibaro Motion Sensor Z-Wave Plus (also known as ZW5) version (firmware 3.2)

This DTH provides not only standard functionality like:

 * Motion detection
 * Tamper detection
 * Temperature measurement
 * Light intensity measurement
 * Battery readout

But also additionality offers:

 * Advanced parameter configuration for 24 parameters
 * Readout for the "Earth Quake" detection function (depending on parameter configuration)
 * Readout for sensor orientation (depending on parameter configuration)
 * Implemented Three Axis capability (depending on parameter configuration)
 * Automatic parameter synchronization on manual or periodic wake up.


Avaible parameters are (I've purposefully skipped parameter nr 3 since manufacturer doesn't recomment changing it and parameters related to associations as SmartThings does not support associations):

* Wake Up interval
* Motion Detection Settings:
  * Sensitivity
  * Blind time
  * Time window
  * Alarm cancellation delay
  * Operating mode
  * Night/day
* Tamper Alarm Settings:
  * Sensitivity
  * Alarm cancellation delay
  * Operating mode
  * Alarm cancellation
* Illuminance Reporting Settings:
  * Reporting threshold
  * Reporting interval
* Temperature Reporting Settings:
  * Reporting threshold
  * Measuring interval
  * Reporting Interval
  * Temperature offset
* Led Indicator Settings:
  * Signalling mode
  * Brightness
  * Illuminance for low indicator brightness
  * Illuminance for high indicator brightness
  * Temperature for blue colour
  * Temperature for red colour
  * Tamper alarm

For details on how parameters work if not self explanatory refer to manual for the device: http://manuals.fibaro.com/motion-sensor/
