#include <SoftwareSerial.h>
#include <L298N.h>
#include <HCSR04.h>

// BT Serial RX TX pins
int btRX = 12;
int btTX = 13;

// Setup a Software serial port for the HC-06
SoftwareSerial bluetoothSerial(btRX, btTX);
L298N motorController;
HCSR04 distanceMeter;

void setup() {
  // initialize serial communication:
  Serial.begin(9600);
  bluetoothSerial.begin(57600);
}


long distance = 0;
int wentleft = 0;
unsigned long timelastforward; 
void loop()
{
  distance = distanceMeter.getDistance();
   
  if (distance > 25) {
    timelastforward = millis();
    motorController.motorAForward(160);
    motorController.motorBForward(160);
  }
  if (distance > 40) {
    timelastforward = millis();
    motorController.motorAForward(255);
    motorController.motorBForward(255);
  }

  if (distance < 10) {
    motorController.motorAReverse(120);
    motorController.motorBReverse(120);
    delay(1000);
  }
  if (distance < 25) {
    if (wentleft > 0 && ((millis() - timelastforward) > 7000)){
      goright();
    } else {
      goleft();
    }
    delay(1000);
  }
  delay(100);
}

void goleft(){
  motorController.motorAReverse(120);
  motorController.motorBForward(120);
  wentleft = 1;
}
void goright(){
  motorController.motorBReverse(120);
  motorController.motorAForward(120);
  wentleft = 0;
}
