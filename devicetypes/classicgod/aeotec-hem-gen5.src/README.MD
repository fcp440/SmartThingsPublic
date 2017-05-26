This DTH is designed to work with 3, 2 and 1 clamp configurations but was only tested on 3 clamp configuration so any input is welcome.

This is a composite device. That means it will install a virtual device for every clamp (except for 1 clamp configuration) and a main device displaying information for the whole HEM. It requires "Aeotec HEM Gen 5 Child Device" to work correctly so install both parts of the DTH before switching to it.

This DTH provides the following functionality:

* Power Monitoring
* Energy Monitoring
* Voltage Monitoring
* Current Monitoring
* Configuration for following parameters:
  * Power and Energy detection mode
  * Reporting Threshold
  * HEM threshold
  * Clamp 1 threshold
  * Clamp 2 threshold
  * Clamp 3 threshold
  * HEM percentage
  * Clamp 1 percentage
  * Clamp 2 percentage
  * Clamp 3 percentage
  * CRC-16 reporting
  * Reporting settings for group 1
  * Reporting settings for group 2
  * Reporting settings for group 3
  * Group 1 time interval
  * Group 2 time interval
  * Group 3 time interval
* Reset for accummulated kWh value for separate channels and whole HEM
* Refresh

Tips:
Aeotec Home Energy Meter Gen5 3 Clamp configuration has issues communicating when not securely included. To make sure you included your HEM securely remove it from the network and then re-add it pressing the buton on the HEM twice.

If the meter is trying to send too many reports at once some of them will go missing so I recommend distributing readouts over all 3 groups and setting the groups to different time intervals.

If you make the meter report too often it can stop responding to commands. If that is the case you can try sending the configuration few times, patiently waiting few minutes and retrying or resetting the HEM to factory defaults.

My HEM is on early 1.0 FW version so the issues mentioned above could be potentially fixed in the newer fw version. I'm waiting for my Z-Stick to perform update on mine. For FW update file contact Aeotec Support.



