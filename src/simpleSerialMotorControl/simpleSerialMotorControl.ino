
#include <SoftwareSerial.h>
#include <Servo.h>
#include <L298N.h>

String command = String();
boolean commandComplete = false; // flag for the command reading

// Servo Pin
int servoAPin = 11;

Servo servoA;

/*
Command Motor Direction Speed (0-255)
CMD     MA    F         255

e.g. Motor A Forward Full speed
CMDMAF255

e.g. Motor B Reverse Half Speed
CMDMBR127

e.g. Motor A Stop
CMDMAS

*/

// Setup a Software serial port for the HC-06 using 12 and 13 for RX and TX
SoftwareSerial bluetoothSerial(12, 13);

L298N motorController;

void setup() {
  // initialize debugging serial
  Serial.begin(9600);
  
  // initialize bluetooth serial
  bluetoothSerial.begin(57600);
  
  //Setup the servos
  servoA.attach(servoAPin);
  //"Zero" the servo
  servoA.write(90);
  delay(500);

}

void loop() {
  if (bluetoothSerial.available() > 0) {
    getIncomingBTChars();
  }
  if (Serial.available() > 0) {
    getIncomingSerialChars();
  }

  if (commandComplete == true) {
    processCommand();
  }

}

void getIncomingSerialChars() {
  char inChar = Serial.read();
  if (inChar == 59 || inChar == 10 || inChar == 13) { // complete the command if there is a semi colon, Line Feed or Carriage Return
    commandComplete = true;
  } else {
    command += inChar;
  }
}



void getIncomingBTChars() {
  char inChar = bluetoothSerial.read();
  if (inChar == 59 || inChar == 10 || inChar == 13) { // complete the command if there is a semi colon, Line Feed or Carriage Return
    commandComplete = true;
  } else {
    command += inChar;
  }
}

void processCommand() {
  if (commandCorrect()) {
    Serial.print("Processing ");
    Serial.println(command);
    // Motor Command
    
    char componentChar = command.charAt(3);
    switch (componentChar) {
      case 'M':
        processMotorCommand(command);   
        break;
        
      case 'S':
        processServoCommand(command);
        break;
        
      default:
        return clearCommand();
      
    }
  }
  //bin the command
  clearCommand();
}

void processServoCommand(String command){
  char servo = command.charAt(4);
  int angle = parseAngle(command.substring(5));
  
  switch (servo) {
    case 'A':
      servoADrive(angle);
      break;
    
    default:
      return;
  }
}

void servoADrive(int angle){
  if (angle <= 180 && angle >=0){
    servoA.write(angle);
    delay(15);// Wait for the servo
  }
}


void processMotorCommand(String command){
      char motor = command.charAt(4);
      char direction = command.charAt(5);
      int speed = parseSpeed(command.substring(6));

      if (motor == 'A') {
        switch (direction) {
        case 'F':
          motorController.motorAForward(speed);
          break;

        case 'R':
          motorController.motorAReverse(speed);
          break;

        case 'S':
          motorController.motorAStop(); //ignore the speed
          break;

        default:
          return clearCommand();
        }
      }
      if (motor == 'B') {
        switch (direction) {
        case 'F':
          motorController.motorBForward(speed);
          break;

        case 'R':
          motorController.motorBReverse(speed);
          break;

        case 'S':
          motorController.motorBStop(); //ignore the speed
          break;

        default:
          return clearCommand();
        }
      }
}

void clearCommand () {
  Serial.println("Clearing command");
  command = "";
  commandComplete = false;
  Serial.println(command);
}

int parseSpeed(String arg) {
  int speed = arg.toInt();

  if (speed > 255) {
    speed = 255;
  }
  if (speed < 0) {
    speed = 0;
  }
  return speed;
}

int parseAngle(String arg) {
  int angle = arg.toInt();
  if (angle > 180){
    angle = 180;
  }
  if (angle < 0) {
    angle = 0;
  }
  return angle;
}

boolean commandCorrect() {
  if (command.charAt(0) == 'C' &&
      command.charAt(1) == 'M' &&
      command.charAt(2) == 'D' &&
      command.length() < 10 ) {
    return true;
  }
  return false;
}
