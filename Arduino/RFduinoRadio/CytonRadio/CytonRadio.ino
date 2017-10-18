#include <RFduinoBLE.h>

const int LED = 2;
bool isAdvertising = false;
bool isLedOn = false;

void setup() {
  Serial.begin(9600);

  pinMode(LED, OUTPUT);

  // radio config
  RFduinoBLE.deviceName = "WEEGi";
  RFduinoBLE.advertisementInterval = 333;
  RFduinoBLE.txPowerLevel = -8;
  RFduinoBLE.advertisementData = "cyton";
  RFduinoBLE.begin();
}

void loop() {
  RFduino_ULPDelay(INFINITE);
  
  if (isAdvertising) {
    if (isLedOn)
      digitalWrite(LED, LOW);
    else
      digitalWrite(LED, HIGH);
  } else {
    if (isLedOn)
      digitalWrite(LED, LOW);
  }
}

void RFduinoBLE_onAdvertisement(bool start) {
  isAdvertising = start;
}

void RFduinoBLE_onConnect() {
  digitalWrite(LED, HIGH);
}

void RFduinoBLE_onDisconnect() {
  digitalWrite(LED, LOW); 
}

void StartRecording() {
  Serial.write("start recording\n");
  char data[] = {'0', '1', '2'};
  RFduinoBLE.send(data, 3);
  return;
}

void StopRecording() {
  return;
}

void GetFileList() {
  return;
}

void TransmitFile() {
  
}
void RFduinoBLE_onReceive(char *data, int len) {
  if (len < 1)
    return;

  switch (data[0]) {
  case (char) 0: 
    StopRecording();
    break;
  case (char) 1:
    StartRecording();
    break;
  case (char) 2:
    GetFileList();
    break;
  case (char) 3:
    TransmitFile();
    break;
  default:
    return;  
  }
}

