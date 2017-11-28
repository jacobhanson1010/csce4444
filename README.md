csce4444

This "master" branch is reflected in our Heroku instance. All work is done in the "development" branch.

This is the readme for our CSCE4444 project, which is a webapp designed to report the live capacity of the UNT Pohl Recreation Center.

Group members:
Jacob "jeb" Hanson
Ibrahim El-Rayes
John Nguyen
Anthony Hicks

This is the directory structure for our repository:

/src - The folder containing our project's source code.

/data - The folder containing input and output files

/doc - The folder containing the documentation for our project, including reports, presentations, and meeting minutes.

# Instructions

### *NOTE: If you already have the converted csv data, skip to Step 8*

1.  Pre-requirements (*NOTE: Architectures must match*)
    * Java JRE
    * Eclipse
2. Clone repository
    * From the command line: “git clone https://github.com/jacobhanson1010/csce4444”

***IMPORTANT*** 
* **Steps 3 - 7 are for converting all xlsx sheets to csv format.** 
* **We have already provided All_training_data.csv in the repository.**
* **Skip to step 8 when attempting to execute the program.**

3. Change directory into csce4444/data/
4. Open All_training_data.xlsm
    * *NOTE: In order for the following VBA macro to work, you must:*
        * Be using Microsoft Windows
        * Open the workbook using Microsoft Excel
        * There may be a popup about macros, MAKE SURE YOU PRESS ENABLE. Otherwise, be sure macros are enabled in your Microsoft Excel settings.
5. Under the “Views” tab of the ribbon, click the “Macros” icon
6. Select “main” from the list of macros, then press “Run”
    * This will execute the VBA macro that does the following:
        * Formats all of the input files
        * Appends said files to All_training_data.xlsm
        * Converts All_training_data.xlsm to a .csv
7. Close out of Excel
    * Press “Don’t Save” in the dialog
    * The macro already saved the .csv file
8. Open project in Eclipse
    * File→import →General→ Existing Projects into Workspace → Select root directory → Finish
    * You may have to point your project to your JRE:
        * Right-click package→Properties→Java Build Path→JRE System Library→Check box
9. Run project
    * Right-click package → Run As → Java Application
    * Select “SwolePatrol - csce4444” when prompted to Select Java Application
10. Confirm project has been started
    * Visit “localhost:8080” within a web browser
