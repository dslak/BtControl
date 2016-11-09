// RELAYS from 8 to 13

#include <SoftwareSerial.h>

#define btVcc 5
#define btGnd 4
#define btTx 3
#define btRx 2

SoftwareSerial BT(btTx, btRx); 

char a;


void setup(){
  
  for(int i=8; i<=13; i++){
    pinMode(i, OUTPUT);
  }
    
  pinMode(btVcc, OUTPUT);
  pinMode(btGnd, OUTPUT);

  digitalWrite(btVcc, HIGH);
  digitalWrite(btGnd, LOW);
  
  Serial.begin(9600);
  BT.begin(9600);
  //BT.println("Hello from Arduino");

  
}




void loop(){
  
  if (BT.available()){
    
    //a=(BT.read());
    Serial.print(BT.read());
    
  }
  
  if (Serial.available()){
  
    BT.println(Serial.read());
  
  }
  //delay(500);
}



