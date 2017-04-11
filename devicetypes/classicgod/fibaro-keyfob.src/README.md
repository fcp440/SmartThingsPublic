# Fibaro KeyFob - 30 actions, Z-Wave Plus compatible, battery-powered, compact remote control 

Fibaro KeyFob is a six button Z-Wave Plus remote that enables user to execute 30 action through the combination of single, double and triple presses, holding the buttons and 6 programmable 2 to 5 button sequences. The remote can also be protected against unauthorized use by 2 to 5 button sequence (check Lock Mode configuration section below).

#### This DTH exposes 30 buttons and 6 switches. They are assigned as follows:

* Buttons 1-6 - "pushed" event on physical buttons 1-6 single press
* Buttons 7-12 - "pushed" event on physical buttons 1-6 double press
* Buttons 13-18 - "pushed" event on physical buttons 1-6 triple press
* Buttons 19-24 - "pushed" event on physical buttons 1-6 hold
* Buttons 25-30 - "pushed" event on execution of sequences 1 to 6
* Switches 1-6 - "on" event on physical buttons 1-6 hold, "off" event on physical buttons 1-6 release

#### Steps to configure the KeyFob:

1. Go the the settings and change them to your liking (people who used the previous version of the DTH have to go to the settings and set the button modes again)
2. After clicking 'Done' the SYNC tile will change to 'PENDING'
3. Press Circle + Plus on the KeyFob
4. SYNC tile will change to 'SYNCING' - this can take up to 30sec
5. If the SYNC tile changes to 'INCOMPLETE' repeat step 3
6. If the SYNC tile changes to 'FAILED' verify your settings.

if you want to force the sync of all of the settings tap the SYNC tile. It should change to 'FORCE' then proceed form step 3

#### Lock Mode configuration:

To activate it you have to:

1. Change 'Protection State' to 'Protection by sequence' in settings
2. Set the 'Unlocking Sequence' to value other than 0
3. Set one or both of 'Time To Lock' and 'Locking Button' to value other than 0 (if you want to lock the KeyFob only with a button, set 4. 'Time To Lock' to 0 and set 'Locking Button' to the number of the button you want to use to lock the KeyFob etc)


**[UPDATE] 2017-04-02:**
* Added new mainTile - the main menu now shows a push button instead of battery percentage. Pressing it activates the button which number can be configured in the settings menu (Button 1 by default).

**[UPDATE] 2017-04-07:**
* Bug fixes suggested by @erocm1231 (Thank you! :) )
