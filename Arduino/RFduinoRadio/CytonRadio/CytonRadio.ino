#include <RFduinoBLE.h>

const int LED = 2;

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

void StartRecording() {
  return;
}

void StopRecording() {
  return;
}

void GetFileList(char(*filename)[] files) {


}


void RFduinoBLE_onReceive(char *data, int len) {
  if (len < 1)
    return;

  if (len == 1) {
    switch (data[0]) {
    case '0': 
      StartRecording();
      break;
    case '1':
      StopRecording();
      break;
    case '2':
      GetFileList();
      break;
    default:
      return;  
    }
  }
  else if (len > 1) {
    TransmitFile();
  }
}

