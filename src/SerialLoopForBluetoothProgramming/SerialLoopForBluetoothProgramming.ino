#include <SoftwareSerial.h>


SoftwareSerial bluetoothSerial(12, 13);
void setup() {
  Serial.begin(9600);
  bluetoothSerial.begin(57600);
}

void loop() {
  if (bluetoothSerial.available() > 0) {
    Serial.write(bluetoothSerial.read());
  }
  
  if (Serial.available() > 0){
    bluetoothSerial.write(Serial.read());
  }
}
