#define STEPPER_PIN_1 4
#define STEPPER_PIN_2 5
#define STEPPER_PIN_3 6 
#define STEPPER_PIN_4 7 

void setup() {
  pinMode(STEPPER_PIN_1, OUTPUT);
  pinMode(STEPPER_PIN_2, OUTPUT);
  pinMode(STEPPER_PIN_3, OUTPUT);
  pinMode(STEPPER_PIN_4, OUTPUT);
}

void loop() {
  // Wykonanie 512 cykli (po 4 kroki) daje 1 pełny obrót
  for (int i = 0; i < 512; i++) {
    digitalWrite(STEPPER_PIN_4, LOW);
    digitalWrite(STEPPER_PIN_1, HIGH);
    delay(2); // Minimalne opóźnienie dla tego silnika to 2ms
    digitalWrite(STEPPER_PIN_1, LOW);
    digitalWrite(STEPPER_PIN_2, HIGH);
    delay(2);
    digitalWrite(STEPPER_PIN_2, LOW);
    digitalWrite(STEPPER_PIN_3, HIGH);
    delay(2);
    digitalWrite(STEPPER_PIN_3, LOW);
    digitalWrite(STEPPER_PIN_4, HIGH);
    delay(2);
  }
  delay(2000); // Przerwa po wykonaniu pełnego obrotu
}

ZAD2

#define STEPPER_PIN_1 4
#define STEPPER_PIN_2 5
#define STEPPER_PIN_3 6 
#define STEPPER_PIN_4 7 

void setup() {
  pinMode(STEPPER_PIN_1, OUTPUT);
  pinMode(STEPPER_PIN_2, OUTPUT);
  pinMode(STEPPER_PIN_3, OUTPUT);
  pinMode(STEPPER_PIN_4, OUTPUT);
}

void loop() {
  int potValue = analogRead(A0);
  // Mapowanie wartości potencjometru na czas opóźnienia (od 2ms dla maks. prędkości do 50ms)
  int motorDelay = map(potValue, 0, 1023, 2, 50);

  digitalWrite(STEPPER_PIN_4, LOW);
  digitalWrite(STEPPER_PIN_1, HIGH);
  delay(motorDelay);
  digitalWrite(STEPPER_PIN_1, LOW);
  digitalWrite(STEPPER_PIN_2, HIGH);
  delay(motorDelay);
  digitalWrite(STEPPER_PIN_2, LOW);
  digitalWrite(STEPPER_PIN_3, HIGH);
  delay(motorDelay);
  digitalWrite(STEPPER_PIN_3, LOW);
  digitalWrite(STEPPER_PIN_4, HIGH);
  delay(motorDelay);
}

ZAD3

#include <Stepper.h>

const int stepsPerRevolution = 2048; 
// Inicjalizacja biblioteki (zamienione piny środkowe dla poprawnego działania z ULN2003)
Stepper myStepper(stepsPerRevolution, 4, 6, 5, 7);

void setup() {
  // Brak potrzeby definiowania pinów - biblioteka robi to za nas
}

void loop() {
  int potValue = analogRead(A0);
  // Mapowanie wartości z A0 na prędkość w RPM (od 1 do maksymalnie około 15 obr/min dla tego silnika)
  int motorSpeed = map(potValue, 0, 1023, 1, 15);
  
  if (motorSpeed > 0) {
    myStepper.setSpeed(motorSpeed);
    // Wykonanie jednego kroku (zapętlenie sprawi, że będzie się obracał płynnie)
    myStepper.step(1);
  }
}

ZAD4

#include <Servo.h>

Servo servoBase; // Utworzenie obiektu dla serwomechanizmu

void setup() {
  servoBase.attach(9); // podłącza serwomechanizm na pinie 9
  servoBase.write(0);  // nakazuje serwomechanizmowi przejście do pozycji 0
}

void loop() {
  for(int i = 0; i <= 180; i = i + 15) {
    servoBase.write(i); // nakazuje przejście do pozycji w zmiennej 'i'
    delay(400);
  }
  servoBase.write(0);  // powrót do pozycji 0
  delay(2000);         // oczekiwanie na powrót
}

ZAD5

#include <Servo.h>

Servo servoBase;

void setup() {
  servoBase.attach(9); // Przypisanie serwomechanizmu do pinu 9
}

void loop() {
  int potValue = analogRead(A0);            // Odczyt z potencjometru (od 0 do 1023)
  int angle = map(potValue, 0, 1023, 0, 180); // Przeliczenie na kąt (od 0 do 180 stopni)
  
  servoBase.write(angle);                   // Ustawienie wychylenia
  delay(15);                                // Krótkie opóźnienie dla płynnego działania
}
