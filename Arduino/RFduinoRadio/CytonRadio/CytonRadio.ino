#include <RFduinoBLE.h>

const int LED = 2;
char* msgA = "a received";
char* msgB = "b received";

void setup() {
  pinMode(LED, OUTPUT);

  // radio config
  RFduinoBLE.deviceName = "WEEGi";
  RFduinoBLE.advertisementInterval = 500;
  RFduinoBLE.txPowerLevel = -8;
  RFduinoBLE.advertisementData = "cyton";
  Serial.begin(9600);

  RFduinoBLE.begin();
}

void loop() {
  
}

void RFduinoBLE_onConnect() {
  digitalWrite(LED, HIGH);
}

void RFduinoBLE_onDisconnect() {
  digitalWrite(LED, LOW); 
}

void RFduinoBLE_onReceive(char *data, int len) {
  char b0;
  
  if (len > 0)
    b0 = data[0];

  if (b0 == 'a')
   RFduinoBLE.send(msgA, 10);
  
  if (b0 == 'b')
   RFduinoBLE.send(msgB, 10);
}

