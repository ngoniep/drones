
## Drones Service REST API

### How to build

#### Requirements

- Java 8
- Java IDE (Intellij or any other compatible editor)
- I have used an in-memory database (H2)
- Can use any Swagger Documentation on this link http://localhost:8080/swagger-ui/index.html# REST client for testing eg Postman


### Testing the API
Assumptions Made: 

- Medication code is not unique as it identifies a specific type of medication.
- A drone can carry more than one Medication at a time as long as the total weight does not exceed the capacity of the drone
- Authentication and authorization is not required for testing (in production Oauth2 will be implemented using an Identity Provider like Auth0,Keycloak etc.)
- Various assumptions have been made on the state of the battery 
    * Battery looses power at the rate of 1% every 18 seconds
    * Battery is always recharge when it is below 100% and can only be recharged in the IDLE state
    * Battery recharges at the rate of 1% every 30 seconds


Steps to be taken on testing:
1. Start the project (a fleet of 10 drones with random wights and models will be created and will all be in Idle status)
2. Open Swagger by opening this link on web browser http://localhost:8080/swagger-ui/index.html#/
3. Expand drone controllers and expand /available-drones endpoint and click on Try It Out and then Execute on the response you should be able to see all the randomly generated drones available copy one serial number and note the weight of the drone
4. Minimise drone controllers and expand load medication and click try it out NB: convert the image to base64 String and supply the base64 String on the image input
   http://localhost:8080/load-medication/0552-0928-0375-0371   
{
   "code": "TT4415",
   "image": "thiscombinationwilldeterminetheamountoftimethatthedronewillbeintheDELIVERINGstate(distance/speed)minutesforexampleifdistanceis1000andspeedis700thedronewillbeinflightforapproximately2minutesthedronewillbeinDELIVERINGstate,andthenitchangesstatetoDELIVERED.Dispatchthedrone,youhavetospecifysourceanddestination,aswellasthedistanceandthespeedofthedroneforsimulationstoworkproperly",
   "name": "CANCER DRUGS",
   "weight": 248
   }
5. Enter the request Parameter serial number, on the body add a code a base64 string of an image, name of the medication and a weight that is half the weight of the drone
6. Navigate to /drones-by-state/{state} API and select LOADING as the state to filter the drone should now appear here
7. Go back to load medication and load another medication to get the total weight to the weight of the drone
8. Navigate to /drones-by-state/{state} and select the LOADED status the drone should now appear under this status
9. Navigate to the /dispatch-drone api, paste the serial number, enter a source and destination, distance in KM and speed of drone in KM/m (recommended values are 1000 and 300 for distance and speed)
   http://localhost:8080/dispatch-drone 
  {
   "destination": "PLOVDIV, BULGARIA",
   "distanceInMetres": 1700,
   "droneSerialNumber": "0552-0928-0375-0371",
   "droneSpeedInMetresPerSecond": 20,
   "source": "SOPHIA, BULGARIA"
   }
   NB: this combination will determine the amount of time that the drone will be in the DELIVERING state (distance/speed) minutes for example if distance is 1000 and speed is 700 the drone will be in flight for approximately 2 minutes the drone will be in DELIVERING state, and then it changes state to DELIVERED.
   Dispatch the drone, you have to specify source and destination, as well as the distance and the speed of the drone for simulations to work properly
10. Check drones by status and check for the DELIVERING status, you can also check for the logs for the battery status which should decrease whilst the drone is in the DELIVERING status, you can also use the check battery status whilst the drone is DELIVERING
11. After, time in seconds that is equivalent to distance divided by speed  check drones with status DELIVERED the drone must be there 
12. Navigate to return drone API and enter the serial number of the drone the drone should now be available under the returning drones
    http://localhost:8080/return-drone/0552-0928-0375-0371
13. After a similar time as in 11 above the status of the drone should change to IDLE and the drone should now be available on available drones, the drone should also start to recharge until it gets to 100% this can be validated on the battery audit log


These additional APIs are available for use at any given point in time
a) Register Drone using the api /register-drone
b) Load multiple medications at once as long as the total weights do not exceed the weight of the drone


APIs
_________________
DESCRIPTION: View all Available Drones
ENDPOINT: http://localhost:8080/available-drones
SAMPLE RESPONSE:
[
{
"serialNumber": "016-0255-069-0474",
"model": "Heavyweight",
"weight": 406,
"batteryCapacity": 100,
"state": "IDLE",
"medication": []
},
{
"serialNumber": "0588-0651-0634-0541",
"model": "Cruiserweight",
"weight": 219,
"batteryCapacity": 100,
"state": "IDLE",
"medication": []
},
{
"serialNumber": "0933-0525-0483-0807",
"model": "Cruiserweight",
"weight": 235,
"batteryCapacity": 100,
"state": "IDLE",
"medication": []
},
{
"serialNumber": "0890-0905-0625-0442",
"model": "Middleweight",
"weight": 135,
"batteryCapacity": 100,
"state": "IDLE",
"medication": []
},
{
"serialNumber": "0433-0935-0406-0207",
"model": "Heavyweight",
"weight": 493,
"batteryCapacity": 100,
"state": "IDLE",
"medication": []
},
{
"serialNumber": "0244-0602-0865-0910",
"model": "Heavyweight",
"weight": 399,
"batteryCapacity": 100,
"state": "IDLE",
"medication": []
},
{
"serialNumber": "0112-0796-0419-0193",
"model": "Lightweight",
"weight": 17,
"batteryCapacity": 100,
"state": "IDLE",
"medication": []
},
{
"serialNumber": "0527-0526-0934-0723",
"model": "Cruiserweight",
"weight": 240,
"batteryCapacity": 100,
"state": "IDLE",
"medication": []
},
{
"serialNumber": "0441-0200-0163-0807",
"model": "Heavyweight",
"weight": 475,
"batteryCapacity": 100,
"state": "IDLE",
"medication": []
},
{
"serialNumber": "0788-0164-0259-0181",
"model": "Middleweight",
"weight": 177,
"batteryCapacity": 100,
"state": "IDLE",
"medication": []
}
]

DESCRIPTION: load Medication
API: http://localhost:8080/load-medication/{drone derial number}
SAMPLE REQUEST: http://localhost:8080/load-medication/0788-0164-0259-0181
SAMPLE REQUEST BODY:
{
"code": "XXXTT10",
"image": "Navigatetothe/dispatchdroneapi,pastetheserialnumber,enterasourceanddestination,distanceinKMandspeedofdroneinKM/m(recommendedvaluesare1000and300fordistanceandspeed)NB:thiscombinationwilldeterminetheamountoftimethatthedronewillbeintheDELIVERINGstate(distance/speed)minutesforexampleifdistanceis1000andspeedis700thedronewillbeinflightforapproximately2minutesthedronewillbeinDELIVERINGstate,andthenitchangesstatetoDELIVERED.Dispatchthedrone,youhavetospecifysourceanddestination,aswellasthedistanceandthespeedofthedroneforsimulationstoworkproperly",
"weight": 355,
"name":"CANCER DRUG"
}
SAMPLE RESPONSE BODY:
{
"id": 10,
"serialNumber": "0467-0499-0924-0652",
"model": "Heavyweight",
"weight": 177,
"batteryCapacity": 100,
"state": "LOADED",
"medication": [
{
"id": 11,
"name": "CANCER DRUG",
"weight": 355,
"code": "XXXTT10",
"image": "Navigatetothe/dispatchdroneapi,pastetheserialnumber,enterasourceanddestination,distanceinKMandspeedofdroneinKM/m(recommendedvaluesare1000and300fordistanceandspeed)NB:thiscombinationwilldeterminetheamountoftimethatthedronewillbeintheDELIVERINGstate(distance/speed)minutesforexampleifdistanceis1000andspeedis700thedronewillbeinflightforapproximately2minutesthedronewillbeinDELIVERINGstate,andthenitchangesstatetoDELIVERED.Dispatchthedrone,youhavetospecifysourceanddestination,aswellasthedistanceandthespeedofthedroneforsimulationstoworkproperly",
"createdTime": "2023-06-11T09:45:02.357427",
"updatedTime": "2023-06-11T09:45:02.357626"
}
]
}

