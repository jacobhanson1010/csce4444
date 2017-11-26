csce4444

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

## Instructions

1. Make sure you have Java downloaded and installed
2. Make sure you have Eclipse downloaded and installed 
    * Java and Eclipse architectures need to match, 32-bit and 32-bit, or 64-bit and 64-bit
3. Clone repository: git clone https://github.com/jacobhanson1010/csce4444
4. Change directory into csce4444/data/
5. Open All_training_data.xlsm 
    * There may be a popup about macros, MAKE SURE YOU PRESS ENABLE
6. Under the "Views" tab of the ribbon, click "Macros"
7. Select "main" from the list of macros, then press "Run"
    * This will execute the VBA script that formats all of the input files, appends them to training_data.xlsm, and converts it to a .csv
8. Close out of Excel
    * Press "Don't Save" in the dialog
9. Open project in Eclipse
10. Run project "as Java Application"
    * This starts the server
11. Test by visiting localhost:8080 in a web browser