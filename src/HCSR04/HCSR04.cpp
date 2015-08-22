/*
	HCSR04.h - A class for abstracting a HC-SR04 Ultrasonic distance sensor.
	Created by Geoff McIver, 8/22/2015

	The MIT License (MIT)

	Copyright (c) 2015 Geoff McIver

	Permission is hereby granted, free of charge, to any person obtaining a copy
	of this software and associated documentation files (the "Software"), to deal
	in the Software without restriction, including without limitation the rights
	to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
	copies of the Software, and to permit persons to whom the Software is
	furnished to do so, subject to the following conditions:

	The above copyright notice and this permission notice shall be included in
	all copies or substantial portions of the Software.

	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
	THE SOFTWARE.

*/

#include "Arduino.h"
#include "HCSR04.h"

HCSR04::HCSR04(int triggerPin, int echoPin)
{
	_triggerPin = triggerPin;
	_echoPin = echoPin;

	// Set Trigger and echo pins for the HC-SR04
	pinMode(_triggerPin, OUTPUT);
  	pinMode(_echoPin,INPUT);
}

long HCSR04::getDistance()
{
	long d;

	// Trigger the HC-SR04
	digitalWrite(_triggerPin, LOW);
    delayMicroseconds(2);
    digitalWrite(_triggerPin, HIGH);
    delayMicroseconds(5);
    digitalWrite(_triggerPin, LOW);

    // read the duration
    d = pulseIn(_echoPin, HIGH);

    // duration includes time to target and back so halve it.
    return _msToCm(d) / 2;
}

long HCSR04::_msToCm(long ms) 
{
	// 29 microseconds per centimeter
	return ms / 29;
}