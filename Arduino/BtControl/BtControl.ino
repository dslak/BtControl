// RELAYS from 8 to 13

#include <SoftwareSerial.h>

#define btVcc 5
#define btGnd 4
#define btTx 3
#define btRx 2

SoftwareSerial BT(btTx, btRx); 




void setup(){
  
  for(int i=8; i<=13; i++){
    pinMode(i, OUTPUT);
  }
    
  pinMode(btVcc, OUTPUT);
  pinMode(btGnd, OUTPUT);

  //digitalWrite(btVcc, HIGH);
  //digitalWrite(btGnd, LOW);
  
  Serial.begin(9600);
  BT.begin(9600);
  //BT.println("Hello from Arduino");

  
}




void loop(){

  char strc[1];
  String str="";
  int strp=0;

  // From App to Arduino
  while (BT.available() > 0) {
    strc[0]=BT.read();
    str=String(str + strc[0]);
    strp++;
  }

  if(strp>0){
    Serial.print(str);
    Serial.println();
  }


  str=" ";
  strp=0;

  // From Arduino to App
  /*
  while (Serial.available() > 0) {
    strc[0]=Serial.read();
    str=String(str + strc[0]);
    strp++;
  }
  */

  for(int i=0;i<3;i++){
    //strc[0]=char(random(44,60));
    str=String(str + random(10,90) + " ");
    strp++;
    delay(100);
  }
  

  if(strp>0){
    BT.println(str);
    Serial.println(str);
    
    //delay(500);
    //BT.println();
  }
  //BT.println();
  
}


//void serialEvent() {
//}




