
#include <SoftwareSerial.h>

String command = String();
boolean commandComplete = false; // flag for the command reading

// Motor A pins
int dir1PinA = 2;
int dir2PinA = 4;
int speedPinA = 3; // Needs to be a PWM pin to be able to control motor speed

// Motor B pins
int dir1PinB = 5;
int dir2PinB = 7;
int speedPinB = 6; // Needs to be a PWM pin to be able to control motor speed


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

void setup() {
  // initialize debugging serial
  Serial.begin(9600);
  
  // initialize bluetooth serial
  bluetoothSerial.begin(9600);
  
  //Set the output to L298N Dual H-Bridge Motor Controller Pins
  pinMode(dir1PinA, OUTPUT);
  pinMode(dir2PinA, OUTPUT);
  pinMode(speedPinA, OUTPUT);
  pinMode(dir1PinB, OUTPUT);
  pinMode(dir2PinB, OUTPUT);
  pinMode(speedPinB, OUTPUT);

}

void loop() {
  if (bluetoothSerial.available() > 0) {
    getIncomingBTChars();
  }

  if (commandComplete == true) {
    processCommand();
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
    if (command.charAt(3) == 'M') {
      char motor = command.charAt(4);
      char direction = command.charAt(5);
      int speed = parseSpeed(command.substring(6));

      if (motor == 'A') {
        switch (direction) {
        case 'F':
          motorAForward(speed);
          break;

        case 'R':
          motorAReverse(speed);
          break;

        case 'S':
          motorAStop(); //ignore the speed
          break;

        default:
          return clearCommand();
        }
      }
      if (motor == 'B') {
        switch (direction) {
        case 'F':
          motorBForward(speed);
          break;

        case 'R':
          motorBReverse(speed);
          break;

        case 'S':
          motorBStop(); //ignore the speed
          break;

        default:
          return clearCommand();
        }
      }
    }
  }
  //bin the command
  clearCommand();
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

boolean commandCorrect() {
  if (command.charAt(0) == 'C' &&
      command.charAt(1) == 'M' &&
      command.charAt(2) == 'D' &&
      command.length() < 10 ) {
    return true;
  }
  return false;
}

void motorAForward(int speed) {
  analogWrite(speedPinA, speed);
  digitalWrite(dir1PinA, LOW);
  digitalWrite(dir2PinA, HIGH);
}
void motorBForward(int speed) {
  analogWrite(speedPinB, speed);
  digitalWrite(dir1PinB, LOW);
  digitalWrite(dir2PinB, HIGH);
}

void motorAReverse(int speed) {
  analogWrite(speedPinA, speed);
  digitalWrite(dir1PinA, HIGH);
  digitalWrite(dir2PinA, LOW);
}
void motorBReverse(int speed) {
  analogWrite(speedPinB, speed);
  digitalWrite(dir1PinB, HIGH);
  digitalWrite(dir2PinB, LOW);
}

void motorAStop() {
  analogWrite(speedPinA, 0);
  digitalWrite(dir1PinA, LOW);
  digitalWrite(dir2PinA, HIGH);
}

void motorBStop() {
  analogWrite(speedPinB, 0);
  digitalWrite(dir1PinB, LOW);
  digitalWrite(dir2PinB, HIGH);
}
