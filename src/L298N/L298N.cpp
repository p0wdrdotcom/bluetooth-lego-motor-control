/*
	L298N.h - A class for abstracting a L298N Dual H-Bridge Motor Controller.
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
#include "L298N.h"

L298N::L298N(int dir1pinA, int dir2pinA, int speedPinA, int dir1PinB, int dir2PinB, int speedPinB)
{
	_dir1PinA = dir1pinA;
	_dir2PinA = dir2pinA;
	_speedPinA = speedPinA;
	_dir1PinB = dir1PinB;
	_dir2PinB = dir2PinB;
	_speedPinB = speedPinB;

	//Set the output to L298N Dual H-Bridge Motor Controller Pins
	pinMode(_dir1PinA, OUTPUT);
	pinMode(_dir2PinA, OUTPUT);
	pinMode(_speedPinA, OUTPUT);
	pinMode(_dir1PinB, OUTPUT);
	pinMode(_dir2PinB, OUTPUT);
	pinMode(_speedPinB, OUTPUT);
}

int L298N::_gateSpeed(int speed)
{
	if (speed > 255) {
		return 255;
	}
	if (speed < 0) {
		return 0;
	}
	return speed;
}

void L298N::motorAForward(int speed)
{
	analogWrite(_speedPinA, _gateSpeed(speed));
  	digitalWrite(_dir1PinA, LOW);
  	digitalWrite(_dir2PinA, HIGH);
}

void L298N::motorBForward(int speed)
{
  analogWrite(_speedPinB,  _gateSpeed(speed));
  digitalWrite(_dir1PinB, LOW);
  digitalWrite(_dir2PinB, HIGH);
}

void L298N::motorAReverse(int speed) 
{
  analogWrite(_speedPinA,  _gateSpeed(speed));
  digitalWrite(_dir1PinA, HIGH);
  digitalWrite(_dir2PinA, LOW);
}
void L298N::motorBReverse(int speed) 
{
  analogWrite(_speedPinB,  _gateSpeed(speed));
  digitalWrite(_dir1PinB, HIGH);
  digitalWrite(_dir2PinB, LOW);
}

void L298N::motorAStop() 
{
  analogWrite(_speedPinA, 0);
  digitalWrite(_dir1PinA, LOW);
  digitalWrite(_dir2PinA, HIGH);
}

void L298N::motorBStop() 
{
  analogWrite(_speedPinB, 0);
  digitalWrite(_dir1PinB, LOW);
  digitalWrite(_dir2PinB, HIGH);
}

void L298N::allStop()
{
	motorAStop();
	motorBStop();
}
void L298N::allAheadFull()
{
	motorAForward(255);
	motorBForward(255);
}

void L298N::allBackFull()
{
	motorAReverse(255);
	motorBReverse(255);
}
